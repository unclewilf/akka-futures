package future.actor

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.contrib.throttle.Throttler.{SetTarget, _}
import akka.contrib.throttle.TimerBasedThrottler
import akka.testkit.{ImplicitSender, TestKit}
import future.message.{LookupFailed, LookupHotels, PersistContent}
import future.service.EvenToStringHotelService
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.concurrent.duration._

class PropertyContentActorThrottlingTest extends TestKit(ActorSystem("TestSystem"))
with FlatSpecLike with BeforeAndAfterAll with Matchers with ImplicitSender with ScalaFutures {

  val servicePause = 100L

  override def afterAll() {

    system.shutdown()
  }

  "A Property Content Actor" should "find all available hotels within acceptable throttle limit" in {

    val throttler: ActorRef = createThrottler(3, 1.second)
    val contentActor: ActorRef = system.actorOf(PropertyContentActor.props(throttler))

    contentActor ! LookupHotels(List(2, 4, 6))

    expectMsg(2 seconds, PersistContent(List("2", "4", "6")))
  }

  "A Property Content Actor" should "throw exception when throttle times out" in {

    val throttler: ActorRef = createThrottler(2, 1.second)
    val contentActor: ActorRef = system.actorOf(PropertyContentActor.props(throttler))

    contentActor ! LookupHotels(List(2, 4, 6, 8, 10))

    expectNoMsg(2 seconds)
  }

  "A Property Content Actor" should "send failure message when service fails to find a hotel" in {

    val throttler: ActorRef = createThrottler(2, 1.second)
    val contentActor: ActorRef = system.actorOf(PropertyContentActor.props(throttler))

    contentActor ! LookupHotels(List(1))

    expectMsgType[LookupFailed](2 seconds)
  }

  def createThrottler(rate: Int, period: FiniteDuration): ActorRef = {

    val lookupActor: ActorRef = system.actorOf(HotelLookupActor.props(new EvenToStringHotelService(servicePause)))

    val throttler = system.actorOf(Props(classOf[TimerBasedThrottler],
      rate msgsPer period))
    throttler ! SetTarget(Some(lookupActor))

    throttler
  }
}