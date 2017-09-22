package com.unibo.s3.main_system.world.actors

import com.badlogic.gdx.ai.utils.Collision
import com.badlogic.gdx.math.Vector2

/**
  * Simple wrapper for a [[Collision]].
  * Used to make it [[Serializable]] when is needed to send it
  * to a remote actor.
  * @see [[Collision]]
  */
case class CollisionHolder(point: Vector2, normal: Vector2)

object CollisionHolder{
  /**
    * Return a [[CollisionHolder]] wrapping the given [[Collision]]
    * @param c a [[Collision]]
    * @return a [[CollisionHolder]] wrapping c
    */
  def of(c: Collision[Vector2]): CollisionHolder =
    CollisionHolder(c.point, c.normal)
}
