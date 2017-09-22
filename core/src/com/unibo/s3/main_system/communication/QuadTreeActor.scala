package com.unibo.s3.main_system.communication
import akka.actor.{ActorRef, Props, UntypedAbstractActor}
import com.badlogic.gdx.ai.steer.Proximity.ProximityCallback
import com.badlogic.gdx.ai.steer.Steerable
import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.characters.BaseCharacter
import com.unibo.s3.main_system.characters.Guard
import com.unibo.s3.main_system.characters.Thief
import com.unibo.s3.main_system.communication.Messages.{AskNeighboursMsg, InitialSavingCharacterMsg, RebuildQuadTreeMsg, SendNeighboursMsg, _}
import com.unibo.s3.main_system.game.AkkaSystemNames
import com.unibo.s3.main_system.util.GdxImplicits._
import com.unibo.s3.main_system.world.actors.{FilterReachableByRay, SendFilterReachableByRay}
import com.unibo.s3.main_system.world.spatial.{Bounds, QuadTreeNode}

import scala.collection.mutable

case class AskNeighboursWithinFovMsg(character: BaseCharacter)
case class SendThievesInProximityMsg(thieves: Iterable[BaseCharacter])
case class SendGuardsInProximityMsg(thieves: Iterable[BaseCharacter])
case class AskNearbyGuardsMsg(c: BaseCharacter, r: Option[Float] = None)
case class SendNearbyGuardsMsg(n: Iterable[ActorRef])

class QuadTreeActor extends UntypedAbstractActor {

  type RequestId = (Long, Int)

  private[this] var agentsTable = mutable.AnyRefMap[BaseCharacter, ActorRef]()
  private[this] var nearbyRequestCache = mutable.AnyRefMap[RequestId, Iterable[BaseCharacter]]()
  private[this] var bounds = Bounds(0, 0, 100, 100)
  private[this] var root = QuadTreeNode[BaseCharacter](bounds)

  private def timestamp: Long = System.nanoTime()

  private def queryForNeighbors(c: BaseCharacter, r: Option[Float]): Iterable[BaseCharacter] = {
    val radius = if(r.isDefined) r.get else c.getFieldOfView.getRadius
    val pos = c.getPosition.cpy().sub(radius, radius)
    val twiceQueryRadius= 2 * radius
    val neighbours = root.rangeQuery(
      Bounds(pos.x, pos.y, twiceQueryRadius, twiceQueryRadius))
    neighbours
  }

  override def onReceive(message: Any): Unit = message match {
    case GameSettingsMsg(gs) =>
      bounds = Bounds(0, 0, gs.mapSize.x, gs.mapSize.y)

    case InitialSavingCharacterMsg(newCharacter, characterRef) =>
      agentsTable += (newCharacter -> characterRef)
      val ref = SystemManager.getLocalActor(GeneralActors.GAME_ACTOR)
      ref ! SendAllCharactersMsg(agentsTable.keys)
      val refLig = SystemManager.getLocalActor(GeneralActors.LIGHTING_SYSTEM_ACTOR)
      refLig ! SendAllCharactersMsg(agentsTable.keys)

    case RebuildQuadTreeMsg() =>
      root = QuadTreeNode(bounds)
      agentsTable.foreachKey(c => root.insert(c))

    case AskNearbyGuardsMsg(character, radius) =>
      val neighbours = queryForNeighbors(character, radius)
      sender ! SendNearbyGuardsMsg(neighbours.collect {
        case n: Guard if !agentsTable(n).equals(sender) => agentsTable(n)
      })

    case AskNeighboursMsg(character, radius) =>
      val neighbours = queryForNeighbors(character, radius)
      sender ! SendNeighboursMsg(neighbours.map(c => agentsTable(c)))

    case AskNeighboursWithinFovMsg(c) =>
      val fov = c.getFieldOfView
      val neighbors = queryForNeighbors(c, None).map { x: Steerable[Vector2] => x }
      var neighborsInFov = List[BaseCharacter]()

      fov.setAgents(neighbors.asGdxArray)
      fov.findNeighbors(new ProximityCallback[Vector2] {
        override def reportNeighbor(n: Steerable[Vector2]): Boolean = n match {
          case nx: BaseCharacter => neighborsInFov :+= nx; true
          case _ => false
        }
      })

      if (neighborsInFov.nonEmpty) {

        val reqId = (timestamp, c.getId)
        val filterOnlyOnSightLine =
          FilterReachableByRay(c.getPosition, neighborsInFov.map(p => p.getPosition), reqId)

        nearbyRequestCache += (reqId -> neighborsInFov)

        val refWorld = SystemManager.getLocalActor(GeneralActors.WORLD_ACTOR)
        refWorld ! filterOnlyOnSightLine

      } else {
        sender ! SendNeighboursMsg(List())
      }

    case SendFilterReachableByRay(filter, reqId) =>
      val onlyVisible = nearbyRequestCache(reqId)
        .zip(filter)
        .collect{case (x, true) => x}

      val reqCharacter = agentsTable.keys.filter(a => a.getId == reqId._2).head
      val requester = agentsTable(reqCharacter)

      requester ! SendNeighboursMsg(
        onlyVisible.filter(a => a match {
          case _ : Guard => true
          case _ => false
        }).map(a => agentsTable(a)))

      val thievesInProximity = onlyVisible.filter(a => a match {
          case t : Thief => !t.hasReachedExit && !t.gotCaughtByGuard
          case _ => false
        })

      requester ! SendThievesInProximityMsg(thievesInProximity)
      thievesInProximity.foreach(t =>
        agentsTable(t) ! SendGuardsInProximityMsg(List(reqCharacter)))

      nearbyRequestCache -= reqId

    case AskAllCharactersMsg =>
      sender ! SendAllCharactersMsg(agentsTable.keys)

    case _ =>
  }
}

object QuadTreeActor {
  def props(): Props = Props(new QuadTreeActor())
}