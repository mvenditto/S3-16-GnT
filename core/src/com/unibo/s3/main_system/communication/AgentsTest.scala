package com.unibo.s3.main_system.communication

import akka.actor.ActorRef
import com.badlogic.gdx.{ApplicationAdapter, Gdx}
import com.badlogic.gdx.graphics.{GL20, Texture}
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import com.unibo.s3.main_system.characters.BaseCharacter
import com.unibo.s3.main_system.communication.Messages.{ActMsg, CreateCharacterMsg, SendGraphMsg, SendNeighboursMsg}
import com.unibo.s3.main_system.world.actors.WorldActor
import org.jgrapht.graph.{DefaultEdge, SimpleGraph}

class AgentsTest extends ApplicationAdapter{

  private val testGraph = new SimpleGraph[Vector2, DefaultEdge](classOf[DefaultEdge])
  private val v1 = new Vector2(3f, 3f)
  private val v2 = new Vector2(7f, 3f)
  private val v3 = new Vector2(11f, 3f)
  private val v4 = new Vector2(11f, 7f)
  private val v5 = new Vector2(7f, 7f)

  override def create(): Unit = {
    testGraph.addVertex(v1)
    testGraph.addVertex(v2)
    testGraph.addVertex(v3)
    testGraph.addVertex(v4)
    testGraph.addVertex(v5)
    testGraph.addEdge(v1, v2)
    testGraph.addEdge(v1, v5)
    testGraph.addEdge(v2, v3)
    testGraph.addEdge(v3, v4)
    testGraph.addEdge(v5, v4)
    testGraph.addEdge(v2, v5)
    SystemManager.createSystem("System", null)
    SystemManager.createActor(WorldActor.props(new World(new Vector2(0, 0), true)), GeneralActors.WORLD_ACTOR)
    SystemManager.createActor(GraphActor.props(), GeneralActors.GRAPH_ACTOR)
    val mapActor = SystemManager.createActor(MapActor.props(), GeneralActors.MAP_ACTOR)
    SystemManager.createActor(QuadTreeActor.props(), GeneralActors.QUAD_TREE_ACTOR)
    val masterActor = SystemManager.createActor(MasterActor.props(), GeneralActors.MAP_ACTOR)

    masterActor ! ActMsg(0.016f)

    masterActor.tell(Messages.CreateCharacterMsg(new Vector2(1, 1)), ActorRef.noSender)
    masterActor.tell(Messages.CreateCharacterMsg(new Vector2(1, 1)), ActorRef.noSender)
    masterActor.tell(Messages.CreateCharacterMsg(new Vector2(1, 1)), ActorRef.noSender)

    var copOne = SystemManager.getLocalActor(CharacterActors.COP, 1)
    var copTwo = SystemManager.getLocalActor(CharacterActors.COP, 2)
    var copThree = SystemManager.getLocalActor(CharacterActors.COP, 3)

    copOne ! SendGraphMsg(testGraph)
    copTwo ! SendGraphMsg(testGraph)
    copThree ! SendGraphMsg(testGraph)

    var list1: List[ActorRef] = List(copTwo, copThree)
    var list2: List[ActorRef] = List(copOne)
    var list3: List[ActorRef] = List(copOne)

    copOne ! SendNeighboursMsg(list1)
    copTwo ! SendNeighboursMsg(list2)
    copThree ! SendNeighboursMsg(list3)

    copTwo ! SendNeighboursMsg(List())
  }

  override def render(): Unit = {
    Gdx.gl.glClearColor(1, 0, 0, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
  }

  override def dispose(): Unit = {}

}
