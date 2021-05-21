package code.infrastructure

object Exception {

  abstract class BasicBusinessException(message: String) extends RuntimeException(s"E0514|$message")

  case class BusinessException(message: String = "", code: Int = -500000) extends BasicBusinessException(s"$code|$message")

  case class HaveNotException(message: String = "") extends BasicBusinessException(s"-500001|$message")

}
