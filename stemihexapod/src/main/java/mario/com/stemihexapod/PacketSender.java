package mario.com.stemihexapod;

import java.io.OutputStream;

/**
 * @author Mario
 */

public class PacketSender {
    public interface PacketSenderDelegate{
        void connectionLost();
        void connectionActive();
    }

    public Hexapod hexapod;
    public int sendingInterval = 100;
    public OutputStream outputStream;
    public Boolean openCommunication = true;
    public Boolean connected = false;
    public int counter = 0;

    public void init(Hexapod hexapod){
        this.hexapod = hexapod;
    }

    public void init(Hexapod hexapod, int sendingInterval){
        this.hexapod = hexapod;
        this.sendingInterval = sendingInterval;
    }

    public void startSendingData(){

    }

    public void stopSendingData(){
        this.openCommunication = false;
    }
    private void dropConnection(){
        this.connected = false;
        this.stopSendingData();
    }
}
