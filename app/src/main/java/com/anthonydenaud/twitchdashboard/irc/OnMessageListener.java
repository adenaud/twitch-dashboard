package com.anthonydenaud.twitchdashboard.irc;

public interface OnMessageListener {
    void onMessage(String sender, String message);
}
