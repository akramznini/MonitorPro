package com.jiangdg.usbcamera.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jiangdg.usbcamera.R;
import com.jiangdg.usbcamera.utils.BlackMagicCommands;
import com.jiangdg.usbcamera.utils.CustomRecyclerAdapter;
import com.jiangdg.usbcamera.utils.Utility;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


/**
 * permission checking
 * Created by jiangdongguo on 2019/6/27.
 */

public class SplashActivity extends AppCompatActivity {
    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            "android.permission.BLUETOOTH_SCAN",
            "android.permission.BLUETOOTH_CONNECT"
    };
    private static final int REQUEST_CODE = 1;
    private List<String> mMissPermissions = new ArrayList<>();
    private final int ENABLE_BLUETOOTH_REQUEST_CODE = 1;
    private final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    private final int BLUETOOTH_SCAN_REQUEST_CODE = 3;
    private final int BLUETOOTH_CONNECT_REQUEST = 4;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private Button scanButton;
    private Button performAction;
    private BluetoothLeScanner bleScanner;
    private ScanCallback scanCallback;
    private boolean isScanning = false;
    private RecyclerView recyclerView;
    private CustomRecyclerAdapter customRecyclerAdapter;
    private ArrayList<ScanResult> scanResults = new ArrayList();
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic notificationCharacteristic;
    private BluetoothGattCharacteristic writeCharacteristic;
    public static final String BLUETOOTH_CONNECT = "android.permission.BLUETOOTH_CONNECT";
    public static final String BLUETOOTH_SCAN = "android.permission.BLUETOOTH_SCAN";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        Utility.loadWBList();
        Log.d("statusbe", "stap1");
        checkAndRequestPermissions();
        BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorRead(gatt, descriptor, status);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                Log.d("statusbe", Utility.byteArrayString(characteristic.getValue()));
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                byte[] value = characteristic.getValue();
                String messageConverted = null;
                try {
                    messageConverted = new String(value, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Log.d("statusbe", messageConverted);
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                Log.d("statusbe", "onservicesdiscovered launched");
                if (status == BluetoothGatt.GATT_SUCCESS){
                    Log.d("statusbe", "servicesdiscovered success");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            BluetoothGattService bluetoothGattService = bluetoothGatt.getService(UUID.fromString("291D567A-6D75-11E6-8B77-86F30CA893D3"));
                            writeCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString("5DD3465F-1AEE-4299-8493-D2ECA2F8E1BB"));
                            notificationCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString("B864E140-76A0-416A-BF30-5876504537D9"));
                            BluetoothGattDescriptor descriptor = notificationCharacteristic.getDescriptors().get(0);
                            boolean state = bluetoothGatt.setCharacteristicNotification(notificationCharacteristic, true);
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                            bluetoothGatt.writeDescriptor(descriptor);
                            startMainActivitywithIntent();
                        }
                    });
                }
            }

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("statusbe", "stap1");
                    bluetoothGatt = gatt;
                    gatt.discoverServices();
                    if (newState == BluetoothProfile.STATE_CONNECTED){
                        Log.d("statusbe", "connection successful");

                    }
                    else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                        Log.d("statusbe", "successfully diconnected");
                        gatt.close();
                    }
                    else {
                        Log.d("statusbe", "erroooor");

                    }

                }
                else if (status == 133){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast("Couldn't connect to camera");
                        }
                    });
                }
            }
        };
        performAction = findViewById(R.id.testButton);
        recyclerView = findViewById(R.id.recycler_view);
        customRecyclerAdapter = new CustomRecyclerAdapter(scanResults, gattCallback);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(customRecyclerAdapter);
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bleScanner = bluetoothAdapter.getBluetoothLeScanner();
        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                if(!Utility.checkResult(scanResults, result)){
                    scanResults.add(result);
                };
                customRecyclerAdapter.notifyDataSetChanged();
            }
        };
        scanButton = findViewById(R.id.scan_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isScanning){
                    startBleScan();}
                else{stopBleScan();
                }
            }
        });
        performAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SplashActivity.this, USBCameraActivity.class));
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (!bluetoothAdapter.isEnabled()) {
            promptEnableBluetooth();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ENABLE_BLUETOOTH_REQUEST_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                promptEnableBluetooth();
            }
        }
    }

    private boolean isVersionM() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    private void checkAndRequestPermissions() {
        mMissPermissions.clear();
        for (String permission : REQUIRED_PERMISSION_LIST) {
            int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                mMissPermissions.add(permission);
            }
        }
        // check permissions has granted
        if (mMissPermissions.isEmpty()) {
        } else {
            ActivityCompat.requestPermissions(this,
                    mMissPermissions.toArray(new String[mMissPermissions.size()]),
                    REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            for (int i = grantResults.length - 1; i >= 0; i--) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    mMissPermissions.remove(permissions[i]);
                }
            }
        }
        // Get permissions success or not
        if (mMissPermissions.isEmpty()) {
        } else {
            checkAndRequestPermissions();
        }
    }

    private void startMainActivitywithIntent() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, USBCameraActivity.class);
                intent.putExtra("cameraBluetooth", bluetoothGatt.getDevice());
                bluetoothGatt.close();
                stopBleScan();
                startActivity(intent);
                SplashActivity.this.finish();
            }
        }, 0);
    }
    private void promptEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            requestBluetoothConnectPermission();
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Log.d("DEBUG", "Nopermission");
                promptEnableBluetooth();
                return;
            }
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE);
        }
    }
    private boolean hasPermission(String permissionType){
        return ContextCompat.checkSelfPermission(this, permissionType) == PackageManager.PERMISSION_GRANTED;
    }
    @SuppressLint("MissingPermission")
    private void startBleScan(){
        requestLocationPermission();
        requestScanPermission();
        requestBluetoothConnectPermission();
        if (hasPermission(BLUETOOTH_SCAN) && hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                && hasPermission(BLUETOOTH_CONNECT)){/*Code will come here after*/
            Log.i("ScanCallback", "scannind start");
            ScanFilter scanFilter = new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString("291D567A-6D75-11E6-8B77-86F30CA893D3")).build();
            ScanSettings scanSettings = new ScanSettings.Builder().build();
            bleScanner.startScan(Arrays.asList(new ScanFilter[]{scanFilter}), scanSettings, scanCallback);

            isScanning = true;
            scanButton.setText("Stop Scan");
            scanResults.clear();
            customRecyclerAdapter.notifyDataSetChanged();}
    }
    @SuppressLint("MissingPermission")
    private void stopBleScan(){
        bleScanner.stopScan(scanCallback);
        isScanning = false;
        scanButton.setText("Start Scan");
    }
    private void requestScanPermission(){
        if (hasPermission(BLUETOOTH_SCAN)){return;}
        requestPermissions(new String[]{BLUETOOTH_SCAN}, BLUETOOTH_SCAN_REQUEST_CODE);
    }

    private void requestLocationPermission(){
        if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)){return;}
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }
    private void requestBluetoothConnectPermission(){
        if (hasPermission(BLUETOOTH_CONNECT)){return;}
        requestPermissions(new String[]{BLUETOOTH_CONNECT}, BLUETOOTH_CONNECT_REQUEST);
    }
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

        }
    };
    private void showToast(String string){
        Toast toast = Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT);
        toast.show();
    }
}

