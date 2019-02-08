package me.toxicmushroom.twobsliders;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.*;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends Activity {

    private BluetoothAdapter bluetoothAdapter;

    private SeekBar seekBarLeft;
    private SeekBar seekBarRight;
    private TextView textViewLeft;
    private TextView textViewRight;
    private TextView textViewDevice;
    private OutputStream bluetoothOutputStream;

    private Button resetButton;
    private Button selectorButton;
    private boolean enabling = false;
    private boolean registeredReceiver = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setActionBar(myToolbar);

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeAsUpIndicator(R.drawable.ic_bluetoothsliders);
        }

        resetButton = findViewById(R.id.button2);
        selectorButton = findViewById(R.id.button1);

        seekBarLeft = findViewById(R.id.seekBar1);
        seekBarRight = findViewById(R.id.seekBar2);
        textViewLeft = findViewById(R.id.textView1);
        textViewRight = findViewById(R.id.textView2);
        textViewDevice = findViewById(R.id.textView3);

        seekBarLeft.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textViewLeft.setText(String.valueOf(i-255));
                if (bluetoothOutputStream != null) {
                    try {
                        bluetoothOutputStream.write(("l" + i).getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarRight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textViewRight.setText(String.valueOf(i-255));
                if (bluetoothOutputStream != null) {
                    try {
                        bluetoothOutputStream.write(("r" + i).getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBarLeft.setProgress(255);
                seekBarRight.setProgress(255);
                textViewLeft.setText("0");
                textViewRight.setText("0");
            }
        });

        selectorButton.setOnClickListener(selectorListener);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (registeredReceiver)
            unregisterReceiver(broadcastReceiver);
    }

    private final View.OnClickListener selectorListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (bluetoothAdapter == null) {
                Toast.makeText(view.getContext(), "This device doesn't have bluetooth", Toast.LENGTH_LONG).show();
                return;
            }
            if (!bluetoothAdapter.isEnabled()) {
                enabling = true;
                Intent bluetoothEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(bluetoothEnableIntent);

                IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                registerReceiver(broadcastReceiver, intentFilter);
                registeredReceiver = true;
            } else {
                openDeviceSelector();
            }
        }
    };

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED) &&
                    intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR) == BluetoothAdapter.STATE_ON) {
                enabling = false;
                openDeviceSelector();
            }
        }
    };

    private void openDeviceSelector() {
        List<String> options = new ArrayList<>();
        for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
            Variables.deviceList.add(device);
            options.add(device.getName() + " - " + device.getAddress());
        }
        if (options.isEmpty()) {
            Toast.makeText(this, "You don't have any devices", Toast.LENGTH_LONG).show();
        }
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Select a device")
                .setIcon(R.drawable.ic_bluetooth_white_24dp)
                .setSingleChoiceItems(options.toArray(new String[0]), 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int index) {
                        if (index >= 0 && index < Variables.deviceList.size()) Variables.selectedDevice = Variables.deviceList.get(0);
                        else Variables.selectedDevice = Variables.deviceList.get(index);
                    }
                })
                .setPositiveButton("confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int button) {
                        if (Variables.selectedDevice == null)
                            Variables.selectedDevice = Variables.deviceList.get(0);
                        System.out.println(Variables.selectedDevice);
                        textViewDevice.setText(Variables.selectedDevice.getName());

                        try {
                            UUID uuid = Variables.selectedDevice.getUuids()[0].getUuid();
                            BluetoothSocket socket = Variables.selectedDevice.createRfcommSocketToServiceRecord(uuid);
                            socket.connect();
                            bluetoothOutputStream = socket.getOutputStream();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (id >= 0 && id < Variables.deviceList.size()) {
                            System.out.println(Variables.deviceList.get(id));
                        }
                    }
                })
                .create();
        alertDialog.show();
    }
}
