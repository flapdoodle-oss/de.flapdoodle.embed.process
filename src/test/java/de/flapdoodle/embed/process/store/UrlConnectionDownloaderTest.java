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