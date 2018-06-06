package com.example.kseniya.projectservicetrackinglocation.bluetooth;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class ConnectThread extends Thread {
	
	BluetoothAdapter mBluetoothAdapter;
	private final BluetoothSocket mmSocket;
	BluetoothActivity mActivityBluetooth;
	
	public ConnectThread(BluetoothDevice device, BluetoothActivity ac) {
		this.mActivityBluetooth=ac;
		BluetoothSocket tmp = null;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		try {
			UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
			tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
		} catch (IOException ignored) {
			Log.e("ConnectThread", "Error on getting the device");
		}
		mmSocket = tmp;
	}

	public void run() {
		mBluetoothAdapter.cancelDiscovery();
		int ok =0;
		while(ok<2)
			try {
				if(mmSocket.isConnected())
					mmSocket.close();
				mmSocket.connect();
				ok=5;
			} catch (IOException connectException) {
				if (ok==0)
					ok++;
				else{
					mActivityBluetooth.connectionError();
					Log.e("ConnectThread","Error with the BT stack " + connectException.toString());

					try {
						mmSocket.close();
					} catch (IOException closeException) {
                        Log.e("ConnectThread", "Error on getting the stack");
                    }
					return;
				}
			}
		while (true){
			try {
				DataHandler.getInstance().acqui(mmSocket.getInputStream().read());
			} catch (IOException e) {
				mActivityBluetooth.connectionError();
				Log.e("ConnectThread","Error with the BT stack " + e.toString());

				try {
					mmSocket.getInputStream().close();
					mmSocket.close();
				} catch (IOException closeException) {
                    Log.e("ConnectThread", "Error on getting the stack");
                }
				return;
			}
		}
	}
	public void cancel() {
		Log.i("ConnectThread","Closing BT connection");
		try {
			if(mmSocket!=null && mmSocket.isConnected())
				mmSocket.close();
		} catch (IOException e) {
            Log.e("ConnectThread", "Error on closing bluetooth");
        }
	}
}