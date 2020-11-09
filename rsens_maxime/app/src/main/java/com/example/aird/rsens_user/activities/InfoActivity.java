
package com.example.aird.rsens_user.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.icu.text.IDNA;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.example.aird.rsens_user.controller.SharedPreferencesManager;
import com.example.aird.rsens_user.tasks.FetchMdpTask;
import com.example.aird.rsens_user.R;
import com.example.aird.rsens_user.models.Account;

import java.net.URL;
import java.util.Locale;

public class InfoActivity extends Activity {

    public static Context context;

    private String url_user;
    private String login;
    private EditText mdp;
    private EditText remdp;
    private Switch switchNotifOn;
    private Switch switchNotifPM;
    private Spinner languages;

    private String sLocale;
    private Locale myLocale;

    private boolean notifOn = false;
    private boolean notifPM = false;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String NOTIFON = "notifOn";
    public static final String NOTIFPM = "notifPM";
    public static final String LANG = "lang";
    public static final String PWD = "password";

    /** TEST MAXIME */
    public static final String LOGIN = "login";
    public static final String LOGINSAVED = "login saved";
    public static final String PASSWORDSAVED = "password saved";
    /** ---------- */

    private String uNotifOn;
    private String uNotifPM;
    private String uLang;

    public void setNotifOn(boolean notifOn) {
        this.notifOn = notifOn;
    }

    public void setNotifPM(boolean notifPM) {
        this.notifPM = notifPM;
    }

    public void setLocale(String lang) {
        sLocale = lang;
        myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        Intent refresh = new Intent(this, ConnectedActivity.class);
        saveData();
        startActivity(refresh);
        finish();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        //permet le retour vers activity prec
        getActionBar().setDisplayHomeAsUpEnabled(true);
        login = Account.getLogin();
        mdp = findViewById(R.id.info_nmdp);
        remdp = findViewById(R.id.info_verif);
        switchNotifOn = findViewById(R.id.swt_notifOn);
        switchNotifPM = findViewById(R.id.swt_notifPM);
        languages = findViewById(R.id.spn_languages);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.languages_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        languages.setAdapter(adapter);
        languages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                if (pos == 1) {
                    setLocale("fr");
                } else if (pos == 2) {
                    setLocale("en");
                }
                Toast.makeText(parent.getContext(),
                        R.string.info_choose, Toast.LENGTH_SHORT)
                        .show();
            }
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        loadData();
        updateData();

        switchNotifOn.setChecked(notifOn);
        switchNotifPM.setChecked(notifPM);
        Toast.makeText(this,uLang,Toast.LENGTH_LONG);

        if(!notifOn){
            switchNotifPM.setClickable(false);
        }

        switchNotifOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setNotifOn(isChecked);
                if (isChecked) {
                    switchNotifPM.setClickable(true);
                }
                else{
                    setNotifPM(false);
                    switchNotifPM.setChecked(false);
                    switchNotifPM.setClickable(false);
                }
                saveData();
            }
        });

        switchNotifPM.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setNotifPM(isChecked);
                saveData();
            }
        });

        Button btnModify = findViewById(R.id.info_sauvegarder);
        btnModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //recupere le login et mdp (Text)
                        url_user = getResources().getString(R.string.URL_mdp);
                        String login_s = login;
                        String mdp_s = mdp.getText().toString();
                        String remdp_s = remdp.getText().toString();
                        if(mdp_s.length()>2 && remdp_s.length()>2) {
                            if (mdp_s.contains(remdp_s) && mdp_s.length() == remdp_s.length()) {
                                FetchMdpTask task = new FetchMdpTask(InfoActivity.this);
                                //ajoute login et mdp a l'url pour verifier les identifiants
                                String url_s = url_user + "?login=" + login_s + "&mdp=" + mdp_s + "&remdp=" + remdp_s;
                                try {
                                    Log.d("fetch: ", url_s);
                                    URL url = new URL(url_s);
                                    task.execute(url);

                                    SharedPreferencesManager.getInstance(getContext()).storeString(PWD, mdp.getText().toString());


                                    Toast.makeText(InfoActivity.this, R.string.info_okMDP, Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Log.d("fetch: ", "Malformed URL exception");
                                }
                            }
                            else{
                                Toast.makeText(InfoActivity.this, R.string.info_errMDP, Toast.LENGTH_SHORT).show();
                            }
                        }
                        else{
                            //Toast.makeText(InfoActivity.this, "Rentrer un mot de passe valide.", Toast.LENGTH_SHORT).show();
                        }
                Intent intent = new Intent(InfoActivity.this, ConnectedActivity.class);
                startActivity(intent);
                finish();
            }
        });
        //Bouton pour quitter l'appli
        Button btnExit = findViewById(R.id.info_annuler);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(InfoActivity.this, ConnectedActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button btnDeconnexion = findViewById(R.id.btn_deconnexion);
        btnDeconnexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String login = SharedPreferencesManager.getInstance(getBaseContext()).retrieveString(LOGIN);
                String pwd = SharedPreferencesManager.getInstance(getBaseContext()).retrieveString(PWD);

                //SharedPreferencesManager.getInstance(getContext()).clear();
                SharedPreferencesManager.getInstance(getContext()).delete(LOGIN);
                SharedPreferencesManager.getInstance(getContext()).delete(PWD);
                SharedPreferencesManager.getInstance(getContext()).delete(LANG);
                SharedPreferencesManager.getInstance(getContext()).delete("URL");

                // Stocke le login et le mdp du user dans les preference pour les sauvagarder et avoir le champ
                // pour le login et mdp du dernier user remplit.
                SharedPreferencesManager.getInstance(getContext()).storeString(LOGINSAVED, login);
                SharedPreferencesManager.getInstance(getContext()).storeString(PASSWORDSAVED, pwd);

                Intent intentDec = new Intent(InfoActivity.this, MainActivity.class);
                startActivity(intentDec);
                finish();
            }
    });
    }

    public void saveData() {
        SharedPreferencesManager.getInstance(getContext()).storeString(NOTIFON, notifOn+"");
        SharedPreferencesManager.getInstance(getContext()).storeString(NOTIFPM, notifPM+"");
        SharedPreferencesManager.getInstance(getContext()).storeString(LANG, sLocale);

        Toast.makeText(this, R.string.data_ok, Toast.LENGTH_SHORT).show();
    }

    public void loadData() {
        uNotifOn = SharedPreferencesManager.getInstance(getContext()).retrieveString(NOTIFON);
        uNotifPM = SharedPreferencesManager.getInstance(getContext()).retrieveString(NOTIFPM);
        uLang = SharedPreferencesManager.getInstance(getContext()).retrieveString(LANG);
    }

    public void updateData() {
        if(uNotifPM != "" && uNotifOn != "")
            setNotifOn(Boolean.parseBoolean(uNotifOn));
            setNotifPM(Boolean.parseBoolean(uNotifPM));
    }

    //action click sur btn connexion/deconnexion/sync/abo
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(InfoActivity.this, ConnectedActivity.class);
                startActivity(intent);
                /** Test */
                finish();
                /** ---- */
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** TEST ------- */
    @Override
    public void onBackPressed(){
        //Account.clearC();
        Intent myIntent2 = new Intent(InfoActivity.this,
                ConnectedActivity.class);
        startActivity(myIntent2);
        /** ------*/
        finish();
        /** -------*/
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
