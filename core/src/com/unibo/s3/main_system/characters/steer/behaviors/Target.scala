package com.unibo.s3.main_system.characters.steer.behaviors

import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.characters.{BaseCharacter, Guard, Thief}

/**
  * A trait describing a generic target of an entity.
  * @tparam A the type of this [[Target]]
  *
  * @author mvenditto
  */
trait Target[+A] {

  /**
    * Get the target entity
    * */
  def get: A

  /**
    * @return The last time this target was seen by the owner of this [[Target]].
    */
  def lastTimeSeen: Long

  /**
    * @return The last position the target entity was seen at
    */
  def lastKnownPos: Vector2

  /**
    * Refresh lastTimeSeen and lastKnownPos of this [[Target]]
    */
  def refresh(): Unit
}


/**
  * The [[Thief]] a [[Guard]] is pursuing
  * @param t a [[Thief]]
  */
case class Fugitive(t: Thief) extends BaseTarget[Thief](t)

/**
  * The [[Guard]] from which a [[Thief]] is evading from.
  * @param g a [[Guard]]
  *
  * @author mvenditto
  */
case class Pursuer(g: Guard) extends BaseTarget[Guard](g)

/**
  *
  * Abstract base implementation of the [[Target]] trait.
  *
  * @param t the target
  * @tparam A the type of this [[Target]]
  *
  * @author mvenditto
  */
abstract class BaseTarget[A <: BaseCharacter](t: A) extends Target[A] {

  private[this] val target: A = t
  private[this] var _lastTimeSeen: Long = _
  private[this] var _lastKnownPos: Vector2 = _

  refresh()

  override def get: A = target

  override def lastTimeSeen: Long = _lastTimeSeen

  override def lastKnownPos: Vector2 = _lastKnownPos

  override def refresh(): Unit = {
    _lastTimeSeen = System.currentTimeMillis()
    _lastKnownPos = t.getPosition
  }
}