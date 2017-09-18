package world

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.unibo.s3.main_system.communication.SystemManager
import org.junit.Assert.fail

import scala.concurrent.duration._

/**
  * Base actors test class including AkkaTestKit tools
  * + custom SystemManager's actor system.
  * @author mvenditto
*/
abstract class BaseTestInvolvingActors(val systemName: String)
  extends TestKit(ActorSystem(systemName)) with ImplicitSender {

  implicit val timeout = Timeout(5 seconds)

  protected val ErrBadMatch = "Failed to retrieve future or bad match."

  def deployActors()

  protected def initActorSystem(): Unit = {
    SystemManager.createSystem(systemName, None, None)
    deployActors()
  }

  protected def BadMatchFail(): Unit = {
    fail(ErrBadMatch)
  }
}
