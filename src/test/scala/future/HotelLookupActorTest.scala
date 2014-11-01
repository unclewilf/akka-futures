package future

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class HotelLookupActorTest extends TestKit(ActorSystem("TestSystem")) with FlatSpecLike with BeforeAndAfterAll with Matchers with ImplicitSender with ScalaFutures {

  implicit val timeout = Timeout(60 seconds)

  override def afterAll() {

    system.shutdown()
  }

  "A HotelLookupActor" should "forward a successful result to its sender" in {

    val lookupActor: ActorRef = system.actorOf(HotelLookupActor.props(new EvenToStringHotelService))

    val future: Future[Any] = lookupActor ? 2

    val result = Await.result(future, 2 seconds)

    result should be("2")
  }
}