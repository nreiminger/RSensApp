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

package com.example.aird.rsens_user.services;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.aird.rsens_user.R;

import java.util.List;
import java.util.UUID;

/**
 * Activite pour gerer les connexions/deconnexions et donnees de l'appreil bluetooth
 */

public class BluetoothLeService extends Service {
    //Nom de l'activite pour les log qui sont retournes
    private final static String TAG = BluetoothLeService.class.getSimpleName();
    //variable pour le code
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private String sub;

    private int mConnectionState = STATE_DISCONNECTED;

    //constantes d'etat
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    //constantes d'action savoir quelle action a faire/filtrer
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    // redefini les methodes que le GattCallBack doit s'occuper
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        //au changement d'etat de connexion
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        //quand un service est trouve
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        //quand descriptor ecrit
        @Override
        public void onDescriptorWrite (BluetoothGatt gatt,
                                       BluetoothGattDescriptor descriptor,
                                       int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, descriptor);
            }
        }

        //quand il y a un changement de caracteristique
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    //fonction pour faire passer les donnees
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    //surcharge on donne la caracteristique en plus
    private void broadcastUpdate(final String action,
                                  final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
            intent.putExtra(EXTRA_DATA, new String(data));
        }
        sendBroadcast(intent);
    }

    //surcharge on donne le descriptor en plus
    private void broadcastUpdate(final String action,
                                 final BluetoothGattDescriptor descriptor) {
        final Intent intent = new Intent(action);

        final byte[] data = descriptor.getValue();
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
            intent.putExtra(EXTRA_DATA, new String(data));
        }
        sendBroadcast(intent);
    }

    //classe creee pour ajouter la methode getService qui retourne la classe entiere
    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    //methode a appliquer quand la fonction bind est appele
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    //methode a appliquer quand la fonction unbind est appele
    @Override
    public boolean onUnbind(Intent intent) {
        //On doit penser a close la BluetoothGatt pour etre sur que les ressources soit libere
        close();
        return super.onUnbind(intent);
    }

    //mBinder va permettre de recuperer BluetoothLeService
    private final IBinder mBinder = new LocalBinder();

    public boolean initialize() {
        // initialise Adapter grace a Manager retourne true si bien init
        boolean res=true;
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                res=false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            res=false;
        }
        return res;
    }

    //Connecte a mBluetoothGatt et retourne true si succes
    public boolean connect(final String address) {
        boolean res=true;
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            res=false;
        }

        // s'il y a deja un mBluetoothGatt essaye de reconnecter
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                res=true;
            } else {
                res=false;
            }
        }


        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            res=false;
        }
        // ici false est pour l'autoconnect
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return res;
    }

    //deconnecte BluetoothGatt s'il y en a un
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();

    }

    //close pour etre sur que les ressources soient bien libere
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    //write a value to BluetoothGattCharacteristic s'il y a un BluetoothGatt
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic, String value) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        byte[] data_to_write = value.getBytes();
        characteristic.setValue(data_to_write);
        mBluetoothGatt.writeCharacteristic(characteristic);
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    //active ou desactive la notification d'une caracteristique
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        sub=getResources().getString(R.string.UUID_BLE_1_Sub);
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        try {

            if(enabled) {
                UUID uuid = UUID.fromString(sub);
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(uuid);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);

                //Toast.makeText(getBaseContext(),"Subscribe", Toast.LENGTH_SHORT).show();
            }
            else{
                UUID uuid = UUID.fromString(sub);
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(uuid);
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
            }
        }
        catch (Exception e){
            //ignore
            Toast.makeText(getBaseContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }

    }

    //recupere la liste de services de BluetoothGatt
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
}
