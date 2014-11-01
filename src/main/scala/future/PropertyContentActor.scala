package future

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits._

object PropertyContentActor {
  def props(lookupActor: ActorRef): Props = Props(new PropertyContentActor(lookupActor))
}

class PropertyContentActor(lookupActor: ActorRef) extends Actor {

  implicit val timeout = Timeout(2 seconds)

  override def receive: Receive = {

    case ids: Seq[Int] => findAllContent(ids)
  }

  def findAllContent(ids: Seq[Int]): Unit = {

    val futures: Seq[Future[String]] = lookupIds(ids)

    val sequence: Future[Seq[String]] = Future.sequence(futures)

    sequence onSuccess {
      case hotels: Seq[String] => sender ! hotels
    }

    Await.result(sequence, 10 seconds)
  }

  def lookupIds(ids: Seq[Int]): Seq[Future[String]] = {

    ids map {
      id => (lookupActor ? id).mapTo[String]
    }
  }

}
