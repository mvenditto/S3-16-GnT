package com.unibo.s3.testbed.ui

import com.badlogic.gdx.scenes.scene2d.{Action, Actor}
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.{sequence, run}

abstract class AdaptiveActor(val actor: Actor) {

  def resize(stageWidth: Float, stageHeight: Float): Unit

  def getActor[T <: Actor]: T = actor.asInstanceOf[T]
}

class AdaptiveSizeActor(
  override val actor: Actor) extends AdaptiveActor(actor){

  protected var stageWidthPercent: Option[Float] = None
  protected var stageHeightPercent: Option[Float] = None

  def setSize(stageWidthPercent: Float, stageHeightPercent: Float): Unit = {
    this.stageWidthPercent = Option(stageWidthPercent)
    this.stageHeightPercent = Option(stageHeightPercent)
  }

  override def resize(stageWidth: Float, stageHeight: Float): Unit = {
    stageWidthPercent.foreach(wp => actor.setWidth((stageWidth * wp) / 100f))
    stageHeightPercent.foreach(hp => actor.setHeight((stageHeight * hp) / 100f))
  }

}

object AdaptiveSizeActor {
  def apply(actor: Actor): AdaptiveSizeActor =
    new AdaptiveSizeActor(actor)
}

sealed trait Anchor
case object TopLeft extends Anchor
case object TopRight extends Anchor
case object BottomLeft extends Anchor
case object BottomRight extends Anchor

trait Anchorable extends AdaptiveActor {

  var anchor: Option[Anchor] = None
  var padX = 0
  var padY = 0

  def setAnchor(anchor: Anchor): Unit = this.anchor = Option(anchor)

  def setPadding(x: Int, y: Int): Unit = {
    padX = x
    padY = y
  }

  abstract override def resize(stageWidth: Float, stageHeight: Float): Unit = {
    super.resize(stageWidth, stageHeight)
    val a = actor
    anchor match {
      case Some(TopLeft) => a.setPosition(a.getX, stageHeight - a.getHeight)
      case Some(TopRight) => a.setPosition(stageWidth - a.getWidth, stageHeight - a.getHeight)
      case Some(BottomLeft) => a.setPosition(0, 0)
      case Some(BottomRight) => a.setPosition(stageWidth - a.getWidth, 0)
      case None => ()
    }
    a.setPosition(a.getX + padX, a.getY + padY)
  }
}

object TransitionFunctions {
  
  val slideLeft: (Actor, (Float, Float)) => Action = (a: Actor, s: (Float, Float)) =>
    Actions.moveTo(if (a.getX >= 0) -a.getWidth else 0, a.getY, 0.30f)

  val slideRight: (Actor, (Float, Float)) => Action = (a: Actor, s: (Float, Float)) =>
    Actions.moveTo(if (a.getX <= s._1) s._1 else s._1 - a.getWidth, a.getY, 0.30f)

  val slideDown: (Actor, (Float, Float)) => Action = (a: Actor, s: (Float, Float)) =>
    Actions.moveTo(a.getX, if (a.getY >= 0) -a.getHeight else 0, 0.30f)

  val slideUp: (Actor, (Float, Float)) => Action = (a: Actor, s: (Float, Float)) =>
    Actions.moveTo(a.getX, if (a.getY <= s._2) a.getHeight + s._2 else s._2, 0.30f)
}

trait Toggleable extends AdaptiveActor {

  var transition: (Actor, (Float, Float)) => Action = TransitionFunctions.slideDown
  var isTransitionOngoing = false

  def setTransitionFunc(t: (Actor, (Float, Float)) => Action): Unit = {
    transition = t
  }

  def toggle(stageWidth: Float, stageHeight: Float): Unit = {
    isTransitionOngoing = true
    val t = transition(actor, (stageWidth, stageHeight))
    val onTransitionFinished = new Runnable {
      override def run(): Unit = isTransitionOngoing = false
    }
    actor.addAction(sequence(t, run(onTransitionFinished)))
  }
}

object AdaptiveActorTest extends App {
  val a = new Actor
  val ta = new AdaptiveSizeActor(a) with Anchorable
  val tt = new AdaptiveSizeActor(a) with Toggleable
}
