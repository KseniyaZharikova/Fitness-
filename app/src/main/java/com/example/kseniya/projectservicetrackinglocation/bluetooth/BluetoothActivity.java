package com.example.kseniya.projectservicetrackinglocation.bluetooth;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.example.kseniya.projectservicetrackinglocation.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
        if (searchBt) {
            final List<String> list = new ArrayList<>();
            list.add("");
            pairedDevices.addAll(mBluetoothAdapter.getBondedDevices());
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    list.add(device.getName() + "\n" + device.getAddress());
                }
            }
            if (!h7) {
                final BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
                    public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                        if (!list.contains(device.getName() + "\n" + device.getAddress())) {
                            list.add(device.getName() + "\n" + device.getAddress());
                            pairedDevices.add(device);
                        }
                    }
                };
                Thread scannerBTLE = new Thread() {
                    public void run() {
                        mBluetoothAdapter.startLeScan(leScanCallback);
                        try {
                            Thread.sleep(5000);
                            mBluetoothAdapter.stopLeScan(leScanCallback);
                        } catch (InterruptedException e) {
                        }
                    }
                };

                scannerBTLE.start();
            }
            spinner1 = findViewById(R.id.spinner1);
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, list);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner1.setOnItemSelectedListener(this);
            spinner1.setAdapter(dataAdapter);

            if (DataHandler.getInstance().getID() != 0 && DataHandler.getInstance().getID() < spinner1.getCount())
                spinner1.setSelection(DataHandler.getInstance().getID());
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            menuBool = false;
            if (spinner1 != null) {
                spinner1.setSelection(0);
            }
            if (DataHandler.getInstance().getReader() == null) {
                DataHandler.getInstance().getH7().cancel();
                DataHandler.getInstance().setH7(null);
                h7 = false;
            } else {
                DataHandler.getInstance().getReader().cancel();
                DataHandler.getInstance().setReader(null);
                normal = false;
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        if (arg2 != 0) {
            DataHandler.getInstance().setID(arg2);
            if (!h7 && ((BluetoothDevice) pairedDevices.toArray()[DataHandler.getInstance().getID() - 1]).getName().contains("H7") && DataHandler.getInstance().getReader() == null) {
                DataHandler.getInstance().setH7(new H7ConnectThread((BluetoothDevice) pairedDevices.toArray()[DataHandler.getInstance().getID() - 1], this));
                h7 = true;
            } else if (!normal && DataHandler.getInstance().getH7() == null) {
                DataHandler.getInstance().setReader(new ConnectThread((BluetoothDevice) pairedDevices.toArray()[arg2 - 1], this));
                DataHandler.getInstance().getReader().start();
                normal = true;
            }
            menuBool = true;
        }
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_settings).setEnabled(menuBool);
        menu.findItem(R.id.action_settings).setVisible(menuBool);
        return true;
    }

    public void onNothingSelected(AdapterView<?> arg0) {
    }

    public void connectionError() {
        if (menuBool) {
            menuBool = false;
            final BluetoothActivity ac = this;
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getBaseContext(), getString(R.string.couldnotconnect), Toast.LENGTH_SHORT).show();
                    Spinner spinner1 = findViewById(R.id.spinner1);
                    if (DataHandler.getInstance().getID() < spinner1.getCount())
                        spinner1.setSelection(DataHandler.getInstance().getID());

                    if (!h7) {
                        DataHandler.getInstance().setReader(null);
                        DataHandler.getInstance().setH7(new H7ConnectThread((BluetoothDevice) pairedDevices.toArray()[DataHandler.getInstance().getID() - 1], ac));
                        h7 = true;
                    } else if (!normal) {
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
