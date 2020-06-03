resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("com.eed3si9n" % "sbt-unidoc" % "0.4.2")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.1")
addSbtPlugin("ch.epfl.scala" % "sbt-bloop" % "1.0.0")
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.2")
addSbtPlugin("de.wayofquality.sbt" % "sbt-testlogconfig" % "0.1.0")

// Contains project dependency information from blended project as sbt plugin
//libraryDependencies ++= Seq(
addSbtPlugin(
  "de.wayofquality.blended" % "blended.dependencies" % "3.1-RC7"
)
