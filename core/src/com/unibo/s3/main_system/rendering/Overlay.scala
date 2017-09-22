package com.unibo.s3.main_system.rendering

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.kotcrab.vis.ui.util.ToastManager
import com.kotcrab.vis.ui.widget.{VisLabel, VisTable}
import com.kotcrab.vis.ui.widget.toast.Toast
import com.unibo.s3.Main
import com.unibo.s3.main_system.characters.BaseCharacter
import com.unibo.s3.main_system.characters.Guard
import com.unibo.s3.main_system.characters.Thief
import com.unibo.s3.main_system.communication.Messages.{ThiefCaughtMsg, ThiefReachedExitMsg}
import com.unibo.s3.main_system.modules.BasicModuleWithGui
import com.unibo.s3.main_system.util.{GraphicsUtils, ScaleUtils}

/**
  * A trait that can be mixed with a [[BasicModuleWithGui]] to
  * add an 'overlay' layer rendered on top of the module gui.
  *
  * @author mvenditto
  */
trait Overlay extends BasicModuleWithGui {

  protected var overlay: Stage = _

  def getOverlay: Stage = overlay

  abstract override def init(owner: Main): Unit = {
    super.init(owner)
    overlay = new Stage(new ScreenViewport())
  }

  abstract override def resize(newWidth: Int, newHeight: Int): Unit = {
    super.resize(newWidth, newHeight)
    overlay.getViewport.update(newWidth, newHeight, true)
  }

  abstract override def update(dt: Float): Unit = {
    super.update(dt)
    overlay.act(dt)
  }

  abstract override def renderGui(): Unit = {
    super.renderGui()
    overlay.draw()
  }

  abstract override def cleanup(): Unit = {
    super.cleanup()
    overlay.dispose()
  }
}

/**
  * An implementation of [[Overlay]] that provides the possibility
  * to show various kind of notifications on the 'overlay' layer.
  */
trait GameOverlay extends Overlay {

  private var fadeTextFont: BitmapFont = _
  private var toastFont: BitmapFont = _
  private var notifications: ToastManager = _

  private val fadeTextFontSize = 75
  private val toastFontSize = 14
  private var fadeTextDuration = 2f
  private var toastFadeDuration = 5f
  private var fadeTextMoveYBy = 200f

  private val openSquareBracket = "["
  private val closedSquareBracket = "]"
  private val thiefLabel = "Thief"
  private val guardLabel = "Guard"
  private val space = " "
  private val reachedExitMsg = space + "reached exit!" + space
  private val caughtMsg = space + "caught" + space

  abstract override def init(owner: Main): Unit = {
    super.init(owner)
    notifications = new ToastManager(getGuiStage)
    notifications.setAlignment(Align.topRight)
    fadeTextFont = GraphicsUtils.createBitmapFromTtf("fonts/OpenSans-Regular.ttf", fadeTextFontSize)
    toastFont = GraphicsUtils.createBitmapFromTtf("fonts/OpenSans-SemiBold.ttf", toastFontSize)
  }

  /**
    * Set the duration of the notifications
    * @param d the new duration
    */
  def setToastFadeDuration(d: Float): Unit = toastFadeDuration = d

  /**
    * Set the duration of fading effect of 'fading text notifications'
    * @param d the new duration
    */
  def setFadeDuration(d: Float): Unit = fadeTextDuration = d

  /**
    * Set how much a 'fading text notification' moves up when fading out.
    * @param y the new duration
    */
  def setFadeTextMoveYBy(y: Float): Unit = fadeTextMoveYBy = y

  /**
    * Set the font for the 'fading text notification'
    * @param f the new font to set
    */
  def setFadeTextFont(f: BitmapFont): Unit = fadeTextFont = f

  /**
    * Set the font for the 'toast notifications'
    * @param f the new font to test
    */
  def setToastFont(f: BitmapFont): Unit = toastFont = f

  private def characterTag(c: BaseCharacter): String = {
    val tmp = c match {
      case t: Thief => thiefLabel
      case g: Guard => guardLabel
      case _ => "?"
    }
    tmp + openSquareBracket + c.getId + closedSquareBracket
  }

  /**
    * Show a 'toast notification' based on the gameEvent provided.
    * @param gameEvent the given game event
    */
  protected def showGameEventToast(gameEvent: Any): Unit = {
    val toast: Option[Toast] = gameEvent match {
      case ThiefCaughtMsg(t, g) =>
        val toast = new Toast("dark", new VisTable(true))
        val guardLabel = new VisLabel(characterTag(g))
        val thiefLabel = new VisLabel(characterTag(t))
        guardLabel.getStyle.font = toastFont
        guardLabel.setColor(Color.BLUE)
        thiefLabel.getStyle.font = toastFont
        thiefLabel.setColor(Color.RED)
        val ct = toast.getContentTable
        ct.add(guardLabel)
        val hc = GraphicsUtils.loadIconAsImage("sprites/arrest3.png",
          (TextureFilter.Nearest, TextureFilter.Nearest))
        ct.add(hc).padLeft(8).padRight(8)
        ct.add(thiefLabel)
        Option(toast)

      case ThiefReachedExitMsg(t) =>
        val toast = new Toast("dark", new VisTable(true))
        val thiefLabel = new VisLabel(characterTag(t))
        thiefLabel.getStyle.font = toastFont
        thiefLabel.setColor(Color.RED)
        val ct = toast.getContentTable
        ct.add(thiefLabel)
        val hc = GraphicsUtils.loadIconAsImage("sprites/exit3.png",
          (TextureFilter.Nearest, TextureFilter.Nearest))
        ct.add(hc).padLeft(8).padRight(8)
        Option(toast)

      case _ => None
    }
    toast.foreach(t => notifications.show(t, toastFadeDuration))
  }


  /**
    * Show a notification text that fades out when moving up of a predefined amount.
    * @param pos the position where to show the text
    * @param msg the message to show
    * @param col the color of the text to show
    */
  protected def showPopupFadingText(pos: Vector2, msg: String, col: Color): Unit = {
    val vs = new LabelStyle()
    vs.font = fadeTextFont
    vs.fontColor = col
    val v = new VisLabel(msg, vs)
    v.setPosition(pos.x - v.getPrefWidth / 2f, pos.y)
    v.addAction(
      Actions.sequence(
        Actions.parallel(
          Actions.fadeOut(fadeTextDuration),
          Actions.moveBy(0, fadeTextMoveYBy, fadeTextDuration)),
        Actions.run(new Runnable{override def run(): Unit = v.remove()})
      ))
    overlay.addActor(v)
  }
}
