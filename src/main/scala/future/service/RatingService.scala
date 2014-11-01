package future.service

import future.domain.Rating
import scala.concurrent.Future

trait RatingService {

  def find(id: Int): Future[Rating]
}
