package mario.com.stemihexapod;

/**
 * @author Mario
 */


public class Hexapod implements PacketSenderInterface {
    public Packet currentPacket;
    public PacketSender sendPacket;
    public String ipAddress;
    public int port;
    public CalibrationPacket calibrationPacket;
    public CalibrationPacketSender calibrationPacketSender;
    public byte[] slidersArray = {50, 25, 0, 0, 0, 50, 0, 0, 0, 0, 0};
    public boolean calibrationModeEnabled = false;
    public byte[] initialCalibrationData = {50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50};
    public HexapodInterface hexapodInterface = new HexapodInterface() {
        @Override
        public void connectionStatus(boolean isConnected) {

        }
    };

    @Override
    public void connectionLost() {
        hexapodInterface.connectionStatus(false);
        System.out.println("IZGUBLJENA KONEKCIJA");
    }

    @Override
    public void connectionActive() {
        hexapodInterface.connectionStatus(true);
        System.out.println("KONEKCIJA AKTIVNA");
    }


    public enum WalkingStyle {
        TripodGait,
        TripodGaitAngled,
        TripodGaitStar,
        WaveGait
    }

    /**
     * Initializes default connection with IP address: 192.168.4.1 and port: 80
     */

    public Hexapod() {
        this.ipAddress = "192.168.4.1";
        this.port = 80;
        this.currentPacket = new Packet();
    }

    /**
     * Initializes default connection with IP address: 192.168.4.1 and port: 80. Includes calibration mode.
     *
     * @param withCalibrationMode Takes true or false if calibration mode should be enabled.
     */

    public Hexapod(boolean withCalibrationMode) {
        this.calibrationModeEnabled = withCalibrationMode;
        this.ipAddress = "192.168.4.1";
        this.port = 80;
        if (calibrationModeEnabled)
            this.calibrationPacket = new CalibrationPacket();
        else
            this.currentPacket = new Packet();
    }

    /**
     * Initializes connection with custom IP address and port.
     *
     * @param ip   Takes given IP Address (default: 192.168.4.1)
     * @param port Takes given port (default: 80)
     */

    public Hexapod(String ip, int port) {
        this.ipAddress = ip;
        this.port = port;
        this.currentPacket = new Packet();
    }

    /**
     * Sets new IP address. By default this is set to 192.168.4.1
     *
     * @param newIpAddress Takes given IP address
     */

    public void setIpAddress(String newIpAddress) {
        this.ipAddress = newIpAddress;
    }

    /**
     * Establish connection with Hexapod. After connection is established, it sends new packet every 100 ms.
     */

    public void connect() {
        if (calibrationModeEnabled) {
            calibrationPacketSender = new CalibrationPacketSender(this);
            calibrationPacketSender.enterCalibrationMode(); //check if you entered calibration mode!!!
            this.initialCalibrationData = this.calibrationPacket.legsValues;
        } else {
            sendPacket = new PacketSender(this);
            sendPacket.packetSenderInterface = this;
            sendPacket.startSendingData();
        }
    }

    /**
     * Stop sending data to Hexapod and closes connection.
     */


    public void disconnect() {
        if (calibrationModeEnabled) {
            calibrationPacketSender.stopSendingData();
        } else {
            sendPacket.stopSendingData();
        }

    }

    /**
     * Moves Hexapod forward with max power.
     */

    public void goForward() {
        stopMoving();
        currentPacket.power = 100;
    }

    /**
     * Moves Hexapod backwards with max power.
     */

    public void goBackward() {
        stopMoving();
        currentPacket.power = 100;
        currentPacket.angle = 90;
    }

    /**
     * Moves Hexapod left with max power.
     */

    public void goLeft() {
        stopMoving();
        currentPacket.power = 100;
        currentPacket.angle = 210;
    }

    /**
     * Moves Hexapod right with max power.
     */

    public void goRight() {
        stopMoving();
        currentPacket.power = 100;
        currentPacket.angle = 45;
    }

