package com.ankuranurag2.smartband.Utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by ankur on 18/4/18.
 */

public class ExtraFile {
    TextView myLabel;
    EditText myTextbox;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;

    void openBT() throws IOException
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

        beginListenForData();

        myLabel.setText("Bluetooth Opened");
    }

    void beginListenForData()
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            myLabel.setText(data);
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    void sendData() throws IOException
    {
        String msg = myTextbox.getText().toString();
        msg += "\n";
        mmOutputStream.write(msg.getBytes());
        myLabel.setText("Data Sent");
    }

    void closeBT() throws IOException
    {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
        myLabel.setText("Bluetooth Closed");
    }
}





















//private class ConnectedThread extends Thread {
//    private final InputStream mmInStream;
//
//    private ConnectedThread(BluetoothSocket socket) {
//        InputStream tmpIn = null;
//
//        try {
//            tmpIn = socket.getInputStream();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        mmInStream = tmpIn;
//    }
//
//    public void run() {
//        byte[] buffer = new byte[256];  // buffer store for the stream
//        int bytes; // bytes returned from read()
//
//        // Keep listening to the InputStream
//        while (true) {
//            try {
//                // Read from the InputStream
//                bytes = mmInStream.read(buffer);
//                handler.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();
//            } catch (IOException e) {
//                break;
//            }
//        }
//    }
//}

//        handler = new Handler() {
//public void handleMessage(android.os.Message msg) {
//        switch (msg.what) {
//        case RECIEVE_MESSAGE:
//        byte[] readBuf = (byte[]) msg.obj;
//        String strIncom = new String(readBuf, 0, msg.arg1);
//        builder.append(strIncom);
//        int endOfLineIndex = builder.indexOf("\r\n");
//        if (endOfLineIndex > 0) {
//        String sbprint = builder.substring(0, endOfLineIndex);
//        builder.delete(0, builder.length());
////                            txtArduino.setText("Data from Arduino: " + sbprint);
////                            btnOff.setEnabled(true);
////                            btnOn.setEnabled(true);
//        }
//        //Log.d(TAG, "...String:"+ builder.toString() +  "Byte:" + msg.arg1 + "...");
//        break;
//        }
//        }
//        };
