import com.typesafe.sbt.packager.docker._

enablePlugins(JavaServerAppPackaging)

lazy val root = (project in file(".")).
  settings (
    name := "CSC536 Final Project",
    version := "1.0",
    scalaVersion := "2.12.8",
    scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation"),
    resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.21",
    libraryDependencies += "com.typesafe.akka" %% "akka-remote" % "2.5.21",
	libraryDependencies += "com.typesafe.akka" %% "akka-persistence" % "2.5.21",
	libraryDependencies += "com.typesafe.akka" %% "akka-cluster" % "2.5.21",
	libraryDependencies += "com.typesafe.akka" %% "akka-cluster-sharding" % "2.5.21",
    libraryDependencies += "com.typesafe.akka" %% "akka-cluster-metrics" % "2.5.21",
    libraryDependencies += "com.typesafe.akka" %% "akka-cluster-tools" % "2.5.21",
	libraryDependencies += "com.typesafe.akka" %% "akka-distributed-data" % "2.5.21",
	libraryDependencies += "com.typesafe.akka" %% "akka-discovery" % "2.5.21",
	libraryDependencies += "com.lightbend.akka.management" %% "akka-management" % "1.0.1",
    libraryDependencies += "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % "1.0.1",
    libraryDependencies += "com.lightbend.akka.management" %% "akka-management-cluster-http" % "1.0.1",

    dockerEntrypoint ++= Seq(
      """-Dakka.remote.netty.tcp.hostname="$(eval "echo $AKKA_REMOTING_BIND_HOST")"""",
      """-Dakka.management.http.hostname="$(eval "echo $AKKA_REMOTING_BIND_HOST")""""
    ),
      dockerCommands := dockerCommands.value.flatMap {
		case Cmd("FROM",  args @ _*) => Seq(Cmd("FROM", "openjdk:8-jdk"))
        case ExecCmd("ENTRYPOINT", args @ _*) => Seq(Cmd("ENTRYPOINT", args.mkString(" ")))
        case v => Seq(v)
    },

    dockerUsername := Some("dbeck6"),
    packageName in Docker := "client",
    version in Docker := "v1.0.0",

    // use += to add an item to a Sequence
    dockerCommands += Cmd("USER", "root")
)


