package de.flapdoodle.embed.process.config.store;

import de.flapdoodle.embed.process.distribution.Distribution;

public class DownloadPath implements IDownloadPath {

	private final String _path;

	public DownloadPath(String path) {
		_path = path;
	}

	@Override
	public String getPath(Distribution distribution) {
		return _path;
	}

}