package mario.com.stemihexapod;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Mario on 23/10/2016.
 */

class Packet {
    public int power = 0;
    public int angle = 0;
    public int rotation = 0;
    public byte staticTilt = 0;
    public byte movingTilt = 0;
    public byte onOff = 1;
    public int accelerometerX = 0;
    public int accelerometerY = 0;
    public int height = 50;
    public int walkingStyle = 0;
    public byte[] slidersArray = {0, 0, 0, 50, 0, 0, 0};

    public ByteArrayOutputStream outputStream;

    public byte[] toByteArray() {
        this.outputStream = new ByteArrayOutputStream() {
        };

        try {
            this.outputStream.write("PKT".getBytes());
            this.outputStream.write(power);
            this.outputStream.write(angle);
            this.outputStream.write(rotation);
            this.outputStream.write(staticTilt);
            this.outputStream.write(movingTilt);
            this.outputStream.write(onOff);
            this.outputStream.write(accelerometerX);
            this.outputStream.write(accelerometerY);
            this.outputStream.write(height);
            this.outputStream.write(walkingStyle);
            this.outputStream.write(slidersArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }
}
