import akka.actor._, akka.event._
import com.typesafe.config.ConfigFactory

object Main {

  def main(args: Array[String]) {

    val config = ConfigFactory.load(ConfigFactory.parseString("""
      akka {
        event-handlers = ["No"]
      }
    """))

    val system = ActorSystem("TheSystem", config)

    val a = system.actorOf(Props(new Actor with ActorLogging {
      def receive = { case m => log warning "hi" }
    }))

    a ! ()
    Thread.sleep(1000)
    system.shutdown()
  }

}

class No extends Actor {
  def receive = {
    case Logging.InitializeLogger(_) => sender ! Logging.LoggerInitialized
  }
}

