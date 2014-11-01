package future

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import scala.concurrent.duration._

class PropertyContentActorTest extends TestKit(ActorSystem("TestSystem"))
with FlatSpecLike with BeforeAndAfterAll with Matchers with ImplicitSender with ScalaFutures {

  override def afterAll() {

    system.shutdown()
  }

  "A Property Content Actor" should "find all available hotels" in {

    val lookupActor: ActorRef = system.actorOf(HotelLookupActor.props(new EvenToStringHotelService))
    val contentActor: ActorRef = system.actorOf(PropertyContentActor.props(lookupActor))

    contentActor ! List(2, 4, 6)

    expectMsg(10 seconds, List("2", "4", "6"))
  }
}