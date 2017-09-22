package com.unibo.s3.main_system.communication

import java.beans.{PropertyChangeEvent, PropertyChangeListener}
import java.net.InetAddress

import akka.actor.{Props, UntypedAbstractActor}
import akka.util.Timeout
import com.unibo.s3.main_system.communication.Messages.{ACKComputationNode, AskIPMsg, SendIPMsg}
import com.unibo.s3.main_system.game.{AkkaSystemNames, GUISystemPort}

import scala.concurrent.duration._
import akka.pattern.ask
import com.badlogic.gdx.Gdx

import scala.concurrent.{Await, TimeoutException}
/**
  * Actor used for handshake between systems
  * @author Daniele Rosetti
  * @author Sara Sintoni
  */
class CommunicatorActor extends UntypedAbstractActor{

  implicit val timeout: Timeout = Timeout(1.5 seconds)

  private def showDialog(): Unit = {
    import javax.swing.JOptionPane
    val optionPane = new JOptionPane("Connection with compute node is not available. " +
      "\n Run compute application and check connection.", JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION)
    val dialog = optionPane.createDialog("Alert")
    optionPane.addPropertyChangeListener(new PropertyChangeListener {
      override def propertyChange(evt: PropertyChangeEvent): Unit = Gdx.app.exit()
    })
    dialog.setAlwaysOnTop(true)
    dialog.setVisible(true)
  }

  override def onReceive(message: Any): Unit = message match {
    case _:AskIPMsg => val ref = SystemManager.getRemoteActor(AkkaSystemNames.ComputeSystem, "/user/",
      GeneralActors.COMMUNICATOR_ACTOR.name)
      val future = ref ? SendIPMsg(InetAddress.getLocalHost.getHostAddress)
      try {
        Await.result(future, timeout.duration).asInstanceOf[ACKComputationNode]
      } catch {
        case _: TimeoutException => showDialog()
      }

    case msg:SendIPMsg =>
      SystemManager.setIPForRemoting(msg.IP, GUISystemPort)
      sender() ! ACKComputationNode()

    case _: Any =>
  }
}

/**
  * Companion object of CommunicatorActor
  * @author Daniele Rosetti
  */
object CommunicatorActor{
  def props(): Props = Props(new CommunicatorActor())
}
