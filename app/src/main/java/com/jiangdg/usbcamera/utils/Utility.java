package com.jiangdg.usbcamera.utils;

import android.bluetooth.le.ScanResult;

import java.util.ArrayList;
import java.util.Arrays;

public class Utility {
    public static ArrayList<Integer> isovalues = new ArrayList<Integer>(Arrays.asList(new Integer[]{100, 125, 160, 200, 250, 320, 400, 500, 640, 800, 1000, 1250, 1600, 2000, 2500, 3200, 4000, 5000, 6400, 8000, 10000, 12800, 16000, 20000, 25600}));
    public static ArrayList<Integer> wbValues = new ArrayList<>();
    public static ArrayList<Integer> shutterValues = new ArrayList<>(Arrays.asList(new Integer[]{24, 25, 30, 48, 50, 60, 96, 100, 125, 200, 250, 500, 1000, 2000}));
    public static boolean checkResult(ArrayList<ScanResult> scanResults, ScanResult result){
        for (ScanResult s: scanResults){
            if (s.getDevice().getAddress().equals(result.getDevice().getAddress())){
                return true;
            }
        }
        return false;
    }
    public static String byteArrayString(byte[] bytearray){
        String output = "[";
        for (byte b : bytearray){
            output += String.valueOf((int) b) +", ";
        }
        output += "]";
        return output;
    }
    public static int isoSeekBarToValue(int seekBarValue){
        int value = isovalues.get(seekBarValue);
        return value;
    }
    public static int isoValueToSeekBar(int value){
        int seekBarValue = isovalues.indexOf(value);
        return seekBarValue;
    }
    public static int wbValueToSeekBar(int value){
        int seekBarValue = wbValues.indexOf(value);
        return seekBarValue;
    }
    public static int wbSeekBarToValue(int seekBarValue){
        int value = wbValues.get(seekBarValue);
        return value;
    }
    public static void loadWBList(){
        if (wbValues.size() == 0) {for (int x = 2500; x<=10000; x+= 50){
            wbValues.add(x);
        }}
    }
}
