package com.example.zxa01.blecentral;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.example.zxa01.blecentral.recyclerView.ScanAdapter;

import java.util.LinkedList;


@SuppressLint("NewApi")
public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_ENABLE_BT = 1;
    private final static int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private final String TAG = "com.example.zxa01.blecentral.MainActivity";

    private RecyclerView mRecyclerView;
    private ScanAdapter mScanAdapter;
    private LinkedList<BluetoothDevice> mList = new LinkedList<>();

    private Button startScanningButton;
    private Button stopScanningButton;
    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;
    private BluetoothLeScanner btScanner;

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            if (mList.stream()
                    .filter(e -> e.getAddress().equals(result.getDevice().getAddress()))
                    .count() == 0 && result.getDevice().getName() != null
                    ) {
                mList.addFirst(result.getDevice());
                mRecyclerView.getAdapter().notifyItemInserted(0);
                mRecyclerView.smoothScrollToPosition(0);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initComponent();
        initBle();
        initRecyclerView();
    }

    private void initRecyclerView() {
        mRecyclerView = findViewById(R.id.recyclerview);
        mScanAdapter = new ScanAdapter(this, mList);
        mRecyclerView.setAdapter(mScanAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initComponent() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        startScanningButton = findViewById(R.id.StartScanButton);
        startScanningButton.setOnClickListener(v -> startScanning());

        stopScanningButton = findViewById(R.id.StopScanButton);
        stopScanningButton.setOnClickListener(v -> stopScanning());
    }

    private void initBle() {
        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();

        // 要求開啟藍芽
        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        // 要求開啟定位
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(dialog -> requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION));
            builder.show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(dialog -> {
                    });
                    builder.show();
                }
                return;
            }
        }
    }


    /**
     * 開始掃描
     */
    @SuppressLint("LongLogTag")
    public void startScanning() {
        Log.v(TAG, "startScanning");
        mList.clear();
        mRecyclerView.getAdapter().notifyDataSetChanged();
        AsyncTask.execute(() -> btScanner.startScan(scanCallback));
    }

    /**
     * 停止掃描
     */
    @SuppressLint("LongLogTag")
    public void stopScanning() {
        Log.v(TAG, "stopScanning");
        mList.clear();
        mRecyclerView.getAdapter().notifyDataSetChanged();
        AsyncTask.execute(() -> btScanner.stopScan(scanCallback));
    }
}
