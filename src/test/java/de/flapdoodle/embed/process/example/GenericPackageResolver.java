package de.flapdoodle.embed.process.example;

import java.util.regex.Pattern;

import de.flapdoodle.embed.process.config.store.IPackageResolver;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;

@Deprecated
public class GenericPackageResolver implements IPackageResolver {

	@Override
	public Pattern executeablePattern(Distribution distribution) {
		return Pattern.compile(".*"+executableFilename(distribution));
	}

	@Override
	public String executableFilename(Distribution distribution) {
		switch (distribution.getPlatform()) {
			case Windows:
				return "phantomjs.exe";
		}
		return "phantomjs";
	}

	@Override
	public ArchiveType getArchiveType(Distribution distribution) {
		switch (distribution.getPlatform()) {
			case OS_X:
			case Windows:
				return ArchiveType.ZIP;
		}
		return ArchiveType.TBZ2;
	}

	@Override
	public String getPath(Distribution distribution) {
		final String packagePrefix;
		String bitVersion="";
		switch (distribution.getPlatform()) {
			case OS_X:
				packagePrefix="macosx";
				break;
			case Windows:
				packagePrefix="windows";
				break;
			default:
				packagePrefix="linux";
				switch (distribution.getBitsize()) {
					case B64:
						bitVersion="-x86_64";
						break;
					default:
						bitVersion="-i686";
				}
		}
		
		String packageExtension=".zip";
		if (getArchiveType(distribution)==ArchiveType.TBZ2) {
			packageExtension=".tar.bz2";
		}
		return "phantomjs-"+distribution.getVersion().asInDownloadPath()+"-"+packagePrefix+bitVersion+packageExtension;
	}
	
}