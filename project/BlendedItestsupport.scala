import de.wayofquality.sbt.testlogconfig.TestLogConfig
import de.wayofquality.sbt.testlogconfig.TestLogConfig.autoImport._
import com.typesafe.sbt.SbtScalariform.autoImport._
import sbt.Keys._
import sbt._
import xerial.sbt.Sonatype.SonatypeKeys._

object BlendedItestsupport {

  private[this] val m2Repo = "file://" + System.getProperty("maven.repo.local", System.getProperty("user.home") + "/.m2/repository")

  def apply() : Project = Project("blendedItestsupport", file("."))
    .settings(
      organization := "de.wayofquality.blended",
      homepage := Some(url("https://github.com/woq-blended/blended.itestsupport")),
      
      moduleName := "blended.itestsupport",

      publishMavenStyle := true,

      resolvers ++= Seq(
        Resolver.sonatypeRepo("snapshots"),
        "Maven2 Local" at m2Repo
      ),

      licenses += ("Apache 2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),

      scmInfo := Some(
        ScmInfo(
          url("https://github.com/woq-blended/blended.itestsupport"),
          "scm:git@github.com:woq-blended/blended.itestsupport.git"
        )
      ),

      developers := List(
        Developer(id = "atooni", name = "Andreas Gies", email = "andreas@wayofquality.de", url = url("https://github.com/atooni")),
        Developer(id = "lefou", name = "Tobias Roeser", email = "tobias.roser@tototec.de", url = url("https://github.com/lefou"))
      ),

      sonatypeProfileName := "de.wayofquality",

      javacOptions in Compile ++= Seq(
        "-source", "1.8",
        "-target", "1.8"
      ),

      scalaVersion := "2.12.6",
      scalacOptions ++= Seq("-deprecation", "-feature", "-Xlint", "-Ywarn-nullary-override"),

      sourcesInBase := false,

      scalariformAutoformat := false,
      scalariformWithBaseDirectory := true,

      libraryDependencies ++= Seq(
        Dependencies.activeMqBroker,

        Dependencies.akkaActor,
        Dependencies.akkaCamel,
        Dependencies.akkaTestkit,
        Dependencies.akktHttpTestkit,

        Dependencies.camelCore,
        Dependencies.camelJms,

        Blended.utilLogging,
        Blended.jmsUtils,
        Blended.jolokia,
        Blended.testSupport,
        Blended.securitySsl,

        Dependencies.commonsCompress,
        Dependencies.dockerJava,

        Dependencies.sttp,

        Dependencies.jolokiaJvmAgent % "runtime",

        Dependencies.akkaSlf4j % "test",
        Dependencies.logbackCore % "test",
        Dependencies.logbackClassic % "test",

        Dependencies.mockitoAll % "test"
      ),

      Test / javaOptions += ("-DprojectTestOutput=" + (Test / classDirectory).value),
      Test / javaOptions += {
        val jolokiaAgent = BuildHelper.resolveModuleFile(Dependencies.jolokiaJvmAgent.intransitive(), target.value).distinct.headOption.get
        s"-javaagent:${jolokiaAgent.getAbsolutePath()}=port=7777,host=localhost"
      },
      Test / fork := true,

      Test / testlogDirectory := target.value,
      Test / testlogLogToConsole := false,
      Test / testlogLogToFile := true,

      Test / resourceGenerators += (Test / testlogCreateConfig).taskValue
    )
    .settings(PublishConfig.doPublish)
    .enablePlugins(TestLogConfig)
}
