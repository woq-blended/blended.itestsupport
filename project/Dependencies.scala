import sbt._

object Dependencies extends blended.sbt.Dependencies {

  private[this] val dockerJavaVersion = "3.0.13"

  val akktHttpTestkit = akka_Http("http-testkit")

  val commonsCompress = "org.apache.commons" % "commons-compress" % "1.13"

  val dockerJava = "com.github.docker-java" % "docker-java" % dockerJavaVersion

}

object Blended extends blended.sbt.Blended {

  override def blendedVersion: String = BuildHelper.readAsVersion(new File("version.txt"))

}
