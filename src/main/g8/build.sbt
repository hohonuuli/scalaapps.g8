// PROJECT PROPERTIES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
organization in ThisBuild := "$organization$"

name := "$name$"

version in ThisBuild := "$version$"

scalaVersion in ThisBuild := "$scala_version$"

//crossScalaVersions := Seq("$scala_version$", "2.10.5", "2.11.6")

// https://tpolecat.github.io/2014/04/11/scalac-flags.html
scalacOptions in ThisBuild ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",       // yes, this is 2 args
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture")

javacOptions in ThisBuild ++= Seq("-target", "1.8", "-source","1.8")

incOptions := incOptions.value.withNameHashing(true)

// DEPENDENCIES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

updateOptions := updateOptions.value.withCachedResolution(true)

// Add SLF4J, Logback and testing libs
libraryDependencies ++= {
  val slf4jVersion = "1.7.13"
  val logbackVersion = "1.1.3"
  Seq(
    "ch.qos.logback" % "logback-classic" % logbackVersion,
    "ch.qos.logback" % "logback-core" % logbackVersion,
    "com.typesafe" % "config" % "1.3.0",
    "junit" % "junit" % "4.12" % "test",
    "org.scalatest" %% "scalatest" % "2.2.4" % "test",
    "org.slf4j" % "log4j-over-slf4j" % slf4jVersion,
    "org.slf4j" % "slf4j-api" % slf4jVersion)
}

resolvers in ThisBuild ++= Seq(Resolver.mavenLocal,
    "hohonuuli-bintray" at "http://dl.bintray.com/hohonuuli/maven")

//publishMavenStyle := true

//publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))

// OTHER SETTINGS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Adds commands for dependency reporting
net.virtualvoid.sbt.graph.Plugin.graphSettings


// -- PROMPT
// set the prompt (for this build) to include the project id.
shellPrompt in ThisBuild := { state =>
  val user = System.getProperty("user.name")
  "\n" + user + "@" + Project.extract(state).currentRef.project + "\nsbt> "
}


// -- VERSIONREPORT
// Add this setting to your project to generate a version report (See ExtendedBuild.scala too.)
// Use as 'sbt versionReport' or 'sbt version-report'
versionReport <<= (externalDependencyClasspath in Compile, streams) map {
  (cp: Seq[Attributed[File]], streams) =>
    val report = cp.map {
      attributed =>
        attributed.get(Keys.moduleID.key) match {
          case Some(moduleId) => "%40s %20s %10s %10s".format(
            moduleId.organization,
            moduleId.name,
            moduleId.revision,
            moduleId.configurations.getOrElse("")
          )
          case None =>
            // unmanaged JAR, just
            attributed.data.getAbsolutePath
        }
    }.sortBy(a => a.trim.split("\\\\s+").map(_.toUpperCase).take(2).last).mkString("\n")
    streams.log.info(report)
    report
}


// -- VERSION.PROPERTIES
// Code for adding a version.propertes file
gitHeadCommitSha := scala.util.Try(Process("git rev-parse HEAD").lines.head).getOrElse("")

makeVersionProperties := {
  val propFile = (resourceManaged in Compile).value / "version.properties"
  val content = "version=%s" format (gitHeadCommitSha.value)
  IO.write(propFile, content)
  Seq(propFile)
}

resourceGenerators in Compile <+= makeVersionProperties


// -- SBT-PACK
// For sbt-pack
packAutoSettings

// For sbt-pack
val apps = Seq("main")

packAutoSettings ++ Seq(packExtraClasspath := apps.map(_ -> Seq("${PROG_HOME}\/conf")).toMap,
  packJvmOpts := apps.map(_ -> Seq("-Duser.timezone=UTC", "-Xmx4g")).toMap,
  packDuplicateJarStrategy := "latest",
  packJarNameConvention := "original")


// -- SCALARIFORM
// Format code on save with scalariform
import scalariform.formatter.preferences._

scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(IndentSpaces, 2)
  .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, false)
  .setPreference(DoubleIndentClassDeclaration, true)

// Fail if style is bad
scalastyleFailOnError := true


// -- MISC
// fork a new JVM for run and test:run
fork := true

// Aliases
addCommandAlias("cleanall", ";clean;clean-files")

initialCommands in console :=
  """
    |import java.util.Date
  """.stripMargin
