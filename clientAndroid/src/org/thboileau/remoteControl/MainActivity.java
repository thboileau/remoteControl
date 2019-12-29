package org.thboileau.remoteControl;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends Activity {

    ListView mDeviceList;
    private BluetoothAdapter mBluetoothAdapter = null;
    private Set <BluetoothDevice> mPairedDevices;

    public static String EXTRA_ADDRESS = "device_address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mDeviceList = (ListView) findViewById(R.id.listView);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
            finish();
        } else if (mBluetoothAdapter.isEnabled()) {
            listPairedDevices();
        } else {
            //Ask to the user turn the bluetooth on
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon,1);
        }
    }

    private void listPairedDevices() {
        mPairedDevices = mBluetoothAdapter.getBondedDevices();

        ArrayList list = new ArrayList();

        if (mPairedDevices.isEmpty()) {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        } else {
            for(BluetoothDevice bt : mPairedDevices) {
                list.add(bt.getName() + "\n" + bt.getAddress());
            }
        }

        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        mDeviceList.setAdapter(adapter);
        mDeviceList.setOnItemClickListener(myListClickListener);

    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick (AdapterView parent, View view, int position, long id) {
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);
            
            // Make an intent to start next activity.
            Intent i = new Intent(MainActivity.this, RemoteCommandsActivity.class);
            // transmit the address
            i.putExtra(EXTRA_ADDRESS, address);
            startActivity(i);
            
        }
    };
}
