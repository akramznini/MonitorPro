package com.jiangdg.usbcamera.utils;

import java.util.Arrays;

public class BlackMagicCommands {
    public static final int ISO_COMMAND = 0;
    public static final int WHITE_BALANCE_COMMANDE = 1;
    public static final int SHUTTER_SPEED = 2;
    public static final byte[] intToByteArray(int value, int length) {
        byte[] bytes = new byte[length];
        for (int i = 0; i<length; i++){
            bytes[i] = (byte)(value >>> 8*i);
        }
        return bytes;
    }
    public static int byteArrayToInt(byte[] byteArray){
        String hexValue = "";
        for (int i = byteArray.length-1; i>=0; i--){
            if (byteArray[i] == 0){
                hexValue+="00";
            }
            else if (Math.abs(byteArray[i])<16){
                if (byteArray[i]<0){
                    String hexString = Integer.toHexString(byteArray[i]);
                    hexValue += hexString.substring(hexString.length()-2);
                } else{hexValue += "0" + Integer.toHexString(byteArray[i]);}
            }
            else {
                String hexString = Integer.toHexString(byteArray[i]);
                hexValue += hexString.substring(hexString.length()-2, hexString.length());
            }
        }
        return Integer.parseInt(hexValue, 16);
    }
    public static byte[] setISO(int isoValue){
        int byteValueLength = 4;
        int commandLength = 8;
        byte[] isoByte = intToByteArray(isoValue, byteValueLength);
        byte[] bytes = new byte[]{1, (byte)commandLength, 0, 0, 1, 14, 3, 0, isoByte[0], isoByte[1], isoByte[2], isoByte[3]};
        return bytes;
    }
    public static byte[] setShutter(int shutterValue){
        int byteValueLength = 4;
        byte[] shutterByte = intToByteArray(shutterValue, byteValueLength);
        byte[] bytes = new byte[]{1, 8, 0, 0, 1, 12, 3, 0, shutterByte[0], shutterByte[1], shutterByte[2], shutterByte[3]};
        return bytes;
    }
    public static int commandType(byte[] commandArray){
        if (commandArray[4] == 1){
            switch(commandArray[5]){
                case 2  : return WHITE_BALANCE_COMMANDE;
                case 12 : return SHUTTER_SPEED;
                case 14 : return ISO_COMMAND;
            }
        }
        return -1;
    }
    public static int getISO(byte[] commandArray){
        byte[] isoArray = Arrays.copyOfRange(commandArray, 8, 12);
        return byteArrayToInt(isoArray);
    }
    public static int[] getWbTint(byte[] commandArray){
        byte[] wbArray = Arrays.copyOfRange(commandArray, 8, 10);
        return new int[]{byteArrayToInt(wbArray), commandArray[10]};
    }
    public static int getShutterSpeed(byte[] commandArray){
         byte[] shutterSpeedArray = Arrays.copyOfRange(commandArray, 8, 12);
         return byteArrayToInt(shutterSpeedArray);
    }
    public static byte[] setWB(int wbValue){
        int byteValueLength = 2;
        int commandLength = 6;
        byte[] wbByte = intToByteArray(wbValue, byteValueLength);
        byte[] bytes = new byte[]{1, (byte) commandLength, 0 , 0, 1, 2, 2, 0, wbByte[0], wbByte[1]};
        return bytes;
    }
    public static byte[] setTint(int tintValue, int wbValue){
        int byteValueLength = 2;
        int commandLength = 8;
        int num;
        byte[] wbByte = intToByteArray(wbValue, byteValueLength);
        if (tintValue>=0){
            num = 0;
        }
        else {num = -1;}
        byte[] bytes = new byte[]{1, (byte) commandLength, 0 , 0, 1, 2, 2, 0, wbByte[0], wbByte[1], (byte) tintValue, (byte) num};
        return bytes;
    }
}
