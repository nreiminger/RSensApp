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

import android.Manifest;
import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aird.rsens_user.R;
import com.example.aird.rsens_user.models.Account;

import java.util.ArrayList;


/**
 * Cette activite permet de chercher et afficher les appareils bluetooth
 */


public class DeviceScanActivity extends ListActivity {
    private Integer count =0;
    //variable pour le code
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private boolean mActif;
        //pour arreter le scan
    private Handler mHandler;
    private static final int REQUEST_ENABLE_BT = 1;
        // Stop scan apres 10s
    private static final long SCAN_PERIOD = 10000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getActionBar().setTitle(R.string.title_devices);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorBar)));
        getActionBar().setTitle(Html.fromHtml("<font color=\"#FFFFFF\">" + getTitle() + "</font>"));
        mHandler = new Handler();

        Intent myIntent = getIntent();
        Bundle extra = myIntent.getExtras();

        // verifie si BLE est supporte
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Init Bluetooth adapter
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // verifie si Bluetooth est supporte
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //verifie si permission ACCESS_COARSE_LOCATION
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // pas permission request the permission
            Toast.makeText(this, R.string.ACCESS_COARSE_LOCATION, Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);
        }
        else {
            // deja permission
        }

        // idem pour ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // pas permission request the permission
            Toast.makeText(this,R.string.ACCESS_FINE_LOCATION, Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);
        }
        else {
            // deja permission
        }

       /* try{
            Account.setid(extra.getString("id"));
            Account.setidCapteur(extra.getString("idC"));
            Account.setvCapteur(extra.getString("vC"));
            Log.d("CAPTEUR", extra.getString("id") +" "+extra.getString("idC")+" "+extra.getString("vC"));
        }
        catch(Exception e){}
*/
    }

    //afficher btn pour scan ou stop scan d'appereil bluetooth en fonction de la variable de scan
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    //action click sur btn scan/stop
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                mLeDeviceListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                final Intent intent = new Intent(DeviceScanActivity.this, ConnectedActivity.class);
                startActivity(intent);
                finish();
                break;
            case android.R.id.home:
                Account.clear();
                onBackPressed();
                return true;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mActif=false;
        //verif si bluetooth est active sinon envoie demande d'activation grace au intent
        if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Init list view adapter
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        setListAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //l'utilisateur n'a pas permis le bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //re-init l'interface de l'activite
    @Override
    protected void onPause() {
        super.onPause();
        mActif=true;
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }

    //au clic se connecte a l'appareil choisi
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
        if (device == null) return;
        final Intent intent = new Intent(this, DeviceControlActivity.class);
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
        if (mScanning) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
        }
        startActivity(intent);
        finish();
    }

    //recherche d'appareils bluetooth
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stop scan apres le temps definit
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(!mActif) {
                        mActif=true;
                        final Intent intent = new Intent(DeviceScanActivity.this, ConnectedActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    //adapter pour les appareils trouves
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        //constructeur
        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = DeviceScanActivity.this.getLayoutInflater();
        }

        //ajouter un appareil
        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                if(count==0) {
                    mLeDevices.add(device);
                    count++;
                }
                if(device.getName()!=null) {
                    if(!device.getName().isEmpty()) {
                        if (device.getName().contains("RSens")) {
                            String deviceName = device.getName();
                            int f = deviceName.indexOf(".");
                            int l = deviceName.lastIndexOf(".");
                            String second = deviceName.substring(f, l);
                            String third = deviceName.substring(l);
                            Log.d("CAPTEUR", "" + f + " " + l + " " + deviceName.length() + " " + second + " " + third);
                            if (second.contains(Account.getvCapteur())) {
                                if (third.contains(Account.getidCapteur())) {
                                    mActif = true;
                                    final Intent intent = new Intent(DeviceScanActivity.this, DeviceControlActivity.class);
                                    intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
                                    intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        }
                    }
                }
            }
        }

        //recuperer un appareil
        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        //reset
        public void clear() {
            mLeDevices.clear();
        }

        //nombre d'appareils
        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        //retourne l'appareil en tant qu'objet
        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        //retourne l'id de l'appareil
        @Override
        public long getItemId(int i) {
            return i;
        }

        //affiche l'adapter
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

    //scancallback pour chercher les appareils
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();

                        }
                    });
                }
            };

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
}