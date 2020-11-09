package com.example.aird.rsens_user.models;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.aird.rsens_user.tasks.FetchTask;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URL;
import java.util.ArrayList;


import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;



/**
 * Classe qui permet de stocker les données recu du capteur pour à la fin les envoyees.
 * Elle permet aussi de savoir s'il les jeux de données son complet.
 * Si un jeu n'est pas complet, ne pouvant pas determiner lequel,
 * Aucune donnee n'est envoyee.
 */

public class Data {

    static ArrayList<String> data_day = new ArrayList<String>();
    static ArrayList<String> data_heure = new ArrayList<String>();
    static ArrayList<String> data_pm1 = new ArrayList<String>();
    static ArrayList<String> data_pm25 = new ArrayList<String>();
    static ArrayList<String> data_pm10 = new ArrayList<String>();
    static ArrayList<String> data_latt = new ArrayList<String>();
    static ArrayList<String> data_longit = new ArrayList<String>();

    //Retourne les lists

    static public ArrayList<String> getData_day() {
        return data_day;
    }

    static public ArrayList<String> getData_heure() {
        return data_heure;
    }

    static public ArrayList<String> getData_pm1() {
        return data_pm1;
    }

    static public ArrayList<String> getData_pm10() {
        return data_pm10;
    }

    static public ArrayList<String> getData_pm25() {
        return data_pm25;
    }

    static public ArrayList<String> getData_latt() {
        return data_latt;
    }

    static public ArrayList<String> getData_longit() {
        return data_longit;
    }

    //Add une valeur aux lists

    static public void setData_day(String day) {
        data_day.add(day);
    }

    static public void setData_heure(String heure) {
        data_heure.add(heure);
    }

    static public void setData_pm1(String pm) {
        data_pm1.add(pm);
    }

    static public void setData_pm10(String pm) {
        data_pm10.add(pm);
    }

    static public void setData_pm25(String pm) {
        data_pm25.add(pm);
    }

    static public void setData_latt(String latt) {
        data_latt.add(latt);
    }

    static public void setData_longit(String longit) {
        data_longit.add(longit);
    }

    //Clear les lists

    static public void resData_day() {
        data_day.clear();
    }

    static public void resData_heure() {
        data_heure.clear();
    }

    static public void resData_pm1() {
        data_pm1.clear();
    }

    static public void resData_pm10() {
        data_pm10.clear();
    }

    static public void resData_pm25() {
        data_pm25.clear();
    }

    static public void resData_latt() {
        data_latt.clear();
    }

    static public void resData_longit() {
        data_longit.clear();
    }

    //Recupere les donnees dans la class Data pour completer l'url

    static public void SendData(String url_base, String mDeviceName){
        try {
            if (Data.getData_day().size()>0){
                if (Data.getData_day().size() == Data.getData_heure().size() && Data.getData_heure().size() == Data.getData_latt().size() && Data.getData_pm1().size() == Data.getData_pm25().size() && Data.getData_pm25().size() == Data.getData_pm10().size() && Data.getData_pm10().size() == Data.getData_latt().size() && Data.getData_longit().size() == Data.getData_latt().size()) {
                    for (int i = 0; i < Data.getData_day().size(); i++) {
                        FetchTask task = new FetchTask();
                        try {
                            URL url = new URL(url_base + "?idCapteur=" + mDeviceName.charAt(mDeviceName.length() - 3) + mDeviceName.charAt(mDeviceName.length() - 2) + mDeviceName.charAt(mDeviceName.length() - 1) + "&v=" + mDeviceName.charAt(mDeviceName.length() - 6) + mDeviceName.charAt(mDeviceName.length() - 5) + "&day=" + Data.getData_day().get(i) + "&heure=" + Data.getData_heure().get(i) + "&pm1=" + Data.getData_pm1().get(i) + "&pm25=" + Data.getData_pm25().get(i) + "&pm10=" + Data.getData_pm10().get(i) + "&latt=" + Data.getData_latt().get(i) + "&longit=" + Data.getData_longit().get(i));
                            task.execute(url);
                            Log.d("SENDATA", "URL OK : " + url.toString());
                        } catch (Exception e) {
                            //Toast.makeText(this, mDeviceName +" " + e.getMessage(), Toast.LENGTH_LONG).show();
                            Log.d("SENDATA", "Malformed URL exception");
                        }
                    }
                    Data.resData_day();
                    Data.resData_heure();
                    Data.resData_pm1();
                    Data.resData_pm10();
                    Data.resData_pm25();
                    Data.resData_latt();
                    Data.resData_longit();
                } else {
                    //Toast.makeText(this,"Probleme taille", Toast.LENGTH_LONG).show();
                }
            }

        } catch (Exception e) {
            Log.e("Exception", "Data send failed: " + e.toString());
        }
    }


}
