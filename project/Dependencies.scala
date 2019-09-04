import sbt._

object Dependencies {

  private[this] val activeMqVersion = "5.15.6"
  private[this] val akkaHttpVersion = "10.1.5"
  private[this] val akkaVersion = "2.5.19"
  private[this] val blendedCoreVersion = "3.0.4"
  private[this] val dockerJavaVersion = "3.0.13"
  private[this] val camelVersion = "2.19.5"

  private[this] def akka(m: String) : ModuleID = "com.typesafe.akka" %% s"akka-${m}" % akkaVersion
  private[this] def akka_Http(m: String): ModuleID = "com.typesafe.akka" %% s"akka-${m}" % akkaHttpVersion
  private[this] def blended(module: String) : ModuleID = "de.wayofquality.blended" %% module % blendedCoreVersion
  
  val activeMqBroker = "org.apache.activemq" % "activemq-broker" % activeMqVersion
  val activeMqKahadbStore = "org.apache.activemq" % "activemq-kahadb-store" % activeMqVersion
  val akkaActor = akka("actor")
  val akkaCamel = akka("camel")
  val akkaSlf4j = akka("slf4j")
  val akkaTestkit = akka("testkit")
  val akktHttpTestkit = akka_Http("http-testkit")

  val blendedSecuritySsl = blended("blended.security.ssl")
  val blendedJmsUtils = blended("blended.jms.utils")
  val blendedJolokia = blended("blended.jolokia")
  val blendedUtilLogging = blended("blended.util.logging")
  val blendedTestsupport = blended("blended.testsupport")

  val camelCore = "org.apache.camel" % "camel-core" % camelVersion
  val camelJms = "org.apache.camel" % "camel-jms" % camelVersion

  val commonsCompress = "org.apache.commons" % "commons-compress" % "1.13"

  val dockerJava = "com.github.docker-java" % "docker-java" % dockerJavaVersion

  val jolokiaJvm = "org.jolokia" % "jolokia-jvm" % "1.5.0"
  val jolokiaJvmAgent = jolokiaJvm.classifier("agent")

  val logbackCore = "ch.qos.logback" % "logback-core" % "1.2.3"
  val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.2.3"

  val mockitoAll = "org.mockito" % "mockito-all" % "1.9.5"

  val sttp = "com.softwaremill.sttp" %% "core" % "1.3.7"
}

