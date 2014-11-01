package future.actor

import akka.actor.{Props, ActorRef, ActorSystem}
import akka.contrib.throttle.Throttler.SetTarget
import akka.contrib.throttle.TimerBasedThrottler
import akka.testkit.{ImplicitSender, TestKit}
import future.message.{LookupHotels, PersistContent}
import future.service.EvenToStringHotelService
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import akka.contrib.throttle.Throttler._

import scala.concurrent.duration._

class PropertyContentActorThrottlingTest extends TestKit(ActorSystem("TestSystem"))
 with FlatSpecLike with BeforeAndAfterAll with Matchers with ImplicitSender with ScalaFutures {

   override def afterAll() {

     system.shutdown()
   }

   "A Property Content Actor" should "find all available hotels " in {

     val lookupActor: ActorRef = system.actorOf(HotelLookupActor.props(new EvenToStringHotelService))
     val throttler = system.actorOf(Props(classOf[TimerBasedThrottler],
       3 msgsPer 1.second))

     throttler ! SetTarget(Some(lookupActor))

     val contentActor: ActorRef = system.actorOf(PropertyContentActor.props(throttler))

     contentActor ! LookupHotels(List(2, 4, 6))

     expectMsg(10 seconds, PersistContent(List("2", "4", "6")))
   }
 }