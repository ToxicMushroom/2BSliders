package me.toxicmushroom.twobsliders;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {

    private BluetoothAdapter bluetoothAdapter;

    private SeekBar seekBarLeft;
    private SeekBar seekBarRight;
    private TextView textViewLeft;
    private TextView textViewRight;

    private Button selectorButton;
    private boolean enabling = false;
    private boolean registeredReceiver = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectorButton = findViewById(R.id.button1);

        seekBarLeft = findViewById(R.id.seekBar1);
        seekBarRight = findViewById(R.id.seekBar2);
        textViewLeft = findViewById(R.id.textView1);
        textViewRight = findViewById(R.id.textView2);

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
        Variables.deviceList.addAll(bluetoothAdapter.getBondedDevices());
        AlertDialog alertDialog = new AlertDialog.Builder(getApplicationContext())
                .setTitle("Select a device")
                .setIcon(R.drawable.ic_bluetooth_white_24dp)
                .setSingleChoiceItems((CharSequence[]) Variables.deviceList.toArray(), 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        System.out.println(Variables.deviceList.get(i));
                    }
                })
                .setPositiveButton("confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        System.out.println(id);
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        System.out.println(id);
                    }
                })
                .create();
        alertDialog.show();
    }
}
