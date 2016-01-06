package com.anthonydenaud.twitchdashboard.irc;

import android.util.Log;


import org.jibble.pircbot.IrcException;

import java.io.IOException;

public class IrcClient implements OnMessageListener{
    private TwitchIrcBot irc;

    private String channel;

    private OnMessageListener onMessageListener;

    public IrcClient() {
        irc = new TwitchIrcBot();
    }

    public void connect(final String hostname, final int port, final String password, final String username){

        this.channel = "#"+channel;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("IRC","connecting ...");

                try {
                    irc.connect(hostname,port,password);
                    irc.joinChannel(channel);
                    irc.setOnMessageListener(IrcClient.this);

                    Log.d("IRC","IRC success");

                } catch (IOException | IrcException e) {
                    Log.e("IRC",e.getMessage());
                }

            }
        },"IRC_THREAD");
        thread.start();

    }

    public void disconnect(){
        irc.disconnect();
    }

    public boolean isConnected(){
        return irc.isConnected();
    }

    public String getChannel() {
        return channel;
    }

    public void setOnMessageListener(OnMessageListener onMessageListener) {
        this.onMessageListener = onMessageListener;
    }

    @Override
    public void onMessage(String sender, String message) {
        if (onMessageListener != null){
            onMessageListener.onMessage(sender, message);
        }
    }

    public void sendMessage(String message){
        irc.sendMessage(channel,message);
    }
}
