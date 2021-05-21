val SBT_PROTOC_VERSION = "1.0.0-RC4"
val SBT_NATIVE_PACKAGER_VERSION = "1.3.4"
val ZIO_GRPC_VERSION = "0.4.2"
val COMPILER_PLUGIN_VERSION = "0.10.11"

addSbtPlugin("com.thesamet" % "sbt-protoc" % SBT_PROTOC_VERSION)
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % SBT_NATIVE_PACKAGER_VERSION)

libraryDependencies ++= Seq(
  "com.thesamet.scalapb.zio-grpc" %% "zio-grpc-codegen" % ZIO_GRPC_VERSION,
  "com.thesamet.scalapb" %% "compilerplugin" % COMPILER_PLUGIN_VERSION
)