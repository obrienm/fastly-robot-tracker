import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._
import sbtbuildinfo.Plugin._
import scala.Some


object ApplicationBuild extends Build {

  val appName = "fastly-robot-tracker"
  val appVersion = "0.1"

  lazy val buildSettings = Defaults.defaultSettings ++ Seq(
    jarName in assembly := "fastly-robot-tracker.jar",
    version := "0.1",
    organization := "me.moschops",
    scalaVersion := "2.10.0",
    libraryDependencies += "org.syslog4j" % "syslog4j" % "0.9.30"
  )

  lazy val playArtifactDistSettings = assemblySettings ++ Seq(
    mainClass in assembly := Some("play.core.server.NettyServer"),
    jarName in assembly := "fastly-robot-tracker.jar"
  )

  val standardSettings = buildInfoSettings ++ Seq[Setting[_]](
    sourceGenerators in Compile <+= buildInfo,
    buildInfoKeys := Seq[BuildInfoKey](
      libraryDependencies in Compile,
      name,
      version,
      BuildInfoKey.constant("buildNumber", Option(System.getenv("BUILD_NUMBER")) getOrElse "DEV"),
      // so this next one is constant to avoid it always recompiling on dev machines.
      // we only really care about build time on teamcity, when a constant based on when
      // it was loaded is just fine
      BuildInfoKey.constant("buildTime", System.currentTimeMillis)
    )
  )

  val main = play.Project(appName, appVersion, settings = buildSettings ++ playArtifactDistSettings ++ standardSettings ++ artifactDistSettings)
    .settings(resolvers += "Guardian Github Snapshots" at "http://guardian.github.com/maven/repo-releases")
    .settings(
    ivyXML :=
      <dependencies>
        <exclude org="commons-logging"/>
      </dependencies>,

    mergeStrategy in assembly <<= (mergeStrategy in assembly) {
      (old) => {
        case "play/core/server/ServerWithStop.class" => MergeStrategy.first
        case x => old(x)
      }
    }
  )

  /**
   * artifact creation for teamcity
   * all of this, just to create a bloody zip!
   */
  val artifactZip = TaskKey[File]("artifact-zip", "Builds a deployable zip file")

  lazy val artifactDistSettings = Seq(
    artifactZip <<= buildDeployArtifact
  )

  private def buildDeployArtifact = (target, assembly, baseDirectory) map {
    (target, assembly, baseDirectory) =>

      val resources = Seq(
        assembly -> "packages/%s/%s".format("fastly-robot-tracker", assembly.getName)
      )

      val distFile = target / "artifacts.zip"

      if (distFile.exists()) distFile.delete()

      IO.zip(resources, distFile)

      // Tells TeamCity to publish the artifact => leave this println in here
      println("##teamcity[publishArtifacts '%s => .']" format distFile)

      distFile
  }
}
