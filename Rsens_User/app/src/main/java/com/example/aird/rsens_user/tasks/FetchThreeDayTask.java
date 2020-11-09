package com.example.aird.rsens_user.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.example.aird.rsens_user.activities.ThreeDayActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by naegel on 07/11/2016.
 * Modified by Quentin Mayer, AIR&D.
 *
 * Recupere les jours des données en fonction du capteur choisi
 */

public class FetchThreeDayTask extends AsyncTask<URL,Integer,JSONObject> {
    //param pour stocker l'activity, specifique car doit ecrire fonction (vu plus tard)
    ThreeDayActivity activity;

    //stock l'activity
    public FetchThreeDayTask(ThreeDayActivity activity){
        this.activity=activity;
    }

    //toast pour tenir au courant l'user
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //Toast.makeText(activity,"Acces aux donnees", Toast.LENGTH_SHORT).show();
    }

    //action fait en arriere plan
    @Override
    protected JSONObject doInBackground(URL... urls) {
        StringBuffer json=null;
        JSONObject jsonObject=null;
        try {
            //recup l'url
            URL url = urls[0];
            Log.d("doInBackground","url : "+url);
            //connection etablie
            HttpURLConnection connection =(HttpURLConnection) url.openConnection();
            //recupere le json
            BufferedReader reader = new BufferedReader( new InputStreamReader(connection.getInputStream()));
            json = new StringBuffer(1024);
            String tmp="";
            while((tmp=reader.readLine())!=null) {
                Log.d("connexion http", "connexion !!!");
                json.append(tmp).append("\n");
            }
            reader.close();
            jsonObject=new JSONObject(json.toString());
            publishProgress(1);
        }
        catch(Exception e) {
            Log.d ("Exception", e.getMessage());}

        return jsonObject;
    }

    //toast pour tenir au courant l'user
    @Override
    protected void onProgressUpdate(Integer... values) {
        //Toast.makeText(activity,"Acces aux donnees en cours", Toast.LENGTH_SHORT).show();

    }

    //toast pour tenir au courant l'user + fait passer dans la fonction specifique le json pour l'utiliser
    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        //Toast.makeText(activity,"Fin acces aux donnees", Toast.LENGTH_SHORT).show();
        if(jsonObject==null){
            //Toast.makeText(activity,"Erreur", Toast.LENGTH_SHORT).show();
        }else {
            activity.updateView(jsonObject);
        }
    }
}
