package de.flapdoodle.embed.process.io.progress;

import de.flapdoodle.checks.Preconditions;

import java.io.Closeable;
import java.util.Optional;

public class ProgressListeners {

	private static final ThreadLocal<ProgressListener> threadLocal=new ThreadLocal<>();

	private static final ProgressListener NOOP = new ProgressListener() {
		@Override public void progress(String label, int percent) {

		}
		@Override public void done(String label) {

		}
		@Override public void start(String label) {

		}
		@Override public void info(String label, String message) {

		}
	};

	public static Optional<ProgressListener> progressListener() {
		return Optional.ofNullable(threadLocal.get());
	}

	public static RemoveProgressListener setProgressListener(ProgressListener progressListener) {
		Preconditions.checkNotNull(progressListener,"progressListener is null");
		threadLocal.set(progressListener);

		return () -> clearProgressListener();
	}

	public static void clearProgressListener() {
		threadLocal.set(null);
	}
	
	public static ProgressListener noop() {
		return NOOP;
	}

	public interface RemoveProgressListener extends AutoCloseable {
		@Override void close();
	}
}
