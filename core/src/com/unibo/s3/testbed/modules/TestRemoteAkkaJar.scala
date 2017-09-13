package com.unibo.s3.testbed.modules

import akka.actor.{ActorSystem, Props, UntypedAbstractActor}
import com.typesafe.config.ConfigFactory
import com.unibo.s3.main_system.communication.SystemManager
import com.unibo.s3.testbed.model.{BaseTestbedModule, Testbed}

class TestRemoteAkkaJar extends BaseTestbedModule{

  private class DummyActor extends UntypedAbstractActor {
    override def onReceive(message: Any): Unit = message match {
      case x => println(x, sender.path)
    }
  }

  object DummyActor {
    def props(): Props = Props(new DummyActor())
  }

  override def init(owner: Testbed): Unit = {
    super.init(owner)
    val hostAddress = "127.0.0.1"
    val confText = "{\"akka\":{\"actor\":{\"provider\":\"akka.remote.RemoteActorRefProvider\"}," + "\"loglevel\":\"INFO\",\"remote\":{\"enabled-transports\":[\"akka.remote.netty.tcp\"]" + ",\"log-received-messages\":\"on\",\"log-sent-messages\":\"on\"" + ",\"netty\":{\"tcp\":{\"hostname\":\"" + hostAddress + "\",\"port\":2727}}}}}"
    var customConf = ConfigFactory.parseString(confText)
    SystemManager.createSystem("LocalSystem", customConf)
    val localActor = SystemManager.createActor(DummyActor.props(), "localActor")
    customConf = ConfigFactory.parseString(confText.replace("2727", "5050"))
    val remoteSystem = ActorSystem.create("RemoteSystem", customConf)
    remoteSystem.actorOf(DummyActor.props(), "remoteActor")
    val remoteActor = SystemManager.getRemoteActor("RemoteSystem", hostAddress, "5050", "/user/remoteActor")

    remoteActor.tell("ciao", localActor)
    localActor ! "il jar funziona (local)!"

  }

}
