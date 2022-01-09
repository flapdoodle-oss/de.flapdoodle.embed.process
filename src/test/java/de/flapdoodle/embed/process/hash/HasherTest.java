package de.flapdoodle.embed.process.hash;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HasherTest {

	@Test
	public void sha256Hash() {
		String result = Hasher.instance().update("hello").hashAsString();
		assertThat(result).isEqualTo("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824");
	}

	@Test
	public void md5Hash() {
		String result = Hasher.md5Instance().update("hello").hashAsString();
		assertThat(result).isEqualTo("5d41402abc4b2a76b9719d911017c592");
	}
}