package mario.com.stemihexapod;

/**
 * Created by Mario on 12/11/2016.
 */

public interface HexapodInterface {
    /**
     * Check if app is connected to STEMI Hexapod.
     * returns True if stemi is connected and sending data. False if it is not connected or not sending data.
     */

    void connectionStatus(boolean isConnected);
}
