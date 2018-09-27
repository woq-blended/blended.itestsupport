import sbt._

val travisBuildNumber = sys.env.getOrElse("TRAVIS_BUILD_NUMBER", "Not on Travis")

// A convenience to execute all tests in travis
addCommandAlias("ciBuild", "; clean ; test ")

// A convenience to push SNAPSHOT to sonatype Snapshots
addCommandAlias(name = "ciPublish", value="; clean ; packageBin ; publishSigned ")

// A convenience to package everything, sign it and push it to maven central
addCommandAlias("ciRelease", s"""; clean; packageBin ; sonatypeOpen "Auto Release via Travis ($travisBuildNumber)" ; publishSigned ; sonatypeClose ; sonatypeRelease""")

inThisBuild(
  BuildHelper.readVersion(file("version.txt")) ++ Seq(
    useGpg := false,
    pgpPublicRing := baseDirectory.value / "project" / ".gnupg" / "pubring.gpg",
    pgpSecretRing := baseDirectory.value / "project" / ".gnupg" / "secring.gpg",
    pgpPassphrase := sys.env.get("PGP_PASS").map(_.toArray)
  )
)

lazy val blendedItestsupport = BlendedItestsupport()
