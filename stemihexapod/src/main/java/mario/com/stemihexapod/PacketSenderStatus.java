package mario.com.stemihexapod;

/**
 * Created by Mario on 12/11/2016.
 */

interface PacketSenderStatus {
    void connectionLost();
    void connectionActive();
}
