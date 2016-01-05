package com.anthonydenaud.twitchdashboard.irc;

import org.jibble.pircbot.PircBot;

/**
 * Created by Anthony on 07/11/2015.
 */
public class TwitchIrcBot extends PircBot {

    private OnMessageListener onMessageListener;

    public TwitchIrcBot() {
        setName("elbarto331");
    }

    @Override
    protected void onMessage(String channel, String sender, String login, String hostname, String message) {
        if(onMessageListener != null){
            onMessageListener.onMessage(sender, message);
        }
    }

    public void setOnMessageListener(OnMessageListener onMessageListener) {
        this.onMessageListener = onMessageListener;
    }
}
