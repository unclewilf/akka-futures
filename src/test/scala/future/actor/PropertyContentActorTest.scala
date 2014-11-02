package future.actor

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import future.message.{LookupFailed, LookupHotels, PersistContent}
import future.service.{EvenIdOnlyHotelService, RandomRatingService}
import org.scalatest.Inspectors._
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.duration._
import scala.util.Random

class PropertyContentActorTest extends TestKit(ActorSystem("TestSystem"))
with FlatSpecLike with BeforeAndAfterAll with Matchers with ImplicitSender with ScalaFutures {

  override def afterAll() {

    system.shutdown()
  }

  "A Property Content Actor" should "find all available hotels" in {

    val pause = 30L
    val maxRating = 10

    val lookupActor: ActorRef = system.actorOf(HotelLookupActor.props(new EvenIdOnlyHotelService(pause)))
    val ratingActor: ActorRef = system.actorOf(RatingLookupActor.props(new RandomRatingService(pause, maxRating)))
    val contentActor: ActorRef = system.actorOf(PropertyContentActor.props(lookupActor, ratingActor))

    contentActor ! LookupHotels(List(2, 4, 6))

    val msg: PersistContent = expectMsgType[PersistContent](2 seconds)
    val ids = msg.content map {
      c => c.hotel.id
    }

    ids should equal(List("2", "4", "6"))
  }

  "A Property Content Actor" should "find all ratings" in {

    val hotelPause = 50L
    val ratingPause = 30L
    val maxRating = 10

    val lookupActor: ActorRef = system.actorOf(HotelLookupActor.props(new EvenIdOnlyHotelService(hotelPause)))
    val ratingActor: ActorRef = system.actorOf(RatingLookupActor.props(new RandomRatingService(ratingPause, maxRating)))
    val contentActor: ActorRef = system.actorOf(PropertyContentActor.props(lookupActor, ratingActor))

    contentActor ! LookupHotels(List(2, 4, 6, 8, 10))

    val msg: PersistContent = expectMsgType[PersistContent](5 seconds)
    val ids = msg.content map {
      c => c.rating.score
    }

    forAll(ids) {
      id => id should be <= maxRating
    }
  }

  "A Property Content Actor" should "fail gracefully when future timeout exceeded" in {

    val pause = 200L
    val maxRating = 10

    val lookupActor: ActorRef = system.actorOf(HotelLookupActor.props(new EvenIdOnlyHotelService(pause)))
    val ratingActor: ActorRef = system.actorOf(RatingLookupActor.props(new RandomRatingService(pause, maxRating)))
    val contentActor: ActorRef = system.actorOf(PropertyContentActor.props(lookupActor, ratingActor, 100 millis))

    contentActor ! LookupHotels(List(2))

    expectMsgType[LookupFailed](2 seconds)
  }

  "A Property Content Actor" should "find mega ratings" in {

    val hotelPause = new Random
    val ratingPause = 10L
    val maxRating = 10

    val lookupActor: ActorRef = system.actorOf(HotelLookupActor.props(new EvenIdOnlyHotelService(hotelPause.nextInt(1000))))
    val ratingActor: ActorRef = system.actorOf(RatingLookupActor.props(new RandomRatingService(ratingPause, maxRating)))
    val contentActor: ActorRef = system.actorOf(PropertyContentActor.props(lookupActor, ratingActor, 100.seconds))

    val bigList = (1 to 100).toList filter {
      i => i % 2 == 0
    }

    contentActor ! LookupHotels(bigList)

    val msg: PersistContent = expectMsgType[PersistContent](100 seconds)
    val ids = msg.content map {
      c => c.rating.score
    }

    forAll(ids) {
      id => id should be <= maxRating
    }
  }
}