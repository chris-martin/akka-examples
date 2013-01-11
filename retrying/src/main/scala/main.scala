import akka.actor._
import akka.routing._
import SupervisorStrategy._
import scala.concurrent.duration._

trait Retrying {

  def retry(props: Props, message: AnyRef, interval: FiniteDuration = 1 second)
           (implicit topContext: ActorContext): ActorRef = {

    topContext.actorOf(Props(new Actor {

      var dangerousActor: Option[ActorRef] = None

      override def preStart() {
        init()
      }

      def init() {
        dangerousActor = Some(context.watch(context.actorOf(props, "attempt")))
        dangerousActor.get.tell(message, topContext.self)
      }

      override def supervisorStrategy = OneForOneStrategy(0) {
        case _: Throwable => Stop
      }

      def receive = {

        case Terminated(a) if Some(a) == dangerousActor =>
          dangerousActor = None
          val s = context.system; import s._
          scheduler.scheduleOnce(interval, self, Retrying.Retry)

        case Retrying.Retry =>
          init()

      }

    }), "retry")
  }

}

object Retrying {

  private case object Retry

}

class Main

object Main {

  def main(args: Array[String]) {

    val system = ActorSystem()

    var i = -5

    val dangerousProps = Props(new Actor {

      def receive = {
        case _ =>
          i += 1
          if (i <= 0) {
            throw new IllegalStateException(i.toString)
          } else {
            sender ! i
          }
      }

    })

    val a = system.actorOf(Props(new Actor with Retrying {

      override def preStart() {
        retry(dangerousProps, Unit)
      }

      def receive = {

        case i: Int =>
          println("# %d".format(i))

      }
    }), "xyz")


    Thread.sleep(6000)

    system.shutdown()

  }

}

class Log extends Actor {

  import akka.event.Logging._

  def receive = {

    case InitializeLogger(_) => sender ! LoggerInitialized

    case Error(cause, logSource, logClass, message) =>
      println("[ERROR] [%s] %s".format(logSource, message))

  }

}
