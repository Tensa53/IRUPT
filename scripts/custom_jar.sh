#!/usr/bin/bash
SHADED_JAR_NAME=avro-1.10.0-SNAPSHOT.jar
CUSTOM_JAR_NAME=avro-benchmarks.jar

cd target/
mkdir -p custom-jar
cd custom-jar/
cp ../$SHADED_JAR_NAME .
jar -xf $SHADED_JAR_NAME
rm $SHADED_JAR_NAME
cp ../classes/META-INF/BenchmarkList ../classes/META-INF/CompilerHints META-INF/
jar cmf0 META-INF/MANIFEST.MF ../avro-benchmarks.jar  *
