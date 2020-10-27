#!/bin/sh
./mvnw release:clean
./mvnw release:prepare
./mvnw release:perform

