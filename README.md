# Organisation Flapdoodle OSS

We are now a github organisation. You are invited to participate.

# Embedded Process Util

Embedded Process Util will provide a platform neutral way for running processes in unittests.


## Why?

- its easy, much easier as installing right version by hand
- you can change version per test

## Howto

### Maven

Stable (Maven Central Repository, Released: 01.10.2012 - wait 24hrs for maven central)

	<dependency>
		<groupId>de.flapdoodle.embed</groupId>
		<artifactId>de.flapdoodle.embed.process</artifactId>
		<version>1.24</version>
	</dependency>

Snapshots (Repository http://oss.sonatype.org/content/repositories/snapshots)

	<dependency>
		<groupId>de.flapdoodle.embed</groupId>
		<artifactId>de.flapdoodle.embed.process</artifactId>
		<version>1.25-SNAPSHOT</version>
	</dependency>

### Projects using this Tool

- Embedded MongoDB [embedmongo.flapdoodle.de](https://github.com/flapdoodle-oss/embedmongo.flapdoodle.de)
- Embedded node.js [embedmongo.flapdoodle.de](https://github.com/flapdoodle-oss/de.flapdoodle.embed.nodejs)

### Changelog

Initial Version starts with 1.19 SNAPSHOT as extract from Embedded MongoDB Project

#### 1.25 (SNAPSHOT)

- fixed NPE on process start

#### 1.24

- fixed NPE on process start

#### 1.23

- fixed some shutdown race conditions
- removed File.deleteOnExit
- shutdown hock refactoring

#### 1.22

- you can change temp dir with code or system property "de.flapdoodle.embed.io.tmpdir"

#### 1.21

- "archive type" exe supported

#### 1.20

- creating subdirs if needed

#### 1.19

- initial cut out

#### 1.18

### Usage

 NOT DOCUMENTED


