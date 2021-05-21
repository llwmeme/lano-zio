# ZIO Grpc 项目的异常设计

近期我们重新设计了服务间的异常体系。这是一个范例项目，有以演示如何在 ZIO Grpc 项目中抛出异常。

这个项目实现了一个很简单的功能，根据请求的值返回一个正常或者异常的结果。这些异常我们通过封装转换成了一个 Grpc 的 Status，这样调用方将可以根据 Status 获知接口调用是否正常。

下面会演示具体应该如何在项目中抛出异常，具体代码可以这个范例项目中获得。范例项目的 Context 类中，我们根据请求参数返回了响应：

```scala
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
```

我们抛出了三种异常：RuntimeException、BusinessException、HaveNotException，分别代表了一般性异常，自定义异常 code 的异常，封装了异常 code 的异常。后两种异常是我们封装异常：

```scala
object Exception {

  abstract class BasicBusinessException(message: String) extends RuntimeException(s"E0514|$message")

  case class BusinessException(message: String = "", code: Int = -500000) extends BasicBusinessException(s"$code|$message")

  case class HaveNotException(message: String = "") extends BasicBusinessException(s"-500001|$message")

}
```

封装的异常和一般性异常的区别是，封装的异常统一了类型，同时也统一了异常的 message 格式，**异常的 message 都以 “E0514” 起始**。我们设计成这样异常体系是用于统一转换成指定的 Grpc Status 和便于网关层进行进一步的异常转换，下面是异常的转换的实现：

```scala
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
```

这里我们把 BasicBusinessException 都转成了 Grpc 的 UNKNOWN Status，而一般性异常则是 INTERNAL Status。

调用方在收到异常后，可以进行特定的异常处理。这里展示了一个 grpc gateway 的处理：

先转成运行时异常

```scala
private def toRuntimeException[A](zio: ZIO[A, Status, GeneratedMessage]): RIO[A, GeneratedMessage] = zio.mapError(_.asRuntimeException())
```

然后根据异常的 Status 进行对应的异常转换。如果是业务系统则可以执行对应的异常逻辑分支行。

```scala
case exception: StatusRuntimeException if exception.getStatus.getCode == Status.UNKNOWN.getCode =>
  ZIO.succeed(decodeException(exception.getMessage).map(v => {
    val (code, message) = v
    ApiResponse(status = code, message = message)
  }).getOrElse(ApiResponse(status = -500000, message = exception.getMessage)))
```

这里是异常 message 的解析方法：

```scala
private def decodeException(message: String): Option[(Int, String)] = {
  if (!message.startsWith("UNKNOWN: E0514")) return None
  val array = message.split("\\|")
  val code = allCatch.opt(array(1).toInt)
  if (array.length < 3 || code.isEmpty) None else Some(code.get, array(2))
}
```

这也解释了为什么我们要求异常需要以特定的字符串起始。因为这种情况下我们会把异常 message 中的 code 和 message 转换成网关层的 status 和 message。

