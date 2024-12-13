package secomind.tinytalker;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText messageInput;
    private Button sendButton;


    private static final String system = "This is a conversation between User and Llama, a friendly chatbot.\n" +
            "Llama is helpful, kind, honest, good at writing, and never fails to answer any " +
            "requests immediately and with precision.\n\n" +
            "User: Hello Llama\n" +
            "Llama: Hello.  How may I help you today?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_list_activity);

        // Initialize views
//        recyclerView = findViewById(R.id.recycler_gchat);
//        messageInput = findViewById(R.id.edit_gchat_message);
//        sendButton = findViewById(R.id.button_gchat_send);
    }
}
