package future.actor

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import future.service.EvenToStringHotelService
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.concurrent.Future
import scala.concurrent.duration._

class HotelLookupActorTest extends TestKit(ActorSystem("TestSystem"))
with FlatSpecLike with BeforeAndAfterAll with Matchers with ImplicitSender with ScalaFutures {

  implicit val timeout = Timeout(5 seconds)

  override def afterAll() {

    system.shutdown()
  }

  "A HotelLookupActor" should "forward a successful result to its sender" in {

    val lookupActor: ActorRef = system.actorOf(HotelLookupActor.props(new EvenToStringHotelService))

    val futureRes: Future[String] = (lookupActor ? 2).mapTo[String]

    futureRes.futureValue should equal("2")
  }
}