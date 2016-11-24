package com.stemi.STEMIHexapod;

/**
 * Created by Mario on 23/11/2016.
 */

public interface JoystickMovement {
    void leftJoystickMoved(int power, int angle);
    void rightJoystickMoved(int rotation);
}
