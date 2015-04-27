/**
 * The bluetooth-arduino connection part uses code from the following site
 * https://bellcode.wordpress.com/2012/01/02/android-and-arduino-bluetooth-communication/
 */

package pegasus.bluetootharduino;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.*;
import android.os.Handler;

public class Bluetooth {

    BluetoothAdapter adapter;
    BluetoothDevice MiDevice;
    BluetoothSocket socket;
    InputStream in;
    OutputStream out;

    boolean btEnabled = true;
    String returnResult;

    Thread BlueToothThread;
    boolean stop = false;
    int position;
    byte read[];
    Netstrings nt = new Netstrings();


    //connect
    public void checkBT() {

        //check if bluetooth on device is enabled
        adapter = BluetoothAdapter.getDefaultAdapter();

        if (!adapter.isEnabled()) {
            btEnabled = false;
        }

        // pairs device
        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
        if(pairedDevices.size() > 0) {
            for(BluetoothDevice device : pairedDevices) {
                if(device.getName().equals("HC-06")) {
                    MiDevice = device;
                    // data.setText("device paired");
                    break;
                }
            }
        }
    }

    public void runBT() throws IOException, NullPointerException {

        //opens connection
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID

        socket = MiDevice.createRfcommSocketToServiceRecord(uuid);


        socket.connect();
        out = socket.getOutputStream();
        in = socket.getInputStream();
//      data.setText("connection established");

        //gets data
        final Handler handler = new Handler();
        final byte delimiter = 10;

        stop = false;
        position = 0;
        read = new byte[1024];
        BlueToothThread = new Thread(new Runnable() {

            public void run() {

                while(!Thread.currentThread().isInterrupted() && !stop) {

                    try {

                        int bytesAvailable = in.available();
                        if(bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            in.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++) {
                                byte b = packetBytes[i];
                                if(b == delimiter) {
                                    byte[] encodedBytes = new byte[position];
                                    System.arraycopy(read, 0, encodedBytes, 0, encodedBytes.length);
                                    // final String result = new String(encodedBytes, "US-ASCII");
                                    final String result = "hi";
                                    position = 0;

                                    handler.post(new Runnable() {
                                        public void run() {
                                            returnResult = nt.decodedNetstring(result);
                                        }
                                    });

                                } else {
                                    read[position++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex) {
                        stop = true;
                    }
                }
            }
        });

        BlueToothThread.start();

    }

    public void connect() {
        try {
            runBT();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void send() {

        try {
//			String text = nt.encodedNetstring(input.getText().toString());
            String text = nt.encodedNetstring("hello".toString());
            if(socket.isConnected()) {
                out.write(text.getBytes());
            }
//			data.setText("message sent");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {

        try {
            stop = true;
            out.close();
            in.close();
            socket.close();
//		    data.setText("Connection Closed");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}