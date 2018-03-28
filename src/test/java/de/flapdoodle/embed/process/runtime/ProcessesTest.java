package de.flapdoodle.embed.process.runtime;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;

import javax.lang.model.SourceVersion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.*;

public class ProcessesTest {

	@Test
	public void usesPidMethodWithJava9() {
		assumeTrue(SourceVersion.latest().toString().equals("RELEASE_9"));
		Process mockProcess = mock(Process.class, withSettings().extraInterfaces(PidMethod.class));
		when(((PidMethod) mockProcess).pid()).thenReturn(123L);
		Long pid = Processes.processId(mockProcess);
		verify((PidMethod) mockProcess).pid();
		assertEquals(123L, pid.longValue());
	}

	@Test
	public void usesPidMethodWithJava10() {
		assumeTrue(SourceVersion.latest().toString().equals("RELEASE_10"));
		Process mockProcess = mock(Process.class, withSettings().extraInterfaces(PidMethod.class));
		when(((PidMethod) mockProcess).pid()).thenReturn(123L);
		Long pid = Processes.processId(mockProcess);
		verify((PidMethod) mockProcess).pid();
		assertEquals(123L, pid.longValue());
	}

	@Test
	public void usesPidFieldWithUNIXOSInJava8() {
		assumeTrue(SourceVersion.latest().toString().equals("RELEASE_8") && SystemUtils.IS_OS_UNIX);
		Process mockProcess = new ProcessWithPidField();
		Long pid = Processes.processId(mockProcess);
		assertEquals(123L, pid.longValue());
	}
}

