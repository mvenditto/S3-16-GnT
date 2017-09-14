package com.unibo.s3.main_system.communication
import akka.actor.{ActorRef, Props, UntypedAbstractActor}
import com.badlogic.gdx.ai.steer.Proximity.ProximityCallback
import com.badlogic.gdx.ai.steer.{Proximity, Steerable}
import com.badlogic.gdx.ai.steer.proximities.FieldOfViewProximity
import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.characters.BaseCharacter
import com.unibo.s3.main_system.communication.Messages.{AskNeighboursMsg, InitialSavingCharacterMsg, RebuildQuadTreeMsg, SendNeighboursMsg}
import com.unibo.s3.main_system.world.spatial.{Bounds, QuadTreeNode}
import com.unibo.s3.main_system.communication.Messages._
import com.unibo.s3.main_system.util.GdxImplicits._
import com.unibo.s3.main_system.world.actors.{FilterReachableByRay, SendFilterReachableByRay}

case class AskNeighboursWithFovMsg(character: BaseCharacter)

class QuadTreeActor extends UntypedAbstractActor {

  private[this] var agentsTable = Map[BaseCharacter, ActorRef]()
  private[this] var nearbyRequestCache = Map[Int, Iterable[BaseCharacter]]()
  private[this] var bounds = Bounds(0, 0, 100, 100)
  private[this] var root = QuadTreeNode[BaseCharacter](Bounds(0, 0, 60, 60))
  private[this] val queryRadius = 5f

  private def queryForNeighbors(c: BaseCharacter): Iterable[BaseCharacter] = {
    val pos = c.getPosition.cpy().sub(queryRadius, queryRadius)
    val twiceQueryRadius= 2 * queryRadius
    val neighbours = root.rangeQuery(
      Bounds(pos.x, pos.y, twiceQueryRadius, twiceQueryRadius))
    neighbours
  }

  override def onReceive(message: Any): Unit = message match {
    case MapSettingsMsg(w, h) =>
      bounds = Bounds(0, 0, w, h)

    case msg: InitialSavingCharacterMsg =>
      agentsTable += (msg.newCharacter -> msg.characterRef)

    case RebuildQuadTreeMsg() =>
      root = QuadTreeNode(bounds)
      agentsTable.keys.foreach(c => root.insert(c))

    case AskNeighboursMsg(character) =>
      val neighbours = queryForNeighbors(character)
      sender ! SendNeighboursMsg(neighbours.map(c => agentsTable(c)))

    case AskNeighboursWithFovMsg(c) =>
      val fov = c.getFieldOfView
      val neighbors = queryForNeighbors(c).map { x: Steerable[Vector2] => x }
      var neighborsInFov = List[BaseCharacter]()

      fov.setAgents(neighbors.asGdxArray)
      fov.findNeighbors(new ProximityCallback[Vector2] {
        override def reportNeighbor(n: Steerable[Vector2]): Boolean = n match {
          case nx: BaseCharacter => neighborsInFov :+= nx; true
          case _ => false
        }
      })

      if (neighborsInFov.nonEmpty) {

        val filterOnlyOnSightLine =
          FilterReachableByRay(c, neighborsInFov.map(p => p.getPosition))

        nearbyRequestCache += (c.getId -> neighborsInFov)

        SystemManager.getLocalGeneralActor(
          GeneralActors.WORLD_ACTOR) ! filterOnlyOnSightLine

      } else {
        sender ! SendNeighboursMsg(List())
      }

    case SendFilterReachableByRay(filter, reqId) =>
      val onlyVisible = nearbyRequestCache(reqId)
        .zip(filter)
        .collect{case (x, true) => x}

      nearbyRequestCache -= reqId
      val requester = agentsTable(agentsTable.keys.filter(a => a.getId == reqId).head)
      requester ! SendAllCharactersMsg(onlyVisible)//.map(a => agentsTable(a)))


    case AskAllCharactersMsg =>
      sender ! SendAllCharactersMsg(agentsTable.keys)

    case _ => println("(quadTreeActor) message unknown:" + message)
  }
}

object QuadTreeActor {
  def props(): Props = Props(new QuadTreeActor())
}