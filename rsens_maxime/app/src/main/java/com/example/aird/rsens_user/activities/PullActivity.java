package com.example.aird.rsens_user.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aird.rsens_user.controller.SharedPreferencesManager;
import com.example.aird.rsens_user.tasks.FetchPullTask;
import com.example.aird.rsens_user.R;
import com.example.aird.rsens_user.models.Account;
import com.example.aird.rsens_user.models.CapteurButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;

/**
 * Activity qui affiche les capteurs associe a user.
 */

public class PullActivity  extends Activity {

    private String url_capteur;
    private LinearLayout layout;
    static ArrayList<String> data_id = new ArrayList<String>();
    static ArrayList<String> data_idC = new ArrayList<String>();
    static ArrayList<String> data_vC = new ArrayList<String>();

    public static Context context;

    private String login;

    private int count=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //permet le retour vers activity prec
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle("Choix du capteur");
        getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorBar)));
        getActionBar().setTitle(Html.fromHtml("<font color=\"#FFFFFF\">" + getTitle() + "</font>"));
        setContentView(R.layout.activity_pull);
        /** Modif Max */
        layout = (LinearLayout) findViewById(R.id.llt_capteurs);
        /** ---------*/
        //url_capteur=getResources().getString(R.string.URL_capteur)+Account.getLogin();

        if(!SharedPreferencesManager.getInstance(getContext()).contains("URL")){
            url_capteur=getResources().getString(R.string.URL_capteur)+Account.getLogin();
            SharedPreferencesManager.getInstance(this.getContext()).storeString("URL", url_capteur);
        }
        else{
            url_capteur = SharedPreferencesManager.getInstance(this.getContext()).retrieveString("URL");
        }

        /** ---------- */
        //recupere les jours de la bdd
        //Log.e("Before FetchPullTask", "Before FetchPullTask");
        FetchPullTask task = new FetchPullTask(this);
        try {
            //Log.e("Inside FetchPullTask", "Inside FetchPullTask");
            URL url = new URL(url_capteur);
            task.execute(url);
        } catch (Exception e) {
            Log.d("fetch: ", "Malformed URL exception");
        }
        //Log.e("After FetchPullTask", "After FetchPullTask");
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.info_user, menu);
        menu.findItem(R.id.menu_info_user).setVisible(true);
        return true;
    }

    //action click sur btn connexion/deconnexion/sync/abo
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_info_user:
                Intent myIntent = new Intent(this,
                        InfoActivity.class);
                startActivity(myIntent);
                // Test
                finish();
                // ------
                return true;
            // TEST
            case android.R.id.home:
                myIntent = new Intent(PullActivity.this, MainActivity.class);
                startActivity(myIntent);
                finish();
                return true;
            // --------
        }
        return super.onOptionsItemSelected(item);
    }

    //fait une task en fonction de l'url passee
    public void fetch(String s_url, View v) {
        if(s_url == url_capteur) {
            FetchPullTask task = new FetchPullTask(this);
            try {
                URL url = new URL(s_url);
                task.execute(url);
            } catch (Exception e) {
                Log.d("fetch: ", "Malformed URL exception");
            }
        }

    }

    //update pour la requete des jours
    public void updateView(JSONObject jsonObject) {
        //layout = (LinearLayout) findViewById(R.id.llt_capteurs);
        //Log.e("UpdateView", "UpdateView Begining");
        //Log.e("JSONObject", jsonObject + "");
        if(jsonObject==null) {
            Toast.makeText(this,R.string.data_err, Toast.LENGTH_LONG).show();
        }
        else {
            try {
                JSONArray capteur = jsonObject.getJSONArray("products");
                for (int j = 0; j <capteur.length(); j++) {

                    String id = capteur.getJSONObject(j).getString("id");
                    String idCapteur = capteur.getJSONObject(j).getString("idCapteur");
                    String vCapteur = capteur.getJSONObject(j).getString("vCapteur");

                    if (capteur.length() == 1) {
                        data_id.add(id);
                        data_idC.add(idCapteur);
                        data_vC.add(vCapteur);

                        final CapteurButton b = new CapteurButton(this, id);
                        b.setText("Dolphin " + id);
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
                                String b_id = b.getText().toString().replace("Dolphin ", "");
                                int rang = data_id.indexOf(b_id);
                                String b_idC = data_idC.get(rang);
                                String b_vC = data_vC.get(rang);

                                Log.d("CAPTEUR", b_id + " " + b_idC + " " + b_vC);
                                Log.e("CAPTEUR", b_id + " " + b_idC + " " + b_vC);

                                Account.setid(b_id);
                                Account.setidCapteur(b_idC);
                                Account.setvCapteur(b_vC);

                                Intent myIntent = new Intent(v.getContext(), DeviceScanActivity.class);
                                myIntent.putExtra("id", b_id);
                                myIntent.putExtra("idC", b_idC);
                                myIntent.putExtra("vC", b_vC);
                                startActivity(myIntent);
                                finish();
                            }
                        });

                        Account.setid(id);
                        Account.setidCapteur(idCapteur);
                        Account.setvCapteur(vCapteur);

                        Intent myIntent = new Intent(this, DeviceScanActivity.class);
                        myIntent.putExtra("id", id);
                        myIntent.putExtra("idC", idCapteur);
                        myIntent.putExtra("vC", vCapteur);
                        startActivity(myIntent);
                        finish();
                    } else {

                        Log.d("CAPTEUR", id + " " + idCapteur + " " + vCapteur);
                        Log.e("CAPTEUR", id + " " + idCapteur + " " + vCapteur);

                        data_id.add(id);
                        data_idC.add(idCapteur);
                        data_vC.add(vCapteur);

                        final CapteurButton b = new CapteurButton(this, id);
                        b.setText("Dolphin " + id);
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
                                String b_id = b.getText().toString().replace("Dolphin ", "");
                                int rang = data_id.indexOf(b_id);
                                String b_idC = data_idC.get(rang);
                                String b_vC = data_vC.get(rang);

                                Log.d("CAPTEUR", b_id + " " + b_idC + " " + b_vC);
                                Log.e("CAPTEUR", b_id + " " + b_idC + " " + b_vC);


                                Account.setid(b_id);
                                Account.setidCapteur(b_idC);
                                Account.setvCapteur(b_vC);

                                Intent myIntent = new Intent(v.getContext(), DeviceScanActivity.class);
                                myIntent.putExtra("id", b_id);
                                myIntent.putExtra("idC", b_idC);
                                myIntent.putExtra("vC", b_vC);
                                startActivity(myIntent);
                                finish();
                            }
                        });
                    }
                }
                Log.d("Contenu du JSON reçu : ", jsonObject.toString());
            }
            catch (Exception e){}
        }
        try {
            //Log.e("getChildCount", layout.getChildCount() + "");
           if(layout.getChildCount()==0){
               //Log.e("Try", "Inside try");
               TextView noCapteur = new TextView(this);
               noCapteur.setText(R.string.pull_noCapteur);
               noCapteur.setTextSize(24);
               noCapteur.setGravity(Gravity.CENTER);
               noCapteur.setHeight(300);
               layout.addView(noCapteur);
           }
        }
        catch(Exception e){
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed()
    }

    /**
     * Get the context of the activie
     * @return
     */
    public static Context getContext() {
        return context;
    }
}
