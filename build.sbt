name := "lano-zio"

version := "0.0.1"

scalaVersion := "2.13.5"

enablePlugins(JavaAppPackaging)

PB.targets in Compile := Seq(
  scalapb.gen(grpc = true, singleLineToProtoString = true) -> (sourceManaged in Compile).value / "scalapb",
  scalapb.zio_grpc.ZioCodeGenerator -> (sourceManaged in Compile).value / "scalapb"
)

val ZIO_VERSION = "1.0.7"
val GRPC_NETTY_VERSION = "1.37.0"
val SLF4ZIO_VERSION = "1.0.0"
val LOGBACK_CLASSIC_VERSION = "1.2.3"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % ZIO_VERSION,
  "dev.zio" %% "zio-macros" % ZIO_VERSION,
  "io.grpc" % "grpc-netty" % GRPC_NETTY_VERSION,
  "ch.qos.logback" % "logback-classic" % LOGBACK_CLASSIC_VERSION,
  "com.github.mlangc" %% "slf4zio" % SLF4ZIO_VERSION,
  "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,

  "dev.zio" %% "zio-test" % ZIO_VERSION % "test",
  "dev.zio" %% "zio-test-sbt" % ZIO_VERSION % "test"
)

scalacOptions += "-Ymacro-annotations"