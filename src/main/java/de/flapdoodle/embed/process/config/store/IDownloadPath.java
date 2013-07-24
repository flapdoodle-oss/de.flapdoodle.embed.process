package de.flapdoodle.embed.process.config.store;

import de.flapdoodle.embed.process.distribution.Distribution;


public interface IDownloadPath {
	String getPath(Distribution distribution);
}
