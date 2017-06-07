package com.stemi.STEMIHexapod.interfaces;

/**
 * Created by Mario on 23/11/2016.
 */

public interface JoystickMovement {
    void leftJoystickMoved(int power, int angle);
    void rightJoystickMoved(int rotation);
}
