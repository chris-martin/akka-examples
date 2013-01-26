import akka.actor._
import com.typesafe.config.ConfigFactory

object Main {

  def main(args: Array[String]) {

    val config = ConfigFactory.load(ConfigFactory.parseString("""
      akka {
        event-handlers = []
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

