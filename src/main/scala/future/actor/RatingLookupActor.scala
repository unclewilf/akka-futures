package future.actor

import akka.actor.{Actor, Props}
import akka.pattern.pipe
import future.domain.Rating
import future.service.RatingService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object RatingLookupActor {
  def props(service: RatingService): Props = Props(new RatingLookupActor(service))
}

class RatingLookupActor(service: RatingService) extends Actor {

  override def receive: Receive = {
    case id: Int => respondToSender(id)
  }

  def respondToSender(i: Int): Unit = {
    val hotel: Future[Rating] = service.find(i)

    pipe(hotel) to sender
  }
}
