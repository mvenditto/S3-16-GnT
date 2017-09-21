package com.unibo.s3.main_system.communication


import akka.actor.{ActorRef, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object SystemManager {
  private[this] type GeneralActors = GeneralActors.Value
  private[this] type CharacterActors = CharacterActors.Value

  private[this] var system: ActorSystem = _
  private[this] var actorList = Map[String, ActorRef]()

  private[this] var ipToConnect: Option[String] = Option.empty
  private[this] var portToConnect: Int = _

  def createSystem(systemName: String, ip: Option[String], portNumber: Option[Int]): Unit = {
    if(portNumber.isDefined && ip.isDefined)
      this.system = ActorSystem.create(systemName, ConfigFactory.parseString(
        "{\"akka\":{\"actor\":{\"provider\":\"akka.remote.RemoteActorRefProvider\"}," +
          "\"loglevel\":\"INFO\",\"remote\":{\"enabled-transports\":[\"akka.remote.netty.tcp\"]" +
          ",\"log-received-messages\":\"on\",\"log-sent-messages\":\"on\"" +
          ",\"netty\":{\"tcp\":{\"hostname\":\""+ ip.get +"\",\"port\":"+portNumber.get+"}}}}}"))
    else {
      this.system = ActorSystem.create(systemName)
    }
  }

  def setIPForRemoting(ip: String, portNumber: Int): Unit = {
    this.ipToConnect = Option.apply(ip)
    this.portToConnect = portNumber
  }

  def getIP: Option[String] = {
    this.ipToConnect
  }

  def createActor(props: Props, actorCode: String): ActorRef = {
    val ref: ActorRef = this.system.actorOf(props, actorCode)
    this.actorList += actorCode -> ref
    ref
  }

  def createActor(props: Props, actorName: GeneralActors): ActorRef = {
    createActor(props, actorName.toString)
  }

  def createActor(props: Props, actorName: CharacterActors, id: Int): ActorRef = {
    createActor(props, actorName.toString + id)
  }

  def getLocalActor(actorCode: String): ActorRef = this.actorList.filter(elem => elem._1.equals(actorCode)).head._2

  def getLocalActor(actorName: GeneralActors): ActorRef = {
    getLocalActor(actorName.toString)
  }

  def getLocalActor(actorName: CharacterActors, id: Int): ActorRef = {
    getLocalActor(actorName.toString + id)
  }

  def getRemoteActor(systemName: String, path: String, actorName: String): ActorSelection = {
    val tmp = new StringBuilder(60)
    tmp.append("akka.tcp://")
    tmp.append(systemName)
    tmp.append("@")
    tmp.append(this.ipToConnect.get)
    tmp.append(":")
    tmp.append(this.portToConnect)
    tmp.append(path)
    tmp.append(actorName)
    this.system.actorSelection(tmp.toString())
  }

  def shutdownSystem(): Unit = this.system.terminate()
}