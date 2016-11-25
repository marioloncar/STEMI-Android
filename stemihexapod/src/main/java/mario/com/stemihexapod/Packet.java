package mario.com.stemihexapod;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Mario on 23/10/2016.
 */

class Packet {
    int power = 0;
    int angle = 0;
    int rotation = 0;
    byte staticTilt = 0;
    byte movingTilt = 0;
    byte onOff = 1;
    int accelerometerX = 0;
    int accelerometerY = 0;
    int height = 50;
    int walkingStyle = 0;
    private byte[] slidersArray = {0, 0, 0, 50, 0, 0, 0};

    byte[] toByteArray() {
        byte[] pkt = "PKT".getBytes();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream() {
        };

        try {
            outputStream.write(pkt);
            outputStream.write(power);
            outputStream.write(angle);
            outputStream.write(rotation);
            outputStream.write(staticTilt);
            outputStream.write(movingTilt);
            outputStream.write(onOff);
            outputStream.write(accelerometerX);
            outputStream.write(accelerometerY);
            outputStream.write(height);
            outputStream.write(walkingStyle);
            outputStream.write(slidersArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }
}
