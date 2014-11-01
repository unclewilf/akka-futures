package future.service

import future.domain.Rating
import org.scalatest.concurrent.{AsyncAssertions, ScalaFutures}
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class RandomRatingServiceTest extends FlatSpec with Matchers with ScalaFutures with AsyncAssertions {

  val pause: Long = 100L
  val max: Int = 5

  val service = new RandomRatingService(pause, max)

  it should "return a random rating" in {

    val futureRes: Future[Rating] = service.find(1)

    whenReady(futureRes, timeout(Span(2, Seconds))) {
      rating => rating.score should be <= max
    }
  }
}