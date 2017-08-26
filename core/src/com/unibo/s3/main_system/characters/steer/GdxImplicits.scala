package com.unibo.s3.main_system.characters.steer

import scala.collection.JavaConversions._

object GdxImplicits {

  implicit class AugmentedGdxArray[T](a: com.badlogic.gdx.utils.Array[T]) {
    def asScalaIterable: Iterable[T] = {
      a.iterator().toIterable
    }
  }

  implicit class AugmentedIterable[T](i: Iterable[T]) {

    def asGdxArray: com.badlogic.gdx.utils.Array[T] = {

      val gdxArray = new com.badlogic.gdx.utils.Array[T]()
      i.foreach(elem => gdxArray.add(elem))
      gdxArray

    }
  }
}
