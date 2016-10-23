package mario.com.stemihexapod;

/**
 * @author Mario
 */

public class Hexapod {

    /**
     * Public variables
     */
    public String ipAddress;
    public int port;
    public byte[] slidersArray = {0, 0, 0, 50, 0, 0, 0, 0, 0};


    /**
     * Initializes default connection with IP address: 192.168.4.1 and port: 80
     */
    public void init() {
        this.ipAddress = "192.168.4.1";
        this.port = 80;
    }

    /**
     * Initializes connection with custom IP address and port
     *
     * @param ip   Takes given IP Address (default: 192.168.4.1)
     * @param port Takes given port (default: 80)
     */

    public void init(String ip, int port) {
        this.ipAddress = ip;
        this.port = port;
    }

    /**
     * Sets new IP address. By default this is set to 192.168.4.1
     *
     * @param newIP Takes given IP address
     */

    public void set(String newIP) {
        this.ipAddress = newIP;
    }

}
