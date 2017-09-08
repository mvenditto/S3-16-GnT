package com.unibo.s3.main_system.util

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.{Body, CircleShape, PolygonShape}

object Box2dImplicits {

  implicit class AugmentedBody(b: Body) {

    def size: Vector2 = {
      val s = new Vector2()
      b.getFixtureList.get(0).getShape match {
        case sh: PolygonShape =>
          val tmp = new Vector2()
          sh.getVertex(0, tmp)
          s.set(tmp.x.abs, tmp.y.abs)
        case sh: CircleShape => new Vector2(sh.getRadius, sh.getRadius)
        case _ => s.set(0, 0)
      }
    }

    def size2: Vector2 = size.scl(2f)
  }
}
