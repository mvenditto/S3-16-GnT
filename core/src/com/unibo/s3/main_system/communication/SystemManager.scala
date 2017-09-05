package com.unibo.s3.main_system.communication

import akka.actor.{ActorRef, ActorSystem, Props}
import com.typesafe.config.Config

object SystemManager {
  private[this] var system: ActorSystem = _
  private[this] var actorList: Map[String, ActorRef] = _

  def createSystem(systemName: String, config: Config): Unit = {
    this.system = ActorSystem.create(systemName, config)
  }

  def createActor(props: Props, actorName: String): ActorRef = {
    if(this.actorList == null) {
      this.actorList = Map()
    }
    val ref: ActorRef = this.system.actorOf(props, actorName)
    this.actorList += actorName -> ref
    ref
  }

  def getLocalActor(actors: String): ActorRef = this.actorList.filter(elem => elem._1.equals(actors)).head._2

  def getRemoteActor(systemName: String, ip: String, portNumber: String, path: String) = {
    val tmp = new StringBuilder(60)
    tmp.append("akka.tcp://")
    tmp.append(systemName)
    tmp.append("@")
    tmp.append(ip)
    tmp.append(":")
    tmp.append(portNumber)
    tmp.append(path)
    this.system.actorSelection(tmp.toString())
  }

  def shutdownSystem(): Unit = this.system.terminate()

}