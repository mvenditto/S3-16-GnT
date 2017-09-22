package com.unibo.s3.testbed.ui

import com.badlogic.gdx.scenes.scene2d.{Action, Actor}
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.{sequence, run}

/**
  * This class represents a wrapper for an [[Actor]] that enables it
  * to change its attributes (position, size) based on its owning [[com.badlogic.gdx.scenes.scene2d.Stage]]
  * size.
  * @param actor an [[Actor]] to be wrapped as an [[AdaptiveActor()]]
  * @author mvenditto
  */
abstract class AdaptiveActor(val actor: Actor) {

  /**
    * Update this actor according to a change of its [[com.badlogic.gdx.scenes.scene2d.Stage]] size
    * @param stageWidth
    * @param stageHeight
    */
  def resize(stageWidth: Float, stageHeight: Float): Unit

  /**
    * @return the actor wrapped by this [[AdaptiveActor]]
    * */
  def getActor[T <: Actor]: T = actor.asInstanceOf[T]
}

/**
  * This is an [[AdaptiveActor]] whose size is linked to the size of the
  * [[com.badlogic.gdx.scenes.scene2d.Stage]] in which the wrapped [[Actor]] is contained.
  * @param actor an [[Actor]] to be wrapped as an [[AdaptiveActor()]]
  * @author mvenditto
  */
class AdaptiveSizeActor(
  override val actor: Actor) extends AdaptiveActor(actor){

  protected var stageWidthPercent: Option[Float] = None
  protected var stageHeightPercent: Option[Float] = None

  /**
    * Set the size of this actor as a percentage of the [[com.badlogic.gdx.scenes.scene2d.Stage]] size.
    * @param stageWidthPercent the width as a percentage of the [[com.badlogic.gdx.scenes.scene2d.Stage]] width
    * @param stageHeightPercent the height as a percentage of the [[com.badlogic.gdx.scenes.scene2d.Stage]] height
    */
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

/**
  * An enumeration of possible 'anchor' supported by [[Anchorable]] trait.
  */
sealed trait Anchor
case object TopLeft extends Anchor
case object TopRight extends Anchor
case object BottomLeft extends Anchor
case object BottomRight extends Anchor

/**
  * A trait that can be mixed in with an [[AdaptiveActor]] enabling it to anchor
  * at a certain position on the [[com.badlogic.gdx.scenes.scene2d.Stage]] in which it's contained.
  *
  * @author mvenditto
  */
trait Anchorable extends AdaptiveActor {

  var anchor: Option[Anchor] = None
  var padX = 0
  var padY = 0

  /**
    * @param anchor the [[Anchor]] to be set to this [[Anchorable]].
    */
  def setAnchor(anchor: Anchor): Unit = this.anchor = Option(anchor)

  /**
    * Set a padding from [[Anchor]]
    * @param x the x padding
    * @param y the y padding
    */
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

/**
  * A collection of functions to be used with [[Toggleable]] trait.
  */
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

/**
  * A trait that can be mixed in with an [[AdaptiveActor]] enabling it to be moved
  * in or out to its [[com.badlogic.gdx.scenes.scene2d.Stage]], based on a strategy
  * specified by a 'transition' function.
  * @see [[TransitionFunctions]]
  * @author mvenditto
  */
trait Toggleable extends AdaptiveActor {

  var transition: (Actor, (Float, Float)) => Action = TransitionFunctions.slideDown
  var isTransitionOngoing = false

  /**
    * @param t The function that manages the transition of this actor.
    */
  def setTransitionFunc(t: (Actor, (Float, Float)) => Action): Unit = {
    transition = t
  }

  /**
    * Move in/out of screen this actor.
    * @param stageWidth the [[com.badlogic.gdx.scenes.scene2d.Stage]] width
    * @param stageHeight the [[com.badlogic.gdx.scenes.scene2d.Stage]] height
    */
  def toggle(stageWidth: Float, stageHeight: Float): Unit = {
    isTransitionOngoing = true
    val t = transition(actor, (stageWidth, stageHeight))
    val onTransitionFinished = new Runnable {
      override def run(): Unit = isTransitionOngoing = false
    }
    actor.addAction(sequence(t, run(onTransitionFinished)))
  }
}
