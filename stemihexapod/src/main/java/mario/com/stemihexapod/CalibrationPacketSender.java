package mario.com.stemihexapod;

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
    private byte[] calibrationArray = {50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50};
    private boolean openCommunication = true;

    CalibrationPacketSender(Hexapod hexapod) {
        this.hexapod = hexapod;
    }


    void enterCalibrationMode(EnterCalibrationCallback enterCalibrationCallback) {
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
        enterCalibrationCallback.onEnteredCalibration(true);
        this.sendData();
    }

    private void sendData() {
        try {
            Socket socket = new Socket(this.hexapod.ipAddress, this.hexapod.port);
            OutputStream outputStream = socket.getOutputStream();
            BufferedOutputStream buffer = new BufferedOutputStream(outputStream, 30);

            while (this.openCommunication) {
                int sendingInterval = 100;
                Thread.sleep(sendingInterval);
                buffer.write(this.hexapod.calibrationPacket.toByteArray());
                buffer.flush();
                System.out.println("CALIBRATION -> " + Arrays.toString(this.hexapod.calibrationPacket.toByteArray()));
                this.connected = true;
            }
            socket.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            this.dropConnection();
        }

    }

    void sendOnePacket() {
        try {
            Socket socket = new Socket(this.hexapod.ipAddress, this.hexapod.port);
            OutputStream outputStream = socket.getOutputStream();

            BufferedOutputStream buffOutStream = new BufferedOutputStream(outputStream, 30);
            buffOutStream.write(this.hexapod.calibrationPacket.toByteArray());
            buffOutStream.flush();

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void stopSendingData() {
        this.openCommunication = false;
    }

    private void dropConnection() {
        this.connected = false;
        this.stopSendingData();
    }
}
