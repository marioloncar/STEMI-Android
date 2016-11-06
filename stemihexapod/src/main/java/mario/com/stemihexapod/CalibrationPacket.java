package mario.com.stemihexapod;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Mario on 06/11/2016.
 */

class CalibrationPacket {
    public int writeToHexapod = 0;
    public byte[] legsValues = {50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50};
    public ByteArrayOutputStream outputStream;

    public byte[] toByteArray() {

        this.outputStream = new ByteArrayOutputStream() {
        };
        try {
            outputStream.write("LIN".getBytes());
            outputStream.write(legsValues);
            outputStream.write(writeToHexapod);
        } catch (IOException ignored) {
        }

        return outputStream.toByteArray();
    }

}
