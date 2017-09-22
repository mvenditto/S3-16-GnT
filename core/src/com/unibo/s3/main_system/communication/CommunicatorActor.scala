package com.unibo.s3.main_system.communication

import java.net.InetAddress

import akka.actor.{Props, UntypedAbstractActor}
import akka.util.Timeout
import com.unibo.s3.main_system.communication.Messages.{ACKComputationNode, AskIPMsg, SendIPMsg}
import com.unibo.s3.main_system.game.AkkaSettings
import scala.concurrent.duration._
import akka.pattern.ask

import scala.concurrent.{Await, TimeoutException}

class CommunicatorActor extends UntypedAbstractActor{

  implicit val timeout = Timeout(1 seconds)

  def showDialog(): Unit = {
    import javax.swing.JOptionPane
    val optionPane = new JOptionPane("Connection with compute node is not available. " +
      "\n Run compute application and check connection.", JOptionPane.ERROR_MESSAGE)
    val dialog = optionPane.createDialog("Alert")
    dialog.setAlwaysOnTop(true)
    dialog.setVisible(true)
  }

  override def onReceive(message: Any): Unit = message match {
    case _:AskIPMsg => val ref = SystemManager.getRemoteActor(AkkaSettings.ComputeSystem, "/user/",
      GeneralActors.COMMUNICATOR_ACTOR.name)
      println("ref = " + ref.toString())
      val future = ref ? SendIPMsg(InetAddress.getLocalHost.getHostAddress)
      try {
        Await.result(future, timeout.duration).asInstanceOf[ACKComputationNode]
      } catch {
        case e: TimeoutException => showDialog()
      }
    case msg:SendIPMsg => println("IP ricevuto")
      SystemManager.setIPForRemoting(msg.IP, AkkaSettings.GUISystemPort)
      sender() ! ACKComputationNode()
    case msg: Any => println(msg)
  }
}

object CommunicatorActor{
  def props(): Props = Props(new CommunicatorActor())
}
