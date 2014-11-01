package future.service

import future.domain.Hotel
import org.scalatest.concurrent.{AsyncAssertions, ScalaFutures}
import org.scalatest.time.{Span, Seconds}
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scala.util._

class EvenIdOnlyHotelServiceTest
  extends FlatSpec with Matchers with ScalaFutures with AsyncAssertions {

  private val pause: Long = 100L

  val service = new EvenIdOnlyHotelService(pause)

  it should "return a valid id for even number" in {

    val futureRes: Future[Hotel] = service.find(2)

    whenReady(futureRes, timeout(Span(2, Seconds))) {
      res => res.id should equal("2")
    }
  }

  it should "throw for odd number" in {

    val futureRes: Future[Hotel] = service.find(1)
    val w = new Waiter

    futureRes onComplete {
      case Failure(e) => w(throw e); w.dismiss()
      case Success(_) => w.dismiss()
    }

    intercept[IllegalArgumentException] {
      w.await
    }
  }
}
