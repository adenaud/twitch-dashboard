package com.anthonydenaud.twitchdashboard;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;


import com.anthonydenaud.twitchdashboard.irc.IrcClient;
import com.anthonydenaud.twitchdashboard.irc.OnMessageListener;
import com.anthonydenaud.twitchdashboard.settings.SettingsActivity;
import com.anthonydenaud.twitchdashboard.settings.SettingsFragment;
import com.anthonydenaud.twitchdashboard.twitch.TwitchAPI;


import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


public class Dashboard extends AppCompatActivity implements View.OnClickListener, OnMessageListener, TextView.OnEditorActionListener {

    private View mContentView;
    private ImageButton btnQuit;
    private TextView textTime;
    private TextView textChat;

    private TextView textViewers;
    private TextView textViews;
    private TextView textFollowers;
    private TextView textChannelTitle;

    private EditText editTextSendMessage;

    private Button buttonSendMessage;

    private Timer dateTimer;
    private Timer twitchApiTimer;

    private String chat = "";
    private String time = "";
    private String viewers = "";
    private String views = "";
    private String followers = "";

    private IrcClient ircClient;
    private ImageButton buttonSettings;

    private String username;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dashboard);

        mContentView = findViewById(R.id.fullscreen_content);
        btnQuit = (ImageButton) findViewById(R.id.btn_quit);
        textTime = (TextView) findViewById(R.id.text_clock);
        textChat = (TextView) findViewById(R.id.text_chat);

        textViewers = (TextView) findViewById(R.id.text_viewers);
        textViews = (TextView) findViewById(R.id.text_views);
        textFollowers = (TextView) findViewById(R.id.text_followers);
        textChannelTitle = (TextView) findViewById(R.id.text_channel_title);

        editTextSendMessage = (EditText) findViewById(R.id.text_chat_send);
        editTextSendMessage.setOnEditorActionListener(this);

        buttonSendMessage = (Button) findViewById(R.id.button_send);
        buttonSettings = (ImageButton) findViewById(R.id.btn_settings);

        btnQuit.setOnClickListener(this);
        buttonSettings.setOnClickListener(this);
        buttonSendMessage.setOnClickListener(this);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

    }

    private void initUi() {

        username = preferences.getString("twitch_username","");

        ircClient = new IrcClient();
        ircClient.connect("irc.twitch.tv", 6667, "", "#" + username);

        ircClient.setOnMessageListener(this);


        textChat.setText(chat);
        textTime.setText(time);

        textViewers.setText(viewers);
        textViews.setText(views);
        textFollowers.setText(followers);

        textChat.setMovementMethod(new ScrollingMovementMethod());


        hide();

        if(StringUtils.isNotEmpty(username)){
            twitchApiTimer = new Timer();
            twitchApiTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    refreshTwitchDatas();
                }
            }, 0, preferences.getInt("refresh_rate",5000));
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        hide();


        if(!preferences.getBoolean("firstrun",false)){
            Intent intent = new Intent(this,SettingsActivity.class);
            preferences.edit().putBoolean("firstrun",true).apply();
            startActivityForResult(intent, 0x55);
        }else{
            initUi();
        }


        final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.FRANCE);

        dateTimer = new Timer();
        dateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final Date date = new Date();
                        textTime.setText(sdf.format(date));
                    }
                });
            }
        }, 0, 1000);
    }

    private void refreshTwitchDatas() {


        final int viewers = TwitchAPI.getViewers(username);
        final int views = TwitchAPI.getViews(username);
        final int followers = TwitchAPI.getFollowers(username);
        final String channelTitle = TwitchAPI.getChannelTitle(username);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (viewers == -2) {
                    textViewers.setText("Offline");
                    textViewers.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                } else if (viewers == -1) {
                    textViewers.setText("Error");
                } else {
                    textViewers.setText(String.valueOf(viewers));
                }

                textViews.setText(String.valueOf(views));
                textFollowers.setText(String.valueOf(followers));
                textChannelTitle.setText(channelTitle);

            }
        });
    }


    @SuppressLint("InlinedApi")
    private void hide() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

    }

    @Override
    public void onClick(View v) {
        if (v.equals(btnQuit)) {
            dateTimer.cancel();
            twitchApiTimer.cancel();
            finish();
        }

        if (v.equals(buttonSendMessage)) {
            sendMessage();
        }

        if (v.equals(buttonSettings)){
            Intent intent = new Intent(this,SettingsActivity.class);
            startActivityForResult(intent, 0x55);
        }
    }

    @Override
    public void onMessage(final String sender, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textChat.append(sender + " : " + message + "\n");
                final int scrollAmount = textChat.getLayout().getLineTop(textChat.getLineCount()) - textChat.getHeight();
                if (scrollAmount > 0) {
                    textChat.scrollTo(0, scrollAmount);
                } else {
                    textChat.scrollTo(0, 0);
                }
            }
        });
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        chat = textChat.getText().toString();
        time = textTime.getText().toString();

        viewers = textViewers.getText().toString();
        views = textViews.getText().toString();
        followers = textFollowers.getText().toString();

        initUi();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        boolean handled = false;

        if (v.equals(editTextSendMessage) && actionId == EditorInfo.IME_ACTION_SEND) {
            sendMessage();

        }
        return handled;
    }

    private void sendMessage() {
        final String message = editTextSendMessage.getText().toString();
        if (StringUtils.isNotEmpty(message)) {
            ircClient.sendMessage(message);
            editTextSendMessage.setText("");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textChat.append(username + " : " + message + "\n");
                    final int scrollAmount = textChat.getLayout().getLineTop(textChat.getLineCount()) - textChat.getHeight();
                    if (scrollAmount > 0) {
                        textChat.scrollTo(0, scrollAmount);
                    } else {
                        textChat.scrollTo(0, 0);
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == 0x55){
            initUi();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
