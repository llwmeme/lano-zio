package code

import code.application.grpcServiceLayer
import scalapb.zio_grpc.CanBind.canBindRC
import scalapb.zio_grpc.{ServerMain, ServiceList}

object Main extends ServerMain {
  override def services: ServiceList[zio.ZEnv] = ServiceList.add(Router).provideLayer(grpcServiceLayer)
}