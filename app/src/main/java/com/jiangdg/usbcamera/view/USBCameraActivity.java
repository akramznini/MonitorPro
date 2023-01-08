package com.jiangdg.usbcamera.view;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

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
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Looper;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.jiangdg.usbcamera.R;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.core.app.ActivityCompat;

import com.jiangdg.usbcamera.UVCCameraHelper;
import com.jiangdg.usbcamera.utils.BlackMagicCommands;
import com.jiangdg.usbcamera.utils.FileUtils;
import com.jiangdg.usbcamera.utils.Utility;
import com.jiangdg.usbcamera.utils.ZoomLayout;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.common.AbstractUVCCameraHandler;
import com.serenegiant.usb.widget.CameraViewInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * UVCCamera use demo
 * <p>
 * Created by jiangdongguo on 2017/9/30.
 */

public class USBCameraActivity extends AppCompatActivity implements CameraDialog.CameraDialogParent, CameraViewInterface.Callback {
    private static final String TAG = "Debug";
    @BindView(R.id.camera_view)
    public View mTextureView;
    @BindView(R.id.seekbar_brightness)
    public SeekBar mSeekBrightness;
    @BindView(R.id.seekbar_contrast)
    public SeekBar mSeekContrast;
    @BindView(R.id.switch_rec_voice)
    public Switch mSwitchVoice;

    private UVCCameraHelper mCameraHelper;
    private CameraViewInterface mUVCCameraView;
    private AlertDialog mDialog;

    private boolean isRequest;
    private boolean isPreview;

    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;

