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
package de.flapdoodle.embed.process.store;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.flapdoodle.embed.process.TempDir;
import de.flapdoodle.embed.process.config.store.DistributionPackage;
import de.flapdoodle.embed.process.config.store.DownloadConfig;
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.config.store.PackageResolver;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.embed.process.extract.UUIDTempNaming;
import de.flapdoodle.embed.process.io.directories.Directory;
import de.flapdoodle.embed.process.io.progress.StandardConsoleProgressListener;

public class LocalArtifactStoreTest {

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @Test
  public void storingArtifactShouldNotFailWhenAlreadyExists() throws IOException {
    Distribution distribution = Distribution.detectFor(Version.of("1.0.37"));

    Directory artifactDir = new TempDir(tempFolder);

    File source = new File(this.getClass().getResource("/mocks/mocked-artifact.zip").getPath());
    Path copiedFile = Files.copy(source.toPath(),
                                 artifactDir.asFile().toPath().resolve(artifactName(distribution)));
    assertNotNull(copiedFile);

    assertTrue(LocalArtifactStore.store(downloadConfig(artifactDir),
                                        distribution,
                                        source));
  }

  private static String artifactName(Distribution distribution) {
    return ExtractedArtifactStore.asPath(distribution) + ".zip";
  }

  private DownloadConfig downloadConfig(Directory artifactDir) {
    return DownloadConfig.builder().downloadPrefix("prefix")
                                      .downloadPath(__ -> "foo")
                                      .packageResolver(packageResolver())
                                      .artifactStorePath(artifactDir)
                                      .fileNaming(new UUIDTempNaming())
                                      .progressListener(new StandardConsoleProgressListener())
                                      .userAgent("foo-bar")
                                      .build();
  }

  private PackageResolver packageResolver() {
    return new PackageResolver() {

      @Override
      public DistributionPackage packageFor(Distribution distribution) {
        return DistributionPackage.of(getArchiveType(distribution),
                                      getFileSet(distribution),
                                      getPath(distribution));
      }

      public String getPath(Distribution distribution) {
        return artifactName(distribution);
      }

      public FileSet getFileSet(Distribution distribution) {
        return new FileSet.Builder().addEntry(FileType.Executable, "my-prog.bat")
                                    .addEntry(FileType.Library, "lib/my-lib.txt", ".*my-lib.txt")
                                    .build();
      }

      public ArchiveType getArchiveType(Distribution distribution) {
        return ArchiveType.TGZ;
      }
    };
  }
}
