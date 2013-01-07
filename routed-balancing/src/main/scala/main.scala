import akka.actor._
import akka.routing._

class Main

object Main {

  def main(args: Array[String]) {
    
    val system = ActorSystem()

    val routees = Vector[ActorRef](
      system.actorOf(Props(new Actor() {
        override def preStart() {
          Thread.sleep(2000)
        }
        def receive = {
          case s: Int => println("Tortoise: %d".format(s))
        }
      }).withDispatcher("aesop-dispatcher")),
      system.actorOf(Props(new Actor() {
        def receive = {
          case s: Int => println("Hare: %d".format(s))
        }
      }).withDispatcher("aesop-dispatcher"))
    )

    val router = system.actorOf(
      props = Props().withRouter(
        new RoundRobinRouter(
          routees = routees.map(_.path.toString)
        )
      ), 
      name = "router"
    )

    1 to 10 foreach {
      i => router ! i
      Thread.sleep(400)
    }

    system.shutdown()

  }

}

