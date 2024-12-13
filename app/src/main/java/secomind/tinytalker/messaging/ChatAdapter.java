package secomind.tinytalker.messaging;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import secomind.tinytalker.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * ChatAdapter is a RecyclerView adapter that manages the display of chat messages
 * in a two-sided chat interface, handling both user and model messages with different layouts.
 *
 * This adapter supports two types of messages:
 * - User messages (displayed on one side with item_chat_me layout)
 * - Model messages (displayed on the opposite side with item_chat_other layout)
 *
 * The adapter uses the ViewHolder pattern with two distinct ViewHolder classes:
 * - UserMessageViewHolder: For displaying user messages
 * - ModelMessageViewHolder: For displaying model responses
 *
 * @author Dennis Dosso dennis.dosso@secomind.com
 * @version 1.0
 * @see RecyclerView.Adapter
 * @see ChatMessage
 *
 * Usage example:
 * <pre>
 * List<ChatMessage> messages = new ArrayList<>();
 * ChatAdapter adapter = new ChatAdapter(messages);
 * recyclerView.setAdapter(adapter);
 * </pre>
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_MODEL = 2;
    private List<ChatMessage> chatMessages;

    /**
     * Constructs a new ChatAdapter with the specified list of chat messages.
     *
     * @param chatMessages The list of chat messages to display
     */
    public ChatAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    /**
     * Determines the view type for a given position in the dataset.
     *
     * @param position The position of the item in the dataset
     * @return VIEW_TYPE_USER for user messages, VIEW_TYPE_MODEL for model messages
     */
    @Override
    public int getItemViewType(int position) {
        return chatMessages.get(position).isUser() ? VIEW_TYPE_USER : VIEW_TYPE_MODEL;
    }

    /**
     * Creates a new ViewHolder instance based on the message type.
     * The types can be "me" or "the model"
     *
     * @param parent The parent ViewGroup
     * @param viewType The type of view (VIEW_TYPE_USER or VIEW_TYPE_MODEL)
     * @return A new ViewHolder instance appropriate for the message type
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER) { // layout when the user writes
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_me, parent, false);
            return new UserMessageViewHolder(view);
        } else { // layout when the model responds
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_other, parent, false);
            return new ModelMessageViewHolder(view);
        }
    }


    /**
     * Binds chat message data to the appropriate ViewHolder.
     *
     * @param holder The ViewHolder to bind data to
     * @param position The position of the item in the dataset
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position < 0 || position >= chatMessages.size()) return;

        // retrieve the message from the linked list
        ChatMessage chatMessage = chatMessages.get(position);

        if (chatMessage == null) return;

        // check which instance of message we are, so we can cast it and bind the info
        try {
            if (holder instanceof UserMessageViewHolder) {
                ((UserMessageViewHolder) holder).bind(chatMessage);
            } else if (holder instanceof ModelMessageViewHolder) {
                ((ModelMessageViewHolder) holder).bind(chatMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the total number of items in the dataset.
     *
     * @return The total number of chat messages
     */
    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    /*  Static inner class that is used to represent the
    *   ViewHolders generated and maintained by this Adapter
    *   when the message is from the user of the app.
    * */
    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView dateText;
        TextView hourText;

        /** Creates the View Holder for the user messages*/
        UserMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_gchat_message_me);
            dateText = itemView.findViewById(R.id.text_gchat_date_me);
            hourText = itemView.findViewById(R.id.text_gchat_timestamp_me);
        }

        /** Sets the data being dispayed in this ViewHolder,
         *  i.e., the text being displayed in the message
         *  */
        void bind(ChatMessage message) {
            try {
                if (message == null) return;

                if (messageText != null) {
                    messageText.setText(message.getMessage());
                }

                if (dateText != null && message.getTimestamp() != null) {
                    SimpleDateFormat formatter = new SimpleDateFormat("MMMM d", Locale.getDefault());
                    String formattedDate = formatter.format(message.getTimestamp());
                    dateText.setText(formattedDate);
                    hourText.setText(message.getHour());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /** A ViewHolder that represents a message when it is a response from the model
     *  */
    static class ModelMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView dateText;
        TextView hourText;

        ModelMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_gchat_message_other);
            dateText = itemView.findViewById(R.id.text_gchat_date_other);
            hourText = itemView.findViewById(R.id.text_gchat_timestamp_other);
        }

        void bind(ChatMessage message) {
            messageText.setText(message.getMessage());
            // Format and set the date
            SimpleDateFormat formatter = new SimpleDateFormat("MMMM d", Locale.getDefault());
            String formattedDate = formatter.format(message.getTimestamp());
            dateText.setText(formattedDate);
            hourText.setText(message.getHour());
        }
    }
}

