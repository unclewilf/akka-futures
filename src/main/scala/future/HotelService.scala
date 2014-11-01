package future

import scala.concurrent.Future

trait HotelService {

  def find(id: Int): Future[String]

}
