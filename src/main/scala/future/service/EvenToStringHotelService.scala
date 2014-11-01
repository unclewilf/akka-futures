package future.service

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EvenToStringHotelService(pause: Long) extends HotelService {

  override def find(id: Int): Future[String] = {

    Future {
      Thread.sleep(pause)
      if (id % 2 == 0) String.valueOf(id)
      else throw new IllegalArgumentException("Even number required")
    }
  }
}
