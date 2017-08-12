package com.unibo.s3.main_system.rendering;

/**
 *
 * @author mvenditto
 * */
public final class ScaleUtils {

    private static final int PIXELS_PER_METER = 50;
    private static final float METERS_PER_PIXEL = 1.0f / PIXELS_PER_METER;

    public static float getMetersPerPixel() {
        return METERS_PER_PIXEL;
    }

    public static int getPixelsPerMeter() {
        return PIXELS_PER_METER;
    }

    public static float pixelsToMeters (int pixels) {
        return (float)pixels * METERS_PER_PIXEL;
    }

    public static int metersToPixels (float meters) {
        return (int)(meters * PIXELS_PER_METER);
    }

}
