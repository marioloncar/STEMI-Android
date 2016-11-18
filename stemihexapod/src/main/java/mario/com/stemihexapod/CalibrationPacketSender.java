package mario.com.stemihexapod;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

/**
 * Created by Mario on 10/11/2016.
 */

class CalibrationPacketSender {

    public Hexapod hexapod;
    public int sendingInterval = 100;
    public Boolean connected = false;
    public byte[] calibrationArray = {50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 0};
    public boolean openCommunication = true;

    public CalibrationPacketSender(Hexapod hexapod) {
        this.hexapod = hexapod;
    }


    public void enterCalibrationMode() {
        ByteArrayOutputStream baos = null;
        try {
            URL url = new URL("http://" + this.hexapod.ipAddress + "/linearization.bin");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();

            baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            byte[] data = new byte[4096];
            int count = conn.getInputStream().read(data);
            while (count != -1) {
                dos.write(data, 3, 18);
                count = conn.getInputStream().read(data);

            }
        } catch (IOException ignored) {
        }
        this.calibrationArray = baos.toByteArray();
        this.sendData();
    }

    public void sendData() {
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

    public void sendOnePacket() {
        try {
            Socket socket = new Socket(this.hexapod.ipAddress, this.hexapod.port);
            OutputStream outputStream = socket.getOutputStream();
            BufferedOutputStream buffOutStream = new BufferedOutputStream(outputStream, 30);
            buffOutStream.write(this.hexapod.calibrationPacket.toByteArray());
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stopSendingData() {
        this.openCommunication = false;
    }

    public void dropConnection() {
        this.connected = false;
        this.stopSendingData();
    }
}
