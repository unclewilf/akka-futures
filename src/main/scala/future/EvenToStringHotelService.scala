package future

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global

class EvenToStringHotelService extends HotelService {

  override def find(id: Int): Future[String] = {

    Future {
      if (id % 2 == 0) String.valueOf(id)
      else throw new IllegalArgumentException("Even number required")
    }
  }
}
