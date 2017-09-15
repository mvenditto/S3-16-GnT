package com.unibo.s3.main_system.communication

import java.net.InetAddress

import akka.actor.{ActorRef, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import com.unibo.s3.main_system.game.AkkaSettings

object SystemManager {
  private[this] type GeneralActors = GeneralActors.Value
  private[this] type CharacterActors = CharacterActors.Value

  private[this] var system: ActorSystem = _
  private[this] var actorList: Map[String, ActorRef] = _

  private[this] var ipToConnect: Option[String] = Option.empty

  def createSystem(systemName: String, portNumber: Option[Int]): Unit = {
    if(portNumber.isEmpty)
      this.system = ActorSystem.create(systemName)
    else {
      this.system = ActorSystem.create(systemName, ConfigFactory.parseString(
        "{\"akka\":{\"actor\":{\"provider\":\"akka.remote.RemoteActorRefProvider\"}," +
          "\"loglevel\":\"INFO\",\"remote\":{\"enabled-transports\":[\"akka.remote.netty.tcp\"]" +
          ",\"log-received-messages\":\"on\",\"log-sent-messages\":\"on\"" +
          ",\"netty\":{\"tcp\":{\"hostname\":\""+ InetAddress.getLocalHost.getHostAddress+"\",\"port\":"+portNumber.get+"}}}}}"))
    }
  }

  def setIPForRemoting(ip: String): Unit = {
    this.ipToConnect = Option.apply(ip)
  }

  def getIP: Option[String] = {
    this.ipToConnect
  }

  def createActor(props: Props, actorCode: String): ActorRef = {
    if(this.actorList == null) {
      this.actorList = Map()
    }
    val ref: ActorRef = this.system.actorOf(props, actorCode)
    this.actorList += actorCode -> ref
    ref
  }

  def createGeneralActor(props: Props, actorName: GeneralActors): ActorRef = {
    createActor(props, actorName.toString)
  }

  def createCharacterActor(props: Props, actorName: CharacterActors, id: Int): ActorRef = {
    val actorCode = actorName.toString + id
    createActor(props, actorCode)
  }

  def getLocalActor(actorCode: String): ActorRef = this.actorList.filter(elem => elem._1.equals(actorCode)).head._2

  def getLocalGeneralActor(actorName: GeneralActors): ActorRef = {
    getLocalActor(actorName.toString)
  }

  def getLocalCharacterActor(actorName: CharacterActors, id: Int): ActorRef = {
    val actorCode = actorName.toString + id
    getLocalActor(actorCode.toString)
  }

  def getRemoteActor(systemName: String, path: String, actorName: String): ActorSelection = {
    val tmp = new StringBuilder(60)
    tmp.append("akka.tcp://")
    tmp.append(systemName)
    tmp.append("@")
    tmp.append(this.ipToConnect.get)
    tmp.append(":")
    tmp.append(AkkaSettings.ComputeSystemPort)
    tmp.append(path)
    tmp.append(actorName)
    this.system.actorSelection(tmp.toString())
  }

  def shutdownSystem(): Unit = this.system.terminate()
}