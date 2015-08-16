package de.flapdoodle.embed.process;

import java.io.File;
import java.io.IOException;

import org.junit.rules.TemporaryFolder;

import de.flapdoodle.embed.process.io.directories.IDirectory;

public class TempDir implements IDirectory {

    final File f;

    public TempDir(final TemporaryFolder tempFolder) throws IOException {
        f = tempFolder.newFolder();
    }

    @Override
    public File asFile() {
        return f;
    }

    @Override
    public boolean isGenerated() {
        return true;
    }
}