    /**
     * Rotate Hexapod left with max power.
     */

    public void turnLeft() {
        stopMoving();
        currentPacket.rotation = 156;
    }

    /**
     * Rotate Hexapod right with max power.
     */

    public void turnRight() {
        stopMoving();
        currentPacket.rotation = 100;
    }

    /**
     * Turns orientation mode on and tilt Hexapod forward.
     */

    public void tiltForward() {
        setOrientationMode();
        currentPacket.accelerometerX = 226;
    }

    /**
     * Turns orientation mode on and tilt Hexapod backwards.
     */

    public void tiltBackward() {
        setOrientationMode();
        currentPacket.accelerometerX = 30;
    }

    /**
     * Turns orientation mode on and tilt Hexapod left.
     */

    public void tiltLeft() {
        setOrientationMode();
        currentPacket.accelerometerY = 226;
    }

    /**
     * Turns orientation mode on and tilt Hexapod right.
     */

    public void tiltRight() {
        setOrientationMode();
        currentPacket.accelerometerY = 30;
    }

    /**
     * Sets parameters for moving Hexapod with custom Joystick. This is intended for moving the Hexapod: forward, backward, left and right.
     * <p>
     * It is proposed for user to use a circular joystick!
     * <p>
     * angle values: Because Byte values are only positive numbers from 0 to 255, Hexapod gets angle as shown:
     * For angle 0 - 180 you can use 0-90 (original divided by 2)
     * For angle 180 - 360 you can use 166-255 (this can be represented like value from -180 to 0. Logic is same: 255 + (original devided by 2))
     *
     * @param power Takes values for movement speed (Values must be: 0-100)
     * @param angle Takes values for angle of moving (Values can be: 0-255, look at the description!)
     */

    public void setJoystickParams(int power, int angle) {
        currentPacket.power = power;
        currentPacket.angle = angle;
    }

    /**
     * Sets parameters for moving Hexapod with custom Joystick. This is intended for rotating the Hexapod left and right.
     * <p>
     * It is proposed for user to use a linear (left to right) joystick!
     * <p>
     * angle values: Because Byte values are only positive numbers from 0 to 255, Hexapod gets rotation as shown:
     * For rotate to right you can use values 0 - 100
     * For rotate to left you can use 255-156 (this can be represented like value from 0 to -100 as 255 - position)
     *
     * @param rotation Takes values for rotation speed (Values must be: 0-255, look at the description!)
     */

    public void setJoystickParams(int rotation) {
        currentPacket.rotation = rotation;
    }

    /**
     * Sets parameters for tilding Hexapod in X direction.
     * <p>
     * This value must be max 40!
     * <p>
     * x values: Because Byte values are only positive numbers from 0 to 255, Hexapod gets x rotation as shown:
     * For tilt forward you can use values 0 - 216 (this can be represented like value from 0 to -100 as 255 - position)
     * For tilt backward you can use 0 - 100.
     *
     * @param x Takes values for X tilting (Values must be: 0-255, look at the description!)
     */

    public void setAccelerometerX(int x) {
        currentPacket.accelerometerX = x;
    }

    /**
     * Sets parameters for tilding Hexapod in Y direction.
     * <p>
     * This value must be max 40!
     * <p>
     * y values: because Byte values are only positive numbers from 0 to 255, Hexapod gets y rotation as shown:
     * For tilt left you can use values 0 - 216 (this can be represented like value from 0 to -100 as 255 - position.)
     * For tilt right you can use 0 - 100.
     *
     * @param y Takes values for Y tilting (Values must be: 0-255, look at the description!)
     */

    public void setAccelerometerY(int y) {
        currentPacket.accelerometerY = y;
    }

    /**
     * Stops Hexapod by setting power, angle and rotation to 0.
     */

    public void stopMoving() {
        currentPacket.power = 0;
        currentPacket.angle = 0;
        currentPacket.rotation = 0;
    }

    /**
     * Resets all Hexapod moving and tilt values to 0.
     */

