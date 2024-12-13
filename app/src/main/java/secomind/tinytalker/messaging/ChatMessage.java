package secomind.tinytalker.messaging;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatMessage {
    private String message;
    private boolean isUser;
    private Date timestamp;
    private String hour;
    private String day;

    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
        this.timestamp = new Date();
        this.hour = formatCurrentHour(this.timestamp); // Format hour as "HH:mm"
        this.day = getFormattedDate(this.timestamp);
    }

    public String getMessage() {
        return message;
    }

    public boolean isUser() {
        return isUser;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    private String formatCurrentHour(Date timestamp) {
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm");
        return hourFormat.format(timestamp);
    }

    public String getFormattedDate(Date timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM");
        return dateFormat.format(timestamp); // Format as "!0 June"
    }
}

