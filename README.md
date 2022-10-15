[![Build Status](https://travis-ci.org/flapdoodle-oss/de.flapdoodle.embed.process.svg)](https://travis-ci.org/flapdoodle-oss/de.flapdoodle.embed.process)
[![Maven Central](https://img.shields.io/maven-central/v/de.flapdoodle.embed/de.flapdoodle.embed.process.svg)](https://maven-badges.herokuapp.com/maven-central/de.flapdoodle.embed/de.flapdoodle.embed.process)
# Organisation Flapdoodle OSS

We are now a github organisation. You are invited to participate. Starting with version 2 we are going to support only java 8 or higher. If you are looking for the older version you can find it in the 1.7 branch.


# Embedded Process Util

Embedded Process Util will provide a platform neutral way for running processes in unittests. This version is a complete rewrite,
so there are major changes.

## Why?

- its easy, much easier as installing right version by hand
- you can change version per test

## License

We use http://www.apache.org/licenses/LICENSE-2.0

## Howto

[How to run a process](HowToRunAProcess.md)

### Maven

Stable (Maven Central Repository, Released: 15.10.2022 - wait 24hrs for [maven central](http://repo1.maven.org/maven2/de/flapdoodle/embed/de.flapdoodle.embed.process/maven-metadata.xml))

	<dependency>
		<groupId>de.flapdoodle.embed</groupId>
		<artifactId>de.flapdoodle.embed.process</artifactId>
		<version>4.0.2-beta</version>
	</dependency>

Snapshots (Repository http://oss.sonatype.org/content/repositories/snapshots)

	<dependency>
		<groupId>de.flapdoodle.embed</groupId>
		<artifactId>de.flapdoodle.embed.process</artifactId>
		<version>4.0.3-beta-SNAPSHOT</version>
	</dependency>

### Projects using this Tool

- Embedded MongoDB [de.flapdoodle.embed.mongo](https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo)
- Embedded Redis [de.flapdoodle.de.embed.redis](https://github.com/flapdoodle-oss/de.flapdoodle.embed.redis)
- Embedded Memcached [de.flapdoodle.embed.memcached](https://github.com/flapdoodle-oss/de.flapdoodle.embed.memcached)
- Embedded node.js [nodejs.embed.flapdoodle.de](https://github.com/flapdoodle-oss/de.flapdoodle.embed.nodejs)
- Embedded PostgreSQL [ru.yandex.qatools.embed](https://github.com/yandex-qatools/postgresql-embedded)
- Embedded MySQL [com.wix.mysql](https://github.com/wix/wix-embedded-mysql)
- Embedded Consul [com.github.golovnin.embedded.consul](https://github.com/golovnin/embedded-consul)
- Embedded Vault [com.github.golovnin.embedded.vault](https://github.com/golovnin/embedded-vault)
- Embedded InfluxDB [io.apisense.embed.influx](https://github.com/APISENSE/embed-influxDB)
- Embedded Cassandra [com.github.nosan.embedded.cassandra](https://github.com/nosan/embedded-cassandra)

### Changelog

#### 4.0.2

- cleanup, bugfixes

#### 4.0.0

- nearly complete rewrite


