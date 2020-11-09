/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Modified by Quentin Mayer, AIR&D.
 */

package com.example.aird.rsens_user.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aird.rsens_user.controller.SharedPreferencesManager;
import com.example.aird.rsens_user.services.BluetoothLeService;
import com.example.aird.rsens_user.R;
import com.example.aird.rsens_user.models.SampleGattAttributes;
import com.example.aird.rsens_user.models.Account;
import com.example.aird.rsens_user.models.Data;
import com.example.aird.rsens_user.tasks.FetchWeatherTask;
import com.marcinmoskala.arcseekbar.ArcSeekBar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.aird.rsens_user.App.CHANNEL_ID;
import static java.lang.Integer.parseInt;

/**
 * Apres avoir choisi l'appareil bluetooth, cette activite fourni une interface pour l'utilisateur.
 * On pourra y voir les differents services, leurs caracteristiques et les donnees transmises.
 */

public class DeviceControlActivity extends Activity {

    public static Context context;

    //Nom de l'activite pour les log
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    //Constante pour recuperer les valeurs de l'intent (valeurs passées entre activite)
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    //Attributs pour completer les composants du layout de l'activite
        //Text
    //private TextView mConnectionState;

    /*private TextView mDataDate;
    private TextView mDataHeure;
    private TextView mDataLat;
    private TextView mDataLong;*/
    private ImageView mBikePic;
    private ImageView mStrollerPic;
    private TextView mDataPM;
    private TextView mTemp;
    private TextView mHumid;
    private ArcSeekBar mArcSeekBar;
    private ImageView mImage;

    private String mDeviceName;
    private String mDeviceAddress;

    private String dataType = "long";
        //List
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    //Variable pour le code
        //pour savoir si appareil connecte

    private boolean mConnected = false;

    private boolean notifOn = false;
    private boolean notifPM = false;

    public void setNotifOn(boolean notifOn) {
        this.notifOn = notifOn;
    }

    public void setNotifPM(boolean notifPM) {
        this.notifPM = notifPM;
    }

    Timer timer;
    TimerTask timerTask;

    final Handler handler = new Handler();

        //pour savoir si la notification sur une characteristique est mise en place
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothGattCharacteristic mCharacteristic;
        //pour eviter que les boutons n'ouvre plusieurs fois la page
    private int count=0;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private String url_base;
    private String UUID_Charac;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String NOTIFON = "notifOn";
    public static final String NOTIFPM = "notifPM";

    private String uNotifOn;
    private String uNotifPM;



