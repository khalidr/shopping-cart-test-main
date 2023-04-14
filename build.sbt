ThisBuild / organization := "com.example"
ThisBuild / scalaVersion := "2.13.5"

lazy val root = (project in file(".")).settings(
  name := "cats-effect-3-quick-start",
  libraryDependencies ++= Seq(
    // "core" module - IO, IOApp, schedulers
    // This pulls in the kernel and std modules automatically.
    "org.typelevel" %% "cats-effect" % "3.3.12",
    // concurrency abstractions and primitives (Concurrent, Sync, Async etc.)
    "org.typelevel" %% "cats-effect-kernel" % "3.3.12",
    // standard "effect" library (Queues, Console, Random etc.)
    "org.typelevel" %% "cats-effect-std" % "3.3.12",
    // better monadic for compiler plugin as suggested by documentation
    compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    "io.estatico" %% "newtype" % "0.4.4",
    "com.squareup.okhttp3" % "okhttp" % "4.10.0",
    "org.typelevel" %% "squants" % "1.8.3",
    "io.circe" %% "circe-parser" % "0.14.5",
    "io.circe" %% "circe-generic" % "0.14.5",
    "tf.tofu" %% "derevo-circe-magnolia" % "0.13.0" excludeAll (ExclusionRule(organization = "io.circe")),
    "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % Test,
    "org.typelevel" %% "cats-effect-testing-scalatest" % "1.5.0" % Test,
    "org.scalatestplus" %% "scalacheck-1-17" % "3.2.15.0" % Test,
    "org.mockito" %% "mockito-scala-scalatest" % "1.17.14" % Test
  )
)

Global / scalacOptions ++= List("-Ymacro-annotations")
