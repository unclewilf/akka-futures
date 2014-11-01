package future.actor

import akka.actor.{Actor, Props}
import akka.pattern.pipe
import future.service.HotelService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object HotelLookupActor {
  def props(service: HotelService): Props = Props(new HotelLookupActor(service))
}

class HotelLookupActor(service: HotelService) extends Actor {

  override def receive: Receive = {
    case id: Int => respondToSender(id)
  }

  def respondToSender(i: Int): Unit = {
    val hotel: Future[String] = service.find(i)

    pipe(hotel) to sender
  }
}
