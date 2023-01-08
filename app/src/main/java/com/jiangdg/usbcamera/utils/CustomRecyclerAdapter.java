package com.jiangdg.usbcamera.utils;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.le.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jiangdg.usbcamera.R;

import java.util.ArrayList;

public class CustomRecyclerAdapter extends RecyclerView.Adapter<CustomRecyclerAdapter.ViewHolder> {
    ArrayList<ScanResult> itemsList;
    View.OnClickListener listener;
    BluetoothGattCallback bluetoothGattCallback;
    @NonNull
    @Override
    public CustomRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_default, parent, false);
        return new ViewHolder(itemView);
    }
    public CustomRecyclerAdapter(ArrayList<ScanResult> items, BluetoothGattCallback bluetoothGattCallback){
        this.itemsList = items;
        this.bluetoothGattCallback = bluetoothGattCallback;
    }
    @Override
    public void onBindViewHolder(@NonNull CustomRecyclerAdapter.ViewHolder holder, int position) {
        ScanResult result = itemsList.get(position);
        String address = result.getDevice().getName();
        if (address!=null){holder.deviceAddress.setText(address);}
        else {
            holder.deviceAddress.setText("Unknown Device");
        }

    holder.selectButton.setOnClickListener(new View.OnClickListener() {
        @SuppressLint("MissingPermission")
        @Override
        public void onClick(View v) {
            result.getDevice().createBond();
            result.getDevice().connectGatt(v.getContext(), false, bluetoothGattCallback);

        }
    });
    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView deviceAddress;
        Button selectButton;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceAddress = itemView.findViewById(R.id.device_address);
            selectButton = itemView.findViewById(R.id.selectButton);
        }
    }
    public void setBluetoothGattCallback(BluetoothGattCallback bluetoothGattCallback){
        this.bluetoothGattCallback = bluetoothGattCallback;
    }

}
