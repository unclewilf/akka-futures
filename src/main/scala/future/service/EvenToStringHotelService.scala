package future.service

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class EvenToStringHotelService extends HotelService {

  override def find(id: Int): Future[String] = {

    Future {
      if (id % 2 == 0) String.valueOf(id)
      else throw new IllegalArgumentException("Even number required")
    }
  }
}
