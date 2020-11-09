package com.example.aird.rsens_user.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.aird.rsens_user.R;
import com.example.aird.rsens_user.models.Account;

/**
 * Activity qui s'ouvre une fois le user connecte.
 * Elle permet de choisir entre le suivi de donnees d'un capteur
 * Ou de voir les resultats des donnees grace à un image.
 */

public class ConnectedActivity extends Activity {

    //Activity qui fait le lien avec les autres, choix :
    //Recuperer les donnees du capteur
    //Recuperer les donnees de la base de donnee
    //Quitter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);

        setTitle("Dolphin "+Account.getidCapteur());
        getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorBar)));
        getActionBar().setTitle(Html.fromHtml("<font color=\"#FFFFFF\">" + getTitle() + "</font>"));
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Button btnDevice = findViewById(R.id.btn_push);
        btnDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(ConnectedActivity.this,
                        ThreeDayActivity.class);
                startActivity(myIntent);
            }
        });

        Button btnPull = findViewById(R.id.btn_pull);
        btnPull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Intent myIntent = new Intent(ConnectedActivity.this,
                        DayActivity.class);
                startActivity(myIntent);
            }
        });

        Button btnQuitter = findViewById(R.id.btn_quitter);
        btnQuitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
    }

    //afficher btn pour connexion ou deconnexion a l'appereil bluetooth en fonction de la variable de connexion
    //abonnement et sync permettent tous deux de s'abonner mais sync fait une demande a l'arduino pour recup
    //toutes les donnees produites
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
                Intent myIntent = new Intent(ConnectedActivity.this,
                        InfoActivity.class);
                startActivity(myIntent);
                finish();
                return true;
            case android.R.id.home:
                Account.clearC();
                Intent myIntent2 = new Intent(ConnectedActivity.this,
                        MainActivity.class);
                startActivity(myIntent2);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        Account.clearC();
        Intent myIntent2 = new Intent(ConnectedActivity.this,
                MainActivity.class);
        startActivity(myIntent2);
        finish();
    }
}
