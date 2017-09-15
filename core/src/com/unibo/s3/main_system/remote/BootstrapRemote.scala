package com.unibo.s3.main_system.remote

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.widget._
import com.unibo.s3.Main
import com.unibo.s3.main_system.communication.Messages.MapSettingsMsg
import com.unibo.s3.main_system.communication._
import com.unibo.s3.main_system.world.actors.WorldActor


class BootstrapRemote() {
  import BootstrapRemote._

  private[this] var loadingFinished = false
  private[this] var loadingBar: VisProgressBar = _
  private[this] var loadingLabel: VisLabel = _
  private[this] var startBtn: VisTextButton = _
  private[this] var visualLoadingFinished = false

  private def log(msg: String) = {
    println("[BOOTSTRAP REMOTE] " + msg)
  }

  private def initActorSystem(): Unit = {
    log("-- Computation node configuration and startup --");
    SystemManager.createSystem(ActorSystemName, null)
    log("-- Actor system creted --");

    val world = new World(new Vector2(0, 0), true)

    /*SystemManager.createGeneralActor(
      MasterActor.props(), GeneralActors.MASTER_ACTOR)*/

    /*SystemManager.createGeneralActor( ?
      WorldActor.props(world), GeneralActors.WORLD_ACTOR)*/

    SystemManager.createGeneralActor(
      QuadTreeActor.props(), GeneralActors.QUAD_TREE_ACTOR)

    SystemManager.createGeneralActor(SpawnActor.props(),
      GeneralActors.SPAWN_ACTOR)

    SystemManager.createGeneralActor(
      MapActor.props(), GeneralActors.MAP_ACTOR)

    SystemManager.createGeneralActor(
      GraphActor.props(), GeneralActors.GRAPH_ACTOR)
    log("-- Actors created --")
  }


  def init(): Unit = {
    loadingFinished = false

    new Thread(new Runnable {
      override def run(): Unit = {
        try {
          initActorSystem()
          loadingFinished = true
        } catch {
          case err: Exception =>
          case _ =>
        }
      }
    }).start()
  }



}

object BootstrapRemote {
  private val ActorSystemName = "System"

  def apply(): BootstrapRemote =
    new BootstrapRemote()
}