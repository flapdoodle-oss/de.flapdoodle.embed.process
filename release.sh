#!/bin/sh
./mvnw release:clean
./mvnw release:prepare -DskipPitest -DskipTests -Darguments=-DskipTests
./mvnw release:perform -DskipPitest -DskipTests -Darguments=-DskipTests

