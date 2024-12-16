package secomind.tinytalker;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ai.onnxruntime.genai.GenAIException;
import ai.onnxruntime.genai.Generator;
import ai.onnxruntime.genai.GeneratorParams;
import ai.onnxruntime.genai.Sequences;
import ai.onnxruntime.genai.Tokenizer;
import ai.onnxruntime.genai.TokenizerStream;
import secomind.tinytalker.messaging.ChatAdapter;
import secomind.tinytalker.messaging.ChatMessage;

import ai.onnxruntime.genai.Model;
import secomind.tinytalker.utilities.ModelDownloader;

public class MainChatActivity extends AppCompatActivity {

    private static final String TAG = "secomind.tinytalker.MainChatActivity";

    private RecyclerView recyclerView;
    private EditText messageInput;
    private Button sendButton;


    // class necessary with chat applications (manages the messages)
    private ChatAdapter chatAdapter;
    // list with the messages exchanged between us and the model
    private List<ChatMessage> chatMessages;

    // represents the GenAI model used in the application
    private Model model;
    // tokenizer associated with the above model (they work together)
    private Tokenizer tokenizer;


    private static final String system = "This is a conversation between User and Llama, a friendly chatbot.\n" +
            "Llama is helpful, kind, honest, good at writing, and never fails to answer any " +
            "requests immediately and with precision.\n\n" +
            "User: Hello Llama\n" +
            "Llama: Hello.  How may I help you today?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_list_activity);

        // Initialize the views
        recyclerView = findViewById(R.id.recycler_gchat);
        messageInput = findViewById(R.id.edit_gchat_message);
        sendButton = findViewById(R.id.button_gchat_send);

        // Setup RecyclerView - used to display the messages
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            chatMessages = new ArrayList<>();
            chatAdapter = new ChatAdapter(chatMessages);
            recyclerView.setAdapter(chatAdapter);
        }

        // check the presence of the model
        sendButton.setEnabled(false);
        // Load the model
        try {
            prepareTheModel(this);
        } catch (GenAIException e) {
            throw new RuntimeException(e);
        }

        // prepare the "send" button
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the message from the text field
                String message = messageInput.getText().toString();
                if (!message.isEmpty()) {
                    // send the message to the model, obtain an answer
                    sendMessage(message);
                    messageInput.setText("");
                }
            }
        });

    }

    /** Checks for the presence of the LLM mode.
     * If present, it links the model file to the ONNX object
     * in this class that manages it.
     * Otherwise, we download a default model (Phi3) from Hugging Face
     * In that case, an internet connection is necessary.
     *
     * The model files can be saved in the space allocated for the application.
     * use Android studio for this, and do:
     * Device Manager (it should be the lowers icon on the right)
     *
     * Then, once there, navigate through these directories:
     * data -> data -> secomind.tinitalker -> files
     *
     * Copy and paste the model there. Done.
     *
     * */
    private void prepareTheModel(Context context) throws GenAIException  {
        // url where to find the default model Phi3mini4k
        final String baseUrl = "https://huggingface.co/microsoft/Phi-3-mini-4k-instruct-onnx/resolve/main/cpu_and_mobile/cpu-int4-rtn-block-32-acc-level-4/";
        // list with the files that we REQUIRE in the application allocated memory
        List<String> files = Arrays.asList(
                "added_tokens.json",
                "config.json",
                "configuration_phi3.py",
                "genai_config.json",
                "phi3-mini-4k-instruct-cpu-int4-rtn-block-32-acc-level-4.onnx",
                "phi3-mini-4k-instruct-cpu-int4-rtn-block-32-acc-level-4.onnx.data",
                "special_tokens_map.json",
                "tokenizer.json",
                "tokenizer.model",
                "tokenizer_config.json");

        // list that will contain the files that are missing
        List<Pair<String, String>> urlFilePairs = new ArrayList<>();
        for (String file : files) {
            if (!fileExists(context, file)) {
                urlFilePairs.add(new Pair<>(
                        baseUrl + file,
                        file));
            }
        }
        if (urlFilePairs.isEmpty()) { // the list is empy, i.e. everything is presents and nothing needs to be loaded
            // we just assign the variables model and tokenizer
            // Display a message using Toast
            Toast.makeText(this, "All files already exist. Skipping download.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "All files already exist. Skipping download.");
            model = new Model(getFilesDir().getPath());
            tokenizer = model.createTokenizer();
            return; // we are done with this method
        }

        Toast.makeText(this,
                "Downloading model for the app... Model Size greater than 2GB, please allow a few minutes to download.",
                Toast.LENGTH_SHORT).show();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ModelDownloader.downloadModel(context, urlFilePairs, new ModelDownloader.DownloadCallback() {
                @Override
                public void onProgress(long lastBytesRead, long bytesRead, long bytesTotal) {
                    long lastPctDone = 100 * lastBytesRead / bytesTotal;
                    long pctDone = 100 * bytesRead / bytesTotal;
                    if (pctDone > lastPctDone) {
                        Log.d(TAG, "Downloading files: " + pctDone + "%");
                    }
                }
                @Override
                public void onDownloadComplete() {
                    Log.d(TAG, "All downloads completed.");

                    // Last download completed, create SimpleGenAI
                    try {
                        model = new Model(getFilesDir().getPath());
                        tokenizer = model.createTokenizer();
                        runOnUiThread(() -> {
                            Toast.makeText(context, "All downloads completed", Toast.LENGTH_SHORT).show();
                        });
                    } catch (GenAIException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }

                }
            });
        });
        executor.shutdown();
    }

    private static boolean fileExists(Context context, String fileName) {
        File file = new File(context.getFilesDir(), fileName);
        return file.exists();
    }

    private void sendMessage(String message) {
        // Checking if the message entered by user is empty or not.
        if (message.isEmpty()) {
            // if the edit text is empty display a toast message.
            Toast.makeText(MainChatActivity.this, "Please enter your message..", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the new message and add it to the list
        chatMessages.add(new ChatMessage(message, true));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        // move the screen to this last message location
        recyclerView.scrollToPosition(chatMessages.size() - 1);

        // Disable send button while processing
        sendButton.setEnabled(false);
        sendButton.setAlpha(0.5f);

        dealWithMessageOnTheBackground();


//        // Get response from model in background
//        executorService.execute(() -> {
//            String modelResponse = getModelResponse(message);
//
//            mainHandler.post(() -> {
//                // Add model's response to chat
//                chatMessages.add(new ChatMessage(modelResponse, false));
//                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
//                recyclerView.scrollToPosition(chatMessages.size() - 1);
//
//                // Re-enable send button
//                sendButton.setEnabled(true);
//            });
//        });
    }

    private void dealWithMessageOnTheBackground() {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        TokenizerStream stream = null;
                        GeneratorParams generatorParams = null;
                        Generator generator = null;
                        Sequences encodedPrompt = null;

                        try {
                            stream = tokenizer.createStream();
                            generatorParams = model.createGeneratorParams();


                        } catch (GenAIException e) {
                            throw new RuntimeException(e);
                        } finally {
                            if (generator != null) generator.close();
                            if (encodedPrompt != null) encodedPrompt.close();
                            if (stream != null) stream.close();
                            if (generatorParams != null) generatorParams.close();
                        }
                    }
                }
        ).start();
    }
}
