import sbt._

object Dependencies {
  val excludeCglib = ExclusionRule(organization = "org.sonatype.sisu.inject")
  val excludeJackson = ExclusionRule(organization = "org.codehaus.jackson")
  val excludeScalaTest = ExclusionRule(organization = "org.scalatest")
  val excludeScala= ExclusionRule(organization = "org.scala-lang")
  val excludeNettyIo = ExclusionRule(organization = "io.netty", artifact= "netty-all")
  val excludeAsm = ExclusionRule(organization = "asm")
  val excludeQQ = ExclusionRule(organization = "org.scalamacros")

  lazy val typeSafeConfigDeps = "com.typesafe" % "config" % "1.2.1"
  lazy val yammerDeps = "com.yammer.metrics" % "metrics-core" % "2.2.0"

  lazy val yodaDeps = Seq(
    "org.joda" % "joda-convert" % "1.2",
    "joda-time" % "joda-time" % "2.2"
  )

  lazy val akkaDeps = Seq(
    // Akka is provided because Spark already includes it, and Spark's version is shaded so it's not safe
    // to use this one
    "com.typesafe.akka" %% "akka-slf4j" % "2.3.4" % "provided",
    "com.typesafe.akka" %% "akka-cluster" % "2.3.4" exclude("com.typesafe.akka", "akka-remote"),
    "io.spray" %% "spray-json" % "1.3.2",
    "io.spray" %% "spray-can" % "1.3.3",
    "io.spray" %% "spray-caching" % "1.3.3",
    "io.spray" %% "spray-routing" % "1.3.3",
    "io.spray" %% "spray-client" % "1.3.3",
    yammerDeps
  ) ++ yodaDeps

  val hadoopVersion = sys.env.getOrElse("HADOOP_VERSION", "2.6.0")

  val mesosVersion = sys.env.getOrElse("MESOS_VERSION", "0.25.0-0.2.70.ubuntu1404")

  val sparkVersion = sys.env.getOrElse("SPARK_VERSION", "1.6.1")
  lazy val sparkDeps = Seq(
    "org.apache.spark" %% "spark-core" % sparkVersion % "provided" excludeAll(excludeNettyIo, excludeQQ),
    // Force netty version.  This avoids some Spark netty dependency problem.
    "io.netty" % "netty-all" % "4.0.29.Final"
  )

  lazy val sparkExtraDeps = Seq(
    "org.apache.spark" %% "spark-mllib" % sparkVersion % "provided" excludeAll(excludeNettyIo, excludeQQ),
    "org.apache.spark" %% "spark-sql" % sparkVersion % "provided" excludeAll(excludeNettyIo, excludeQQ),
    "org.apache.spark" %% "spark-streaming" % sparkVersion % "provided" excludeAll(excludeNettyIo, excludeQQ),
    "org.apache.spark" %% "spark-hive" % sparkVersion % "provided" excludeAll(excludeNettyIo, excludeQQ, excludeScalaTest)
  )

  lazy val slickDeps = Seq(
    "com.typesafe.slick" %% "slick" % "2.1.0",
    "com.h2database" % "h2" % "1.3.170",
    "commons-dbcp" % "commons-dbcp" % "1.4",
    "org.flywaydb" % "flyway-core" % "3.2.1"
  )

  lazy val logbackDeps = Seq(
    "ch.qos.logback" % "logback-classic" % "1.0.7"
  )

  lazy val coreTestDeps = Seq(
    "org.scalatest" %% "scalatest" % "2.2.1" % "test",
    "com.typesafe.akka" %% "akka-testkit" % "2.3.4" % "test",
    "io.spray" %% "spray-testkit" % "1.3.2" % "test"
  )

  lazy val securityDeps = Seq(
     "org.apache.shiro" % "shiro-core" % "1.2.4"
  )

  lazy val json4sDeps = Seq(
//    "org.json4s" % "json4s-core_2.10" % "3.2.7",
//    "org.json4s" % "json4s-native_2.10" % "3.3.0",
    "org.json4s" % "json4s-jackson_2.10" % "3.2.4",
//      exclude("com.fasterxml.jackson.core", "jacks*/on-databind"),
    "org.json4s" % "json4s-ext_2.10" % "3.2.4"
  )

  lazy val hadoopDeps = Seq(
    "org.apache.hadoop" % "hadoop-mapreduce" % hadoopVersion,
    "org.apache.hadoop" % "hadoop-client" % hadoopVersion exclude("javax.servlet", "servlet-api")
  )

  lazy val scalajDeps = Seq(
    "org.scalaj" % "scalaj-http_2.10" % "2.3.0"
  )

  lazy val zeromqDeps = Seq(
    "org.zeromq" % "jeromq" % "0.3.5"
//    "org.zeromq" % "zeromq-scala-binding_2.10" % "0.0.7"
  )

  lazy val serverDeps = apiDeps ++ yodaDeps
  lazy val apiDeps = sparkDeps :+ typeSafeConfigDeps

  val repos = Seq(
    "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/",
    "sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
    "spray repo" at "http://repo.spray.io"
  )
}
