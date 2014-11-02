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

  def findAllContent(ids: List[Int]): Unit = {

    log.info("looking up ids[{}]", ids)

    val results: Future[(List[Hotel], List[Rating])] = lookupIds(ids)

    val caller = sender()

    results onSuccess {
      case result => sendSuccessMessage(combineResults(result), caller)
    }

    results onFailure {
      case e: Throwable => sendFailureMessage(e, caller)
    }
  }

  def combineResults(results: (List[Hotel], List[Rating])): List[Content] = {
    results._1.zip(results._2) map {
      result => Content(result._1, result._2)
    }
  }

  def sendSuccessMessage(hotels: List[Content], caller: ActorRef): Unit = {

    log.info("completed processing messages[{}]", hotels)

    caller ! PersistContent(hotels)
  }

  def sendFailureMessage(e: Throwable, ref: ActorRef): Unit = {

    log.info("failed to process messsages with error[{}]", e.getMessage)

    ref ! LookupFailed(e)
  }

  def lookupIds(ids: List[Int]): Future[(List[Hotel], List[Rating])] = {

    val hotels: List[Future[Hotel]] = ids map {
      id => askForHotelBy(id)
    }

    val ratings: List[Future[Rating]] = ids map {
      id => askForRatingBy(id)
    }

    val hotelSeq = Future.sequence(hotels)
    val ratingSeq = Future.sequence(ratings)
    
    for {
      h <- hotelSeq
      r <- ratingSeq
    } yield (h, r)
  }

  def askForHotelBy(id: Int): Future[Hotel] = {
    (lookupActor ? id).mapTo[Hotel]
  }

  def askForRatingBy(id: Int): Future[Rating] = {
    (ratingActor ? id).mapTo[Rating]
  }
}
