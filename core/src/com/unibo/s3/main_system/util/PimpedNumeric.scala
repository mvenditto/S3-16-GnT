package com.unibo.s3.main_system.util

object PimpedNumeric {

  implicit class compareFloatWithTolerance(f: Float) {
    def ~==(other: Float, epsilon: Float): Boolean =
      (f - other).abs < epsilon
  }
}

object PimpedNumericTest extends App {

  import PimpedNumeric._
  println(31.499874f ~==(31.5f, 0.001f))

}
