package de.flapdoodle.embed.processg.io.directories;

public class UserNaming implements Naming {
	@Override
	public String nameFor(String prefix, String postfix) {
		String username = System.getProperty("user.name");
		return prefix + "-" + username + "-" + postfix;
	}
}
