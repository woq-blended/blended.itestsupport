import sbt._

object Dependencies extends blended.sbt.Dependencies {
  val commonsCompress = "org.apache.commons" % "commons-compress" % "1.13"
}

object Blended extends blended.sbt.Blended {

  override def blendedVersion: String = BuildHelper.readAsVersion(new File("version.txt"))

}
