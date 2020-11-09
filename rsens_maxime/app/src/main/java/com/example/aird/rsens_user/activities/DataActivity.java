package com.example.aird.rsens_user.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

import com.example.aird.rsens_user.R;
import com.example.aird.rsens_user.controller.SharedPreferencesManager;
import com.squareup.picasso.Picasso;

/**
 * Activity qui permet l'affichage de l'image.
 * L'image est un histogramme des donnees recu pour la journee et le capteur demande.
 */

public class DataActivity extends Activity {

    public static Context context;

    private ImageView imageView;
    private String url_img;

    public static final String LANG = "lang";
    private String uLang;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        Log.e("ImageViewErr", "onCreate: enter");
        //permet de recuperer les valeurs passees entre activity
        Intent myIntent = getIntent();
        Bundle extra = myIntent.getExtras();
        //permet le retour vers activity prec
        getActionBar().setDisplayHomeAsUpEnabled(true);
        //titre de l'activity = le jour choisi
        getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorBar)));
        getActionBar().setTitle(Html.fromHtml("<font color=\"#FFFFFF\">" + extra.getString("day") + "</font>"));
        //assigne les composants
        String day = extra.getString("day");
        String id = extra.getString("id");

        url_img=getResources().getString(R.string.URL_img);

        imageView = (ImageView) findViewById(R.id.imageView);

        uLang = SharedPreferencesManager.getInstance(getContext()).retrieveString(LANG);


        Log.e("ImageViewErr", "onCreate: "+uLang);

        if(uLang.contains("en")){
            Log.e("ImageViewErr", "onCreate: "+url_img+id+"_Data_"+day+"_phone_en.jpg");
            Picasso.with(getBaseContext()).load(url_img+id+"_Data_"+day+"_phone_en.jpg").fit().into(imageView);
        }
        else{
            Log.e("ImageViewErr", "onCreate: "+url_img+id+"_Data_"+day+"_phone_fr.jpg");
            Picasso.with(getBaseContext()).load(url_img+id+"_Data_"+day+"_phone_fr.jpg").fit().into(imageView);
        }
    }

    //action pour retourner a l'activity prec
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                /** Test */
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Get the context of the activie
     * @return
     */
    public static Context getContext() {
        return context;
    }
}
