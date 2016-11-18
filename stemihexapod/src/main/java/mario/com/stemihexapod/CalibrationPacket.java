package mario.com.stemihexapod;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Mario on 06/11/2016.
 */

class CalibrationPacket {

    public enum WriteData {
        No,
        Yes
    }

    public int writeToHexapod = WriteData.No.ordinal();
    public byte[] legsValues = {50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50};
    public ByteArrayOutputStream outputStream;

    public byte[] toByteArray() {
        byte[] pkt = "LIN".getBytes();
        this.outputStream = new ByteArrayOutputStream() {
        };
        try {
            outputStream.write(pkt);
            outputStream.write(legsValues);
            outputStream.write(writeToHexapod);
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }

        return outputStream.toByteArray();
    }

}
