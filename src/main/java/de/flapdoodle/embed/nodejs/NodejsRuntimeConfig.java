/**
 * Copyright (C) 2011
 *   Michael Mosmann <michael@mosmann.de>
 *   Martin Jöhren <m.joehren@googlemail.com>
 *
 * with contributions from
 * 	konstantin-ba@github,Archimedes Trajano (trajano@github)
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
package de.flapdoodle.embed.nodejs;

import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.config.store.IDownloadConfig;
import de.flapdoodle.embed.process.extract.ITempNaming;
import de.flapdoodle.embed.process.extract.UUIDTempNaming;
import de.flapdoodle.embed.process.runtime.ICommandLinePostProcessor;


public class NodejsRuntimeConfig implements IRuntimeConfig {

	private ITempNaming defaultfileNaming = new UUIDTempNaming();
	private ITempNaming executableNaming = defaultfileNaming;
	private ProcessOutput processOutput = ProcessOutput.getDefaultInstance("nodejs");
	private ICommandLinePostProcessor commandLinePostProcessor = new ICommandLinePostProcessor.Noop();
	private NodejsDownloadConfig downloadConfig=new NodejsDownloadConfig();
	
	@Override
	public IDownloadConfig getDownloadConfig() {
		return downloadConfig;
	}

	@Override
	public ITempNaming getDefaultfileNaming() {
		return defaultfileNaming;
	}

	@Override
	public ITempNaming getExecutableNaming() {
		return executableNaming;
	}

	@Override
	public ProcessOutput getProcessOutput() {
		return processOutput;
	}

	@Override
	public ICommandLinePostProcessor getCommandLinePostProcessor() {
		return commandLinePostProcessor;
	}

	
	public void setDefaultfileNaming(ITempNaming defaultfileNaming) {
		this.defaultfileNaming = defaultfileNaming;
	}

	
	public void setExecutableNaming(ITempNaming executableNaming) {
		this.executableNaming = executableNaming;
	}

	
	public void setProcessOutput(ProcessOutput processOutput) {
		this.processOutput = processOutput;
	}

	
	public void setCommandLinePostProcessor(ICommandLinePostProcessor commandLinePostProcessor) {
		this.commandLinePostProcessor = commandLinePostProcessor;
	}

	
	public void setDownloadConfig(NodejsDownloadConfig downloadConfig) {
		this.downloadConfig = downloadConfig;
	}
	
	

}
