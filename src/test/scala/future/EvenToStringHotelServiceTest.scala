package future

import org.scalatest.concurrent.{AsyncAssertions, ScalaFutures}
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scala.util._

class EvenToStringHotelServiceTest extends FlatSpec with Matchers with ScalaFutures with AsyncAssertions {

  val service = new EvenToStringHotelService

  it should "return a valid id for even number" in {

    val futureRes: Future[String] = service.find(2)

    whenReady(futureRes) {
      res => res should equal("2")
    }
  }

  it should "return a valid id for even number again" in {

    val futureRes: Future[String] = service.find(4)

    futureRes.futureValue should equal("4")
  }

  it should "throw for odd number" in {

    val futureRes: Future[String] = service.find(1)
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
