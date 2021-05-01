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
package de.flapdoodle.embed.process.distribution;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class VersionTest {

    @Test
    public void isNewerOrEqual_1() {
        Version v1 = new VersionForComparison(1,2, 3);
        Version v2 = new VersionForComparison(1,2, 4);
        assertFalse(v1.isNewerOrEqual(v2.major(), v2.minor(), v2.patch()));
    }

    @Test
    public void isNewerOrEqual_2() {
        Version v1 = new VersionForComparison(1,2, 4);
        Version v2 = new VersionForComparison(1,2, 4);
        assertTrue(v1.isNewerOrEqual(v2.major(), v2.minor(), v2.patch()));
    }

    @Test
    public void isNewerOrEqual_3() {
        Version v1 = new VersionForComparison(1,2, 5);
        Version v2 = new VersionForComparison(1,2, 4);
        assertTrue(v1.isNewerOrEqual(v2.major(), v2.minor(), v2.patch()));
    }

    @Test
    public void isOlderOrEqual_1() throws Exception {
        Version v1 = new VersionForComparison(1,2, 3);
        Version v2 = new VersionForComparison(1,2, 4);
        assertTrue(v1.isOlderOrEqual(v2.major(), v2.minor(), v2.patch()));
    }

    @Test
    public void isOlderOrEqual_2() throws Exception {
        Version v1 = new VersionForComparison(1,2, 4);
        Version v2 = new VersionForComparison(1,2, 4);
        assertTrue(v1.isOlderOrEqual(v2.major(), v2.minor(), v2.patch()));
    }

    @Test
    public void isOlderOrEqual_3() throws Exception {
        Version v1 = new VersionForComparison(1,2, 5);
        Version v2 = new VersionForComparison(1,2, 4);
        assertFalse(v1.isOlderOrEqual(v2.major(), v2.minor(), v2.patch()));
    }
    private static class VersionForComparison implements Version {

        private final int major;
        private final int minor;
        private final int patch;

        private VersionForComparison(int major, int minor, int patch) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
        }

        @Override
        public int major() {
            return major;
        }

        @Override
        public int minor() {
            return minor;
        }

        @Override
        public int patch() {
            return patch;
        }

        @Override
        public String asInDownloadPath() {
            throw new UnsupportedOperationException("Not relevant for these tests");
        }
    }
}
