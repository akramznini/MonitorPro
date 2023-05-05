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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
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
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;

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
    private Thread thread;
    private Thread histogramThread;
    private ImageView falseColorImageView;
    private ImageButton screenResultionButton;
    private ImageButton isoButton;
    private ImageView batteryImageView;
    private TextView batteryPercentageTextView;
    private boolean batteryImageViewStatus;
    private ImageView bottomBarImageView;
    private Group activeGroup;
    private boolean groupActivated;
    private Group isoGroup;
    private Group frameGroup;
    private ZoomLayout zoomLayout;
    private BluetoothDevice cameraBluetooth;
    private BluetoothGattCallback bluetoothGattCallback;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic writeCharacteristic;
    private BluetoothGattCharacteristic notificationCharacteristic;
    private TextView isoValueTextView;
    private TextView isoValueChangeTextView;
    private SeekBar isoSeekBar;
    private SeekBar frameGuideSeekBar;
    private SeekBar zebraSeekBar;
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
    private RadioButton falseColorButton;
    private RadioButton zebraButton;
    private TextView zebraTextView;
    private RadioButton histogramButton;
    private TextView histogramTextView;
    private RadioButton thirdsButton;
    private TextView thirdsTextView;
    private RadioButton frameGuidesButton;
    private TextView frameGuidesTextView;
    private RadioGroup moreOptionsGroup;
    private Group shutterGroup;
    private ImageButton irisButton;
    private Group cameraSettingsGroup;
    private Button reconnectButton;
    private Group disconnectedGroup;
    private ImageButton moreOptionsButton;
    private ToggleButton falseColorToggleButton;
    private ToggleButton zebraToggleButton;
    private ToggleButton histogramToggleButton;
    private ToggleButton ruleOfThirdsToggleButton;
    private ToggleButton activatedSwitch;
    private ToggleButton frameGuidesToggleButton;
    private ImageButton frameGuideLeftArrowButton;
    private ImageButton frameGuideRightArrowButton;
    private ImageButton zebraLeftArrowButton;
    private ImageButton zebraRightArrowButton;
    private TextView currentFrameGuideTextView;
    private TextView currentZebraTextView;
    private ImageView ruleOfThirdsImageView;
    private ImageView frameGuideImageView;
    private int displayMode = 0;
    private boolean[] primaryButtonsStateArray = new boolean[3];
    private boolean primaryButtonsInitialized = false;
    private ConstraintLayout constraintLayout;
    private Mat[] zebraPatternImages;
    private int currentZebraIndex = 0;
    private LineChart histogramLineChart;
    private Mat frameMat;
    private boolean histogramThreadStarted = false;
    private Bitmap frameBitmap;
    private Group zebraGroup;
    private Mat zebraColorMap0;
    private Mat zebraColorMap1;
    private Mat zebraColorMap2;
    private Mat zebraColorMap3;
    private Mat zebraColorMap4;
    private Mat zebraColorMap5;
    private Mat currentZebraColorMap;
    private int looperFailedAttempts;
    private int bitwiseFailedAttempts;
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
                thread.interrupt();
                histogramThread.interrupt();
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

                            // start thread
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
            thread.interrupt();
            histogramThread.interrupt();
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
        OpenCVLoader.initDebug();
        Mat falseColorMap = new Mat(256, 1, CvType.CV_8UC3);
        for (int i = 0; i<256; i++){
            if (i > 224) {
                falseColorMap.put(i, 0, 255, 0, 0);
            } else if (i > 210) {

                falseColorMap.put(i, 0, 255, 255, 0);

            } else if (i > 160) {
                if (i>212){
                    int factor = (227-i)/(227-212);
                    int red = (factor*200 + (1- factor)*255);
                    int green = (factor*200 + (1- factor)*255);
                    int blue = (factor*200 + (1- factor)*0);
                    falseColorMap.put(i, 0, red,green,blue);
                }
                else {falseColorMap.put(i, 0, 200, 200, 200);}




            } else if (i > 127){
                if (i>150){
                    int factor = (160-i)/(160-150);
                    int red = (factor*251 + (1- factor)*200);
                    int green = (factor*144 + (1- factor)*200);
                    int blue = (factor*139 + (1- factor)*200);
                    falseColorMap.put(i, 0, red,green,blue);
                }
                else {
                    falseColorMap.put(i,0,251, 144, 139);
                }



            }
            else if (i >120){

                falseColorMap.put(i,0,100, 100, 100);
            }
            else if (i > 85){
                if (i>114){
                    int factor = (120-i)/(120-114);
                    int red = (factor*88 + (1- factor)*100);
                    int green = (factor*151 + (1- factor)*100);
                    int blue = (factor*66 + (1- factor)*100);
                    falseColorMap.put(i, 0, red,green,blue);
                }
                else {falseColorMap.put(i,0,88, 151, 66);}

            }
            else if (i > 25.5){

                if (i>75){
                    int factor = (85-i)/(85-75);
                    int red = (factor*40 + (1- factor)*88);
                    int green = (factor*40 + (1- factor)*151);
                    int blue = (factor*40 + (1- factor)*66);
                    falseColorMap.put(i, 0, red,green,blue);
                }
                else {falseColorMap.put(i,0,40, 40, 40);}




            }
            else if (i > 16){

                falseColorMap.put(i,0,0, 0, 255);
            }
            else {
                if (i>9){
                    int factor = (16-i)/(16-9);
                    int red = (factor*100 + (1- factor)*0);
                    int green = (factor*0 + (1- factor)*0);
                    int blue = (factor*100 + (1- factor)*255);
                    falseColorMap.put(i, 0, red,green,blue);
                }
                else {
                    falseColorMap.put(i,0,100, 0, 100);
                }



            }
        }

        // 6 Zebra color maps from 75% to 100%



        // 75%
        zebraColorMap0 = new Mat(256, 1, CvType.CV_8UC3);

        // 80%
        zebraColorMap1 = new Mat(256, 1, CvType.CV_8UC3);

        // 85%
        zebraColorMap2 = new Mat(256, 1, CvType.CV_8UC3);

        // 90%
        zebraColorMap3 = new Mat(256, 1, CvType.CV_8UC3);

        // 95%
        zebraColorMap4 = new Mat(256, 1, CvType.CV_8UC3);

        //100%
        zebraColorMap5 = new Mat(256, 1, CvType.CV_8UC3);

        currentZebraColorMap = zebraColorMap0;

        for (int i = 0; i<256; i++){
            if (i>224){
                zebraColorMap5.put(i, 0, 0, 0, 0);

            } else {
                zebraColorMap5.put(i, 0, 255, 255, 255);

            }
            if (i>222){
                zebraColorMap4.put(i, 0, 0, 0, 0);

            } else {
                zebraColorMap4.put(i, 0, 255, 255, 255);

            }
            if (i>221){
                zebraColorMap3.put(i, 0, 0, 0, 0);

            } else {
                zebraColorMap3.put(i, 0, 255, 255, 255);

            }
            if (i>220){
                zebraColorMap2.put(i, 0, 0, 0, 0);

            } else {
                zebraColorMap2.put(i, 0, 255, 255, 255);

            }
            if (i>219){
                zebraColorMap1.put(i, 0, 0, 0, 0);

            } else {
                zebraColorMap1.put(i, 0, 255, 255, 255);

            }


            if (i>218){
                zebraColorMap0.put(i, 0, 0, 0, 0);

            } else {
                zebraColorMap0.put(i, 0, 255, 255, 255);

            }
        }
        zebraPatternImages = generateZebraImages();

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
        ruleOfThirdsImageView = findViewById(R.id.ruleOfThirdsGrid);
        moreOptionsButton = findViewById(R.id.moreOptionsButton);
        falseColorToggleButton = findViewById(R.id.falseColorToggleButton);
        zebraGroup = findViewById(R.id.zebraGroup);
        zebraToggleButton = findViewById(R.id.zebraToggleButton);
        histogramToggleButton = findViewById(R.id.histogramToggleButton);
        ruleOfThirdsToggleButton = findViewById(R.id.ruleOfThirdsToggleButton);
        frameGuidesToggleButton = findViewById(R.id.frameGuidesToggleButton);
        falseColorImageView = findViewById(R.id.imageView2);
        zoomLayout = findViewById(R.id.zoomLayout);
        isoButton = findViewById(R.id.isoButton);
        bottomBarImageView = findViewById(R.id.bottomBarImageView);
        bottomBarImageView.setVisibility(INVISIBLE);
        isoGroup = findViewById(R.id.isoGroup);
        frameGroup = findViewById(R.id.frameGuideGroup);
        isoValueTextView = findViewById(R.id.isoValueTextView);
        isoValueChangeTextView = findViewById(R.id.isoValueChangeTextView);
        isoSeekBar = findViewById(R.id.isoSeekBar);
        frameGuideSeekBar = findViewById(R.id.frameGuideSeekBar);
        zebraSeekBar = findViewById(R.id.zebraSeekBar);
        isoLeftArrowButton = findViewById(R.id.isoLeftArrowButton);
        isoRightArrowButton = findViewById(R.id.isoRightArrowButton);
        frameGuideLeftArrowButton = findViewById(R.id.frameGuideLeftArrowButton);
        frameGuideRightArrowButton = findViewById(R.id.frameGuideRightArrowButton);
        zebraLeftArrowButton =  findViewById(R.id.zebraLeftArrowButton);
        zebraRightArrowButton = findViewById(R.id.zebraRightArrowButton);

        currentFrameGuideTextView = findViewById(R.id.currentFrameGuideTextView);
        currentZebraTextView = findViewById(R.id.zebraPercentageTextView);
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
        histogramLineChart = findViewById(R.id.histogramLineChart);
        constraintLayout = findViewById(R.id.mainMonitorLayout);
        falseColorButton = findViewById(R.id.falseColorButton);
        zebraButton = findViewById(R.id.zebraButton);

        histogramButton = findViewById(R.id.histogramButton);

        thirdsButton = findViewById(R.id.thirdsButton);

        frameGuidesButton = findViewById(R.id.frameGuidesButton);

        moreOptionsGroup = findViewById(R.id.moreOptionsRadioGroup);
        frameGuideImageView = findViewById(R.id.frameGuideImageView);

        histogramLineChart.setTouchEnabled(false);
        histogramLineChart.setDragEnabled(false);
        histogramLineChart.setScaleEnabled(false);
        histogramLineChart.setScaleXEnabled(false);
        histogramLineChart.setScaleYEnabled(false);
        histogramLineChart.setPinchZoom(false);
        histogramLineChart.setDoubleTapToZoomEnabled(false);
        histogramLineChart.setHighlightPerDragEnabled(false);
        histogramLineChart.setHighlightPerTapEnabled(false);
        histogramLineChart.setDragDecelerationEnabled(false);
        histogramLineChart.setDrawGridBackground(false);
        histogramLineChart.getLegend().setEnabled(false);
        histogramLineChart.getDescription().setEnabled(false);
        histogramLineChart.getAxisLeft().setEnabled(false);
        histogramLineChart.getAxisRight().setEnabled(false);
        histogramLineChart.getXAxis().setEnabled(false);
        histogramLineChart.getLegend().setEnabled(false);
        histogramLineChart.setDrawBorders(true);
        histogramLineChart.invalidate();
        histogramLineChart.setDrawMarkers(false);


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


        moreOptionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moreOptionsGroup.getVisibility() == INVISIBLE){
                    moreOptionsGroup.setVisibility(VISIBLE);
                    makeSwitchVisible(falseColorToggleButton);
                    falseColorButton.setChecked(true);
                }
                else {
                    moreOptionsGroup.setVisibility(INVISIBLE);

                    if (activatedSwitch != null){
                        activatedSwitch.setVisibility(INVISIBLE);
                        activatedSwitch = null;
                    }
                    frameGroup.setVisibility(INVISIBLE);
                    zebraGroup.setVisibility(INVISIBLE);

                }
            }
        });

        falseColorToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    zebraToggleButton.setChecked(false);
                    displayMode = 1;
                } else {
                    displayMode = 0;
                }
            }
        });
        zebraToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    falseColorToggleButton.setChecked(false);
                    displayMode = 2;
                }
                else {
                    displayMode = 0;
                }
            }
        });
        histogramToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    histogramLineChart.setVisibility(VISIBLE);

                }
                else {
                    histogramLineChart.setVisibility(INVISIBLE);
                }
            }
        });
        ruleOfThirdsToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    ruleOfThirdsImageView.setVisibility(VISIBLE);
                }
                else{
                    ruleOfThirdsImageView.setVisibility(INVISIBLE);
                }
            }
        });

        frameGuidesToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    frameGuideImageView.setVisibility(VISIBLE);
                }
                else{
                    frameGuideImageView.setVisibility(INVISIBLE);
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
        // Frame Group Button Listeners
        frameGuideLeftArrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frameGuideSeekBar.setProgress(frameGuideSeekBar.getProgress()-1);
            }
        });
        frameGuideRightArrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frameGuideSeekBar.setProgress(frameGuideSeekBar.getProgress()+1);
            }
        });

        // Zebra Button Listeners
        zebraLeftArrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zebraSeekBar.setProgress(zebraSeekBar.getProgress()-1);
            }
        });
        zebraRightArrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zebraSeekBar.setProgress(zebraSeekBar.getProgress()+1);
            }
        });

        frameGuideSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (progress){
                    case 0: frameGuideImageView.setImageResource(R.drawable.frame_16_to_9); currentFrameGuideTextView.setText("16:9"); break;
                    case 1: frameGuideImageView.setImageResource(R.drawable.frame_2_to_1); currentFrameGuideTextView.setText("2:1"); break;

                    case 2: frameGuideImageView.setImageResource(R.drawable.frame_185_to_1); currentFrameGuideTextView.setText("1.85:1"); break;

                    case 3: frameGuideImageView.setImageResource(R.drawable.frame_14_to_9); currentFrameGuideTextView.setText("14:9"); break;

                    case 4: frameGuideImageView.setImageResource(R.drawable.frame_4_to_3); currentFrameGuideTextView.setText("4:3"); break;

                    case 5: frameGuideImageView.setImageResource(R.drawable.frame_1_to_1); currentFrameGuideTextView.setText("1:1"); break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        zebraSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch(progress){
                    case 0: currentZebraTextView.setText("75%"); currentZebraColorMap = zebraColorMap0; break;
                    case 1: currentZebraTextView.setText("80%"); currentZebraColorMap = zebraColorMap1; break;
                    case 2: currentZebraTextView.setText("85%"); currentZebraColorMap = zebraColorMap2; break;
                    case 3: currentZebraTextView.setText("90%"); currentZebraColorMap = zebraColorMap3; break;
                    case 4: currentZebraTextView.setText("95%"); currentZebraColorMap = zebraColorMap4; break;
                    case 5: currentZebraTextView.setText("100%"); currentZebraColorMap = zebraColorMap5; break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


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
                byte[] value = new byte[]{1, 6, 0, 0, 0, 4, 2, 0, bytevalue[0], bytevalue[1]};
                writeCharacteristic.setValue(value);
                bluetoothGatt.writeCharacteristic(writeCharacteristic);
            }
        });


        falseColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frameGroup.setVisibility(INVISIBLE);
                zebraGroup.setVisibility(INVISIBLE);
                makeSwitchVisible(falseColorToggleButton);
            }
        });
        zebraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frameGroup.setVisibility(INVISIBLE);
                zebraGroup.setVisibility(VISIBLE);
                makeSwitchVisible(zebraToggleButton);
            }
        });
        histogramButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frameGroup.setVisibility(INVISIBLE);
                zebraGroup.setVisibility(INVISIBLE);
                makeSwitchVisible(histogramToggleButton);
            }
        });
        thirdsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frameGroup.setVisibility(INVISIBLE);
                zebraGroup.setVisibility(INVISIBLE);
                makeSwitchVisible(ruleOfThirdsToggleButton);
            }
        });
        frameGuidesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeSwitchVisible(frameGuidesToggleButton);
                frameGroup.setVisibility(VISIBLE);
                zebraGroup.setVisibility(INVISIBLE);
            }
        });
        histogramThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    if (histogramLineChart.getVisibility() == VISIBLE){
                        Mat redChannel = new Mat();
                        Mat greenChannel = new Mat();
                        Mat blueChannel = new Mat();

                        Core.extractChannel(frameMat, redChannel, 2);
                        Core.extractChannel(frameMat, greenChannel, 1);
                        Core.extractChannel(frameMat, blueChannel, 0);

                        Mat histRed = new Mat();
                        Mat histGreen = new Mat();
                        Mat histBlue = new Mat();

                        MatOfInt channels = new MatOfInt(0);
                        MatOfInt histSize = new MatOfInt(256);
                        MatOfFloat ranges = new MatOfFloat(0, 256);

                        Imgproc.calcHist(Arrays.asList(redChannel), channels, new Mat(), histRed, histSize, ranges);
                        Imgproc.calcHist(Arrays.asList(greenChannel), channels, new Mat(), histGreen, histSize, ranges);
                        Imgproc.calcHist(Arrays.asList(blueChannel), channels, new Mat(), histBlue, histSize, ranges);

                        ArrayList valueSetRed = new ArrayList();
                        ArrayList valueSetGreen = new ArrayList();
                        ArrayList valueSetBlue = new ArrayList();
                        for (int i = 0; i < histRed.rows(); i++) {
                            valueSetRed.add(new Entry(i, (float) histRed.get(i,0)[0]));
                            valueSetGreen.add(new Entry(i, (float) histGreen.get(i,0)[0]));
                            valueSetBlue.add(new Entry(i, (float) histBlue.get(i,0)[0]));
                        }
                        LineDataSet lineDataSetRed = new LineDataSet(valueSetRed, "red");
                        lineDataSetRed.setDrawCircles(false);
                        lineDataSetRed.setColor(Color.rgb(0, 0, 255));

                        LineDataSet lineDataSetGreen = new LineDataSet(valueSetGreen, "green");
                        lineDataSetGreen.setDrawCircles(false);
                        lineDataSetGreen.setColor(Color.rgb(0, 255, 0));

                        LineDataSet lineDataSetBlue = new LineDataSet(valueSetBlue, "blue");
                        lineDataSetBlue.setDrawCircles(false);
                        lineDataSetBlue.setColor(Color.rgb(255, 0, 0));

                        LineData lineData = new LineData(Arrays.asList(lineDataSetRed, lineDataSetGreen, lineDataSetBlue));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                histogramLineChart.setData(lineData);
                                histogramLineChart.notifyDataSetChanged();
                                histogramLineChart.invalidate();
                            }
                        });
                        try {
                            Thread.sleep(30);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                frameMat = new Mat(1080, 1920, CvType.CV_8UC3);
                histogramThread.start();
                while(true){
                    frameBitmap = mUVCCameraView.captureStillImage(1080,1920);
                    if (frameBitmap!=null){
                        Utils.bitmapToMat(frameBitmap, frameMat);
                        switch(displayMode){
                            case 0: if (falseColorImageView.getVisibility() == VISIBLE) runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    falseColorImageView.setVisibility(INVISIBLE);
                                }
                            });break;
                            case 1:
                                if (falseColorImageView.getVisibility() == INVISIBLE){
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            falseColorImageView.setVisibility(VISIBLE);
                                        }
                                    });
                                }
                                Mat secondOrigin = new Mat(500, 500, CvType.CV_8UC3);
                                Imgproc.cvtColor(frameMat, secondOrigin, Imgproc.COLOR_RGBA2RGB);
                                Mat falseColorMat = new Mat(500, 500, CvType.CV_8UC3);
                                Imgproc.applyColorMap(secondOrigin, falseColorMat, falseColorMap);
                                Bitmap outBitmap = Bitmap.createBitmap(falseColorMat.width(), falseColorMat.height(), Bitmap.Config.ARGB_8888);
                                Utils.matToBitmap(falseColorMat, outBitmap);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        falseColorImageView.setImageBitmap(outBitmap);
                                    }
                                }); break;
                            case 2:
                                if (falseColorImageView.getVisibility() == INVISIBLE){
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            falseColorImageView.setVisibility(VISIBLE);
                                        }
                                    });
                                }

                                Mat sourceImage2 = new Mat(1080,1920, CvType.CV_8UC3);
                                Imgproc.cvtColor(frameMat, sourceImage2, Imgproc.COLOR_RGBA2RGB);
                                // black and white image with black part representing overexposure
                                Mat zebraArea = new Mat(1080,1920, CvType.CV_8UC3);
                                Imgproc.applyColorMap(sourceImage2, zebraArea, currentZebraColorMap);

                                Mat inverseZebraArea = new Mat(1080, 1920, CvType.CV_8UC3);
                                Core.bitwise_not(zebraArea, inverseZebraArea);


                                Mat deletedOverExposure = new Mat(1080, 1920, CvType.CV_8UC3);
                                Core.bitwise_and(sourceImage2, zebraArea, deletedOverExposure);



                                Mat zebraOverLay = new Mat(1080, 1920, CvType.CV_8UC3);
                                Mat zebraPattern = getNextZebraPattern();


                                // Loopers to test importing zebra images instead of computing them each time
                                /*looperFailedAttempts = 0;
                                bitwiseFailedAttempts = 0;
                                for (int x = 0; x<=143; x++){
                                    Mat newZebraPattern = new Mat(1080, 1920, CvType.CV_8UC3);
                                    try{
                                        Imgproc.cvtColor(zebraPattern, newZebraPattern, x);
                                        try{
                                            Core.bitwise_and(newZebraPattern, inverseZebraArea, zebraOverLay);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    showToast("yes");
                                                }
                                            });
                                        }
                                        catch (Exception exception){
                                            bitwiseFailedAttempts += 1;
                                        }
                                    }
                                    catch(Exception e){
                                        looperFailedAttempts += 1;
                                    }
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showToast("looper failed Attempts " + looperFailedAttempts + "\n" +
                                        "bitwise failed attempts " + bitwiseFailedAttempts);
                                    }
                                });*/



                                Core.bitwise_and(zebraPattern, inverseZebraArea, zebraOverLay);

                                Mat out = new Mat(1080, 1920, CvType.CV_8UC3);
                                Core.bitwise_or(deletedOverExposure, zebraOverLay, out);
                                Bitmap outBitmap1 = Bitmap.createBitmap(out.width(), out.height(), Bitmap.Config.ARGB_8888);
                                Utils.matToBitmap(out, outBitmap1);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        falseColorImageView.setImageBitmap(outBitmap1);
                                    }
                                }); break;

                        }
                    }

                }
            }
        });
        thread.start();

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
    public Bitmap loadBitmapFromView(View v) {
        Bitmap b = Bitmap.createBitmap(v.getWidth() , v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.draw(c);
        return b;
    }
    public Bitmap falseColor(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int colorRed = Color.rgb(255, 0, 0);;
        int colorYellow = Color.rgb(255, 255, 0);
        int colorLightGrey = Color.rgb(200, 200, 200);
        int colorPink = Color.rgb(251, 144, 139);
        int colorGrey = Color.rgb(100,100,100);
        int colorGreen = Color.rgb(88, 151, 66);
        int colorDarkGrey = Color.rgb(40, 40, 40);
        int colorBlue = Color.rgb(0, 0, 255);
        int colorMagenta = Color.rgb(100, 0, 100);
        // Create a new Bitmap object with the same size as the original
        Bitmap falseColorImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // Iterate over every pixel in the image
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Get the pixel color
                int pixelColor = image.getPixel(x, y);
                // Extract the red, green, and blue channels
                int red = Color.red(pixelColor);
                int green = Color.green(pixelColor);
                int blue = Color.blue(pixelColor);

                // Calculate the luminance of the pixel
                double luminance = (0.3 * red) + (0.59 * green) + (0.11 * blue);

                // Set the false color of the pixel based on its luminance
                int falseColor;
                if (luminance > 233) {
                    falseColor = colorRed;
                } else if (luminance > 227) {

                    falseColor = colorYellow;

                } else if (luminance > 160) {
                    falseColor = colorLightGrey;


                } else if (luminance > 127){

                    falseColor = colorPink;




                }
                else if (luminance >120){
                    falseColor = colorGrey;
                }
                else if (luminance > 85){

                    falseColor = colorGreen;



                }
                else if (luminance > 25.5){

                    falseColor = colorDarkGrey;


                }
                else if (luminance >16){

                    falseColor = colorBlue;


                }
                else {

                    falseColor = colorMagenta;


                }

                falseColorImage.setPixel(x, y, falseColor);
            }
        }

        return falseColorImage;
    }
    public Mat[] generateZebraImages(){
        Mat[] zebraArray = new Mat[6];
        int counter = 0;
        boolean white = true;
        double[] colorWhite = {255, 255 ,255};
        double[] colorBlack = {40, 40, 40};
        Mat zebraPattern = new Mat(1080, 1920, CvType.CV_8UC3);
        for (int x = 0; x <= 1920; x++){
            if (counter>=3){
                white = !white;
                counter = 0;
            }
            for (int y = 0; y <=1080; y++){
                if (white){
                    zebraPattern.put(y, x, colorWhite);
                }
                else {
                    zebraPattern.put(y, x, colorBlack);
                }
            }
            counter += 1;
        }
        zebraArray[0] = zebraPattern;
        Log.d("Mat Type", String.valueOf(zebraPattern.channels()));


        counter = 1;
        white = true;
        Mat zebraPattern1 = new Mat(1080, 1920, CvType.CV_8UC3);
        for (int x = 0; x <= 1920; x++){
            if (counter>=3){
                white = !white;
                counter = 0;
            }
            for (int y = 0; y <=1080; y++){
                if (white){
                    zebraPattern1.put(y, x, colorWhite);
                }
                else {
                    zebraPattern1.put(y, x, colorBlack);
                }
            }
            counter += 1;
        }
        zebraArray[1] = zebraPattern1;

        counter = 2;
        white = true;
        Mat zebraPattern2 = new Mat(1080, 1920, CvType.CV_8UC3);
        for (int x = 0; x <= 1920; x++){
            if (counter>=3){
                white = !white;
                counter = 0;
            }
            for (int y = 0; y <=1080; y++){
                if (white){
                    zebraPattern2.put(y, x, colorWhite);
                }
                else {
                    zebraPattern2.put(y, x, colorBlack);
                }
            }
            counter += 1;
        }
        zebraArray[2] = zebraPattern2;


        counter = 3;
        white = true;
        Mat zebraPattern3 = new Mat(1080, 1920, CvType.CV_8UC3);
        for (int x = 0; x <= 1920; x++){
            if (counter>=3){
                white = !white;
                counter = 0;
            }
            for (int y = 0; y <=1080; y++){
                if (white){
                    zebraPattern3.put(y, x, colorWhite);
                }
                else {
                    zebraPattern3.put(y, x, colorBlack);
                }
            }
            counter += 1;
        }
        zebraArray[3] = zebraPattern3;


        counter = 1;
        white = false;
        Mat zebraPattern4 = new Mat(1080, 1920, CvType.CV_8UC3);
        for (int x = 0; x <= 1920; x++){
            if (counter>=3){
                white = !white;
                counter = 0;
            }
            for (int y = 0; y <=1080; y++){
                if (white){
                    zebraPattern4.put(y, x, colorWhite);
                }
                else {
                    zebraPattern4.put(y, x, colorBlack);
                }
            }
            counter += 1;
        }
        zebraArray[4] = zebraPattern4;

        counter = 2;
        white = false;
        Mat zebraPattern5 = new Mat(1080, 1920, CvType.CV_8UC3);
        for (int x = 0; x <= 1920; x++){
            if (counter>=3){
                white = !white;
                counter = 0;
            }
            for (int y = 0; y <=1080; y++){
                if (white){
                    zebraPattern5.put(y, x, colorWhite);
                }
                else {
                    zebraPattern5.put(y, x, colorBlack);
                }
            }
            counter += 1;
        }
        zebraArray[5] = zebraPattern5;

        /*Mat mat0 = loadResource(getResources(), R.drawable.zebra_pattern_5);



        zebraArray[0] = mat0;
        zebraArray[1] = mat0;
        zebraArray[2] = mat0;
        zebraArray[3] = mat0;
        zebraArray[4] = mat0;
        zebraArray[5] = mat0;*/
        return zebraArray;
    }
    public Mat getNextZebraPattern(){
        Mat zebra;
        if (currentZebraIndex<6){
            zebra = zebraPatternImages[currentZebraIndex];
            currentZebraIndex += 1;
        }
        else {
            zebra = zebraPatternImages[0];
            currentZebraIndex = 1;
        }
        return zebra;
    }
    private void makeSwitchVisible(ToggleButton switchButton){
        if (activatedSwitch == null){
            switchButton.setVisibility(VISIBLE);
            activatedSwitch = switchButton;
        }
        else{
            if (activatedSwitch != switchButton){
                activatedSwitch.setVisibility(INVISIBLE);
                switchButton.setVisibility(VISIBLE);
                activatedSwitch = switchButton;
            }
        }
    }
    private Mat loadResource(Resources resources, int resId) {
        Bitmap bitmap = BitmapFactory.decodeResource(resources, resId);
        Mat mat = new Mat(1080, 1920, CvType.CV_8UC3);
        Utils.bitmapToMat(bitmap, mat);
        return mat;
    }
}
