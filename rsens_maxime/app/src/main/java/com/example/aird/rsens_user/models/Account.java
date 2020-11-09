package com.example.aird.rsens_user.models;

import android.content.Context;
import android.util.Log;

import com.example.aird.rsens_user.controller.SharedPreferencesManager;

/**
  * Cette classe permet de garder le login en memoire pour en suite recuperer les capteurs
  * qui lui sont associés
 **/
public class Account {
    public static Context context;
    private static String login=null;
    public static final String LOGIN = SharedPreferencesManager.getInstance(getContext()).retrieveString("LOGIN");
    private static String id=null;
    private static String idCapteur=null;
    private static String vCapteur=null;

    public static String getid() {
        return id;
    }

    public static String getvCapteur() {
        return vCapteur;
    }

    public static void setvCapteur(String vCapteur) {
        Account.vCapteur = vCapteur;
    }

    public static String getLogin() {
        if(login == "") {
            Log.e("Login", SharedPreferencesManager.getInstance(getContext()).retrieveString("LOGIN"));
            return SharedPreferencesManager.getInstance(getContext()).retrieveString("LOGIN");
        }
        return login;
    }

    public static void setLogin(String login) {
        Account.login = login;
    }

    public static void setid(String id) {
        Account.id = id;
    }

    public static String getidCapteur() {
        return idCapteur;
    }

    public static void setidCapteur(String idCapteur) {
        Account.idCapteur = idCapteur;
    }

    /**public static String getLogin(){ return login; }*/

    /**public static void setLogin(String new_login){ login=new_login; }*/

    public static void clear(){
        login=null;
        id=null;
        idCapteur=null;
        vCapteur=null;
    }

    public static void clearC(){
        id=null;
        idCapteur=null;
        vCapteur=null;
    }

    /**
     * Get the context of the activie
     * @return
     */
    public static Context getContext() {
        return context;
    }
}
