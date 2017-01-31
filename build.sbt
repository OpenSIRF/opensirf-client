lazy val commonSettings = Seq(
  organization := "org.opensirf",
  version := "1.0.0",
  name := "opensirf-client"
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "OpenSIRF Java Client",
    version := "1.0.0",
    crossTarget := new java.io.File("target"),
    artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
        artifact.name + "-" + version.value + "." + artifact.extension
	}
)

crossPaths := false
publishTo := Some(Resolver.url("Artifactory Realm", new URL("http://200.144.189.109:58082/artifactory"))(Resolver.ivyStylePatterns))
credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
publishMavenStyle := false
isSnapshot := true

unmanagedClasspath in Test += baseDirectory.value / "WebContent" / "WEB-INF" / "classes"

fork in Test := true


libraryDependencies ++= Seq(
    "org.glassfish.jersey.core" % "jersey-client" % "2.25",
    "org.opensirf" % "opensirf-core" % "1.0.0" changing(),
    "org.opensirf" % "opensirf-storage-monitor" % "1.0.0" changing(),
    "com.novocode" % "junit-interface" % "0.11" % Test,
    "org.glassfish.jersey.ext" % "jersey-entity-filtering" % "2.25",
    "org.glassfish.jersey.media" % "jersey-media-moxy" % "2.25",
    "org.glassfish.jersey.media" % "jersey-media-multipart" % "2.25"
)

publishArtifact in (Compile, packageDoc) := false
publishArtifact in (Compile, packageSrc) := false

resolvers += Resolver.url("SIRF Artifactory", url("http://200.144.189.109:58082/artifactory"))(Resolver.ivyStylePatterns)
