package mario.com.stemihexapod;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;

/**
 * Created by Mario on 10/11/2016.
 */

class CalibrationPacketSender {

    private Hexapod hexapod;
    private Boolean connected = false;
    private byte[] calibrationArray = {};
    private boolean openCommunication = true;
    private int sendingInterval = 100;

    CalibrationPacketSender(Hexapod hexapod) {
        this.hexapod = hexapod;
    }


    void enterCalibrationMode(final EnterCalibrationCallback enterCalibrationCallback)  {
        Thread thread = new Thread() {
            @Override
            public void run() {
                ByteArrayOutputStream baos = null;
                try {
                    URL url = new URL("http://" + hexapod.ipAddress + "/linearization.bin");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(3000);
                    connection.setUseCaches(false);
                    connection.connect();

                    baos = new ByteArrayOutputStream();
                    DataOutputStream dos = new DataOutputStream(baos);
                    byte[] data = new byte[1024];
                    int count = connection.getInputStream().read(data);
                    while (count != -1) {
                        dos.write(data, 3, 18);
                        count = connection.getInputStream().read(data);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                calibrationArray = baos.toByteArray();
                for (int i = 0; i < calibrationArray.length; i++) {
                    hexapod.setCalibrationValue(calibrationArray[i], i);
                }
                enterCalibrationCallback.onEnteredCalibration(true);
                sendData();
            }
        };
        thread.start();
    }

    private void sendData() {
        try {
            Socket socket = new Socket(this.hexapod.ipAddress, this.hexapod.port);
            OutputStream outputStream = socket.getOutputStream();
            BufferedOutputStream buffer = new BufferedOutputStream(outputStream, 30);

            while (this.openCommunication) {
                Thread.sleep(sendingInterval);
                buffer.write(this.hexapod.calibrationPacket.toByteArray());
                buffer.flush();
                this.connected = true;
            }
            socket.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            this.dropConnection();
        }

    }

    void sendOnePacket() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(hexapod.ipAddress, hexapod.port);
                    OutputStream outputStream = socket.getOutputStream();
                    BufferedOutputStream buffOutStream = new BufferedOutputStream(outputStream, 30);
                    buffOutStream.write(hexapod.calibrationPacket.toByteArray());
                    buffOutStream.flush();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    void stopSendingData() {
        this.openCommunication = false;
    }

    private void dropConnection() {
        this.connected = false;
        this.stopSendingData();
    }
}
