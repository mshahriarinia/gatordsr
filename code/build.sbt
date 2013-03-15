name := "gatordsr"

version := "0.01"

scalaVersion := "2.9.2"

resolvers += "spray repo" at "http://repo.spray.io"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases"

libraryDependencies += "org.apache.thrift" % "libthrift" % "0.9.0"

libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "1.3.4"

libraryDependencies += "org.spark-project" % "spark-core_2.9.2" % "0.7.0"

libraryDependencies += "com.typesafe.akka" % "akka-camel_2.10.0-M7" % "2.1-M2"

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.10" % "2.2-M1"

libraryDependencies += "com.typesafe.akka" % "akka-remote_2.10" % "2.2-M1"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.3"

libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp-models" % "1.3.4" from "http://scalasbt.artifactoryonline.com/scalasbt/repo/edu/stanford/nlp/stanford-corenlp/1.3.4/"

libraryDependencies += "com.google.guava" % "guava" % "r09"

