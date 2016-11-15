package com.stemi.STEMIHexapod;

/**
 * Created by Mario on 12/11/2016.
 */

public interface JoystickLInterface{
    void leftJoystickMoved(int power, int angle);
    void rightJoystickMoved(int rotation);
}