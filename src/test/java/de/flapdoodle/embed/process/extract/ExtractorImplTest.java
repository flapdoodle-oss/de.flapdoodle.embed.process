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

import de.flapdoodle.embed.process.TempDir;
import de.flapdoodle.embed.process.config.store.*;
import de.flapdoodle.embed.process.example.GenericPackageResolver;
import de.flapdoodle.embed.process.io.directories.PlatformTempDir;
import de.flapdoodle.embed.process.io.progress.StandardConsoleProgressListener;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author viliusl
 * @since 18/09/14
 */
public class ExtractorImplTest {

    private IDownloadConfig runtime;
    private FilesToExtract fte;
    private File fileInArchive;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        IPackageResolver packageResolver=new GenericPackageResolver();
        runtime=new DownloadConfigBuilder()
                .downloadPath("http://192.168.0.1")
                .downloadPrefix("prefix")
                .packageResolver(packageResolver)
                .artifactStorePath(new PlatformTempDir())
                .fileNaming(new UUIDTempNaming())
                .progressListener(new StandardConsoleProgressListener())
                .userAgent("foo-bar")
                .build();

        fte = new FilesToExtract(
                new TempDir(folder),
                new UUIDTempNaming(),
                FileSet.builder().addEntry(FileType.Executable, "readme.txt").build());
        fileInArchive = new File(this.getClass().getResource("/archives/readme.txt").getPath());
    }

    @Test
    public void testZipFormat() throws IOException {
        File source = new File(this.getClass().getResource("/archives/sample.zip").getPath());
        ZipExtractor extractor = new ZipExtractor();
        IExtractedFileSet extracted = extractor.extract(runtime, source, fte);

        assertTrue("extracted file exists", extracted.executable().exists());
        assertEquals(FileUtils.readFileToString(fileInArchive), FileUtils.readFileToString(extracted.executable()));
    }

    @Test
    public void testTgzFormat() throws IOException {
        File source = new File(this.getClass().getResource("/archives/sample.tgz").getPath());
        TgzExtractor extractor = new TgzExtractor();

        IExtractedFileSet extracted = extractor.extract(runtime, source, fte);

        assertTrue("extracted file exists", extracted.executable().exists());
        assertEquals(FileUtils.readFileToString(fileInArchive), FileUtils.readFileToString(extracted.executable()));
    }

    @Test
    public void testTbz2Format() throws IOException {
        File source = new File(this.getClass().getResource("/archives/sample.tbz2").getPath());
        Tbz2Extractor extractor = new Tbz2Extractor();

        IExtractedFileSet extracted = extractor.extract(runtime, source, fte);

        assertTrue("extracted file exists", extracted.executable().exists());
        assertEquals(FileUtils.readFileToString(fileInArchive), FileUtils.readFileToString(extracted.executable()));
    }
}
