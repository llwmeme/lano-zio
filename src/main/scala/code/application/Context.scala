package code.application

import code.infrastructure.Exception.{BusinessException, HaveNotException}
import sample.sample.{Request, Response}
import zio.macros.accessible
import zio.{Has, Task, ULayer, ZIO, ZLayer}

@accessible
object Context {
  type Context = Has[Context.Service]

  trait Service {
    def process(request: Request): Task[Response]
  }

  val live: ULayer[Context] = ZLayer.succeed(new Service {
    override def process(request: Request): Task[Response] = request.ping match {
      case 1 => ZIO.succeed(Response.of(1))
      case -1 => ZIO.fail(new RuntimeException("ping is -1"))
      case -2 => ZIO.fail(BusinessException("ping is -2", -500002))
      case _ => ZIO.fail(HaveNotException(s"unmatched ${request.ping}"))
    }
  })
}
