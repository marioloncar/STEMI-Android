package mario.com.stemihexapod;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.Objects;

/**
 * Created by Mario on 23/10/2016.
 */

class PacketSender {
    public Hexapod hexapod;
    private int sendingInterval = 100;
    private Boolean connected = false;
    PacketSenderStatus packetSenderStatus;
    private boolean openCommunication = true;

    PacketSender(Hexapod hexapod) {
        this.hexapod = hexapod;
    }

    PacketSender(Hexapod hexapod, int sendingInterval) {
        this.hexapod = hexapod;
        this.sendingInterval = sendingInterval;
    }

    void startSendingData() {
        Thread thread = new Thread(){
            @Override
            public void run(){
                try {
                    URL url = new URL("http://" + hexapod.ipAddress + "/stemiData.json");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(3000);
                    connection.setUseCaches(false);
                    connection.connect();

                    InputStream stream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuilder buffer = new StringBuilder();
                    String line = "";

                    while ((line = reader.readLine()) != null) {
                        buffer.append(line).append("\n");
                    }

                    String bufferNew = buffer.toString();
                    if (bufferNew != null) {
                        JSONObject jsonObject = new JSONObject(bufferNew);
                        if (Objects.equals(jsonObject.getBoolean("isValid"), true)) {
                            sendData();
                        } else {
                            dropConnection();
                        }
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    dropConnection();
                }
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
                try {
                    Thread.sleep(sendingInterval);
                    buffer.write(this.hexapod.currentPacket.toByteArray());
                    buffer.flush();
                    this.packetSenderStatus.connectionActive();
                    this.connected = true;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    this.dropConnection();
                }
            }
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
            this.dropConnection();
        }
    }

    void stopSendingData() {
        this.openCommunication = false;
    }

    private void dropConnection() {
        this.connected = false;
        this.packetSenderStatus.connectionLost();
        this.stopSendingData();
    }

}

