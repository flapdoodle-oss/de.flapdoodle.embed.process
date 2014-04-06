# Organisation Flapdoodle OSS

We are now a github organisation. You are invited to participate.

# Embedded Process Util

Embedded Process Util will provide a platform neutral way for running processes in unittests.


## Why?

- its easy, much easier as installing right version by hand
- you can change version per test

## Howto

### Maven

Stable (Maven Central Repository, Released: 06.10.2013 - wait 24hrs for [maven central](http://repo1.maven.org/maven2/de/flapdoodle/embed/de.flapdoodle.embed.process/maven-metadata.xml))

	<dependency>
		<groupId>de.flapdoodle.embed</groupId>
		<artifactId>de.flapdoodle.embed.process</artifactId>
		<version>1.35</version>
	</dependency>

Snapshots (Repository http://oss.sonatype.org/content/repositories/snapshots)

	<dependency>
		<groupId>de.flapdoodle.embed</groupId>
		<artifactId>de.flapdoodle.embed.process</artifactId>
		<version>1.36-SNAPSHOT</version>
	</dependency>

### Projects using this Tool

- Embedded MongoDB [de.flapdoodle.embed.mongo](https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo)
- Embedded Redis [de.flapdoodle.de.embed.redis](https://github.com/flapdoodle-oss/de.flapdoodle.embed.redis)
- Embedded Memcached [de.flapdoodle.embed.memcached](https://github.com/flapdoodle-oss/de.flapdoodle.embed.memcached)
- Embedded node.js [nodejs.embed.flapdoodle.de](https://github.com/flapdoodle-oss/de.flapdoodle.embed.nodejs)

### Changelog

#### 1.36 (SNAPSHOT)

- detect executable creation collision

#### 1.35

- minor api change

#### 1.34

- write pid file for every executable

#### 1.33

- artifact store refactoring to support extraction of library files

#### 1.32

- solaris detection fixed

#### 1.31

- solaris support added

#### 1.30

- fixed daemon thread problem
- fixed multiple stop calls problem

#### 1.29

- added more builder for configurations

#### 1.28

- added download timeout config

#### 1.27

- generic process builder (download and start packages)
- minor improvments
- better network ipv6 detection error message

#### 1.26

- major api changes, easier configuration

#### 1.25

- fixed NPE on process start
- support sub dirs in temp file creation (thanks to matthewadams)

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

- initial cut out from Embedded MongoDB Project

#### 1.18

### Usage

 NOT DOCUMENTED


