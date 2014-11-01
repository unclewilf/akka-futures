package future.service

import future.domain.Hotel
import scala.concurrent.Future

trait HotelService {

  def find(id: Int): Future[Hotel]

}
