organization  := "com.iceberg"

version       := "0.1"

scalaVersion  := "2.10.3"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV = "2.1.4"
  val sprayV = "1.1.1"
  Seq(
    "io.spray"            %   "spray-can"     % sprayV,
    "io.spray"            %   "spray-routing" % sprayV,
    "io.spray"            %   "spray-testkit" % sprayV  % "test",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "org.specs2"          %%  "specs2"        % "2.2.3" % "test",
    "com.thinkaurelius.titan" % "titan-cassandra" % "0.4.4",
	"com.thinkaurelius.titan" % "titan-es" % "0.4.4",
    "com.tinkerpop.blueprints" % "blueprints-core" % "2.4.0",
    "com.michaelpollmeier" %% "gremlin-scala" % "2.5.0"
  )
}

Revolver.settings
