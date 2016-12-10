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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.extract.ImmutableExtractedFileSet.Builder;
import de.flapdoodle.embed.process.io.directories.IDirectory;
import de.flapdoodle.embed.process.io.file.Files;

public abstract class ExtractedFileSets {
	
	private static Logger logger = LoggerFactory.getLogger(ExtractedFileSets.class);

	private ExtractedFileSets() {
		// no instance
	}
	
	public static IExtractedFileSet copy(IExtractedFileSet src, IDirectory directory, ITempNaming executableNaming) throws IOException {
		File destination = directory.asFile();
		File baseDir = src.baseDir();
		File oldExe = src.executable();
		Builder builder = ImmutableExtractedFileSet.builder(destination)
				.baseDirIsGenerated(directory.isGenerated());

		Files.createOrCheckDir(Files.fileOf(destination, oldExe).getParentFile());
		Path newExeFile = java.nio.file.Files.copy(Files.fileOf(baseDir, oldExe).toPath(), Files.fileOf(destination, executableNaming.nameFor("extract", oldExe.getName())).toPath());
		builder.executable(newExeFile.toFile());

		for (FileType type : EnumSet.complementOf(EnumSet.of(FileType.Executable))) {
			for (File srcFile : src.files(type)) {
				File destinationFile = Files.fileOf(destination, srcFile);
				Files.createOrCheckDir(destinationFile.getParentFile());
				Path newFile=java.nio.file.Files.copy(Files.fileOf(baseDir, srcFile).toPath(), destinationFile.toPath());
				builder.file(type, newFile.toFile());
			}
		}
		return builder.build();
	}

	public static void delete(IExtractedFileSet all) {
		for (FileType type : EnumSet.complementOf(EnumSet.of(FileType.Executable))) {
			for (File file : all.files(type)) {
				if (file.exists() && !Files.forceDelete(file))
					logger.warn("Could not delete {} NOW: {}", type, file);
			}
		}
		File exe=all.executable();
		if (exe.exists() && !Files.forceDelete(exe)) {
			logger.warn("Could not delete executable NOW: {}", exe);
		}
		
		if (all.baseDirIsGenerated()) {
			if (!Files.forceDelete(all.baseDir())) {
				logger.warn("Could not delete generatedBaseDir: {}", all.baseDir());
			}
		}
	}

}
