import akka.actor._
import com.typesafe.config.ConfigFactory

object Main {

  def main(args: Array[String]) {

    val system = ActorSystem(
      "TheSystem", 
      ConfigFactory.load(ConfigFactory.parseString("""
        akka {
          event-handlers = []
        }
      """))
    )

    val a = system.actorOf(Props(new Actor with ActorLogging {
      def receive = {

        case i: Int => 
          log warning i.toString
          sender ! (i + 1)

      }
    }))

    val b = system.actorOf(Props(new Actor() {
      def receive = {
        
        case 'start => 
          Thread.sleep(100); a ! 1
          Thread.sleep(100); a ! 3
          Thread.sleep(100); a ! 5
        
        case i: Int =>
          println(i)

      }
    }))

    b ! 'start

    Thread.sleep(3000)

    system.shutdown()

  }

}

