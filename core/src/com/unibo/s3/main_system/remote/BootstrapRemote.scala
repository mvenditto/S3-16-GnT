package com.unibo.s3.main_system.remote

import java.net.InetAddress

import com.kotcrab.vis.ui.widget._
import com.unibo.s3.main_system.communication._
import com.unibo.s3.main_system.game.{AkkaSystemNames, ComputeSystemPort}



class BootstrapRemote() {

  private[this] var loadingFinished = false
  private[this] var loadingBar: VisProgressBar = _
  private[this] var loadingLabel: VisLabel = _
  private[this] var startBtn: VisTextButton = _
  private[this] var visualLoadingFinished = false

  private def log(msg: String): Unit = {
    println(msg)
  }

  private def initActorSystem(): Unit = {
    log("-- Computation node configuration and startup --")
    val myIp = InetAddress.getLocalHost.getHostAddress
    val portNumber = ComputeSystemPort
    SystemManager.createSystem(AkkaSystemNames.ComputeSystem, ip = Option(myIp), portNumber = Option(portNumber))
    log("-- Actor system created --")
    log("-- IP: " + myIp + ":" + portNumber.portNumber)

    SystemManager.createActor(
      CommunicatorActor.props(), GeneralActors.COMMUNICATOR_ACTOR)

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
          case _: Exception =>
          case _ =>
        }
      }
    }).start()
  }
}