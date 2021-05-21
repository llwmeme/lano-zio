package code.infrastructure

import code.application.GrpcService
import code.infrastructure.Exception.BasicBusinessException
import com.github.mlangc.slf4zio.api.{LoggingSupport, Slf4jLoggerOps}
import io.grpc.Status
import scalapb.GeneratedMessage
import zio.{RIO, ZIO}

object Routers extends LoggingSupport {
  def process[A <: GrpcService, B <: GeneratedMessage, C <: GeneratedMessage](request: B, fn: B => RIO[A, C], method: String): ZIO[A, Status, C] = for {
    _ <- logger.debugIO(s"$method request ${request.toProtoString}")
    result <- fn(request).mapError {
      case e: BasicBusinessException =>
        logger.error(s"$method business-error:", e)
        Status.UNKNOWN.withDescription(e.getMessage).withCause(e)
      case e: Throwable =>
        logger.error(s"$method error:", e)
        Status.INTERNAL.withDescription(e.getMessage).withCause(e)
    }
    _ <- logger.debugIO(s"$method response ${result.toProtoString}")
  } yield result
}
