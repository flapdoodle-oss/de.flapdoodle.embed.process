/**
 * Copyright (C) 2011
 * Michael Mosmann <michael@mosmann.de>
 * Martin Jöhren <m.joehren@googlemail.com>
 * <p>
 * with contributions from
 * konstantin-ba@github,
 * Archimedes Trajano (trajano@github),
 * Kevin D. Keck (kdkeck@github),
 * Ben McCann (benmccann@github)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.flapdoodle.embed.process.config.store;

import de.flapdoodle.embed.process.example.GenericPackageResolver;
import de.flapdoodle.embed.process.extract.UUIDTempNaming;
import de.flapdoodle.embed.process.io.directories.PlatformTempDir;
import de.flapdoodle.embed.process.io.progress.StandardConsoleProgressListener;
import org.junit.Test;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import static org.junit.Assert.assertEquals;

public class AuthorizationTest {

    @Test
    public void setAuthorizationHeaderTest() throws UnsupportedEncodingException {

        final String BASIC_AUTH = "Basic " + Base64.getEncoder()
                .encodeToString(("foo-user:foo-password").getBytes("UTF-8"));

        PackageResolver packageResolver = new GenericPackageResolver();

        DownloadConfig downloadConfig = new DownloadConfigBuilder()
                .downloadPath("http://localhost")
                .downloadPrefix("prefix")
                .packageResolver(packageResolver)
                .artifactStorePath(new PlatformTempDir())
                .fileNaming(new UUIDTempNaming())
                .progressListener(new StandardConsoleProgressListener())
                .userAgent("foo-bar")
                .authorization(BASIC_AUTH)
                .build();

        assertEquals(downloadConfig.getAuthorization(), BASIC_AUTH);

    }

}
