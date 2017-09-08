package com.unibo.s3.main_system.util

import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.characters.steer.CustomLocation

import scala.collection.JavaConversions._

object GdxImplicits {

  implicit def Vector2ToCustomLocation(v: Vector2): CustomLocation =
    new CustomLocation(v)

  implicit class AugmentedGdxArray[T](a: com.badlogic.gdx.utils.Array[T]) {
    def asScalaIterable: Iterable[T] = a.iterator().toIterable
  }

  implicit class AugmentedIterable[T](i: Iterable[T]) {

    def asGdxArray: com.badlogic.gdx.utils.Array[T] = {

      val gdxArray = new com.badlogic.gdx.utils.Array[T]()
      i.foreach(elem => gdxArray.add(elem))
      gdxArray
    }
  }
}
