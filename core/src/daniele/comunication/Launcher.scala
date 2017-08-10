package daniele.comunication

import java.net.InetAddress

import com.typesafe.config.ConfigFactory
import daniele.comunication.Messages.IntMsg

object RemoteLauncher extends App {
  val confText =
    "{\"akka\":{\"actor\":{\"provider\":\"akka.remote.RemoteActorRefProvider\"}," +
      "\"loglevel\":\"INFO\",\"remote\":{\"enabled-transports\":[\"akka.remote.netty.tcp\"]" +
      ",\"log-received-messages\":\"on\",\"log-sent-messages\":\"on\"" +
      ",\"netty\":{\"tcp\":{\"hostname\":\""+ InetAddress.getLocalHost.getHostAddress+"\",\"port\":2727}}}}}"
  val customConf = ConfigFactory.parseString(confText)
  SystemManager.getInstance().createSystem("RemoteSystem", customConf)
  SystemManager.getInstance().createActor(IntActor.props("Remote"), "remote")
  println("remote ready, ip: " + InetAddress.getLocalHost.getHostAddress)
}

object LocalLauncher extends App {
  val confText =
    "{\"akka\":{\"actor\":{\"provider\":\"akka.remote.RemoteActorRefProvider\"}," +
      "\"loglevel\":\"INFO\",\"remote\":{\"enabled-transports\":[\"akka.remote.netty.tcp\"]" +
      ",\"log-received-messages\":\"on\",\"log-sent-messages\":\"on\"" +
      ",\"netty\":{\"tcp\":{\"hostname\":\""+ InetAddress.getLocalHost.getHostAddress+"\",\"port\":5050}}}}}"
  val customConf = ConfigFactory.parseString(confText)
  SystemManager.getInstance().createSystem("LocalSystem", customConf)
  val local = SystemManager.getInstance().createActor(IntActor.props("Local"), "local")
  val remoteActor = SystemManager.getInstance().getRemoteActor("RemoteSystem", "192.168.1.12", "2727", "/user/remote")
  remoteActor.tell(IntMsg(0), local)
}
