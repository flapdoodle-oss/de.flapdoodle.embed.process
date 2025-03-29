[![Build Status](https://travis-ci.org/flapdoodle-oss/de.flapdoodle.embed.process.svg)](https://travis-ci.org/flapdoodle-oss/de.flapdoodle.embed.process)
[![Maven Central](https://img.shields.io/maven-central/v/de.flapdoodle.embed/de.flapdoodle.embed.process.svg)](https://maven-badges.herokuapp.com/maven-central/de.flapdoodle.embed/de.flapdoodle.embed.process)
[![libs.tech recommends](https://libs.tech/project/5412556/badge.svg)](https://libs.tech/project/5412556/de.flapdoodle.embed.process)
# Organisation Flapdoodle OSS

We are a github organisation. You are invited to participate.

# Embedded Process Util

Embedded Process Util will provide a platform neutral way for running processes in unittests. Every version < 4.x.x is now considered as
legacy.

## License

We use http://www.apache.org/licenses/LICENSE-2.0

## Howto

[How to run a process](HowToRunAProcess.md)

### Maven

[maven central](http://repo1.maven.org/maven2/de/flapdoodle/embed/de.flapdoodle.embed.process/maven-metadata.xml)

	<dependency>
		<groupId>de.flapdoodle.embed</groupId>
		<artifactId>de.flapdoodle.embed.process</artifactId>
		<version>4.15.2</version>
	</dependency>

### Changelog

#### Unreleased

#### 4.15.2

- fix an other LocalDownloadCache bug

#### 4.15.0

- fix LocalDownloadCache bug

#### 4.15.0

- dependency updates

#### 4.14.0

- dependency updates, proxy support for http_proxy env variables

#### 4.13.2

- download move atomic exception fix
                       
#### 4.13.1

- download race condition fix
                       
#### 4.13.0

- os detection updates
- dependency updates
- download cache exception fix

#### 4.12.0

- os detection updates
- dependency updates

#### 4.11.0

- slf4j/log4j log bug fix

#### 4.10.5

- cached file set race condition bugfix

#### 4.10.4

- fileset store concurrency bug fix

#### 4.10.3

- fix basic auth in url handling in LocalDownloadCache

#### 4.10.2

- fix file url handling in LocalDownloadCache
- dependency updates

#### 4.10.1

- dependency updates

#### 4.9.0

- fix extract file hash collision in multithreaded situations
- use url user info for basic auth in url connections

#### 4.8.1

- all the latest stuff:)

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
