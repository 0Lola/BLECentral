package com.example.zxa01.blecentral.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

@SuppressLint("NewApi")
public class BluetoothLeService extends Service {

    public static final String SERVICE_UUID = "11101100-0000-0000-8000-000000000000"; //unknown service
    public static final String OBJECT_UUID = "00002ac3-0000-1000-8000-00805f9b34fb"; //object UUID
    public static final String CHANGE_UUID = "00002ac8-0000-1000-8000-00805f9b34fb"; //change UUID
    public static final String DESCRIPTORS_UUID = "00002902-0000-1000-8000-00805f9b34fb"; //description

    public final static String CONNECTED = "GATT_CONNECTED";
    public final static String DISCONNECTED = "DISCONNECTED";
    public final static String SERVICES_DISCOVERED = "SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "DATA_AVAILABLE";
    public final static String EXTRA_DATA = "EXTRA_DATA";
    public final static String WRITE_DATA = "com.example.zxa01.blecentral.bluetoothLeService.writedata";

    private final static String TAG = BluetoothLeService.class.getSimpleName();
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private final IBinder mBinder = new LocalBinder();

    private String deviceAddress;
    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;
    private BluetoothGatt btGatt;

    private int connectionStatete = STATE_DISCONNECTED;

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectionStatete = STATE_CONNECTED;
                btGatt.discoverServices();
                Log.i(TAG, "已連線 status = " + status + " newState = " + newState);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectionStatete = STATE_DISCONNECTED;
                broadcastUpdate(DISCONNECTED);
                Log.i(TAG, "連線中斷 status = " + status + " newState = " + newState);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(SERVICES_DISCOVERED);
                Log.d(TAG, "發現服務 status : " + status);
            } else {
                Log.d(TAG, "onServicesDiscovered  status : " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Characteristic 已讀取 value = " + characteristic.getValue());
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "Characteristic 已更改 value = " + characteristic.getValue());
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    private void broadcastUpdate(String action) {
        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(String action, BluetoothGattCharacteristic characteristic) {
        Intent intent = new Intent(action);
        byte[] data = characteristic.getValue();
        String adapterName = new String(data);
        if (data != null && data.length > 0) {
            StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data) {
                stringBuilder.append(String.format("%02X ", byteChar));
            }
            intent.putExtra(EXTRA_DATA, stringBuilder.toString());
            Log.v(TAG, "資料更新 EXTRA_DATA = " + stringBuilder.toString() + " Peripheral = " + adapterName);
        }
        Log.v(TAG, "資料更新 broadcastUpdate");
        sendBroadcast(intent);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothLeService.WRITE_DATA.equals(action)) {
                Log.v(TAG, "寫入新數值");
                intent.getStringExtra("value");
            }
        }
    };

    private static IntentFilter writeValueIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.EXTRA_DATA);
        return intentFilter;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        disconnect();
        return super.onUnbind(intent);
    }

    public boolean initialize() {
        if (btManager == null) {
            btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (btManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        btAdapter = btManager.getAdapter();
        if (btAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    public boolean connect(String address) {

        if (connectionStatete == BluetoothProfile.STATE_CONNECTED) {
            disconnect();
            Log.d(TAG, "防止133 移除連線");
        }

        if (btAdapter == null || address == null) {
            Log.d(TAG, "BluetoothAdapter 無法初始化");
            return false;
        }

        if (deviceAddress != null && address.equals(deviceAddress) && btGatt != null) {
            Log.d(TAG, "使用現存的藍芽連線");
            if (btGatt.connect()) {
                connectionStatete = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.d(TAG, "查無device");
            return false;
        }

        deviceAddress = address;
        // 防止 133
        btGatt = device.connectGatt(this, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
        connectionStatete = STATE_CONNECTING;
        Log.d(TAG, "建立新連線 address = " + deviceAddress);

        registerReceiver(broadcastReceiver, writeValueIntentFilter());
        return true;

    }

    public void disconnect() {
        // 防止 133
        if (btGatt != null) {
            btGatt.disconnect();
            btGatt.close();
            btGatt = null;
            Log.v(TAG, "GATT 取消連線");
        }
        unregisterReceiver(broadcastReceiver);
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (btAdapter == null || btGatt == null) {
            return;
        }
        boolean isReadCharacteristic = btGatt.readCharacteristic(characteristic);
        Log.d(TAG, "Characteristic 讀取 " + characteristic.getUuid() + " 狀態 = " + isReadCharacteristic);
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (btAdapter == null || btGatt == null) {
            return;
        }
        Log.d(TAG, "Characteristic 通知 " + Arrays.toString(characteristic.getValue()));
        btGatt.setCharacteristicNotification(characteristic, enabled);
    }

    public void writeCharacteristicNotification(BluetoothGattCharacteristic characteristic) {
        if (btAdapter == null || btGatt == null) {
            return;
        }
        Log.d(TAG, "Characteristic 寫入 " + Arrays.toString(characteristic.getValue()));
        boolean isWriteCharacteristic = btGatt.writeCharacteristic(characteristic);
        if (isWriteCharacteristic) readCharacteristic(characteristic);
    }

    public void propertiesMapping(BluetoothGattCharacteristic characteristic) {
        if (characteristic != null) {
            if (characteristic.getProperties() == BluetoothGattCharacteristic.PROPERTY_READ) {
                readCharacteristic(characteristic);
                Log.d(TAG, "讀取功能 Characteristic");
            }
            if (characteristic.getProperties() == BluetoothGattCharacteristic.PROPERTY_NOTIFY) {
                setCharacteristicNotification(characteristic, true);
                Log.d(TAG, "通知 Characteristic");
            }
        }
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (btGatt == null) return null;
        return btGatt.getServices();
    }

    public String getBondState() {
        switch (btGatt.getDevice().getBondState()) {
            case 10:
                return "BOND NONE";
            case 11:
                return "BONDING";
            case 12:
                return "BONDED";
            default:
                return "NULL";
        }
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }
}
