package org.thboileau.remoteControl;

import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class RemoteCommandsActivity extends CommunicationsActivity {


    private String mDeviceAddress;
    protected CommunicationsTask mBluetoothConnection;

    private String mMessageFromServer = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_communications);

        // Retrieve the address of the bluetooth device from the BluetoothListDeviceActivity
        Intent newint = getIntent();
        mDeviceAddress = newint.getStringExtra(MainActivity.EXTRA_ADDRESS);

        // Create a connection to this device
        mBluetoothConnection = new CommunicationsTask(this, mDeviceAddress);

        mBluetoothConnection.execute();

        addListenerOnForwardButton();
        addListenerOnBackwardButton();
        addListenerOnStopButton();
    }

    public void addListenerOnForwardButton() {
        Button button = (Button) findViewById(R.id.buttonForward);

        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                mBluetoothConnection.forward();
            }

        });
    }

    public void addListenerOnBackwardButton() {
        Button button = (Button) findViewById(R.id.buttonBackward);

        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                mBluetoothConnection.backward();
            }

        });
    }

    public void addListenerOnStopButton() {
        Button button = (Button) findViewById(R.id.buttonStop);

        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                mBluetoothConnection.stop();
            }

        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBluetoothConnection.disconnect();
    }

}
