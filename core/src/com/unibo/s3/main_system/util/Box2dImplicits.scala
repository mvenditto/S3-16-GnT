package com.unibo.s3.main_system.util

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._
import GdxImplicits._

object Box2dImplicits {

  implicit class AugmentedWorld(world: World) {

    def bodies: Iterable[Body] = {
      val b = new com.badlogic.gdx.utils.Array[Body]()
      world.getBodies(b)
      b.asScalaIterable
    }

    def bodiesAtPoint(p: Vector2, r: Float = 1): Iterable[Body] = {
      var b = List[Body]()
      world.QueryAABB(
        new QueryCallback {
          override def reportFixture(f: Fixture): Boolean = {
            b :+= f.getBody
            true
          }
      }, p.x - r, p.y - r, p.x + r, p.y + r)
      b
    }

    def createBox(position: Vector2, size: Vector2): Body = {
      val bodyDef = new BodyDef
      bodyDef.position.set(position)
      val body = world.createBody(bodyDef)
      val box = new PolygonShape
      box.setAsBox(Math.abs(size.x / 2), Math.abs(size.y / 2))
      body.createFixture(box, 0.0f)
      box.dispose()
      body
    }
  }

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
