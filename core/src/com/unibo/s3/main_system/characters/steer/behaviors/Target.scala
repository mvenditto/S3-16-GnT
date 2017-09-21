package com.unibo.s3.main_system.characters.steer.behaviors

import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.characters.{BaseCharacter, Guard, Thief}

trait Target[+A] {

  def get: A

  def lastTimeSeen: Long

  def lastKnownPos: Vector2

  def refresh(): Unit
}


case class Fugitive(f: Thief) extends BaseTarget[Thief](f)
case class Pursuer(t: Guard) extends BaseTarget[Guard](t)

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