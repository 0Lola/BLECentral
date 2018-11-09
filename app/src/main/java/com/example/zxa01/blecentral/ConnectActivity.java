package com.example.zxa01.blecentral;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.example.zxa01.blecentral.recyclerView.RecyclerItemClickListener;
import com.example.zxa01.blecentral.recyclerView.ServiceAdapter;
import com.example.zxa01.blecentral.service.BluetoothLeService;
import com.example.zxa01.blecentral.view.InputDialog;

import java.util.LinkedList;
import java.util.List;

@SuppressLint("NewApi")
public class ConnectActivity extends AppCompatActivity {

    private final String TAG = "ConnectActivity";
    private RecyclerView mRecyclerView;
    private ServiceAdapter mServiceAdapter;
    private TextView mName;
    private TextView mAddress;
    private TextView mState;
    private InputDialog mInputDialog;

    private String actionAddress;
    private String actionName;
    private String actionState;

    private BluetoothLeService mBluetoothLeService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Bluetooth 無法初始化");
                finish();
            }
            mBluetoothLeService.connect(actionAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private LinkedList<BluetoothGattCharacteristic> mGattCharacteristics = new LinkedList<>();
    private BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothLeService.CONNECTED.equals(action)) {
                Log.v(TAG, "連線狀態變更 - 連線");
                invalidateOptionsMenu();
            } else if (BluetoothLeService.DISCONNECTED.equals(action)) {
                mBluetoothLeService.disconnect();
                clearUI();
                Log.v(TAG, "連線狀態變更 - 連線中斷");
                invalidateOptionsMenu();
            } else if (BluetoothLeService.SERVICES_DISCOVERED.equals(action)) {
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
                Log.v(TAG, "連線狀態變更 - 發現 Services , Bonded State = " + mBluetoothLeService.getBondState());
            } else if (BluetoothLeService.EXTRA_DATA.equals(action)) {
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
                Log.v(TAG, "資料更新 ");
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.CONNECTED);
        intentFilter.addAction(BluetoothLeService.DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.EXTRA_DATA);
        return intentFilter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        Intent intent = getIntent();
        actionAddress = intent.getStringExtra("address");
        actionName = intent.getStringExtra("name");
        actionState = intent.getStringExtra("state");
        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            boolean result = mBluetoothLeService.connect(actionAddress);
            updateConnectionState();
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBluetoothLeService.disconnect();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothLeService.disconnect();
        unbindService(mServiceConnection);
    }

    private void updateConnectionState() {
        mState.setText(mBluetoothLeService.getBondState());
    }

    // 取得所有 Characteristics
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        mName.setText(actionName);
        mAddress.setText(actionAddress);
        mState.setText(mBluetoothLeService.getBondState());
        if (gattServices == null) return;
        mGattCharacteristics = new LinkedList<>();
        for (BluetoothGattService service : gattServices) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                mBluetoothLeService.propertiesMapping(characteristic);
                mGattCharacteristics.add(characteristic);
            }
        }
        runOnUiThread(() -> {
            mRecyclerView = findViewById(R.id.peripheralRecyclerview);
            mServiceAdapter = new ServiceAdapter(this, mGattCharacteristics);
            mRecyclerView.setAdapter(mServiceAdapter);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.addOnItemTouchListener(
                    new RecyclerItemClickListener(this, mRecyclerView, (view, position) -> {
                        BluetoothGattCharacteristic characteristic = mGattCharacteristics.get(position);
                        showDialog(characteristic);
                    })
            );
            mRecyclerView.getAdapter().notifyDataSetChanged();
        });
    }

    private void showDialog(BluetoothGattCharacteristic characteristic) {
        mInputDialog.show();

//        if (characteristic != null) {
//            if (characteristic.getProperties() == BluetoothGattCharacteristic.PROPERTY_WRITE) {
//                byte[] byteWriteValue =  mWriteValue.getText().toString().getBytes();
//                characteristic.setValue(byteWriteValue);
//                bluetoothLeService.writeCharacteristicNotification(characteristic);
//            }
//        }

//        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
//        alertDialogBuilder.setTitle("Your Title");
//        alertDialogBuilder
//                .setMessage("Click yes to exit!")
//                .setCancelable(false)
//                .setPositiveButton("Yes", (dialog, id) -> {
//                    ConnectActivity.this.finish();
//                })
//                .setNegativeButton("No", (dialog, id) -> {
//                    dialog.cancel();
//                });
//        AlertDialog alertDialog = alertDialogBuilder.create();
//        alertDialog.show();

    }

    private void initUI() {
        mName = findViewById(R.id.name);
        mName.setText(actionName);
        mAddress = findViewById(R.id.address);
        mAddress.setText(actionAddress);
        mState = findViewById(R.id.state);
        mState.setText(actionState);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        mInputDialog = new InputDialog(this);
    }

    private void clearUI() {
        mName.setText("");
        mAddress.setText("");
        mState.setText("");
    }
}
