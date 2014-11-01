package future

import akka.actor.{Props, Actor}
import akka.pattern.pipe
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.Future
import ExecutionContext.Implicits.global

object HotelLookupActor {
  def props(service: HotelService): Props = Props(new HotelLookupActor(service))
}

class HotelLookupActor(service: HotelService) extends Actor {

  override def receive: Receive = {
    case id: Int => respondToSender(id)
    case _ => throw new IllegalArgumentException("Expecting a string")
  }

  def respondToSender(i: Int): Unit = {
    val hotel: Future[String] = service.find(i)

    pipe(hotel) to sender
  }
}
