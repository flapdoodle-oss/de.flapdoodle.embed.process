package de.flapdoodle.embed.process.types;

public class ExecutedProcessImpl implements ExecutedProcess {

	private final int returnCode;

	public ExecutedProcessImpl(int returnCode) {
		this.returnCode = returnCode;
	}

	@Override
	public int returnCode() {
		return returnCode;
	}

	@Override public String toString() {
		return "ExecutedProcess{" +
			"returnCode=" + returnCode +
			'}';
	}
	
	public static <R extends RunningProcess> ExecutedProcess stop(R r) {
		int returnCode = r.stop();
		return new ExecutedProcessImpl(returnCode);
	}
}
