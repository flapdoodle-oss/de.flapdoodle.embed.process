package de.flapdoodle.embed.processg.io.directories;

import java.util.UUID;

public class UUIDNaming implements Naming {
	@Override
	public String nameFor(String prefix, String postfix) {
		return prefix + "-" + UUID.randomUUID() + postfix;
	}
}
