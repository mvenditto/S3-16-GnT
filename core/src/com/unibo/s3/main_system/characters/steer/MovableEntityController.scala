package com.unibo.s3.main_system.characters.steer

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.math.Vector2
import com.unibo.s3.InputProcessorAdapter

import scala.collection.mutable

/**
  *
  * A class implementing a basic controller to move a [[MovableEntity]] with keyboard.
  * This implementation relies on [[com.badlogic.gdx.ai.steer.SteeringBehavior]].
  * The entity position/velocity is not directly modified, instead a 'target point' is moved
  * according to keyboard input and the entity is instructed to follow it. This approach
  * enable also to manage collisions with game objects in the same way as other,
  * non-user-controlled entities.
  *
  * @param entity the entity to control.
  */
class MovableEntityController (
  entity: MovableEntity[Vector2]) extends InputProcessorAdapter {

  private[this] val keyboard = entity.getPosition.cpy()
  private[this] var controlSpeed = entity.getMaxLinearSpeed + 1f
  private[this] val controlDistance = 4f

  private[this] val controls = mutable.Map[Int, Boolean](
    Keys.W -> false,
    Keys.S -> false,
    Keys.A -> false,
    Keys.D -> false
  )

  entity.setMaxAngularAcceleration(10f)
  entity.setMaxAngularSpeed(10f)
  entity.setMaxLinearAcceleration(10f)

  /**
    * Set the speed at which the entity 'target point' is moved.
    * @param speed the new speed to set
    */
  def setDirectionSpeed(speed: Float): Unit = controlSpeed = speed

  /**
    * Returns the 'taget point'
    * @return the [[Vector2]] target point
    */
  def getTargetVector: Vector2 = keyboard

  /**
    * Update the position of the 'target point' and set the entity's behavior accordingly.
    * @param dt delta time
    */
  def update(dt: Float): Unit = {
    val entityPos = entity.getPosition

    if (controls(Keys.W) && keyboard.y < entityPos.y + controlDistance) keyboard.y += controlSpeed * dt
    if (controls(Keys.S) && keyboard.y > entityPos.y - controlDistance) keyboard.y -= controlSpeed * dt
    if (controls(Keys.D) && keyboard.x < entityPos.x + controlDistance) keyboard.x += controlSpeed * dt
    if (controls(Keys.A) && keyboard.x > entityPos.x - controlDistance) keyboard.x -= controlSpeed * dt

    entity.setSteeringBehavior(null)

    val t = new CustomLocation(keyboard)
    val csb = entity.setComplexSteeringBehavior()
    if (entity.hasCollisionDetector) csb.avoidCollisionsWithWorld()

    if (!controls.values.forall(_ == false)) csb.seek(t) else csb.arriveTo(t)

    csb.buildPriority(true)
  }

  private def updateKeyState(key: Int, pressed: Boolean) =
    if (controls.contains(key)) controls(key) = pressed

  override def keyDown(keycode: Int): Boolean = {
    updateKeyState(keycode, pressed = true)
    false
  }

  override def keyUp(keycode: Int): Boolean = {
    updateKeyState(keycode, pressed = false)
    false
  }

}
