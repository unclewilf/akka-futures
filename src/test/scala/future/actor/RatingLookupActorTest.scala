package future.actor

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import akka.pattern.ask
import scala.concurrent.Future
import future.domain.{Rating, Hotel}
import future.service.RandomRatingService
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.concurrent.duration._

class RatingLookupActorTest extends TestKit(ActorSystem("TestSystem"))
 with FlatSpecLike with BeforeAndAfterAll with Matchers with ImplicitSender with ScalaFutures {

   implicit val timeout = Timeout(5 seconds)

   override def afterAll() {

     system.shutdown()
   }

   "A RatingLookupActor" should "forward a successful result to its sender" in {

     val pause = 50L
     val maxRating = 10

     val lookupActor: ActorRef = system.actorOf(RatingLookupActor.props(new RandomRatingService(pause, maxRating)))

     val futureRes: Future[Rating] = (lookupActor ? 2).mapTo[Rating]

     whenReady(futureRes, timeout(Span(2, Seconds))) {
       r => r.score should be <= maxRating
     }
   }
 }