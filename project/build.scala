import sbt._
import Keys._

object BuildSettings {
  val buildOrganization = "com.corruptmemory"
  val buildScalaVersion = "2.11.7"
  val buildVersion      = "0.3-SNAPSHOT"

  lazy val publishSetting = publishTo <<= (version) {
    version: String =>
      val isSnapshot = version.trim.endsWith("SNAPSHOT")
      val repo   = if(isSnapshot) (Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))
                   else (Resolver.file("releases",file("/"+Path.userHome.toString + "/scala-aho-corasick-pages/repository")))
      Some(repo)
  }

  val buildSettings = Defaults.defaultSettings ++ Seq (organization := buildOrganization,
						       scalaVersion := buildScalaVersion,
						       version      := buildVersion,
						       shellPrompt  := ShellPrompt.buildShellPrompt,
                   publishSetting)

}

object ShellPrompt {

  object devnull extends ProcessLogger {
    def info (s: => String) {}
    def error (s: => String) { }
    def buffer[T] (f: => T): T = f
  }

  val current = """\*\s+(\w+)""".r

  def gitBranches = ("git branch --no-color" lines_! devnull mkString)

  val buildShellPrompt = {
    (state: State) => {
      val currBranch = current findFirstMatchIn gitBranches map (_ group(1)) getOrElse "-"
      val currProject = Project.extract (state).currentProject.id
      "%s:%s:%s> ".format (currProject, currBranch, BuildSettings.buildVersion)
    }
  }
}

object Dependencies {
  val scalaCheckVersion = "1.13.1"
  val scalaZVersion = "7.2.4"

  val scalaz = "org.scalaz" %% "scalaz-core" % scalaZVersion
  val scalaCheck = "org.scalacheck" %% "scalacheck" % scalaCheckVersion % "test"
}

object MongoDumperBuild extends Build {
  val buildShellPrompt = ShellPrompt.buildShellPrompt

  import Dependencies._
  import BuildSettings._

  // Sub-project specific dependencies
  val coreDeps = Seq(scalaz,scalaCheck)

  lazy val ahoCorasick = Project("aho-corisick",file("."),
                                 settings = buildSettings ++ Seq(name := "Aho-Corasick",
                                                                 libraryDependencies := coreDeps))
}
