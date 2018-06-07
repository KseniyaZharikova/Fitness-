package com.example.kseniya.projectservicetrackinglocation.bluetooth;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;


import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kseniya.projectservicetrackinglocation.R;

public class BluetoothActivity extends Activity implements OnItemSelectedListener, Observer {

    boolean searchBt = true;
    BluetoothAdapter mBluetoothAdapter;
    List<BluetoothDevice> pairedDevices = new ArrayList<>();
    boolean menuBool = false;
    boolean h7 = false;
    boolean normal = false;
    private Spinner spinner1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        DataHandler.getInstance().addObserver(this);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {

            Log.i("Main Activity", "old device H7 disbled");
            h7 = true;
        }


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (DataHandler.getInstance().newValue) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter != null) {
                if (!mBluetoothAdapter.isEnabled()) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.bluetooth)
                            .setMessage(R.string.bluetoothOff)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    mBluetoothAdapter.enable();
                                    try {
                                        Thread.sleep(2000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    listBT();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    searchBt = false;
                                }
                            })
                            .show();
                } else {
                    listBT();

                }
            }

        }

    }

    protected void onDestroy() {
        super.onDestroy();
        DataHandler.getInstance().deleteObserver(this);
    }

    public void listBT() {
        Log.d("Main Activity", "Listing BT elements");
        if (searchBt) {
            //Discover bluetooth devices
            final List<String> list = new ArrayList<>();
            list.add("");
            pairedDevices.addAll(mBluetoothAdapter.getBondedDevices());
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    list.add(device.getName() + "\n" + device.getAddress());

                }
            }

            Log.d("dataAdapter", "1: ");
            spinner1 = findViewById(R.id.spinner1);
            final ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, list);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            Log.d("dataAdapter", "2: ");
            spinner1.setOnItemSelectedListener(this);
            spinner1.setAdapter(dataAdapter);


            if (!h7) {
                Log.d("Main Activity", "Listing BTLE elements");
                final BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
                    public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                        if (!list.contains(device.getName())) {
                            Log.d("Main Activity", "Adding " + device.getName());
                            list.add(device.getName());
                            dataAdapter.notifyDataSetChanged();

                            pairedDevices.add(device);
                        }
                    }
                };


                Thread scannerBTLE = new Thread() {
                    public void run() {
                        Log.d("Main Activity", "Starting scanning for BTLE");
                        mBluetoothAdapter.startLeScan(leScanCallback);
                        try {
                            Thread.sleep(5000);
                            Log.d("Main Activity", "Stoping scanning for BTLE");
                            mBluetoothAdapter.stopLeScan(leScanCallback);
                        } catch (InterruptedException e) {
                            Log.e("Main Activity", "ERROR: on scanning");
                        }
                    }
                };

                scannerBTLE.start();
            }

            if (DataHandler.getInstance().getID() != 0 && DataHandler.getInstance().getID() < spinner1.getCount())
                spinner1.setSelection(DataHandler.getInstance().getID());

        }

    }

    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        if (arg2 != 0) {
            //Actual work
            Log.d("dataAdapter", "6: ");
            DataHandler.getInstance().setID(arg2);
            Log.d("dataAdapter", "7: ");
            if (!h7 && ((BluetoothDevice) pairedDevices.toArray()[DataHandler.getInstance().getID() - 1])
                    .getName()
                    .contains("H7") && DataHandler.getInstance().getReader() == null) {

                Log.i("Main Activity", "Starting h7");
                DataHandler.getInstance().setH7(new H7ConnectThread((BluetoothDevice)
                        pairedDevices.toArray()[DataHandler.getInstance().getID() - 1], this));
                h7 = true;
            } else {
                Toast.makeText(getApplicationContext(), "Already connect", Toast.LENGTH_LONG).show();

            }
            menuBool = true;

        }

    }


    public void onNothingSelected(AdapterView<?> arg0) {

    }

    public void connectionError() {

        if (menuBool) {
            Log.d("Main Activity", "in the app");
            menuBool = false;
            final BluetoothActivity ac = this;
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getBaseContext(), getString(R.string.couldnotconnect), Toast.LENGTH_SHORT).show();
                    Spinner spinner1 = findViewById(R.id.spinner1);
                    if (DataHandler.getInstance().getID() < spinner1.getCount())
                        spinner1.setSelection(DataHandler.getInstance().getID());

                    if (!h7) {

                        Log.w("Main Activity", "starting H7 after error");
                        DataHandler.getInstance().setReader(null);
                        DataHandler.getInstance().setH7(new H7ConnectThread((BluetoothDevice) pairedDevices.toArray()[DataHandler.getInstance().getID() - 1], ac));
                        h7 = true;
                    } else if (!normal) {
                        Log.w("Main Activity", "Starting normal after error");
                        DataHandler.getInstance().setH7(null);
                        DataHandler.getInstance().setReader(new ConnectThread((BluetoothDevice) pairedDevices.toArray()[DataHandler.getInstance().getID() - 1], ac));
                        DataHandler.getInstance().getReader().start();
                        normal = true;
                    }
                }
            });
        }
    }

    public void update(Observable observable, Object data) {
        receiveData();
    }

    public void receiveData() {
        runOnUiThread(new Runnable() {
            public void run() {
                TextView rpm = findViewById(R.id.rpm);
                rpm.setText(DataHandler.getInstance().getLastValue());

                TextView min = findViewById(R.id.min);
                min.setText(DataHandler.getInstance().getMin());

                TextView avg = findViewById(R.id.avg);
                avg.setText(DataHandler.getInstance().getAvg());

                TextView max = findViewById(R.id.max);
                max.setText(DataHandler.getInstance().getMax());

            }
        });
    }


}
