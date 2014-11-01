package future

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global

class HotelService {

  def find(id: Int): Future[String] = {

    Future {
      if (id % 2 == 0) String.valueOf(id)
      else throw new IllegalArgumentException("Even number required")
    }
  }
}
