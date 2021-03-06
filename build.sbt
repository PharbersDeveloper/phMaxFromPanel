import play.sbt.PlayScala
import scala.language.experimental.macros
import play.routes.compiler.InjectedRoutesGenerator

val paradiseVersion = "2.1.0"
def common = Seq(
    scalaVersion := "2.11.8",
    crossScalaVersions := Seq("2.11.8", "2.12.6"),
    version := "2.0",
    organization := "com.pharbers"
)

lazy val root = (project in file(".")).
        enablePlugins(PlayScala).
        disablePlugins(PlayFilters).
        settings(common: _*).
        settings(
            name := "findAllPaperById",
            fork in run := true,
            javaOptions += "-Xmx2G"
        )

routesGenerator := InjectedRoutesGenerator


// Docker
import NativePackagerHelper.directory
mappings in Universal ++= directory("pharbers_config_deploy")
        .map(x => x._1 -> x._2.replace("pharbers_config_deploy", "pharbers_config"))

// Scala Macro
resolvers += Resolver.sonatypeRepo("releases")
resolvers += Resolver.sonatypeRepo("snapshots")
addCompilerPlugin("org.scalamacros" % "paradise" % paradiseVersion cross CrossVersion.full)
resolvers += Resolver.mavenLocal

libraryDependencies ++= Seq(
    guice,

//    基础依赖
    "org.scala-lang"    % "scala-reflect"       % "2.11.8",
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
    "org.typelevel"     %% "macro-compat"       % "1.1.1",
    "org.scala-lang"    % "scala-compiler"      % scalaVersion.value % "provided",

//    json依赖
    "org.mongodb"       % "casbah_2.11"         % "3.1.1",
    "io.circe"          %% "circe-core"         % "0.9.3",
    "io.circe"          %% "circe-generic"      % "0.9.3",
    "io.circe"          %% "circe-parser"       % "0.9.3",
    "com.dripower"      % "play-circe_2.11"     % "2609.1",
    "io.spray"          % "spray-httpx_2.11"    % "1.3.3",

// redis 依赖
    "net.debasishg"     % "redisclient_2.11"    % "3.4",

// pharbers 依赖
	"com.pharbers"      % "base_module"         % "1.0",
	"com.pharbers"      % "logs"                % "1.0",
    "com.pharbers"      % "errorcode"           % "1.0",
	"com.pharbers"      % "jsonapi"             % "1.0",
    "com.pharbers"      % "mongo_drive"         % "1.0",
    "com.pharbers"      % "macros"              % "1.0",
    "com.pharbers"      % "client_pattern"      % "1.0",
    "com.pharbers"      % "models"              % "1.0",
    "com.pharbers"      % "redis"               % "1.0",
    "com.pharbers"      % "pharbers-spark"      % "0.1",
    "com.pharbers"      % "pharbers-max"        % "0.1",
    "com.pharbers"      % "pharbers-xmpp"       % "0.1",
    "com.pharbers"      % "pharbers-another-pattern"   % "0.1",
    "com.pharbers"      % "pharbers-security"  % "0.1",

// hadoop
    "org.apache.hadoop"  %"hadoop-common"  %"2.7.2",
    "org.apache.hadoop"  %"hadoop-hdfs"  %"2.7.2",
    "org.apache.hadoop"  %"hadoop-client"  %"2.7.2",
    "org.apache.hbase"  %"hbase-client"  %"0.96.1-hadoop2",

//poi
    "org.apache.poi"  %"poi"  %"3.15",
    "org.apache.poi"  %"poi-ooxml"  %"3.15",

//smack
    "org.igniterealtime.smack"   %"smack"  %"3.2.1",
    "org.igniterealtime.smack"   %"smackx"  %"3.2.1",

//spark
    "org.apache.spark" %"spark-core_2.11" %"2.3.0",
    "org.apache.spark" %"spark-sql_2.11" %"2.3.0",
    "org.apache.spark" %"spark-yarn_2.11" %"2.3.0",

//    其他依赖(日志, 测试)
    "log4j"             % "log4j"               % "1.2.17",
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
    "org.specs2"        % "specs2_2.11"         % "3.7" % Test




)