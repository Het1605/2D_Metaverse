package com.yourproject.youename;

public class DirectionController {
    private static String direction = "";
    private static boolean held = false;

    public static synchronized void setDirection(String dir) {
        direction = dir;
        held = true;
    }

    public static synchronized void stop() {
        held = false;
        direction = "";
    }

    public static synchronized String getDirection() {
        return held ? direction : "";
    }

    public static synchronized boolean isHeld() {
        return held;
    }
}
