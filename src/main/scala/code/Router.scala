package code

import code.application.{Context, GrpcService}
import code.infrastructure.Routers
import io.grpc.Status
import sample.sample.ZioSample.RSampleService
import sample.sample.{Request, Response}
import zio.ZIO

object Router extends RSampleService[GrpcService] {
  override def process(request: Request): ZIO[GrpcService with Any, Status, Response] =
    Routers.process(request, Context.process, "process")
}
