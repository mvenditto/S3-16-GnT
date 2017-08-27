package com.unibo.s3.main_system.util

/**
 *
 * @author mvenditto
 * */
object ScaleUtils {

    private[this] val PIXELS_PER_METER: Integer = 50
    private[this] val METERS_PER_PIXEL: Float = 1.0f / PIXELS_PER_METER

    def getMetersPerPixel: Float = METERS_PER_PIXEL

    def getPixelsPerMeter: Integer = PIXELS_PER_METER

    def pixelsToMeters(pixels: Integer): Float = pixels * METERS_PER_PIXEL

    def metersToPixels(meters: Float): Integer = (meters * PIXELS_PER_METER).toInt

}
