package com.anthonydenaud.twitchdashboard.twitch;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;

public class TwitchAPI {

    private static final String API_BASE_URL ="https://api.twitch.tv/kraken/";

    public static String getChannelTitle(String channel){
        String name = "";
        try {
            String json = IOUtils.toString(URI.create(API_BASE_URL+ "channels/"+channel), Charsets.UTF_8);

            JSONObject root = new JSONObject(json);
            name = root.getString("status");

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return name;
    }

    public static int getViewers(String channel){
        int viewers = -1;
        try {
            String json = IOUtils.toString(URI.create(API_BASE_URL+ "streams/"+channel), Charsets.UTF_8);

            JSONObject root = new JSONObject(json);

            if(root.isNull("stream")){
                viewers = -2;
            }else{
                viewers = root.getJSONObject("stream").getInt("viewers");
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return viewers;
    }

    public static int getViews(String channel){
        int views = -1;
        try {
            String json = IOUtils.toString(URI.create(API_BASE_URL+ "channels/"+channel), Charsets.UTF_8);

            JSONObject root = new JSONObject(json);
            views = root.getInt("views");

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return views;
    }

    public static int getFollowers(String channel){
        int views = -1;
        try {
            String json = IOUtils.toString(URI.create(API_BASE_URL+ "channels/"+channel), Charsets.UTF_8);

            JSONObject root = new JSONObject(json);
            views = root.getInt("followers");

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return views;
    }

}
