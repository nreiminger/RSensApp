package com.example.aird.rsens_user.activities;

import android.app.Activity;
import android.app.SharedElementCallback;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.aird.rsens_user.controller.SharedPreferencesManager;
import com.example.aird.rsens_user.tasks.FetchUserTask;
import com.example.aird.rsens_user.R;
import com.example.aird.rsens_user.models.Account;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.Locale;

/**
 * Activity qui permet à user de se connecter.
 */

public class MainActivity extends Activity {

    public static Context context;

    //variables
    private String url_user;
    private EditText login;
    private EditText mdp;
    private LinearLayout signin;

    private Locale myLocale;

    public static final String LOGIN = "login";
    public static final String PWD = "password";
    public static final String LANG = "lang";

    private String ulogin;
    private String pwd;
    private String uLang;

    public static final String LOGINSAVED = "login saved";
    public static final String PASSWORDSAVED = "password saved";

    private Integer count =0;
    private String loginSaved;

    public void setLocale(String lang) {
        myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorBar)));
        getActionBar().setTitle(Html.fromHtml("<font color=\"#FFFFFF\">" + getTitle() + "</font>"));
        //recupere le login et mdp (EditText)
        login = findViewById(R.id.edt_login);
        mdp = findViewById(R.id.edt_mdp);
        signin = findViewById(R.id.llt_signin);
        loadData();


        if(uLang!=""){
            setLocale(uLang);
        }

        // Récupere les valeur du login et du mdp du dernier utilisateur connecté.
        String loginSaved = SharedPreferencesManager.getInstance(this.getContext()).retrieveString(LOGINSAVED);
        String mdpSaved = SharedPreferencesManager.getInstance(this.getContext()).retrieveString(PASSWORDSAVED);

        // Prérempli les champs avec ceux du dernier utilisateur connecté.
        login.setText(loginSaved);
        mdp.setText(mdpSaved);

        //Bouton pour se connecter
        Button btnConnec = findViewById(R.id.btn_connexion);
        btnConnec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //recupere le login et mdp (Text)
                url_user = getResources().getString(R.string.URL_user);
                String login_s = login.getText().toString();
                String mdp_s = mdp.getText().toString();
                FetchUserTask task = new FetchUserTask(MainActivity.this);
                //ajoute login et mdp a l'url pour verifier les identifiants
                String url_s = url_user + "?login=" + login_s + "&mdp=" + mdp_s;
                try {
                    /** TEST MAX */
                    SharedPreferencesManager.getInstance(getBaseContext()).delete(LOGINSAVED);
                    SharedPreferencesManager.getInstance(getBaseContext()).delete(PASSWORDSAVED);
                    /** --------- */
                    URL url = new URL(url_s);
                    task.execute(url);
                } catch (Exception e) {
                    Log.d("fetch: ", "Malformed URL exception");
                }
            }
        });
        //Bouton pour quitter l'appli
        Button btnExit = findViewById(R.id.btn_quitter);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //finish();
                //System.exit(0);
                //Toast.makeText(MainActivity.this,"I'm was clicked", Toast.LENGTH_LONG).show();
                //android.os.Process.killProcess(android.os.Process.myPid());
                finishAffinity(); // Close all activites
                System.exit(0);  // Releasing resources
            }
        });
    }

    public void saveData() {
        SharedPreferencesManager.getInstance(this.getContext()).storeString(LOGIN, login.getText().toString());
        SharedPreferencesManager.getInstance(this.getContext()).storeString(PWD, mdp.getText().toString());
    }

    public void loadData() {
        uLang = SharedPreferencesManager.getInstance(this.getBaseContext()).retrieveString(LANG);
        ulogin = SharedPreferencesManager.getInstance(this.getBaseContext()).retrieveString(LOGIN);
        pwd = SharedPreferencesManager.getInstance(this.getBaseContext()).retrieveString(PWD);

            if(ulogin != "" && pwd != ""){
                count++;
                //recupere le login et mdp (Text)
                url_user = getResources().getString(R.string.URL_user);
                String login_s = ulogin;
                String mdp_s = pwd;
                FetchUserTask task = new FetchUserTask(MainActivity.this);
                //ajoute login et mdp a l'url pour verifier les identifiants
                String url_s = url_user + "?login=" + login_s + "&mdp=" + mdp_s;
                try {
                    URL url = new URL(url_s);
                    task.execute(url);
                } catch (Exception e) {
                    Log.d("fetch: ", "Malformed URL exception");
                }
            }else {
                signin.setVisibility(View.VISIBLE);
            }
    }

    public void updateData() {
        login.setText(ulogin);
        mdp.setText(pwd);
    }

    //verifie si json vide sinon verifie si success si oui connexion
    public void updateView(JSONObject jsonObject){
        if(jsonObject==null) {
            //Toast.makeText(this,"Error fetching", Toast.LENGTH_LONG).show();
        }
        else {
            //Log.e("UpdateView Main", "UpdateView Main");
            try {
                JSONArray data = jsonObject.getJSONArray("products");
                int success=0;
                success=data.getJSONObject(0).getInt("success");
                if(success==1){
                    saveData();
                    //Log.e("Login", SharedPreferencesManager.getInstance(this.getContext()).retrieveString(LOGIN));
                    Account.setLogin(login.getText().toString());
                    //Account.setLogin(SharedPreferencesManager.getInstance(this.getContext()).retrieveString(LOGIN));
                    Intent myIntent = new Intent(MainActivity.this,
                            PullActivity.class);
                    startActivity(myIntent);
                    finish();
                }
                else{
                    Log.d("fetch: ", "Error Connection");
                    Toast.makeText(MainActivity.this,R.string.data_err, Toast.LENGTH_SHORT).show();
                    signin.setVisibility(View.VISIBLE);
                }
            }
            catch (Exception e){}
        }
    }

    /** TEST ------- */
    @Override
    public void onBackPressed(){
        finish();
    }
    /** --------- */

    /**
     * Get the context of the activie
     * @return
     */
    public static Context getContext() {
        return context;
    }

}
