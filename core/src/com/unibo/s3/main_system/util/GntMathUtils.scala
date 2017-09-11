package com.unibo.s3.main_system.util

object GntMathUtils {

  def keepInRange(v: Float, min: Float, max: Float): Float = {
    println(v, min, max, if (v < min) v else if(v > max) max else v)
    if (v < min) min else if(v > max) max else v
  }
}

