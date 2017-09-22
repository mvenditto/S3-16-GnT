package communication

import java.net.InetAddress

import akka.actor.{Props, UntypedAbstractActor}
import com.unibo.s3.main_system.communication.Messages.{GenerateMapMsg, MapElementMsg}
import com.unibo.s3.main_system.communication.SystemManager
import com.unibo.s3.main_system.game.{ComputeSystemPort, GUISystemPort, Ports}

class TestActor extends UntypedAbstractActor {

  override def onReceive(message: Any): Unit = message match {
    case _: GenerateMapMsg => sender().tell(MapElementMsg("0.0:0.0:0.0:0.0"), getSelf())
    case msg: MapElementMsg => println("received map element: " + msg.line)
    case _ => println("(testActor) message unknown: " + message)
  }
}

object TestActor {
  def props(): Props = Props(new TestActor())
}

object RemoteLauncher extends App {
  SystemManager.createSystem("ComputeSystem", Option[String](InetAddress.getLocalHost.getHostAddress),
    Option[Ports](ComputeSystemPort))
  SystemManager.createActor(TestActor.props(), "remoteActor")
  println("remote ready, ip: " + InetAddress.getLocalHost.getHostAddress)
}

object LocalLauncher extends App {
  SystemManager.createSystem("ComputeSystem", Option[String](InetAddress.getLocalHost.getHostAddress),
    Option[Ports](GUISystemPort))
  SystemManager.setIPForRemoting(InetAddress.getLocalHost.getHostAddress, ComputeSystemPort)
  val remoteActor = SystemManager.getRemoteActor("ComputeSystem", "/user/", "remoteActor")
  val localActor = SystemManager.createActor(TestActor.props(), "localActor")
  remoteActor.tell(GenerateMapMsg(), localActor)
}