    private ImageButton screenResultionButton;
    private ImageButton isoButton;
    private ImageView batteryImageView;
    private TextView batteryPercentageTextView;
    private boolean batteryImageViewStatus;
    private ImageView bottomBarImageView;
    private Group activeGroup;
    private boolean groupActivated;
    private Group isoGroup;
    private ZoomLayout zoomLayout;
    private BluetoothDevice cameraBluetooth;
    private BluetoothGattCallback bluetoothGattCallback;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic writeCharacteristic;
    private BluetoothGattCharacteristic notificationCharacteristic;
    private TextView isoValueTextView;
    private TextView isoValueChangeTextView;
    private SeekBar isoSeekBar;
    private ImageButton isoLeftArrowButton;
    private ImageButton isoRightArrowButton;
    private ImageButton wbLeftArrowButton;
    private ImageButton wbRightArrowButton;
    private ImageButton tintLeftArrowButton;
    private ImageButton tintRightArrowButton;
    private ImageButton wbButton;
    private ImageButton tintButton;
    private TextView wbValueTextView;
    private TextView tintValueTextView;
    private TextView wbValueChangeTextView;
    private TextView tintValueChangeTextView;
    private SeekBar wbSeekBar;
    private Group wbGroup;
    private ImageButton shutterButton;
    private TextView shutterValueTextView;
    private TextView shutterValueChangeTextView;
    private ImageButton shutterLeftArrowButton;
    private ImageButton shutterRightArrowButton;
    private Group shutterGroup;
    private ImageButton irisButton;
    private Group cameraSettingsGroup;
    private Button reconnectButton;
    private Group disconnectedGroup;
    private boolean[] primaryButtonsStateArray = new boolean[3];
    private boolean primaryButtonsInitialized = false;
    private ConstraintLayout constraintLayout;
    private UVCCameraHelper.OnMyDevConnectListener listener = new UVCCameraHelper.OnMyDevConnectListener() {

        @Override
        public void onAttachDev(UsbDevice device) {
            // request open permission
            if (!isRequest) {
                isRequest = true;
                if (mCameraHelper != null) {
                    mCameraHelper.requestPermission(0);
                }
            }
        }

        @Override
        public void onDettachDev(UsbDevice device) {
            // close camera
            if (isRequest) {
                isRequest = false;
                mCameraHelper.closeCamera();
                showShortMsg(device.getDeviceName() + " is out");
            }
        }

        @Override
        public void onConnectDev(UsbDevice device, boolean isConnected) {
            if (!isConnected) {
                showShortMsg("fail to connect,please check resolution params");
                isPreview = false;
            } else {
                isPreview = true;
                showShortMsg("connecting");
                // initialize seekbar
                // need to wait UVCCamera initialize over
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Looper.prepare();
                        if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                            mSeekBrightness.setProgress(mCameraHelper.getModelValue(UVCCameraHelper.MODE_BRIGHTNESS));
                            mSeekContrast.setProgress(mCameraHelper.getModelValue(UVCCameraHelper.MODE_CONTRAST));
                        }
                        Looper.loop();
                    }
                }).start();
            }
        }

        @Override
        public void onDisConnectDev(UsbDevice device) {
            showShortMsg("disconnecting");
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_usbcamera);
        ButterKnife.bind(this);
        initView();

        bluetoothGattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("statusbe2", "stap1");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bluetoothGatt = gatt;
                            gatt.discoverServices();
                        }
                    });

                    if (newState == BluetoothProfile.STATE_CONNECTED){
                        //Log.d("statusbe2", "connection successful");
                        //
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                disconnectedGroup.setVisibility(INVISIBLE);
                                showToast("connected");
                                showToast("Fetching camera settings");
                            }
                        });


                        Log.d("statusbe2", "commands executed");

                    }
                    else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                disconnectedGroup.setVisibility(VISIBLE);
                                cameraSettingsGroup.setVisibility(INVISIBLE);
                                disableGroup();
                                primaryButtonsInitialized = false;
                                primaryButtonsStateArray = new boolean[3];
                                showToast("Camera disconnected");
                            }
                        });

                        Log.d("statusbe2", "successfully diconnected");
                        gatt.close();
                        //cameraSettingsGroup.setVisibility(INVISIBLE);

                    }
                    else {
                        Log.d("statusbe2", "erroooor");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cameraSettingsGroup.setVisibility(INVISIBLE);
                                disconnectedGroup.setVisibility(VISIBLE);
                                showToast("error");
                            }
                        });

                    }

                }
                // 133 is the code GATT gives when it fails
                else if(status == 133){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast("Connection Failed\nRestart App to change cameras");
                        }
                    });
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                Log.d("statusbe", "onservicesdiscovered launched");
                if (status == BluetoothGatt.GATT_SUCCESS){
                    Log.d("statusbe", "servicesdiscovered success");
                    BluetoothGattService bluetoothGattService = bluetoothGatt.getService(UUID.fromString("291D567A-6D75-11E6-8B77-86F30CA893D3"));
                    writeCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString("5DD3465F-1AEE-4299-8493-D2ECA2F8E1BB"));
                    notificationCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString("B864E140-76A0-416A-BF30-5876504537D9"));
                    BluetoothGattDescriptor descriptor = notificationCharacteristic.getDescriptors().get(0);
                    bluetoothGatt.setCharacteristicNotification(notificationCharacteristic, true);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                    bluetoothGatt.writeDescriptor(descriptor);

                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                Log.d("statusbe", Utility.byteArrayString(characteristic.getValue()));
                // Incoming ISO Command
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(primaryButtonsInitialized && cameraSettingsGroup.getVisibility() == INVISIBLE){
                            cameraSettingsGroup.setVisibility(VISIBLE);
                        } else if (!primaryButtonsInitialized){
                            boolean new_state = true;
                            for (boolean be: primaryButtonsStateArray){
                                if (be == false){new_state = false;
                                break;}
                            }
                            primaryButtonsInitialized = new_state;
                        }
                        int commandType = BlackMagicCommands.commandType(characteristic.getValue());
                        if (commandType == BlackMagicCommands.ISO_COMMAND){
                            primaryButtonsStateArray[0] = true;
                            Log.d("statusbe2", "received iso command");
                            int isoValue = BlackMagicCommands.getISO(characteristic.getValue());
                            isoSeekBar.setProgress(Utility.isoValueToSeekBar(isoValue));
                        }

                        // Incoming WB TINT Command
                        else if (commandType == BlackMagicCommands.WHITE_BALANCE_COMMANDE){
                            primaryButtonsStateArray[1] = true;
                            int wbValue = BlackMagicCommands.getWbTint(characteristic.getValue())[0];
                            int tintValue = BlackMagicCommands.getWbTint(characteristic.getValue())[1];
                            wbSeekBar.setProgress(Utility.wbValueToSeekBar(wbValue));
                            tintValueTextView.setText(String.valueOf(tintValue));
                            tintValueChangeTextView.setText(String.valueOf(tintValue));
                        } else if (commandType == BlackMagicCommands.SHUTTER_SPEED){
                            primaryButtonsStateArray[2] = true;
                            int shutterValue = BlackMagicCommands.getShutterSpeed(characteristic.getValue());
                            shutterValueChangeTextView.setText("1/" + shutterValue);
                            shutterValueTextView.setText("1/" + shutterValue);
                        }
                    }
                });

            }
        };
        Intent intent = getIntent();
        disconnectedGroup = findViewById(R.id.disconnectedGroup);
        if (!(intent.getParcelableExtra("cameraBluetooth") == null)){
            cameraBluetooth = intent.getParcelableExtra("cameraBluetooth");
            batteryImageViewStatus = true;
            groupActivated = false;
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
            cameraBluetooth.createBond();
            cameraBluetooth.connectGatt(getApplicationContext(), false, bluetoothGattCallback);
            showToast("Connecting to camera commands");
        }

        // step.1 initialize UVCCameraHelper
        mUVCCameraView = (CameraViewInterface) mTextureView;
        mUVCCameraView.setCallback(this);
        mCameraHelper = UVCCameraHelper.getInstance();
        mCameraHelper.setDefaultFrameFormat(UVCCameraHelper.FRAME_FORMAT_MJPEG);
        mCameraHelper.initUSBMonitor(this, mUVCCameraView, listener);


        // my contribution
        screenResultionButton = findViewById(R.id.ScreenResolutionButton);
        screenResultionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCameraHelper == null || !mCameraHelper.isCameraOpened()) {
                    showShortMsg("Please connect capture card and try again");
                }
                else{showResolutionListDialog();}
            }
        });
        // end of contribution

        mCameraHelper.setOnPreviewFrameListener(new AbstractUVCCameraHandler.OnPreViewResultListener() {
            @Override
            public void onPreviewResult(byte[] nv21Yuv) {
                Log.d(TAG, "onPreviewResult: "+ nv21Yuv.length);
            }
        });
        // Views declarations
        zoomLayout = findViewById(R.id.zoomLayout);
        isoButton = findViewById(R.id.isoButton);
        batteryImageView = findViewById(R.id.batteryImageView);
        batteryPercentageTextView = findViewById(R.id.batteryPercentageTextView);
        bottomBarImageView = findViewById(R.id.bottomBarImageView);
        bottomBarImageView.setVisibility(INVISIBLE);
        isoGroup = findViewById(R.id.isoGroup);
        isoValueTextView = findViewById(R.id.isoValueTextView);
        isoValueChangeTextView = findViewById(R.id.isoValueChangeTextView);
        isoSeekBar = findViewById(R.id.isoSeekBar);
        isoLeftArrowButton = findViewById(R.id.isoLeftArrowButton);
        isoRightArrowButton = findViewById(R.id.isoRightArrowButton);
        wbLeftArrowButton = findViewById(R.id.wbLeftArrowButton);
        wbRightArrowButton = findViewById(R.id.wbRightArrowButton);
        tintLeftArrowButton = findViewById(R.id.tintLeftArrowButton);
        tintRightArrowButton = findViewById(R.id.tintRightArrowButton);
        wbButton = findViewById(R.id.wbButton);
        tintButton = findViewById(R.id.tintButton);
        wbValueTextView = findViewById(R.id.wbValueTextView);
        tintValueTextView = findViewById(R.id.tintValueTextView);
        wbValueChangeTextView = findViewById(R.id.wbValueChangeTextView);
        tintValueChangeTextView = findViewById(R.id.tintValueChangeTextView);
        wbSeekBar = findViewById(R.id.wbSeekBar);
        wbGroup = findViewById(R.id.wbGroup);
        shutterButton = findViewById(R.id.shutterButton);
        shutterValueTextView = findViewById(R.id.shutterValueTextView);
        shutterValueChangeTextView = findViewById(R.id.shutterValueChangeTextView);
        shutterLeftArrowButton = findViewById(R.id.shutterLeftArrowButton);
        shutterRightArrowButton = findViewById(R.id.shutterRightArrowButton);
        shutterGroup = findViewById(R.id.shutterGroup);
        irisButton = findViewById(R.id.irisButton);
        cameraSettingsGroup = findViewById(R.id.cameraSettingsGroup);
        reconnectButton = findViewById(R.id.reconnectButton);

        constraintLayout = findViewById(R.id.mainMonitorLayout);
        // primary buttons onClicklisteners
        constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (groupActivated){
                    disableGroup();
                } else if (cameraSettingsGroup.getVisibility() == VISIBLE){
                    cameraSettingsGroup.setVisibility(INVISIBLE);
                } else if (cameraSettingsGroup.getVisibility() == INVISIBLE){
                    cameraSettingsGroup.setVisibility(VISIBLE);
                }
            }
        });

        isoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(groupActivated){
                    disableGroup();
                }
                else{
                    enableGroup(isoGroup);
                }
            }
        });
        View.OnClickListener wbListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(groupActivated){
                    disableGroup();
                }
                else{
                    enableGroup(wbGroup);
                }
            }
        };
        wbButton.setOnClickListener(wbListener);
        tintButton.setOnClickListener(wbListener);
        reconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bluetoothAdapter.isEnabled()){
                    cameraBluetooth.createBond();
                    cameraBluetooth.connectGatt(getApplicationContext(), false, bluetoothGattCallback);
                    showToast("Connecting to camera commands");
                } else {promptEnableBluetooth();}
            }
        });
        // Group Buttons Click Listeners

        // ISO group buttons Listeners
        isoLeftArrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isoSeekBar.setProgress(isoSeekBar.getProgress() - 1);
                byte[] value = BlackMagicCommands.setISO(Utility.isoSeekBarToValue(isoSeekBar.getProgress()));
                writeCharacteristic.setValue(value);
                bluetoothGatt.writeCharacteristic(writeCharacteristic);
            }
        });
        isoRightArrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isoSeekBar.setProgress(isoSeekBar.getProgress() + 1);
                byte[] value = BlackMagicCommands.setISO(Utility.isoSeekBarToValue(isoSeekBar.getProgress()));
                writeCharacteristic.setValue(value);
                bluetoothGatt.writeCharacteristic(writeCharacteristic);
            }
        });
        isoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                isoValueTextView.setText(String.valueOf(Utility.isoSeekBarToValue(progress)));
                isoValueChangeTextView.setText(String.valueOf(Utility.isoSeekBarToValue(progress)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                byte[] value = BlackMagicCommands.setISO(Utility.isoSeekBarToValue(seekBar.getProgress()));
                writeCharacteristic.setValue(value);
                bluetoothGatt.writeCharacteristic(writeCharacteristic);
            }
        });

        // WB Group Buttons Listeners
        wbSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                wbValueChangeTextView.setText(String.valueOf(Utility.wbSeekBarToValue(progress)) + "K");
                wbValueTextView.setText(String.valueOf(Utility.wbSeekBarToValue(progress)) + "K");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                byte[] value = BlackMagicCommands.setWB(Utility.wbSeekBarToValue(seekBar.getProgress()));
                writeCharacteristic.setValue(value);
                bluetoothGatt.writeCharacteristic(writeCharacteristic);
            }
        });
        wbLeftArrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wbSeekBar.setProgress(wbSeekBar.getProgress() - 1);
                byte[] value = BlackMagicCommands.setWB(Utility.wbSeekBarToValue(wbSeekBar.getProgress()));
                writeCharacteristic.setValue(value);
                bluetoothGatt.writeCharacteristic(writeCharacteristic);
            }
        });
        wbRightArrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wbSeekBar.setProgress(wbSeekBar.getProgress() + 1);
                byte[] value = BlackMagicCommands.setWB(Utility.wbSeekBarToValue(wbSeekBar.getProgress()));
                writeCharacteristic.setValue(value);
                bluetoothGatt.writeCharacteristic(writeCharacteristic);
            }
        });
        tintLeftArrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newValue = Integer.parseInt((String) tintValueChangeTextView.getText()) - 1;
                tintValueChangeTextView.setText(String.valueOf(newValue));
                tintValueTextView.setText(String.valueOf(newValue));
                String wbString = (String) wbValueChangeTextView.getText();
                byte[] value = BlackMagicCommands.setTint(newValue, Integer.valueOf(wbString.substring(0,wbString.length()-1)));
                writeCharacteristic.setValue(value);
                bluetoothGatt.writeCharacteristic(writeCharacteristic);
            }
        });
        tintRightArrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newValue = Integer.parseInt((String) tintValueChangeTextView.getText()) + 1;
                tintValueChangeTextView.setText(String.valueOf(newValue));
                tintValueTextView.setText(String.valueOf(newValue));
                String wbString = (String) wbValueChangeTextView.getText();
                byte[] value = BlackMagicCommands.setTint(newValue, Integer.valueOf(wbString.substring(0,wbString.length()-1)));
                writeCharacteristic.setValue(value);
                bluetoothGatt.writeCharacteristic(writeCharacteristic);
            }
        });

        // Shutter Group Listeners

        shutterLeftArrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentStringValue = (String) shutterValueChangeTextView.getText();
                Log.d("statusbe2", currentStringValue.substring(2));
                if (Utility.shutterValues.indexOf(Integer.parseInt(currentStringValue.substring(2)))>0){
                    int nextValue = Utility.shutterValues.get(Utility.shutterValues.indexOf(Integer.parseInt(currentStringValue.substring(2))) - 1);
                    byte[] value = BlackMagicCommands.setShutter(nextValue);
                    shutterValueChangeTextView.setText("1/" + nextValue);
                    shutterValueTextView.setText("1/" + nextValue);
                    writeCharacteristic.setValue(value);
                    bluetoothGatt.writeCharacteristic(writeCharacteristic);
                }

            }
        });
        shutterRightArrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentStringValue = (String) shutterValueChangeTextView.getText();
                if (Utility.shutterValues.indexOf(Integer.parseInt(currentStringValue.substring(2)))<Utility.shutterValues.size()-1){
                    int nextValue = Utility.shutterValues.get(Utility.shutterValues.indexOf(Integer.parseInt(currentStringValue.substring(2))) + 1);
                    byte[] value = BlackMagicCommands.setShutter(nextValue);
                    shutterValueChangeTextView.setText("1/" + nextValue);
                    shutterValueTextView.setText("1/" + nextValue);
                    writeCharacteristic.setValue(value);
                    bluetoothGatt.writeCharacteristic(writeCharacteristic);
                }

            }
        });

        shutterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableGroup(shutterGroup);
            }
        });

        // Aperture button Listeners

        irisButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] bytevalue = BlackMagicCommands.intToByteArray(15, 2);
                byte[] value = new byte[]{1,6,0,0,0,4,2,0,bytevalue[0], bytevalue[1]};
                writeCharacteristic.setValue(value);
                bluetoothGatt.writeCharacteristic(writeCharacteristic);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Utility.loadWBList();
        if (bluetoothAdapter!=null){if (!bluetoothAdapter.isEnabled()) {
            cameraSettingsGroup.setVisibility(INVISIBLE);
            disconnectedGroup.setVisibility(VISIBLE);
            disableGroup();
            showToast("Bluetooth is disabled");
            promptEnableBluetooth();
        }}

    }

    private void initView() {

        mSeekBrightness.setMax(100);
        mSeekBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                    mCameraHelper.setModelValue(UVCCameraHelper.MODE_BRIGHTNESS,progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mSeekContrast.setMax(100);
        mSeekContrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                    mCameraHelper.setModelValue(UVCCameraHelper.MODE_CONTRAST,progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // step.2 register USB event broadcast
        if (mCameraHelper != null) {
            mCameraHelper.registerUSB();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // step.3 unregister USB event broadcast
        if (mCameraHelper != null) {
            mCameraHelper.unregisterUSB();
        }
    }

/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toobar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_takepic:
                if (mCameraHelper == null || !mCameraHelper.isCameraOpened()) {
                    showShortMsg("sorry,camera open failed");
                    return super.onOptionsItemSelected(item);
                }
                String picPath = UVCCameraHelper.ROOT_PATH + MyApplication.DIRECTORY_NAME +"/images/"
                        + System.currentTimeMillis() + UVCCameraHelper.SUFFIX_JPEG;

                mCameraHelper.capturePicture(picPath, new AbstractUVCCameraHandler.OnCaptureListener() {
                    @Override
                    public void onCaptureResult(String path) {
                        if(TextUtils.isEmpty(path)) {
                            return;
                        }
                        new Handler(getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(USBCameraActivity.this, "save path:"+path, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                break;
            case R.id.menu_recording:
                if (mCameraHelper == null || !mCameraHelper.isCameraOpened()) {
                    showShortMsg("sorry,camera open failed");
                    return super.onOptionsItemSelected(item);
                }
                if (!mCameraHelper.isPushing()) {
                    String videoPath = UVCCameraHelper.ROOT_PATH + MyApplication.DIRECTORY_NAME +"/videos/" + System.currentTimeMillis()
                            + UVCCameraHelper.SUFFIX_MP4;

//                    FileUtils.createfile(FileUtils.ROOT_PATH + "test666.h264");
                    // if you want to record,please create RecordParams like this
                    RecordParams params = new RecordParams();
                    params.setRecordPath(videoPath);
                    params.setRecordDuration(0);                        // auto divide saved,default 0 means not divided
                    params.setVoiceClose(mSwitchVoice.isChecked());    // is close voice

                    params.setSupportOverlay(true); // overlay only support armeabi-v7a & arm64-v8a
                    mCameraHelper.startPusher(params, new AbstractUVCCameraHandler.OnEncodeResultListener() {
                        @Override
                        public void onEncodeResult(byte[] data, int offset, int length, long timestamp, int type) {
                            // type = 1,h264 video stream
                            if (type == 1) {
                                FileUtils.putFileStream(data, offset, length);
                            }
                            // type = 0,aac audio stream
                            if(type == 0) {

                            }
                        }

                        @Override
                        public void onRecordResult(String videoPath) {
                            if(TextUtils.isEmpty(videoPath)) {
                                return;
                            }
                            new Handler(getMainLooper()).post(() -> Toast.makeText(USBCameraActivity.this, "save videoPath:"+videoPath, Toast.LENGTH_SHORT).show());
                        }
                    });
                    // if you only want to push stream,please call like this
                    // mCameraHelper.startPusher(listener);
                    showShortMsg("start record...");
                    mSwitchVoice.setEnabled(false);
                } else {
                    FileUtils.releaseFile();
                    mCameraHelper.stopPusher();
                    showShortMsg("stop record...");
                    mSwitchVoice.setEnabled(true);
                }
                break;
            case R.id.menu_resolution:
                if (mCameraHelper == null || !mCameraHelper.isCameraOpened()) {
                    showShortMsg("sorry,camera open failed");
                    return super.onOptionsItemSelected(item);
                }
                showResolutionListDialog();
                break;
            case R.id.menu_focus:
                if (mCameraHelper == null || !mCameraHelper.isCameraOpened()) {
                    showShortMsg("sorry,camera open failed");
                    return super.onOptionsItemSelected(item);
                }
                mCameraHelper.startCameraFoucs();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
*/
    private void showResolutionListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(USBCameraActivity.this);
        View rootView = LayoutInflater.from(USBCameraActivity.this).inflate(R.layout.layout_dialog_list, null);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_dialog);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(USBCameraActivity.this, android.R.layout.simple_list_item_1, getResolutionList());
        if (adapter != null) {
            listView.setAdapter(adapter);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (mCameraHelper == null || !mCameraHelper.isCameraOpened())
                    return;
                final String resolution = (String) adapterView.getItemAtPosition(position);
                String[] tmp = resolution.split("x");
                if (tmp != null && tmp.length >= 2) {
                    int width = Integer.valueOf(tmp[0]);
                    int height = Integer.valueOf(tmp[1]);
                    mCameraHelper.updateResolution(width, height);
                }
                mDialog.dismiss();
            }
        });

        builder.setView(rootView);
        mDialog = builder.create();
        mDialog.show();
    }

    // example: {640x480,320x240,etc}
    private List<String> getResolutionList() {
        List<Size> list = mCameraHelper.getSupportedPreviewSizes();
        List<String> resolutions = null;
        if (list != null && list.size() != 0) {
            resolutions = new ArrayList<>();
            for (Size size : list) {
                if (size != null) {
                    resolutions.add(size.width + "x" + size.height);
                }
            }
        }
        return resolutions;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FileUtils.releaseFile();
        // step.4 release uvc camera resources
        if (mCameraHelper != null) {
            mCameraHelper.release();
        }
        if (bluetoothGatt != null) {bluetoothGatt.close();}
    }

    private void showShortMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public USBMonitor getUSBMonitor() {
        return mCameraHelper.getUSBMonitor();
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (canceled) {
            showShortMsg("取消操作");
        }
    }

    public boolean isCameraOpened() {
        return mCameraHelper.isCameraOpened();
    }

    @Override
    public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
        if (!isPreview && mCameraHelper.isCameraOpened()) {
            mCameraHelper.startPreview(mUVCCameraView);
            isPreview = true;
        }
    }

    @Override
    public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {

    }

    @Override
    public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
        if (isPreview && mCameraHelper.isCameraOpened()) {
            mCameraHelper.stopPreview();
            isPreview = false;
        }
    }
    private void enableGroup(Group group){
        if(!groupActivated){
            bottomBarImageView.setVisibility(View.VISIBLE);
            groupActivated = true;
        } else {
            activeGroup.setVisibility(INVISIBLE);
        }
        group.setVisibility(View.VISIBLE);
        activeGroup = group;
    }
    private void disableGroup(){
        if (activeGroup != null){
            activeGroup.setVisibility(INVISIBLE);
        }

        bottomBarImageView.setVisibility(INVISIBLE);
        groupActivated = false;
    }
    private void promptEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, SplashActivity.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
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
            startActivityForResult(enableBtIntent, 3);
        }
    }
    private void showToast(String string){
        Toast toast = Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT);
        toast.show();
    }
}
