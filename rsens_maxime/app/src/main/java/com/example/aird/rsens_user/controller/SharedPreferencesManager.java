package com.example.aird.rsens_user.controller;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {

    private static SharedPreferencesManager instance;
    private static Context context;
    private SharedPreferences preferences;
    private android.content.SharedPreferences.Editor editor;

    /**
     * Constructor
     * @param context
     */
    private SharedPreferencesManager(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences("RSENS", Context.MODE_PRIVATE);
        editor = preferences.edit();
    }


    /**
     * Get an instance
     * @param context
     * @return an instence
     */
    public static synchronized SharedPreferencesManager getInstance(Context context) {
        if(instance == null)
            instance = new SharedPreferencesManager(context);
        return instance;
    }

    /**
     * Clear an editor.
     */
    public void clear() {
        editor.clear();
        editor.commit();
    }

    //* Delete a SharedPreferences
    public void delete(String key) {
        SharedPreferences settings = context.getSharedPreferences("RSENS", Context.MODE_PRIVATE);
        settings.edit().remove(key).commit();
    }

    /**
     * Store a string in a map
     * @param key key in the map
     * @param value value in the map
     */
    public void storeString(String key, String value){
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * Retrive a string in a map
     * @param key the key in the map
     * @return
     */
    public String retrieveString(String key) {
        return preferences.getString(key, "");
    }

    /**
     * Store an int in a map
     * @param key key in the map
     * @param value value in the map
     */
    public void storeInt(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }

    /**
     * Retrive a key in the map.
     * @param key key in a map
     * @return
     */
    public int retrieveInt(String key) { return preferences.getInt(key, 0); }

    public boolean contains(String key){
        if(preferences.contains(key))
            return true;
        else
            return false;
    }
}
