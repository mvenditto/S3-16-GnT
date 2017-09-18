package com.unibo.s3.main_system.characters.steer.collisions

import akka.actor.{ActorRef, ActorSelection}
import com.badlogic.gdx.ai.steer.Proximity
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector
import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.world.actors.ActorRefOrSelection._
import com.unibo.s3.main_system.world.actors.{Box2dRayCastCollisionDetectorProxy, Box2dSquareAABBProximityProxy}

/**
  * A factory class of Box2d detectors working with (remote too) actors.
  * @param ars an [[ActorRefOrSelectionHolder]] of the 'world actor'
  */
class Box2dProxyDetectorsFactory(ars: ActorRefOrSelectionHolder) extends DetectorsFactory[Vector2] {

  def newRaycastCollisionDetector(): RaycastCollisionDetector[Vector2] =
    new Box2dRayCastCollisionDetectorProxy(ars)

  def newProximityDetector(detectionRadius: Float): Proximity[Vector2] =
    new Box2dSquareAABBProximityProxy(null, ars, detectionRadius)
}

object Box2dProxyDetectorsFactory {
  def apply (ars: ActorRefOrSelectionHolder): Box2dProxyDetectorsFactory =
    new Box2dProxyDetectorsFactory(ars)

  def of(ar: ActorRef): Box2dProxyDetectorsFactory =
    Box2dProxyDetectorsFactory(ar)

  def of(as: ActorSelection): Box2dProxyDetectorsFactory =
    Box2dProxyDetectorsFactory(as)
}


