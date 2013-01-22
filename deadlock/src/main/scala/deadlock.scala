import akka.actor._, akka.pattern.ask, akka.util.Timeout
import concurrent.Await, concurrent.duration._

object Deadlock {
  def main(args: Array[String]) {

    val actorSystem = ActorSystem()

    val alpha = actorSystem.actorOf(Props(new Actor {

      override def preStart() {

        context.actorOf(Props(new Actor {

          var a: ActorRef = null

          override def preStart() {
            a = context.actorOf(Props(new Actor {
              def receive = {
                case 'init => throw new RuntimeException()
              }
            }))
            self ! 'init
          }

          def receive = {
            case 'init => a ! 'init
          }

        }), "delta")

      }

      def receive = {
        case _ => throw new RuntimeException()
      }

    }), "alpha")

    val beta = actorSystem.actorOf(
      Props(new Actor {

        var a: ActorRef = null

        override def preStart() {
          a = context.actorOf(Props(new Actor {
            def receive = {
              case _ =>
                implicit val timeout: Timeout = 2.seconds
                Await.result((alpha ? ()), timeout.duration)
                sender ! 'fail
            }

          }))
          self ! 'init
        }

        def receive = {

          case 'init =>
            a ! 'request

          case 'fail =>
            val s = context.system; import s._
            scheduler.scheduleOnce(2.seconds, self, 'init)
        }

      }).withRouter(new akka.routing.RoundRobinRouter(2)),
      "beta"
    )

    Thread.sleep(800)
    println("\n\nHalting.")
    actorSystem.shutdown()
  }
}
