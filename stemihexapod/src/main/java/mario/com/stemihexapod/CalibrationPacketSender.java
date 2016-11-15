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
        this.connected = true;

        try {
            Socket socket = new Socket(this.hexapod.ipAddress, this.hexapod.port);
            OutputStream outputStream = socket.getOutputStream();
            BufferedOutputStream buffer = new BufferedOutputStream(outputStream, 30);

            while (this.connected) {
                Thread.sleep(sendingInterval);
                buffer.write(this.hexapod.calibrationPacket.toByteArray());
                buffer.flush();
            }
            socket.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            this.stopSendingData();
        }

    }

    public void sendOnePacket() {

    }

    public void stopSendingData() {
        this.connected = false;
    }
}
