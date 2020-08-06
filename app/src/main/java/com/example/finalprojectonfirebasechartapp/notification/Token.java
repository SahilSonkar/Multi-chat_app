package com.example.finalprojectonfirebasechartapp.notification;

public class Token {
    // An FCM Token or much commonly known as a registrationToken
    // an Id issued by GCM connection server to the client app that allows it to recieve message

    String token;

    public Token(String token) {
        this.token = token;
    }

    public Token() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
