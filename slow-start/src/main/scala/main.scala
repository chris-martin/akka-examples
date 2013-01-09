import akka.actor._
import akka.routing._

class Main

object Main {

  def main(args: Array[String]) {
    
    val system = ActorSystem()

    val a = system.actorOf(Props(new Actor() {

      override def preStart() {
        Thread.sleep(2000)
      }

      def receive = {
        
        case i: Int => 
          sender ! (i + 1)

      }
    }))

    val b = system.actorOf(Props(new Actor() {
      def receive = {
        
        case 'start => 
          Thread.sleep(100)
          a ! 1
          Thread.sleep(100)
          a ! 3
          Thread.sleep(100)
          a ! 5
          println("started")
        
        case i: Int =>
          println(i)

      }
    }))

    println("starting")
    b ! 'start

    Thread.sleep(3000)

    system.shutdown()

  }

}

