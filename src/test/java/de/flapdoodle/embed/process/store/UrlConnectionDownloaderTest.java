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

import de.flapdoodle.embed.process.config.store.DownloadConfig;
import de.flapdoodle.embed.process.config.store.TimeoutConfig;
import de.flapdoodle.embed.process.io.progress.ProgressListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UrlConnectionDownloaderTest {
	private static final int LISTEN_PORT = 17171;
	Server server;

	@Rule
	public TemporaryFolder tempDir = new TemporaryFolder();

	private DownloadConfig dc;
	private ProgressListener pl;

	@Before
	public void setUp() throws Exception {
		// start the jetty container
		server = new Server(LISTEN_PORT);
		File myTmpDir = tempDir.newFolder();
		File osxDir = new File(myTmpDir, "sample");
		osxDir.mkdir();
		File tempFile = new File(osxDir, "file");
		tempFile.createNewFile();
		ResourceHandler publicDocs = new ResourceHandler();
		publicDocs.setResourceBase(myTmpDir.getCanonicalPath());

		HandlerList hl = new HandlerList();
		hl.setHandlers(new Handler[]{publicDocs});
		server.setHandler(hl);

		server.start();

		while (true) {
			if (server.isRunning()) {
				break;
			}
			Thread.sleep(100);
		}
		dc = mock(DownloadConfig.class);
		pl = mock(ProgressListener.class);
	}

	@After
	public void tearDown() throws Exception {
		server.stop();
	}

	@Test
	public void testDownload() throws Exception {
		initRuntime();
		Downloader downloader = new UrlConnectionDownloader();
		File downloaded = downloader.download(dc, "http://localhost:" + LISTEN_PORT + "/sample/file");
		assertNotNull(downloaded);
		System.out.println("-> "+downloaded);
	}

	private void initRuntime() {
		when(dc.getTimeoutConfig()).thenReturn(TimeoutConfig.defaults());
		when(dc.getProgressListener()).thenReturn(pl);
	}
}