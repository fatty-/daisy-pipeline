package org.daisy.pipeline.ui.commandline;

import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.pipeline.script.ScriptRegistry;
import org.daisy.pipeline.script.XProcOptionMetadata;
import org.daisy.pipeline.script.XProcPortMetadata;
import org.daisy.pipeline.script.XProcScript;
import org.daisy.pipeline.script.XProcScriptService;

public final class CommandScriptHelp implements Command {

	public static CommandScriptHelp newInstance(String scriptName,
			ScriptRegistry scriptRegistry) {
		return new CommandScriptHelp(scriptName, scriptRegistry);
	}

	private final String scriptName;
	private final ScriptRegistry scriptRegistry;

	private CommandScriptHelp(String scriptName, ScriptRegistry scriptRegistry) {
		this.scriptName = scriptName;
		this.scriptRegistry = scriptRegistry;
	}

	@Override
	public void execute() throws IllegalArgumentException {
		for (XProcScriptService scriptService : scriptRegistry.getScripts()) {
			if (scriptService.getName().equals(scriptName)) {
				System.out.println(toString(scriptService.load()));
				return;
			}
		}
		System.out.println("Script '" + scriptName + "' not found");
	}

	private static String toString(XProcScript script) {
		StringBuilder sb = new StringBuilder();
		sb.append("SCRIPT").append('\n');
		sb.append('\t').append(script.getName()).append('\n');
		sb.append('\t').append(script.getURI()).append('\n');
		sb.append('\n');
		if (script.getDescription() != null) {
			sb.append("DESCRIPTION").append('\n');
			sb.append('\t').append(script.getDescription()).append('\n');
			sb.append('\n');
		}
		// TODO getInputPorts should return a list ? or hasInputPorts() ?
		if (script.getXProcPipelineInfo().getInputPorts().iterator().hasNext()) {
			sb.append("INPUT PORTS").append('\n');
			for (XProcPortInfo port : script.getXProcPipelineInfo()
					.getInputPorts()) {
				XProcPortMetadata meta = script.getPortMetadata(port.getName());
				sb.append('\t').append(port.getName());
				if (meta.getNiceName() != null) {
					sb.append(": ").append(meta.getNiceName());
				}
				sb.append(" (").append(
						port.isSequence() ? "sequence of documents"
								: "single document");
				if (meta.getMediaType() != null) {
					sb.append(" of type '").append(meta.getMediaType());
				}
				sb.append(")").append('\n');
				if (meta.getDescription() != null) {
					sb.append('\t').append('\t').append(meta.getDescription())
							.append('\n');
				}
			}
			sb.append('\n');
		}
		if (script.getXProcPipelineInfo().getOutputPorts().iterator().hasNext()) {
			sb.append("OUTPUT PORTS").append('\n');
			for (XProcPortInfo port : script.getXProcPipelineInfo()
					.getOutputPorts()) {
				XProcPortMetadata meta = script.getPortMetadata(port.getName());
				sb.append('\t').append(port.getName());
				if (meta.getNiceName() != null) {
					sb.append(": ").append(meta.getNiceName());
				}
				sb.append(" (").append(
						port.isSequence() ? "sequence of documents"
								: "single document");
				if (meta.getMediaType() != null) {
					sb.append(" of type '").append(meta.getMediaType());
				}
				sb.append(")").append('\n');
				if (meta.getDescription() != null) {
					sb.append('\t').append('\t').append(meta.getDescription())
							.append('\n');
				}
			}
			sb.append('\n');
		}
		if (script.getXProcPipelineInfo().getParameterPorts().iterator()
				.hasNext()) {
			sb.append("PARAMETER PORTS").append('\n');
			for (String port : script.getXProcPipelineInfo()
					.getParameterPorts()) {
				XProcPortMetadata meta = script.getPortMetadata(port);
				sb.append('\t').append(port);
				if (meta.getNiceName() != null) {
					sb.append(": ").append(meta.getNiceName());
				}
				sb.append('\n');
				if (meta.getDescription() != null) {
					sb.append('\t').append('\t').append(meta.getDescription())
							.append('\n');
				}
			}
			sb.append('\n');
		}
		if (script.getXProcPipelineInfo().getOptions().iterator().hasNext()) {
			sb.append("OPTIONS").append('\n');
			for (XProcOptionInfo option : script.getXProcPipelineInfo()
					.getOptions()) {
				XProcOptionMetadata meta = script.getOptionMetadata(option
						.getName());
				sb.append('\t').append(option.getName());
				if (meta.getNiceName() != null) {
					sb.append(": ").append(meta.getNiceName());
				}
				sb.append(" (").append(
						option.isRequired() ? "required" : "optional");
				if (meta.getType() != null) {
					sb.append(", type: ").append(meta.getType());
				}
				if (option.getSelect() != null) {
					sb.append(", default: ").append(option.getSelect());
				}
				sb.append(")").append('\n');
				if (meta.getDescription() != null) {
					sb.append('\t').append('\t').append(meta.getDescription())
							.append('\n');
				}
			}
			sb.append('\n');
		}
		return sb.toString();
	}

}