    public void resetMovingParams() {
        currentPacket.power = 0;
        currentPacket.angle = 0;
        currentPacket.rotation = 0;
        currentPacket.staticTilt = 0;
        currentPacket.movingTilt = 0;
        currentPacket.onOff = 1;
        currentPacket.accelerometerX = 0;
        currentPacket.accelerometerY = 0;
    }

    /**
     * In this mode, Hexapod can move forward, backwards, left and right, and it can rotate itself to left and right.
     * Accelerometer is off.
     */

    public void setMovementMode() {
        currentPacket.staticTilt = 0;
        currentPacket.movingTilt = 0;
    }

    /**
     * In this mode, Hexapod can tilt backward, forward, left and right, and rotate left and right by accelerometer and joystick in place without moving.
     * Accelerometer is on.
     */

    public void setRotationMode() {
        currentPacket.staticTilt = 1;
        currentPacket.movingTilt = 0;
    }

    /**
     * This is combination of rotation and movement mode, Hexapod can move forward, backward, left and right, and it can rotate itself to left and right.
     * Furthermore the Hexapod can tilt forward, backward, left and right by accelerometer.
     * Accelerometer is on.
     */

    public void setOrientationMode() {
        currentPacket.staticTilt = 0;
        currentPacket.movingTilt = 1;
    }

    /**
     * Puts Hexapod in standby.
     */

    public void turnOn() {
        currentPacket.onOff = 1;
    }

    /**
     * Puts Hexapod out from standby.
     */

    public void turnOff() {
        currentPacket.onOff = 0;
    }

    /**
     * Set Hexapod height.
     *
     * @param height This value can be from 0 to 100.
     */

    public void setHeight(int height) {
        currentPacket.height = height;
    }

    /**
     * Set Hexapod walking style.
     *
     * @param style This value can be TripodGait, TripodGaitAngled, TripodGaitStar or WaveGait.
     */

    public void setWalkingStyle(WalkingStyle style) {
        int walkingStyleValue;
        switch (style) {
            case TripodGait:
                walkingStyleValue = 30;
                break;
            case TripodGaitAngled:
                walkingStyleValue = 60;
                break;
            case TripodGaitStar:
                walkingStyleValue = 80;
                break;
            case WaveGait:
                walkingStyleValue = 100;
                break;
            default:
                walkingStyleValue = 30;
        }
        currentPacket.walkingStyle = walkingStyleValue;
    }

    /**
     * Sets value of Hexapod leg at given index.
     *
     * @param value Value of Hexapod motor.
     * @param index Index of Hexapod motor.
     */

    public void setValue(byte value, int index) {
        if (value >= 0 && value <= 100)
            calibrationPacket.legsValues[index] = value;
        else
            throw new IndexOutOfBoundsException("Value out of bounds");
    }

    /**
     * Increase legs value at given index.
     *
     * @param index Takes leg index.
     */

    public void increaseValueAtIndex(int index) {
        if (calibrationPacket.legsValues[index] < 100)
            calibrationPacket.legsValues[index]++;
    }

    /**
     * Decrease legs value at given index.
     *
     * @param index Takes leg index.
     */

    public void decreaseValueAtIndex(int index) {
        if (calibrationPacket.legsValues[index] > 0)
            calibrationPacket.legsValues[index]--;
    }

    /**
     * Writes new calibration values to Hexapod
     *
     * @throws InterruptedException
     */

    public void writeDataToHexapod() throws InterruptedException {
        calibrationPacketSender.stopSendingData();
        Thread.sleep(500);
        calibrationPacket.writeToHexapod = CalibrationPacket.WriteData.Yes.ordinal();
        calibrationPacketSender.sendOnePacket();
        calibrationPacket.writeToHexapod = CalibrationPacket.WriteData.No.ordinal();
        Thread.sleep(1000);
    }

    /**
     * @return initial array of calibration values stored on Hexapod.
     */
    public byte[] fetchDataFromHexapod() {
        return initialCalibrationData;
    }
}

