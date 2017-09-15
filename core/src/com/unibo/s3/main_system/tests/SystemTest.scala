package com.unibo.s3.main_system.tests
import java.{lang, util}

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import com.unibo.s3.main_system.communication._
import com.unibo.s3.main_system.world.actors.WorldActor

class SystemTest extends TestClass{

  def createSistem(): lang.Boolean = {
    SystemManager.createSystem("System", None)
    SystemManager.createGeneralActor(MasterActor.props, GeneralActors.MASTER_ACTOR)
    SystemManager.createGeneralActor(WorldActor.props(new World(new Vector2(0, 0), true)), GeneralActors.WORLD_ACTOR)
    SystemManager.createGeneralActor(QuadTreeActor.props, GeneralActors.QUAD_TREE_ACTOR)
    SystemManager.createGeneralActor(MapActor.props, GeneralActors.MAP_ACTOR)
    SystemManager.createGeneralActor(GraphActor.props, GeneralActors.GRAPH_ACTOR)

    !(SystemManager.getLocalGeneralActor(GeneralActors.MASTER_ACTOR) == null || SystemManager.getLocalGeneralActor(GeneralActors.MAP_ACTOR) == null || SystemManager.getLocalGeneralActor(GeneralActors.GRAPH_ACTOR) == null || SystemManager.getLocalGeneralActor(GeneralActors.QUAD_TREE_ACTOR) == null)
  }

  override def doTests(): util.Map[String, lang.Boolean] = {
    var res = new util.HashMap[String, lang.Boolean]()
    res.put("Actor system generation", createSistem())
    res
  }
}
