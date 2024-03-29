package org.daisy.pipeline.webserviceutils.xml;

import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.pipeline.script.XProcOptionMetadata;
import org.daisy.pipeline.script.XProcOptionMetadata.Output;
import org.daisy.pipeline.script.XProcPortMetadata;
import org.daisy.pipeline.script.XProcScript;
import org.daisy.pipeline.webserviceutils.Routes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ScriptXmlWriter {
	
	XProcScript script = null;
	boolean details = false;
	private static Logger logger = LoggerFactory.getLogger(ScriptXmlWriter.class.getName());
	
	public ScriptXmlWriter(XProcScript script) {
		this.script = script;
	}
	
	public ScriptXmlWriter withDetails() {
		details = true;
		return this;
	}
	
	public Document getXmlDocument() {
		if (script == null) {
			logger.warn("Could not create XML for null script");
			return null;
		}
		return scriptToXmlDocument(script);
	}
	
	// instead of creating a standalone XML document, add an element to an existing document
	public void addAsElementChild(Element parent) {
		Document doc = parent.getOwnerDocument();
		Element scriptElm = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "script");
		addElementData(script, scriptElm);
		parent.appendChild(scriptElm);
	}
	
	private Document scriptToXmlDocument(XProcScript script) {
		Document doc = XmlUtils.createDom("script");
		Element scriptElm = doc.getDocumentElement();
		addElementData(script, scriptElm);
		
		// for debugging only
		if (!XmlValidator.validate(doc, XmlValidator.SCRIPT_SCHEMA_URL)) {
			logger.error("INVALID XML:\n" + XmlUtils.DOMToString(doc));
		}

		return doc;
	}

	// element is <script> but it's empty
	private void addElementData(XProcScript script, Element element) {
		Document doc = element.getOwnerDocument();
		String baseUri = new Routes().getBaseUri();
		String scriptHref = baseUri + Routes.SCRIPT_ROUTE.replaceFirst("\\{id\\}", script.getDescriptor().getId());

		element.setAttribute("id", script.getDescriptor().getId());
		element.setAttribute("href", scriptHref);

		Element nicenameElm = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "nicename");
		nicenameElm.setTextContent(script.getName());
		element.appendChild(nicenameElm);

		Element descriptionElm = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "description");
		descriptionElm.setTextContent(script.getDescription());
		element.appendChild(descriptionElm);

		if (details) {
			String homepage = script.getHomepage();
			if (homepage != null && homepage.trim().length() > 0) {
				Element homepageElm = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "homepage");
				homepageElm.setTextContent(homepage);
				element.appendChild(homepageElm);
			}
			
			addInputPorts(script.getXProcPipelineInfo().getInputPorts(), element);
			addOptions(script.getXProcPipelineInfo().getOptions(), element);
			addOutputPorts(script.getXProcPipelineInfo().getOutputPorts(), element);
		}
	}
	
	private void addInputPorts(Iterable<XProcPortInfo> inputs, Element parent) {
		Document doc = parent.getOwnerDocument();
		for (XProcPortInfo input : script.getXProcPipelineInfo().getInputPorts()) {
			XProcPortMetadata meta = script.getPortMetadata(input.getName());

			Element inputElm = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "input");
			inputElm.setAttribute("name", input.getName());
			inputElm.setAttribute("sequence", Boolean.toString(input.isSequence()));
			if (meta.getMediaType() != null && !meta.getMediaType().isEmpty()) {
				inputElm.setAttribute("mediaType", meta.getMediaType());
			}
			inputElm.setAttribute("desc", meta.getDescription());

			parent.appendChild(inputElm);
		}
	}

	private void addOptions(Iterable<XProcOptionInfo> options, Element parent) {
		Document doc = parent.getOwnerDocument();
		for (XProcOptionInfo option : options) {
			XProcOptionMetadata meta = script.getOptionMetadata(option.getName());
			
			Element optionElm = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "option");
			optionElm.setAttribute("name", option.getName().toString());
			optionElm.setAttribute("required", Boolean.toString(option.isRequired()));
			
			optionElm.setAttribute("type", meta.getType());
			if (meta.getMediaType() != null && !meta.getMediaType().isEmpty()) {
				optionElm.setAttribute("mediaType", meta.getMediaType());
			}
			optionElm.setAttribute("desc", meta.getDescription());
			optionElm.setAttribute("ordered", Boolean.toString(meta.isOrdered()));
			optionElm.setAttribute("sequence", Boolean.toString(meta.isSequence()));
			
			if (meta.getOutput() != Output.NA) {
				optionElm.setAttribute("outputType", meta.getOutput().toString().toLowerCase());
			}
			parent.appendChild(optionElm);
		}
	}
	
	private void addOutputPorts(Iterable<XProcPortInfo> outputs, Element parent) {
		Document doc = parent.getOwnerDocument();
		for (XProcPortInfo output : outputs) {
			XProcPortMetadata meta = script.getPortMetadata(output.getName());
			Element outputElm = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "output");
			outputElm.setAttribute("name", output.getName());
			outputElm.setAttribute("sequence", Boolean.toString(output.isSequence()));
			if (meta.getMediaType() != null && !meta.getMediaType().isEmpty()) {
				outputElm.setAttribute("mediaType", meta.getMediaType());
			}
			outputElm.setAttribute("desc", meta.getDescription());
			
			parent.appendChild(outputElm);
		}
	}
}