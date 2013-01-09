import akka.actor._
import akka.routing._

class Main

object Main {

  def main(args: Array[String]) {
    
    val system = ActorSystem()

    val a = system.actorOf(Props(new Actor() {
      def receive = {
        
        case i: Int => 
          Thread.sleep(1000)
          sender ! (i + 1)

      }
    }))

    val b = system.actorOf(Props(new Actor() {
      def receive = {
        
        case "start" => 
          a ! 1
          Thread.sleep(1500)
          a ! 3
          context.stop(a)
          a ! 5
        
        case i: Int =>
          println(i)

      }
    }))

    b ! "start"

    Thread.sleep(6000)

    system.shutdown()

  }

}

