package com.example.aird.rsens_user.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.example.aird.rsens_user.tasks.FetchThreeDataTask;
import com.example.aird.rsens_user.tasks.FetchThreeDayTask;
import com.example.aird.rsens_user.R;
import com.example.aird.rsens_user.models.Account;
import com.example.aird.rsens_user.models.UrlButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ThreeDayActivity extends Activity {

    /**
     * Activity qui affiche les jours ou des donnees ont ete recupere pour le capteur demande.
     * Recupere aussi les donnees pour les passer a la prochaine activity.
     */

    private String url_days;
    private String url_data;
    static String idC = Account.getidCapteur();

    private int count=0;

    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //permet le retour vers activity prec
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorBar)));
        setContentView(R.layout.activity_day);
        Intent myIntent = getIntent();

        url_days=getResources().getString(R.string.URL_days);
        url_data=getResources().getString(R.string.URL_data);

        //titre de l'activity = le jour choisi
        setTitle("Dolphin "+Account.getidCapteur());
        getActionBar().setTitle(Html.fromHtml("<font color=\"#FFFFFF\">" + getTitle() + "</font>"));
        //recupere les jours de la bdd
        FetchThreeDayTask task = new FetchThreeDayTask(this);
        try {
            URL url = new URL(url_days + "?id=" + idC);
            task.execute(url);
        } catch (Exception e) {
            //Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d("fetch: ", "Malformed URL exception");
        }

    }

    //action pour retourner a l'activity prec
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //fait une task differente en fonction de l'url passee
    public void fetch(String s_url, View v) {
        if(s_url == url_days) {
            FetchThreeDayTask task = new FetchThreeDayTask(this);
            try {
                URL url = new URL(s_url);
                task.execute(url);
            } catch (Exception e) {
                Log.d("fetch: ", "Malformed URL exception");
            }
        }
        else{
            FetchThreeDataTask task = new FetchThreeDataTask(this);
            try {
                URL url = new URL(s_url);
                task.execute(url);
            } catch (Exception e) {
                Log.d("fetch: ", "Malformed URL exception");
            }
        }

    }


    public static long getDateDiff(SimpleDateFormat format, String oldDate, String newDate) {
        try {
            Date dateNew = new Date(newDate);
            Date dateOld = new Date(oldDate);
            return TimeUnit.DAYS.convert(dateNew.getTime() - dateOld.getTime(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    //update pour la requete des jours
    public void updateView(JSONObject jsonObject) {
        if(jsonObject==null) {
            //Toast.makeText(this,"Error fetching", Toast.LENGTH_LONG).show();
        }
        else {
            try {
                JSONArray days = jsonObject.getJSONArray("products");
                layout = (LinearLayout) findViewById(R.id.llt_days);
                for (int j = 0; j <3; j++) {
                    String day = days.getJSONObject(j).getString("day");
                    Calendar date = Calendar.getInstance();
                    String[] data_date = day.split("-");
                    day = data_date[0] + "/" + data_date[1] + "/" + data_date[2];
                    int dateDifference = (int) getDateDiff(new SimpleDateFormat("YYYY/MM/DD"), day,  date.get(Calendar.YEAR) + "/" + (date.get(Calendar.MONTH) + 1) + "/" + date.get(Calendar.DAY_OF_MONTH));
                    if(dateDifference<=15) {
                        final UrlButton b = new UrlButton(this, url_data + "?day=" + day + "&id=" + idC);
                        b.setText(day);
                        b.setBackgroundColor(Color.parseColor("#0CB9C4"));
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(0, 10, 0, 0);
                        b.setLayoutParams(params);
                        b.setTextColor(Color.WHITE);
                        layout.addView(b);
                        b.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                fetch(b.getUrl(), v);
                            }
                        });
                    }
                }
                Log.d("Contenu du JSON reçu : ", jsonObject.toString());
            }
            catch (Exception e){}
        }
    }

    //update pour la requete des donnees pour un jour
    //fait passer les infos a la prochaine activity
    public void updateDataView(JSONObject jsonObject){
        if(jsonObject==null) {
            //Toast.makeText(this,"Error fetching weather", Toast.LENGTH_LONG).show();
        }
        else {
            try {
                JSONArray data = jsonObject.getJSONArray("products");
                Intent myIntent = new Intent(this, DataActivity.class);
                String day=null;
                ArrayList<String> heure = new ArrayList<String>();
                ArrayList<String> pm1 = new ArrayList<String>();
                ArrayList<String> pm25 = new ArrayList<String>();
                ArrayList<String> pm10 = new ArrayList<String>();
                ArrayList<String> latt = new ArrayList<String>();
                ArrayList<String> longit = new ArrayList<String>();
                for (int j = 0; j <data.length(); j++) {
                    day=data.getJSONObject(j).getString("day");
                }
                myIntent.putExtra("id", idC);
                myIntent.putExtra("day", day);
                startActivity(myIntent);
            }
            catch (Exception e){}
        }
    }
}
