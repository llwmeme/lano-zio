package code

import code.application.Context.Context
import zio.ULayer

package object application {
  type GrpcService = Context

  val grpcServiceLayer: ULayer[Context] = Context.live
}
