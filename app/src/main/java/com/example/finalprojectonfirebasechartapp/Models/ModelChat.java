package com.example.finalprojectonfirebasechartapp.Models;

public class ModelChat {

    String sender,reciever,timestamp,message;
    boolean isseen;

    public ModelChat(String sender, String reciever, String timestamp, String message, boolean isseen) {
        this.sender = sender;
        this.reciever = reciever;
        this.timestamp = timestamp;
        this.message = message;
        this.isseen = isseen;
    }

    public ModelChat() {
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReciever() {
        return reciever;
    }

    public void setReciever(String reciever) {
        this.reciever = reciever;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }
}
