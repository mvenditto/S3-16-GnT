package com.unibo.s3.main_system.communication


import akka.actor.{ActorRef, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import com.unibo.s3.main_system.communication.Messages.RestartMsg
import com.unibo.s3.main_system.game.{AkkaSystemNames, ComputeSystemPort, Ports}

/**
  * Manager of Akka system
  * @author Daniele Rosetti
  */
object SystemManager {
  private[this] type GeneralActors = GeneralActors.Value
  private[this] type CharacterActors = CharacterActors.Value

  private[this] var system: ActorSystem = _
  private[this] var actorList = Map[String, ActorRef]()

  private[this] var ipToConnect: Option[String] = Option.empty
  private[this] var portToConnect: Ports = _

  /**
    * Create an actor system
    * @param systemName Name of system
    * @param ip Address of remote system
    * @param portNumber Number of port for connection
    */
  def createSystem(systemName: String, ip: Option[String], portNumber: Option[Ports]): Unit = {
    if(portNumber.isDefined && ip.isDefined)
      this.system = ActorSystem.create(systemName, ConfigFactory.parseString(
        "{\"akka\":{\"actor\":{\"provider\":\"akka.remote.RemoteActorRefProvider\"}," +
          "\"loglevel\":\"OFF\",\"remote\":{\"enabled-transports\":[\"akka.remote.netty.tcp\"]" +
          ",\"log-received-messages\":\"on\",\"log-sent-messages\":\"on\"" +
          ",\"netty\":{\"tcp\":{\"hostname\":\""+ ip.get +"\",\"port\":"+portNumber.get.portNumber+"}}}}}"))
    else {
      this.system = ActorSystem.create(systemName)
    }
  }

  /**
    * Set elements for connect to another system
    * @param ip IP to connect
    * @param portNumber Port where send
    */
  def setIPForRemoting(ip: String, portNumber: Ports): Unit = {
    this.ipToConnect = Option.apply(ip)
    this.portToConnect = portNumber
  }

  /**
    * Get IP of system
    * @return IP of system
    */
  def getIP: Option[String] = {
    this.ipToConnect
  }

  /**
    * Create an actor with name
    * @param props Actor as to be create
    * @param actorCode Name of actor
    * @return Reference of actor
    */
  def createActor(props: Props, actorCode: String): ActorRef = {
    val ref: ActorRef = this.system.actorOf(props, actorCode)
    this.actorList += actorCode -> ref
    ref
  }

  /**
    * Create an actor with name
    * @param props Actor as to be create
    * @param actorName Name of actor
    * @return Reference to actor
    */
  def createActor(props: Props, actorName: GeneralActors): ActorRef = {
    createActor(props, actorName.toString)
  }

  /**
    * Create a guard or a thief
    * @param props Actor as to be create
    * @param actorName Type of character
    * @param id ID of character
    * @return Reference to actor
    */
  def createActor(props: Props, actorName: CharacterActors, id: Int): ActorRef = {
    createActor(props, actorName.toString + id)
  }

  /**
    * Get reference to an actor
    * @param actorCode Name of actor
    * @return Reference to actor
    */
  def getLocalActor(actorCode: String): ActorRef = this.actorList.filter(elem => elem._1.equals(actorCode)).head._2

  /**
    * Get reference to an actor
    * @param actorName Name of actor
    * @return Reference to actor
    */
  def getLocalActor(actorName: GeneralActors): ActorRef = {
    getLocalActor(actorName.toString)
  }

  /**
    * Get reference to a guard or thief
    * @param actorName Name of actor
    * @param id ID of character
    * @return Reference to actor
    */
  def getLocalActor(actorName: CharacterActors, id: Int): ActorRef = {
    getLocalActor(actorName.toString + id)
  }

  /**
    * Get reference to an actor in another Akka system
    * @param systemName Name of remote system
    * @param path Path of actor in the remote system
    * @param actorName Name of actor
    * @return Reference to remote actor
    */
  def getRemoteActor(systemName: String, path: String, actorName: String): ActorSelection = {
    val tmp = new StringBuilder(60)
    tmp.append("akka.tcp://")
    tmp.append(systemName)
    tmp.append("@")
    tmp.append(this.ipToConnect.get)
    tmp.append(":")
    tmp.append(this.portToConnect.portNumber)
    tmp.append(path)
    tmp.append(actorName)
    this.system.actorSelection(tmp.toString())
  }

  /**
    * Restart the Compute node
    */
  def restartSystem(): Unit = {
    if(this.portToConnect.portNumber == ComputeSystemPort.portNumber) {
      this.getRemoteActor(AkkaSystemNames.ComputeSystem, "/user/", GeneralActors.MAP_ACTOR.name) ! RestartMsg()
      this.getRemoteActor(AkkaSystemNames.ComputeSystem, "/user/", GeneralActors.GRAPH_ACTOR.name) ! RestartMsg()
      this.getRemoteActor(AkkaSystemNames.ComputeSystem, "/user/", GeneralActors.SPAWN_ACTOR.name) ! RestartMsg()
    }
  }

  /**
    * Terminate the Akka system
    */
  def shutdownSystem(): Unit = {
    this.system.terminate()
  }
}