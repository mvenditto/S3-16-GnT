package com.unibo.s3.main_system.characters.steer.collisions

import com.badlogic.gdx.ai.utils.Collision
import com.badlogic.gdx.ai.utils.Ray
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._


/**
  * The callback called when a collision is detected.
  */
class Box2dRaycastCallback extends RayCastCallback {
  var outputCollision: Collision[Vector2] = _
  var collided: Boolean = _

  override def reportRayFixture(fixture: Fixture, point: Vector2, normal: Vector2, fraction: Float): Float = {
    if (outputCollision != null) outputCollision.set(point, normal)
    collided = true
    fraction
  }
}

/** A raycast collision detector for box2d.
  *
  * @author mvenditto */
class Box2dRaycastCollisionDetector(val world: World, val callback: Box2dRaycastCallback) extends RaycastCollisionDetector[Vector2] {

	def this(world: World) {
    this(world, new Box2dRaycastCallback())
	}

	override def collides (ray: Ray[Vector2]): Boolean = findCollision(null, ray)

  override def findCollision(outputCollision: Collision[Vector2], inputRay: Ray[Vector2]): Boolean = {
    callback.collided = false
    if (!inputRay.start.epsilonEquals(inputRay.end, MathUtils.FLOAT_ROUNDING_ERROR)) {
      callback.outputCollision = outputCollision
      world.rayCast(callback, inputRay.start, inputRay.end)
    }
    callback.collided
  }
}
