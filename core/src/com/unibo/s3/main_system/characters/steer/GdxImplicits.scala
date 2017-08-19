package com.unibo.s3.main_system.characters.steer


object GdxImplicits {

  implicit class AugmentedIterable[T](i: Iterable[T]) {

    def asGdxArray: com.badlogic.gdx.utils.Array[T] = {

      val gdxArray = new com.badlogic.gdx.utils.Array[T]()
      i.foreach(elem => gdxArray.add(elem))
      gdxArray

    }
  }
}
