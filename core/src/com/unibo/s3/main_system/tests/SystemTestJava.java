package com.unibo.s3.main_system.tests;

import akka.actor.ActorRef;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.unibo.s3.main_system.communication.*;
import com.unibo.s3.main_system.world.actors.WorldActor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SystemTestJava implements TestClass {
    @Override
    public Map<String, Boolean> doTests() {
        Map<String, Boolean> res = new HashMap<>();
        res.put("Actor system generation", generationTest());
        return res;
    }

    private boolean generationTest() {
        SystemManager.createSystem("System", null);
        SystemManager.createGeneralActor(MasterActor.props(), GeneralActors.MASTER_ACTOR());
        SystemManager.createGeneralActor(WorldActor.props(new World(new Vector2(0, 0), true)), GeneralActors.WORLD_ACTOR());
        SystemManager.createGeneralActor(QuadTreeActor.props(), GeneralActors.QUAD_TREE_ACTOR());
        SystemManager.createGeneralActor(MapActor.props(), GeneralActors.MAP_ACTOR());
        SystemManager.createGeneralActor(GraphActor.props(), GeneralActors.GRAPH_ACTOR());

        return !(SystemManager.getLocalGeneralActor(GeneralActors.MASTER_ACTOR()) == null ||
                SystemManager.getLocalGeneralActor(GeneralActors.MAP_ACTOR()) == null ||
                SystemManager.getLocalGeneralActor(GeneralActors.GRAPH_ACTOR()) == null ||
                SystemManager.getLocalGeneralActor(GeneralActors.QUAD_TREE_ACTOR()) == null);

    }
}
