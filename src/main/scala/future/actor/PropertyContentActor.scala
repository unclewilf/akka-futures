package future.actor

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import future.domain.{Rating, Content, Hotel}
import future.message.{LookupFailed, LookupHotels, PersistContent}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scala.concurrent.duration._

object PropertyContentActor {
  def props(lookupActor: ActorRef, ratingActor: ActorRef, timeout: Timeout = 2.seconds): Props =
    Props(new PropertyContentActor(lookupActor, ratingActor, timeout))
}

class PropertyContentActor(lookupActor: ActorRef, ratingActor: ActorRef, t: Timeout) extends Actor {

  implicit val timeout = t

  val log = Logging(context.system, this)

  override def receive: Receive = {

    case LookupHotels(ids) => findAllContent(ids)
  }

  def findAllContent(ids: Seq[Int]): Unit = {

    log.info("looking up ids[{}]", ids)

    val futures: Seq[Future[Content]] = lookupIds(ids)

    val sequence: Future[Seq[Content]] = Future.sequence(futures)
    val caller = sender()

    sequence onSuccess {
      case hotels: Seq[Content] => sendSuccessMessage(hotels, caller)
    }

    sequence onFailure {
      case e: Throwable => sendFailureMessage(e, caller)
    }
  }

  def sendSuccessMessage(hotels: Seq[Content], caller: ActorRef): Unit = {

    log.info("completed processing messages[{}]", hotels)

    caller ! PersistContent(hotels)
  }

  def sendFailureMessage(e: Throwable, ref: ActorRef): Unit = {

    log.info("failed to process messsages with error[{}]", e.getMessage)

    ref ! LookupFailed(e)
  }

  def lookupIds(ids: Seq[Int]): Seq[Future[Content]] = {

    ids map {
      id => {
        for {
          hotel ← askForHotelBy(id)
          rating ← askForRatingBy(id)
        } yield Content(hotel, rating)
      }
    }
  }

  def askForHotelBy(id: Int): Future[Hotel] = {
    (lookupActor ? id).mapTo[Hotel]
  }

  def askForRatingBy(id: Int): Future[Rating] = {
    (ratingActor ? id).mapTo[Rating]
  }
}