    //action du service
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        //a la connexion verifie si service est initialise si non finish si oui connect
        //Params : ComponentName ici pas utile
        //         IBinder pour recuperer le mBluetoothLeService et faire la connexion
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                stopTimerTask();
                finish();
            }
            //se connecte automatiquement a l'appareil quand il est selectionne
            mBluetoothLeService.connect(mDeviceAddress);
        }

        //Deconnexion : Reinit mBluetoothLeService a null
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // gere plusieurs events
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        //action lors de reception de broadcast
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            //connexion :  passe le statut a connected
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            }
            //deconnexion : passe le statut a disconnected et reinit l'affichage avant de quitter l'activity
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                Data.SendData(url_base, mDeviceName);
                clearUI();
            }
            //service trouve : permet de trouver la charac voulu pour plus tard s'y abonner et/ou sync
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            }
            //reception de donnee : permet de lancer la fonction pour recuperer les donnees du intent passe entre activity
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    //fonction pour re-init l'interface de l'activite et res les donnees de Data
    private void clearUI() {
        /*mDataDate.setText("");
        mDataHeure.setText("");
        mDataLat.setText("");
        mDataLong.setText("");*/
        mDataPM.setText("");
        Data.resData_day();
        Data.resData_heure();
        Data.resData_pm1();
        Data.resData_pm10();
        Data.resData_pm25();
        Data.resData_latt();
        Data.resData_longit();
    }

    //initialise les attributs
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devicecontrol);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        //assigne les composants aux attributs
//        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        /*mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataDate = (TextView) findViewById(R.id.data_date);
        mDataHeure = (TextView) findViewById(R.id.data_heure);
        mDataLat = (TextView) findViewById(R.id.data_lat);
        mDataLong = (TextView) findViewById(R.id.data_long);*/
        mBikePic = (ImageView) findViewById(R.id.img_bike);
        mStrollerPic =(ImageView) findViewById(R.id.img_stroller);
        mDataPM = (TextView) findViewById(R.id.data_pm);
        mHumid = (TextView) findViewById(R.id.txt_humid);
        mTemp = (TextView) findViewById(R.id.txt_temp);
        mArcSeekBar = (ArcSeekBar) findViewById(R.id.seekArc);
        mImage = (ImageView) findViewById(R.id.img_echelle);


        mArcSeekBar.setMaxProgress(600);
        mArcSeekBar.setProgressWidth(50);
        mArcSeekBar.setProgressBackgroundWidth(50);

        url_base = getResources().getString(R.string.URL_base);
        UUID_Charac = getResources().getString(R.string.UUID_BLE_1_Charac);

        loadData();
        updateData();

        startTimer();

        boolean notifAsked = SharedPreferencesManager.getInstance(this.getContext()).contains("NOTIFASK");

        if(uNotifPM == "" && uNotifOn == "" && !notifAsked) {
            SharedPreferencesManager.getInstance(this.getContext()).storeInt("NOTIFASK", 1);
            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setMessage("Voulez vous activer les notifications pour être avertie de la pollution ?");
            builder1.setCancelable(true);

            builder1.setPositiveButton(
                    R.string.device_ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            setNotifOn(true);
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(DeviceControlActivity.this);
                            builder1.setMessage("Voulez vous seulement les notifications au dessus de 40pm ? (Seuil de pollution)");
                            builder1.setCancelable(true);

                            builder1.setPositiveButton(
                                    R.string.device_ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            setNotifPM(true);
                                            saveData();
                                            dialog.cancel();
                                        }
                                    });

                            builder1.setNegativeButton(
                                    R.string.device_cancel,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            saveData();
                                            dialog.cancel();
                                        }
                                    });

                            AlertDialog alert11 = builder1.create();
                            alert11.show();
                            dialog.cancel();
                        }
                    });

            builder1.setNegativeButton(
                    R.string.device_cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();
        }


        FetchWeatherTask task = new FetchWeatherTask(DeviceControlActivity.this);
        try {
            URL url = new URL(getResources().getString(R.string.URL_weather)+"&units=metric&mode=json&appid=0b1cf22c468951b3ab523efe295449cf");
            task.execute(url);
        } catch (Exception e) {
            Log.d("fetch: ", "Malformed URL exception");
        }

        //affiche le nom de l'appareil dans la bar et bind le service pour recuperer les actions transmises
        setTitle("Dolphin "+Account.getidCapteur());
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorBar)));
        getActionBar().setTitle(Html.fromHtml("<font color=\"#FFFFFF\">" + getTitle() + "</font>"));
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);


        count=0;
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }

        /*DialogNotif exampleDialog = new DialogNotif();
        exampleDialog.show(AppCompatActivity.getSupportFragmentManager(), "Dialog Notif");*/

        Button btnData = findViewById(R.id.btn_data);
        btnData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Intent myIntent = new Intent(DeviceControlActivity.this,
                            ConnectedActivity.class);
                    stopTimerTask();
                    startActivity(myIntent);
                    finish();
            }
        });

    }

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 10000); //
    }

    public void stopTimerTask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {

                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            //get the current timeStamp
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(DeviceControlActivity.this);
                            builder1.setMessage(R.string.device_probleme);
                            builder1.setCancelable(true);

                            builder1.setPositiveButton(
                                    R.string.device_oui,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            Intent myIntent = new Intent(DeviceControlActivity.this, MainActivity.class);
                                            stopTimerTask();
                                            startActivity(myIntent);
                                            dialog.cancel();
                                        }
                                    });

                            builder1.setNegativeButton(
                                    R.string.device_non,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            android.os.Process.killProcess(android.os.Process.myPid());
                                            dialog.cancel();
                                        }
                                    });

                            AlertDialog alert11 = builder1.create();
                            alert11.show();
                        }
                        catch (Exception e){

                        }
                    }
                });
            }
        };
    }

    public void saveData() {
        SharedPreferencesManager.getInstance(getContext()).storeString(NOTIFON, notifOn+"");
        SharedPreferencesManager.getInstance(getContext()).storeString(NOTIFPM, notifPM+"");


        Toast.makeText(this, R.string.data_ok, Toast.LENGTH_SHORT).show();
    }

    public void loadData() {
        uNotifOn = SharedPreferencesManager.getInstance(getContext()).retrieveString(NOTIFON);
        uNotifPM = SharedPreferencesManager.getInstance(getContext()).retrieveString(NOTIFPM);
    }

    public void updateData() {
        if(uNotifPM != "" && uNotifOn != "")
        setNotifOn(Boolean.parseBoolean(uNotifOn));
        setNotifPM(Boolean.parseBoolean(uNotifPM));
    }

    //unbind + enleve les filtres + vide le service
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothLeService.disconnect();
        unregisterReceiver(mGattUpdateReceiver);
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    //afficher btn pour connexion ou deconnexion a l'appereil bluetooth en fonction de la variable de connexion
    //abonnement et sync permettent tous deux de s'abonner mais sync fait une demande a l'arduino pour recup
    //toutes les donnees produites
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
            menu.findItem(R.id.menu_sync).setVisible(false);
            menu.findItem(R.id.menu_abo).setVisible(false);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
            menu.findItem(R.id.menu_sync).setVisible(false);
            menu.findItem(R.id.menu_abo).setVisible(false);
        }
        return true;
    }

    //action click sur btn connexion/deconnexion/sync/abo
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                Account.clearC();
                Intent myIntent = new Intent(DeviceControlActivity.this, PullActivity.class);
                stopTimerTask();
                startActivity(myIntent);
                finish();
                return true;
            case android.R.id.home:
                Account.clearC();
                Intent myIntent2 = new Intent(DeviceControlActivity.this, PullActivity.class);
                stopTimerTask();
                startActivity(myIntent2);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //change l'attribut mConnectionState
    //Params : resourceId string qui indique etat de la connection
    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //mConnectionState.setText(resourceId);
            }
        });
    }

    //change l'attribut mDataField a la reception de donnee par notification
    //bluetooth envoie donnee par donnee
    //Params : data string qui contient la donnee a afficher
    //test la donnee pour savoir si c'est day/heure/pm/latt/longit pour bien les enregistrer dans la class Data
    //verifie si la date et heure sont bonnes si non donne date et heure du telephone
    private void displayData(String data) {
        stopTimerTask();

        Calendar date = Calendar.getInstance();
        if (data != null) {
            //verifie que taille superieur a 2 car lors de l'abo recoit une donnee vide
            if(data.length()>2) {
                //verifie si data est la date
                if (data.contains("/")) {
                    if(dataType == "lat"){
                        Data.setData_longit("-1");
                    }
                    else if(dataType == "pm"){
                        Data.setData_latt("-1");
                        Data.setData_longit("-1");
                    }
                    else if(dataType == "heure"){
                        Data.setData_pm1("-1");
                        Data.setData_pm25("-1");
                        Data.setData_pm10("-1");
                        Data.setData_latt("-1");
                        Data.setData_longit("-1");
                    }
                    else if(dataType == "day"){
                        Data.setData_heure(date.get(Calendar.HOUR_OF_DAY) + ":" + date.get(Calendar.MINUTE) + ":" + date.get(Calendar.SECOND));
                        Data.setData_pm1("-1");
                        Data.setData_pm25("-1");
                        Data.setData_pm10("-1");
                        Data.setData_latt("-1");
                        Data.setData_longit("-1");
                    }
                    dataType = "day";
                    String[] data_date = data.split("/");
                    //verifie si la date est "juste" sinon met celle du telephone
                        data = date.get(Calendar.DAY_OF_MONTH) + "/" + (date.get(Calendar.MONTH) + 1) + "/" + date.get(Calendar.YEAR);
                    data_date = data.split("/");
                    String day = data_date[2] + "/" + data_date[1] + "/" + data_date[0];
                    //mDataDate.setText(day);
                    Data.setData_day(day);
                }
                //verifie si data est l'heure
                else if (data.contains(":")) {
                    if (dataType == "day") {
                        dataType = "heure";
                        if (Data.getData_day().size() == Data.getData_heure().size() + 1) {
                            String[] data_time = data.split(":");
                            //verifie si l'heure est "juste" sinon met celle du telephones
                                data = date.get(Calendar.HOUR_OF_DAY) + ":" + date.get(Calendar.MINUTE) + ":" + date.get(Calendar.SECOND);
                            //mDataHeure.setText(data);
                            Data.setData_heure(data);
                        }
                    }
                }
                //verifie si data est les PMs
                else if (data.contains("A")) {
                    if (dataType == "heure") {
                        dataType = "pm";
                        //separe les PMs
                        if (Data.getData_day().size() == Data.getData_pm1().size() + 1 && Data.getData_day().size() == Data.getData_pm25().size() + 1 && Data.getData_day().size() == Data.getData_pm10().size() + 1) {
                            String[] data_pm = data.split("A");
                            Integer pm1 = Integer.parseInt(data_pm[1])+10;
                            Integer pm25 = Integer.parseInt(data_pm[2])+10;
                            Integer pm10 = Integer.parseInt(data_pm[3])+10;
                            Data.setData_pm1(pm1.toString());
                            Data.setData_pm25(pm25.toString());
                            Data.setData_pm10(pm10.toString());
                            if (pm10 < 60) {
                                mArcSeekBar.setProgress(pm10 * 10);
                            } else {
                                mArcSeekBar.setProgress(600);
                            }

                            //recupere la valeur des pm et l'utilise pour remplir la gauge
                            //change la couleur en fonction de la valeur
                            if (mArcSeekBar.getProgress() < 100) {
                                mArcSeekBar.setProgressColor(Color.parseColor(getString(R.string.pm_tb)));
                                mBikePic.setImageResource(R.mipmap.tb_bike);
                                mStrollerPic.setImageResource(R.mipmap.tb_stroller);
                            } else if (mArcSeekBar.getProgress() >= 100 && mArcSeekBar.getProgress() < 200) {
                                mArcSeekBar.setProgressColor(Color.parseColor(getString(R.string.pm_b)));
                                mBikePic.setImageResource(R.mipmap.b_bike);
                                mStrollerPic.setImageResource(R.mipmap.b_stroller);
                            } else if (mArcSeekBar.getProgress() >= 200 && mArcSeekBar.getProgress() < 300) {
                                mArcSeekBar.setProgressColor(Color.parseColor(getString(R.string.pm_m)));
                                mBikePic.setImageResource(R.mipmap.m_bike);
                                mStrollerPic.setImageResource(R.mipmap.m_stroller);
                            } else if (mArcSeekBar.getProgress() >= 300 && mArcSeekBar.getProgress() < 400) {
                                mArcSeekBar.setProgressColor(Color.parseColor(getString(R.string.pm_me)));
                                mBikePic.setImageResource(R.mipmap.me_bike);
                                mStrollerPic.setImageResource(R.mipmap.me_stroller);
                            } else if (mArcSeekBar.getProgress() >= 400 && mArcSeekBar.getProgress() < 500) {
                                mArcSeekBar.setProgressColor(Color.parseColor(getString(R.string.pm_ma)));
                                mBikePic.setImageResource(R.mipmap.ma_bike);
                                mStrollerPic.setImageResource(R.mipmap.ma_stroller);
                            } else {
                                mArcSeekBar.setProgressColor(Color.parseColor(getString(R.string.pm_tm)));
                                mBikePic.setImageResource(R.mipmap.tm_bike);
                                mStrollerPic.setImageResource(R.mipmap.tm_stroller);
                            }
                            mDataPM.setText(pm10.toString());

                            if(notifOn) {
                                if(!notifPM) {
                                    Intent notificationIntent = new Intent(DeviceControlActivity.this, PullActivity.class);
                                    PendingIntent pendingIntent = PendingIntent.getActivity(this,
                                            0, notificationIntent, 0);

                                    Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                                            .setContentTitle("PM value")
                                            .setContentText(pm10.toString())
                                            .setSmallIcon(R.drawable.ic_android)
                                            //.setContentIntent(pendingIntent)
                                            .build();

                                    NotificationManager notificationManager =
                                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                                    notificationManager.notify(0, notification);
                                }
                                else{
                                    if(pm10>40){
                                        Intent notificationIntent = new Intent(DeviceControlActivity.this, PullActivity.class);
                                        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                                                0, notificationIntent, 0);

                                        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                                                .setContentTitle("PM value")
                                                .setContentText(pm10.toString())
                                                .setSmallIcon(R.drawable.ic_android)
                                                //.setContentIntent(pendingIntent)
                                                .build();

                                        NotificationManager notificationManager =
                                                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                                        notificationManager.notify(0, notification);
                                    }
                                }
                            }
                        }
                    }
                }
                //verifie si data est latitude ou longitude quand gps ne fonctionne pas
                else if (data.length() == 9 && !data.contains("/") && !data.contains(":")) {
                    //latitude si il n'a pas encore de donnee car envoye en premier par rapport a longitude
                    if (Data.getData_day().size() == Data.getData_latt().size() + 1 || Data.getData_day().size() == Data.getData_longit().size() + 1) {
                        if (Data.getData_latt().size() == Data.getData_longit().size()) {
                            if (dataType == "pm") {
                                dataType = "lat";
                                Data.setData_latt(data);
                            }
                        }
                        //longitude car latitude a une donnee de plus
                        else {
                            if (dataType == "lat") {
                                dataType = "long";
                                Data.setData_longit(data);
                                Data.SendData(url_base, mDeviceName);
                            }
                        }
                    }
                }

                //plusieurs tests pour latitude ou longitude car des fois le capteur envoi 9 char ou 8 ou 7

                //verifie si data est latitude
                else if (data.length() == 8 && !data.contains("/") && !data.contains(":")) {
                    if (Data.getData_day().size() == Data.getData_latt().size() + 1 || Data.getData_day().size() == Data.getData_longit().size() + 1) {
                        if (Data.getData_latt().size() == Data.getData_longit().size()) {
                            if (dataType == "pm") {
                                dataType = "lat";
                                Data.setData_latt(data);
                            }
                        }
                        //longitude car latitude a une donnee de plus
                        else {
                            if (dataType == "lat") {
                                dataType = "long";
                                Data.setData_longit(data);
                                Data.SendData(url_base, mDeviceName);
                            }
                        }
                    }
                }
                //verifie si data est latitude
                else if (data.length() == 7 && !data.contains("/") && !data.contains(":")){
                    if (Data.getData_day().size() == Data.getData_latt().size() + 1 || Data.getData_day().size() == Data.getData_longit().size() + 1) {
                        if (Data.getData_latt().size() == Data.getData_longit().size()) {
                            if (dataType == "pm") {
                                dataType = "lat";
                                Data.setData_latt(data);
                            }
                        }
                        //longitude car latitude a une donnee de plus
                        else {
                            if (dataType == "lat") {
                                dataType = "long";
                                Data.setData_longit(data);
                                Data.SendData(url_base, mDeviceName);
                            }
                        }
                    }
                }
            }
        }
        startTimer();
    }

    //remplir ExpandableListView avec les services et assigner leurs caracteristiques
    //permet d'enregistrer la charac voulu pour plus tard s'y abonner et/ou sync
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        //boucle pour les services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

            //boucle pour les caracteristiques.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                if(gattCharacteristic.getUuid().toString().compareTo(UUID_Charac)==0){
                    mCharacteristic=gattCharacteristic;
                    if (mNotifyCharacteristic != null) {
                        mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
                        mNotifyCharacteristic = null;
                        mNotifyCharacteristic = mCharacteristic;
                        mBluetoothLeService.setCharacteristicNotification(mCharacteristic, true);
                    } else {
                        mNotifyCharacteristic = mCharacteristic;
                        mBluetoothLeService.setCharacteristicNotification(mCharacteristic, true);
                    }
                }

                /**
                 *
                 * else if en fonction du mode de BLE voulu
                 *
                 **/

                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
    }

    //filtres pour savoir quelle action il faut faire
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public void updateView(JSONObject jsonObject){
        if(jsonObject==null) {
            //Toast.makeText(this,"Error fetching", Toast.LENGTH_LONG).show();
        }
        else {
            try {
                JSONArray data = jsonObject.getJSONArray("list");
                int arrondi = (int) (Math.round(Double.parseDouble(data.getJSONObject(0).getJSONObject("main").getString("temp"))));
                mTemp.setText(arrondi + "°C");
                mHumid.setText(data.getJSONObject(0).getJSONObject("main").getString("humidity") + "%");
            }
            catch (Exception e){
                Log.d(TAG, "updateView DeviceControl:" + e.getMessage());
            }
        }
    }

    /**
     * Get the context of the activie
     * @return
     */
    public static Context getContext() {
        return context;
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Intent myIntent2 = new Intent(DeviceControlActivity.this,
                PullActivity.class);
        startActivity(myIntent2);
        finish();
    }
}
