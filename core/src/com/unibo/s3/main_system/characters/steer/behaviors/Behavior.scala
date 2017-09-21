package com.unibo.s3.main_system.characters.steer.behaviors

import com.badlogic.gdx.ai.steer.SteeringBehavior
import com.badlogic.gdx.math.{MathUtils, Vector2}
import com.unibo.s3.main_system.characters.{BaseCharacter, Guard, Thief}
import com.unibo.s3.main_system.util.GdxImplicits._

import scala.util.Random

/**
  * This trait represent a behavior and follows the Command pattern.
  *
  * @author mvenditto
  *
  */

object Behaviors {

  import BehaviorUtils._

  def hold(target: BaseCharacter): Unit = {
    target.setSteeringBehavior(null)
  }

  def patrol(target: BaseCharacter): Unit = target.chooseBehaviour()

  def randomPatrol(target: BaseCharacter): Unit = {
    val t = target.setComplexSteeringBehavior()
      .avoidCollisionsWithWorld()
    target.selectRandomDestination().foreach(d => t.arriveTo(d))
    t.buildPriority(true)
  }

  def patrolIf(target: BaseCharacter, condition: => Boolean): Unit = if(condition) patrol(target)

  def patrolIfHasNoTarget(target: BaseCharacter): Unit = target match {
    case g: Guard => if (!g.hasTarget) patrol(g)
    case t: Thief => if (!t.hasTarget) patrol(t)
    case _ =>
  }

  def doWhileFollowingGraph(c: BaseCharacter, sb: SteeringBehavior[Vector2]): SteeringBehavior[Vector2] = {
    val behavior = c.setComplexSteeringBehavior()
      .add(sb)
      .arriveTo(c.getCurrentDestination)
      .buildBlended(Array(1.0f, 0.5f), false)

    c.setComplexSteeringBehavior()
      .avoidCollisionsWithWorld()
      .add(behavior)
      .buildPriority(true)
  }

  def onThiefCaught(thief: Thief, guard: Guard, thresold: Float, callback: => Unit): Unit = {
    if (dst2(thief, guard) <= thresold) {
      thief.setGotCaughtByGuard(true)
      thief.setPursuerTarget(None)
      guard.setFugitiveTarget(None)
      hold(thief)
      callback
    }
  }

  def onThiefExit(thief: Thief, exit: Vector2, thresold: Float, callback: => Unit): Unit = {
    if (dst2(thief, exit) <= thresold){
      thief.setReachedExit(true)
      thief.setPursuerTarget(None)
      hold(thief)
      callback
    }
  }

  def guardPursueThieves(guard: Guard, nearbyThieves: Iterable[BaseCharacter]): Unit = {
    val thereAreNeighbors = nearbyThieves.nonEmpty
    val pursuedThief = guard.getTarget

    pursuedThief.foreach {
      case Fugitive(t) if t.gotCaughtByGuard || t.hasReachedExit =>
        guard.setFugitiveTarget(None)
        t.setPursuerTarget(None)
      case _ =>
    }

    /*if previous target is no more in range, restart patrolling.*/
    if (!thereAreNeighbors && pursuedThief.isDefined) {
      randomPatrol(guard)
      guard.setFugitiveTarget(None)

      pursuedThief match {
        case Some(Fugitive(t)) =>
          t.setPursuerTarget(None)
        case _ =>
      }
    }
    
    if (thereAreNeighbors)  {
      if (pursuedThief.isEmpty)
        pursueThieves(guard, nearbyThieves, nearest[BaseCharacter])
      else if (!nearbyThieves.exists(p => p.getId.equals(pursuedThief.get.get.getId))) {
        pursuedThief.get match {
          case Fugitive(t) => t.setPursuerTarget(None)
        }
        pursueThieves(guard, nearbyThieves, nearest[BaseCharacter])
      }
    }
  }

  def pursueThieves(
    c: BaseCharacter,
    nearbyEntities: Iterable[BaseCharacter],
    chooseOne: (BaseCharacter, Iterable[BaseCharacter]) => Option[BaseCharacter]): Unit = {

    chooseOne(c, nearbyEntities).foreach(e => {
      doWhileFollowingGraph(c,
        c.setComplexSteeringBehavior()
          .avoidCollisionsWithWorld()
          .pursue(e)
          .buildPriority(false))

      (c, e) match {
        case (g: Guard, t: Thief) =>
          g.setFugitiveTarget(Option(Fugitive(t)))
        case _ =>
      }
    })
  }

  def runToLocation(c: BaseCharacter, location: Vector2): Unit = {
    doWhileFollowingGraph(c,
      c.setComplexSteeringBehavior()
        .avoidCollisionsWithWorld()
        .seek(location)
        .buildPriority(false))
  }

  def runToLocation(
    c: BaseCharacter,
    nearbyEntities: Iterable[Vector2],
    chooseOne: (Vector2, Iterable[Vector2]) => Option[Vector2]): Unit = {
    chooseOne(c.getPosition, nearbyEntities).foreach(e => runToLocation(c, e))
  }

  def evadeFromGuard(c: BaseCharacter, g: BaseCharacter): Unit = {
    doWhileFollowingGraph(c,
      c.setComplexSteeringBehavior()
        .avoidCollisionsWithWorld()
        .evadeFrom(g)
        .buildPriority(true)
    )
    (c, g) match {
      case (g: Guard, t: Thief) =>
        t.setPursuerTarget(Option(Pursuer(g)))
      case _ =>
    }
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

  def dst2(a: EntityWithPos, b: EntityWithPos): Float =
    a.getPosition.dst2(b.getPosition)

  def dst2(a: EntityWithPos, b: Vector2): Float =
    a.getPosition.dst2(b)

  def sortThenGetFirst[A](
    c: A, n: Iterable[A], ordering: (A, A) => Boolean): Option[A] =
    n.toList.sortWith(ordering) collectFirst {case x: A => x}

  def nearest[A <: EntityWithPos](c: A, n: Iterable[A]): Option[A] =
    sortThenGetFirst(c, n, (a: A, b: A) => dst2(c, a) < dst2(c, b))

  def nearest(c: Vector2, n: Iterable[Vector2]): Option[Vector2] =
    sortThenGetFirst(c, n, (a: Vector2, b: Vector2) => c.dst2(a) < c.dst2(b))

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