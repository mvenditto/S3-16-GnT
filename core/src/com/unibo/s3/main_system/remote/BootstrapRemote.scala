package com.unibo.s3.main_system.remote

import java.net.InetAddress

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import com.kotcrab.vis.ui.widget._
import com.unibo.s3.Main
import com.unibo.s3.main_system.communication.Messages.GameSettingsMsg
import com.unibo.s3.main_system.communication._
import com.unibo.s3.main_system.game.AkkaSettings
import com.unibo.s3.main_system.world.actors.WorldActor


class BootstrapRemote() {

  private[this] var loadingFinished = false
  private[this] var loadingBar: VisProgressBar = _
  private[this] var loadingLabel: VisLabel = _
  private[this] var startBtn: VisTextButton = _
  private[this] var visualLoadingFinished = false

  private def log(msg: String) = {
    println(msg)
  }

  private def initActorSystem(): Unit = {
    log("-- Computation node configuration and startup --");
    val myIp = InetAddress.getLocalHost.getHostAddress
    val portNumber = AkkaSettings.ComputeSystemPort
    SystemManager.createSystem(AkkaSettings.RemoteSystem, ip = Option(myIp), portNumber = Option(portNumber))
    log("-- Actor system creted --");
    log("-- IP: " + myIp + ":" + portNumber)

    val world = new World(new Vector2(0, 0), true)

    /*SystemManager.createActor(
      MasterActor.props(), GeneralActors.MASTER_ACTOR)*/

    SystemManager.createActor(
      CommunicatorActor.props(), GeneralActors.COMMUNICATOR_ACTOR)

    /*SystemManager.createActor(
      WorldActor.props(world), GeneralActors.WORLD_ACTOR)*/

    /*SystemManager.createActor(
      QuadTreeActor.props(), GeneralActors.QUAD_TREE_ACTOR)*/

    SystemManager.createActor(
      SpawnActor.props(), GeneralActors.SPAWN_ACTOR)

    SystemManager.createActor(
      MapActor.props(), GeneralActors.MAP_ACTOR)

    SystemManager.createActor(
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