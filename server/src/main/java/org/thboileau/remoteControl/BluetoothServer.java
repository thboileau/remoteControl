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
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BluetoothServer {

    private static final ResourceBundle messages = ResourceBundle.getBundle("messages");
    final Map<Integer, Command> commandsByCharacter = Arrays.stream(Command.values())
            .collect(Collectors.toMap(Command::getCharacter, Function.identity()));

    public static void main(final String[] args) throws Exception {
        checkLocalDevice();

        final BluetoothServer bluetoothServer = new BluetoothServer();
        while (true) {
            bluetoothServer.start();
        }
    }

    private void start() throws Exception {

        final UUID uuid = new UUID("2d26618601fb47c28d9f10b8ec891363", false);
        final String connectionUrl = "btspp://localhost:" + uuid + ";name=remote control server";

        try {
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
                System.err.println(messages.getString("ERROR_CANT_OPEN_SDP_SESSION_NO_SUCH_FILE"));
            } else if (serviceRegistrationException.getMessage().contains("Can not open SDP session. [13] Permission denied")) {
                System.err.println(messages.getString("ERROR_CANT_OPEN_SDP_SESSION_PERMISSION_DENIED"));
            }
            throw serviceRegistrationException;
        }
    }

    private void readRemoteClientCommands(StreamConnection connection) throws Exception {
        Robot robot = new Robot();

        final RemoteDevice remoteDevice = RemoteDevice.getRemoteDevice(connection);
        System.out.println(String.format("Connected to remote device:\n\t%s\n\t%s", remoteDevice.getBluetoothAddress(), remoteDevice.getFriendlyName(true)));

        final InputStream inStream = connection.openInputStream();
        ConversationStatus loop = ConversationStatus.CONTINUE;
        while (ConversationStatus.CONTINUE == loop) {
            final int read = inStream.read();
            loop = Optional.ofNullable(commandsByCharacter.get(read))
                    .map(command -> command.runCommand(robot))
                    .orElse(ConversationStatus.CONTINUE);
        }
    }

    private static void checkLocalDevice() throws BluetoothStateException {
        try {
            final LocalDevice localDevice = LocalDevice.getLocalDevice();
            System.out.println(String.format("Local device: %s (%s)", localDevice.getFriendlyName(), localDevice.getBluetoothAddress()));
        } catch (final BluetoothStateException bluetoothStateException) {
            if (bluetoothStateException.getMessage().contains("not available")) {
                System.err.println(messages.getString("ERROR_UNAVAILABLE_DEVICE"));
            }
            throw bluetoothStateException;
        }
    }

    private enum ConversationStatus {
        CONTINUE, STOP
    }

    private enum Command {
        FORWARD('f', robot -> {
            robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
            return ConversationStatus.CONTINUE;
        }),
        BACKWARD('b', robot -> {
            robot.keyPress(KeyEvent.VK_UP);
            robot.keyRelease(KeyEvent.VK_UP);
            return ConversationStatus.CONTINUE;
        }),
        STOP('s', robot -> {
            robot.keyPress(KeyEvent.VK_ESCAPE);
            robot.keyRelease(KeyEvent.VK_ESCAPE);
            return ConversationStatus.STOP;
        });

        private final int character;
        private final Function<Robot, ConversationStatus> command;

        Command(final int character, final Function<Robot, ConversationStatus> command) {
            this.character = character;
            this.command = command;
        }

        public ConversationStatus runCommand(Robot robot) {
            return command.apply(robot);
        }

        public int getCharacter() {
            return character;
        }
    }

}
