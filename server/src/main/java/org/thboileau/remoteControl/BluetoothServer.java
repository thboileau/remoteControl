package org.thboileau.remoteControl;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRegistrationException;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.InputStream;

public class BluetoothServer {

    public static void main(final String[] args) throws Exception {
        try {
            final LocalDevice localDevice = LocalDevice.getLocalDevice();
            // display local device address and name
            System.out.println("Address: " + localDevice.getBluetoothAddress());
            System.out.println("Name: " + localDevice.getFriendlyName());
        } catch (final BluetoothStateException bluetoothStateException) {
            if (bluetoothStateException.getMessage().contains("not available")) {
                System.err.println("You should install the library. Try:\n\tapt-get install libbluetooth-dev");
            }
            throw bluetoothStateException;
        }

        final BluetoothServer bluetoothServer = new BluetoothServer();
        while (true) {
            bluetoothServer.start();
        }

    }

    private static final int FORWARD = 'f';
    private static final int BACKWARD = 'b';
    private static final int STOP = 's';

    private Robot robot;

    private void start() throws Exception {
        robot = new Robot();

        final UUID uuid = new UUID("2d26618601fb47c28d9f10b8ec891363", false);
        final String connectionUrl = "btspp://localhost:" + uuid + ";name=remote control server";

        try  {
            final StreamConnectionNotifier streamConnNotifier = (StreamConnectionNotifier) Connector.open(connectionUrl);
            System.out.println("Server is started.");
            
            // Connection opened with a client
            final StreamConnection connection = streamConnNotifier.acceptAndOpen();

            // Read client commands
            readRemoteClientCommands(connection);

            // End of the conversation
            streamConnNotifier.close();

        } catch (final ServiceRegistrationException serviceRegistrationException) {
            if (serviceRegistrationException.getMessage().contains("Can not open SDP session. [2] No such file or directory")) {
                System.err.println("Run this command:\n\tcat /etc/systemd/system/bluetooth.target.wants/bluetooth.service | grep ExecStart");
                System.err.println("It should be as follow (usually add the -C option):\n\tExecStart=/usr/lib/bluetooth/bluetoothd -C");
                System.err.println("Once done, run the two following commands:");
                System.err.println("\tsudo systemctl daemon-reload");
                System.err.println("\tsudo systemctl restart bluetooth");
            } else if (serviceRegistrationException.getMessage().contains("Can not open SDP session. [13] Permission denied")) {
                System.err.println("Run this command:\n\tsudo chmod 777 /var/run/sdp");
            }
            throw serviceRegistrationException;
        }
    }

    private void readRemoteClientCommands(StreamConnection connection) {
        final RemoteDevice dev = RemoteDevice.getRemoteDevice(connection);
        System.out.println("Remote device address: " + dev.getBluetoothAddress());
        System.out.println("Remote device name: " + dev.getFriendlyName(true));

        final InputStream inStream = connection.openInputStream();
        boolean loop = true;
        while (loop) {
            final int read = inStream.read();
            switch (read) {
                case FORWARD:
                    System.out.println("FORWARD");
                    robot.mousePress(InputEvent.BUTTON1_MASK);
                    robot.mouseRelease(InputEvent.BUTTON1_MASK);
                    break;
                case BACKWARD:
                    System.out.println("BACKWARD");
                    robot.keyPress(KeyEvent.VK_UP);
                    robot.keyRelease(KeyEvent.VK_UP);
                    break;
                case STOP:
                    System.out.println("STOP");
                    robot.keyPress(KeyEvent.VK_ESCAPE);
                    robot.keyRelease(KeyEvent.VK_ESCAPE);
                    loop = false;
                    break;
                default:
                    System.out.println("IGNORED");
                    break;
            }
            try {
                Thread.sleep(1000);
            } catch (final InterruptedException e) {
            }
        }
    }

}
