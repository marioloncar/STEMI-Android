package mario.com.stemihexapod;

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
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Mario
 */

class PacketSender {
    public Hexapod hexapod;
    public int sendingInterval = 100;
    public Boolean connected = false;
    public PacketSenderInterface packetSenderInterface;

    public PacketSender(Hexapod hexapod) {
        this.hexapod = hexapod;
    }

    public PacketSender(Hexapod hexapod, int sendingInterval) {
        this.hexapod = hexapod;
        this.sendingInterval = sendingInterval;
    }

    public void startSendingData() {
        try {
            URL url = new URL("http://" + this.hexapod.ipAddress + "/stemiData.json");
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
                    this.sendData();
//                    this.packetSenderInterface.connectionActive();
                } else {
                    this.stopSendingData();
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            this.stopSendingData();
        }
    }

    public void sendData() {
        this.connected = true;

        try {
            Socket socket = new Socket(this.hexapod.ipAddress, this.hexapod.port);
            OutputStream outputStream = socket.getOutputStream();
            BufferedOutputStream buffer = new BufferedOutputStream(outputStream, 30);

            while (this.connected) {
                try {
                    Thread.sleep(sendingInterval);
                    buffer.write(this.hexapod.currentPacket.toByteArray());
                    buffer.flush();
                    System.out.println("BUFFER -> " + Arrays.toString(this.hexapod.currentPacket.toByteArray()));
                    this.packetSenderInterface.connectionActive();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
            this.stopSendingData();
        }
    }

    public void stopSendingData() {
        this.connected = false;
        this.packetSenderInterface.connectionLost();
    }

}

