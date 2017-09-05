package com.unibo.s3.main_system.modules

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.widget._
import com.unibo.s3.Main
import com.unibo.s3.main_system.communication._
import com.unibo.s3.main_system.world.actors.WorldActor


object GameActors extends Enumeration {
  val World, Graph, Map, QuadTree, Master = Value
}

sealed trait BootstrapEvent
case class BootstrapOk(actorsMap: Map[GameActors.Value, String]) extends BootstrapEvent
case class BootstrapFailed(error: String) extends BootstrapEvent
case class UserAck() extends BootstrapEvent


class BootstrapModule(listener: BootstrapEvent => Unit) extends BasicModuleWithGui {

  private[this] var loadingFinished = false

  private[this] val actorSystemName = "System"
  private[this] val gameActorsNames = Map(
    GameActors.World -> "worldActor",
    GameActors.Graph -> "graphActor",
    GameActors.Map -> "mapActor",
    GameActors.Master -> "masterActor",
    GameActors.QuadTree -> "quadTreeActor")

  private[this] val loadingDialogTitle = "System Initialization"
  private[this] var loadingBar: VisProgressBar = _
  private[this] var loadingLabel: VisLabel = _
  private[this] var startBtn: VisTextButton = _
  private[this] var visualLoadingFinished = false

  private def uiSetColor(color: Color) = {
    loadingBar.setColor(color)
    loadingLabel.setColor(color)
  }

  private def uiSetFail() = {
    uiSetColor(Color.RED)
    loadingLabel.setText("Failed!")
    startBtn.setDisabled(true)
  }

  private def uiSetSuccess() = {
    uiSetColor(Color.GREEN)
    loadingLabel.setText("Ready!")
    startBtn.setDisabled(false)
    this.enable(false)
    listener(UserAck())
  }

  private def createGui(): Unit = {
    val w = new VisWindow(loadingDialogTitle)
    w.setModal(true)

    startBtn = new VisTextButton("Start!", "blue")
    startBtn.setDisabled(true)

    loadingLabel = new VisLabel()
    loadingLabel.setColor(Color.LIGHT_GRAY)

    loadingBar = new VisProgressBar(0f, 100f, 1f, false)
    loadingBar.setAnimateDuration(0.4f)

    startBtn.addListener(new ClickListener(){
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        w.fadeOut(1.5f)
        listener(UserAck())
      }
    })

    w.setWidth(gui.getWidth / 3f)

    w.add[VisLabel](loadingLabel).expandX().row()
    w.add[VisProgressBar](loadingBar).expandX().fillX().row()
    w.add[VisTextButton](startBtn).expandX().padTop(10).row()
    w.add().fillY().expandY()

    w.centerWindow()
    gui.addActor(w)
  }

  private def initActorSystem(): Unit = {
    setProgress(0)
    SystemManager.getInstance.createSystem(actorSystemName, null)

    val world = new World(new Vector2(0, 0), true)
    setProgress(20)

    SystemManager.getInstance().createActor(
      MasterActor.props(), gameActorsNames(GameActors.Master))
    setProgress(30)

    SystemManager.getInstance.createActor(
      WorldActor.props(world), gameActorsNames(GameActors.World))
    setProgress(40)

    SystemManager.getInstance.createActor(
      QuadTreeActor.props(), gameActorsNames(GameActors.QuadTree))
    setProgress(60)

    SystemManager.getInstance.createActor(
      MapActor.props(), gameActorsNames(GameActors.Map))
    setProgress(80)

    SystemManager.getInstance.createActor(
      GraphActor.props(), gameActorsNames(GameActors.Graph))
    setProgress(100)

    loadingFinished = true
  }

  private def setProgress(percentage: Float) = {
    loadingBar.setValue(percentage)
  }

  override def init(owner: Main): Unit = {
    super.init(owner)
    createGui()
    loadingFinished = false

    new Thread(new Runnable {
      override def run(): Unit = {
        try {
          initActorSystem()
          loadingFinished = true
          listener(BootstrapOk(gameActorsNames))
        } catch {
          case err: Exception =>
            listener(BootstrapFailed(err.getMessage))
          case _ =>
            listener(BootstrapFailed("Unknown error."))
        }
      }
    }).start()
  }

  override def update(dt: Float): Unit = {
    super.update(dt)

    if (loadingFinished) {
      // after init updates ?
    }

    if (!visualLoadingFinished && loadingBar.getVisualPercent >= 1.0f) {
      uiSetSuccess()
      visualLoadingFinished = true
    }

    if (!visualLoadingFinished) {
      loadingLabel.setText((100 * loadingBar.getVisualPercent).toInt.toString + "%")
    }
  }

  override def cleanup(): Unit = {
    super.cleanup()
  }

}