package future.service

import future.domain.Hotel

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EvenIdOnlyHotelService(pause: Long) extends HotelService {

  override def find(id: Int): Future[Hotel] = {

    Future {
      Thread.sleep(pause)

      if (isNumberEven(id)) {
        Hotel(String.valueOf(id))
      } else {
        throw new IllegalArgumentException("Even number required")
      }
    }
  }

  def isNumberEven(id: Int): Boolean = {
    id % 2 == 0
  }
}
