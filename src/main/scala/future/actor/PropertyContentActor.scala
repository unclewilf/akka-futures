package future.actor

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import future.message.{LookupHotels, PersistContent}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scala.concurrent.duration._

object PropertyContentActor {
  def props(lookupActor: ActorRef): Props = Props(new PropertyContentActor(lookupActor))
}

class PropertyContentActor(lookupActor: ActorRef) extends Actor {

  implicit val timeout = Timeout(2 seconds)

  val log = Logging(context.system, this)

  override def receive: Receive = {

    case LookupHotels(ids) => findAllContent(ids)
  }

  def findAllContent(ids: Seq[Int]): Unit = {

    log.info("looking up ids[{}]", ids)

    val futures: Seq[Future[String]] = lookupIds(ids)

    val sequence: Future[Seq[String]] = Future.sequence(futures)
    val caller = sender()

    sequence onSuccess {
      case hotels: Seq[String] => sendSuccessMessage(hotels, caller)
    }
  }

  def sendSuccessMessage(hotels: Seq[String], caller: ActorRef) {

    log.info("completed processing messages[{}]", hotels)

    caller ! PersistContent(hotels)
  }

  def lookupIds(ids: Seq[Int]): Seq[Future[String]] = {

    ids map {
      id => (lookupActor ? id).mapTo[String]
    }
  }
}
