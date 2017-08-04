package daniele.comunication

import java.net.InetAddress

import akka.actor.{ActorRef, ActorSelection}
import com.typesafe.config.{Config, ConfigFactory}
import daniele.utils.IntMsg

object RemoteLauncher extends App {
  val confText: String =
    "{\"akka\":{\"actor\":{\"provider\":\"akka.remote.RemoteActorRefProvider\"}," +
      "\"loglevel\":\"INFO\",\"remote\":{\"enabled-transports\":[\"akka.remote.netty.tcp\"]" +
      ",\"log-received-messages\":\"on\",\"log-sent-messages\":\"on\"" +
      ",\"netty\":{\"tcp\":{\"hostname\":\""+ InetAddress.getLocalHost.getHostAddress+"\",\"port\":2727}}}}}"
  val customConf: Config = ConfigFactory.parseString(confText)
  SystemManager.getInstance().createSystem("RemoteSystem", customConf)
  SystemManager.getInstance().getSystem("RemoteSystem").actorOf(LocalActor.props("Remote"), "remote")
  println("remote ready, ip: " + InetAddress.getLocalHost.getHostAddress)
}

object LocalLauncher extends App {
  val confText: String =
    "{\"akka\":{\"actor\":{\"provider\":\"akka.remote.RemoteActorRefProvider\"}," +
      "\"loglevel\":\"INFO\",\"remote\":{\"enabled-transports\":[\"akka.remote.netty.tcp\"]" +
      ",\"log-received-messages\":\"on\",\"log-sent-messages\":\"on\"" +
      ",\"netty\":{\"tcp\":{\"hostname\":\""+ InetAddress.getLocalHost.getHostAddress+"\",\"port\":5050}}}}}"
  val customConf: Config = ConfigFactory.parseString(confText)
  SystemManager.getInstance().createSystem("LocalSystem", customConf)
  val local: ActorRef = SystemManager.getInstance().getSystem("LocalSystem").actorOf(LocalActor.props("Local"), "local")
  val remoteActor: ActorSelection = SystemManager.getInstance().getSystem("LocalSystem").actorSelection("akka.tcp://RemoteSystem@192.168.1.12:2727/user/remote")
  println("That 's remote: " + remoteActor)
  remoteActor.tell(new IntMsg(0), local)
}
