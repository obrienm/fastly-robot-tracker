import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._
import sbtbuildinfo.Plugin._


object ApplicationBuild extends Build {

  val appName = "syslog-robot-parser"
  val appVersion = "0.1"

  lazy val buildSettings = Defaults.defaultSettings ++ Seq(
    jarName in assembly := "syslog-robot-parser.jar",
    version := "0.1",
    organization := "me.moschops",
    scalaVersion := "2.10.0",
    libraryDependencies += "org.syslog4j" % "syslog4j" % "0.9.30"
  )

  lazy val playArtifactDistSettings = assemblySettings ++ Seq(
    mainClass in assembly := Some("play.core.server.NettyServer"),
    jarName in assembly := "syslog-robot-parser.jar"
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

  val main = play.Project(appName, appVersion, settings = buildSettings ++ playArtifactDistSettings ++ standardSettings)
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
}
