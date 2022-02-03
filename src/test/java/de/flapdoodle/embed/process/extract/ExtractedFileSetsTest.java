/**
 * Copyright (C) 2011
 *   Michael Mosmann <michael@mosmann.de>
 *   Martin Jöhren <m.joehren@googlemail.com>
 *
 * with contributions from
 * 	konstantin-ba@github,
	Archimedes Trajano (trajano@github),
	Kevin D. Keck (kdkeck@github),
	Ben McCann (benmccann@github)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.flapdoodle.embed.process.extract;

import de.flapdoodle.embed.process.io.directories.PlatformTempDir;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;

public class ExtractedFileSetsTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void shouldNotCopyIfFileAlreadyExists() throws IOException {
        // given
        // Make sure we already have a file at the target destination
        File platformTempDir = new File(System.getProperty("java.io.tmpdir"));
        File executableInPlatformTempDir = new File(platformTempDir, "mongod");
        Files.write(executableInPlatformTempDir.toPath(), "old".getBytes(StandardCharsets.UTF_8));

        // when
        File baseDir = temporaryFolder.newFolder();
        File executable = new File(baseDir, "mongod");
        Files.write(executable.toPath(), "new".getBytes(StandardCharsets.UTF_8));
        ExtractedFileSet src = ExtractedFileSet.builder(baseDir).executable(executable).baseDirIsGenerated(false).build();

        ExtractedFileSets.copy(src, new PlatformTempDir(), (prefix, postfix) -> "mongod");

        // then
        byte[] allBytes = Files.readAllBytes(executableInPlatformTempDir.toPath());
        assertEquals("old", new String(allBytes, StandardCharsets.UTF_8));
    }
}
