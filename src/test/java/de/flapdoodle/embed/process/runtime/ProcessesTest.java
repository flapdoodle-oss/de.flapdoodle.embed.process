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

