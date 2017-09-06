package com.unibo.s3.main_system.communication

import akka.actor.{ActorRef, ActorSelection, ActorSystem, Props}
import com.typesafe.config.Config

object SystemManager {
  private[this] type GeneralActors = GeneralActors.Value
  private[this] type CharacterActors = CharacterActors.Value

  private[this] var system: ActorSystem = _
  private[this] var actorList: Map[String, ActorRef] = _

  def createSystem(systemName: String, config: Config): Unit = {
    this.system = ActorSystem.create(systemName, config)
  }

  def createActor(props: Props, actorName: GeneralActors): ActorRef = {
    createActor(props, actorName.toString)
    /*
    if(this.actorList == null) {
      this.actorList = Map()
    }
    val ref: ActorRef = this.system.actorOf(props, actorName.toString)
    this.actorList += actorName.toString -> ref
    ref*/
  }

  def createActor(props: Props, actorName: CharacterActors, id: Int): ActorRef = {
    val actorCode = actorName.toString + id
    createActor(props, actorCode)
    /*
    if(this.actorList == null) {
      this.actorList = Map()
    }
    val ref: ActorRef = this.system.actorOf(props, actorCode)
    this.actorList += actorCode -> ref
    ref*/
  }

  def getLocalActor(actorName: GeneralActors): ActorRef = {
    getActorRef(actorName.toString)
    //this.actorList.filter(elem => elem._1.equals(actorName.toString)).head._2
  }

  def getLocalActor(actorName: CharacterActors, id: Int): ActorRef = {
    val actorCode = actorName.toString + id
    getActorRef(actorCode.toString)
    //this.actorList.filter(elem => elem._1.equals(actorCode)).head._2
  }


  def getRemoteActor(systemName: String, ip: String, portNumber: String, path: String): ActorSelection = {
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

  private def createActor(props: Props, actorCode: String): ActorRef = {
    if(this.actorList == null) {
      this.actorList = Map()
    }
    val ref: ActorRef = this.system.actorOf(props, actorCode)
    this.actorList += actorCode -> ref
    ref
  }

  private def getActorRef(actorCode: String): ActorRef = this.actorList.filter(elem => elem._1.equals(actorCode)).head._2
}