package future.service

import future.domain.Rating

import scala.concurrent.{Future, Promise}
import scala.util.Random
import scala.concurrent.ExecutionContext.Implicits.global

class RandomRatingService(sleep: Long = 0L, max: Int = 5) extends RatingService {

  val random = new Random()

  override def find(id: Int): Future[Rating] = {
    val p = Promise[Rating]()
    Future {
      Thread.sleep(sleep)
      p.success(Rating(random.nextInt(max)))
    }
    p.future
  }

}
