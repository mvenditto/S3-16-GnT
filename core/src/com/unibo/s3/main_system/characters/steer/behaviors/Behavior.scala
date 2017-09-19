package com.unibo.s3.main_system.characters.steer.behaviors

import com.badlogic.gdx.math.{MathUtils, Vector2}
import com.unibo.s3.main_system.characters.BaseCharacter

import scala.util.Random
import BehaviorUtils._
import com.badlogic.gdx.ai.steer.Steerable
import com.unibo.s3.main_system.characters.Guard.Guard
import com.unibo.s3.main_system.characters.Thief.Thief

/**
  * This trait represent a behavior and follows the Command pattern.
  *
  * @author mvenditto
  *
  */


object Behaviors {

  import BehaviorUtils.AugmentedVector2

  def patrol(target: BaseCharacter): Unit = target.chooseBehaviour()

  def patrolIf(target: BaseCharacter, condition: => Boolean): Unit = if(condition) patrol(target)

  def patrolIfHasNoTarget(target: BaseCharacter): Unit = target match {
    case g: Guard => if (!g.hasTarget) patrol(g)
    case t: Thief => if (!t.hasTarget) patrol(t)
    case _ => patrol _
  }

  def runTowards(
    target: BaseCharacter,
    nearbyEntities: Iterable[BaseCharacter],
    chooseOne: (BaseCharacter, Iterable[BaseCharacter]) => Option[BaseCharacter]): Unit = {

    chooseOne(target, nearbyEntities).map(e =>
      target.setComplexSteeringBehavior()
        .avoidCollisionsWithWorld()
        .seek(e)
        .buildPriority(true)
    )
  }

  def runAway(
    target: BaseCharacter,
    nearbyEntities: Iterable[BaseCharacter],
    chooseOne: (BaseCharacter, Iterable[BaseCharacter]) => Option[BaseCharacter]): Unit = {

    chooseOne(target, nearbyEntities).map(nn =>
      target.setComplexSteeringBehavior()
        .avoidCollisionsWithWorld()
        .evadeFrom(nn)
        .buildPriority(true))
  }
}

/**
  * A collection of utility methods related to ai behaviors.
  *
  * @author mvenditto
  */
object BehaviorUtils {
  /**
    * Any entity exposing a [[Vector2]] position through a getPosition method.
    * */
  type EntityWithPos = {def getPosition(): Vector2}

  trait Target[A <: EntityWithPos] {
    def set(t: A): Unit

    def get: Option[A]

    def exists: Boolean

    def lastTimeSeen: Long

    def lastKnownPos: Vector2

    def refresh(): Unit
  }

  case class BaseTarget[A <: EntityWithPos](t: A) extends Target[A] {

    private[this] var target: Option[A] = None
    private[this] var _lastTimeSeen: Long = _
    private[this] var _lastKnownPos: Vector2 = _

    refresh()

    override def set(t: A): Unit = target = Option(t)

    override def get: Option[A] = target

    override def exists: Boolean = target.isDefined

    override def lastTimeSeen: Long = _lastTimeSeen

    override def lastKnownPos: Vector2 = _lastKnownPos

    override def refresh(): Unit = {
      _lastTimeSeen = System.currentTimeMillis()
      _lastKnownPos = t.getPosition()
    }
  }

  def dst2(a: EntityWithPos, b: EntityWithPos): Float =
    a.getPosition.dst2(b.getPosition)

  def sortThenGetFirst[A <: EntityWithPos](
    c: A, n: Iterable[A], ordering: (A, A) => Boolean): Option[A] =
    n.toList.sortWith(ordering) collectFirst {case x: A => x}

  def nearest[A <: EntityWithPos](c: A, n: Iterable[A]): Option[A] =
    sortThenGetFirst(c, n, (a: A, b: A) => dst2(c, a) < dst2(c, b))

  def farthest[A <: EntityWithPos](c: A, n: Iterable[A]): Option[A] =
    sortThenGetFirst(c, n, (a: A, b: A) => dst2(c, a) > dst2(c, b))

  def random[A](n: Iterable[A]): Option[A] =
    Random.shuffle(n) collectFirst {case x: A => x}

  def random2[A](n: Seq[A]): Option[A] = {
    n.size match {
      case x if x > 2 => Option(n(MathUtils.random(0, x - 1)))
      case _ => n.headOption
    }
  }

  implicit class AugmentedVector2(v: Vector2) {
    def getPosition(): Vector2 = v
  }

}