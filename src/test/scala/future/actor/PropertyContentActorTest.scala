package future.actor

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import future.message.{LookupFailed, LookupHotels, PersistContent}
import future.service.EvenToStringHotelService
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.concurrent.duration._

class PropertyContentActorTest extends TestKit(ActorSystem("TestSystem"))
with FlatSpecLike with BeforeAndAfterAll with Matchers with ImplicitSender with ScalaFutures {

  override def afterAll() {

    system.shutdown()
  }

  "A Property Content Actor" should "find all available hotels" in {

    val pause: Long = 30L

    val lookupActor: ActorRef = system.actorOf(HotelLookupActor.props(new EvenToStringHotelService(pause)), "lookup")
    val contentActor: ActorRef = system.actorOf(PropertyContentActor.props(lookupActor), "content")

    contentActor ! LookupHotels(List(2, 4, 6))

    expectMsg(2 seconds, PersistContent(List("2", "4", "6")))
  }

  "A Property Content Actor" should "fail gracefully when future timeout exceeded" in {

    val pause: Long = 200L

    val lookupActor: ActorRef = system.actorOf(HotelLookupActor.props(new EvenToStringHotelService(pause)), "lookup")
    val contentActor: ActorRef = system.actorOf(PropertyContentActor.props(lookupActor, 100 millis), "content")

    contentActor ! LookupHotels(List(2))

    expectMsgType[LookupFailed](2 seconds)
  }
}