package mario.com.stemihexapod;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Mario on 06/11/2016.
 */

class CalibrationPacket {

    enum WriteData {
        No,
        Yes
    }

    int writeToHexapod = WriteData.No.ordinal();
    byte[] legsValues = {50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50};

    byte[] toByteArray() {
        byte[] pkt = "LIN".getBytes();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream() {
        };
        try {
            outputStream.write(pkt);
            outputStream.write(legsValues);
            outputStream.write(writeToHexapod);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }

}
