package de.flapdoodle.embed.process.store;

import de.flapdoodle.embed.process.config.store.FileSet;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

/**
 * @author [[mailto:michael@ahlers.consulting Michael Ahlers]]
 */
public class FileSetEntryTest {

    @Test
    public void equalsContract() {
        EqualsVerifier
                .forClass(FileSet.Entry.class)
                .verify();
    }

}
