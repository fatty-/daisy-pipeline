package org.daisy.converter.parser.stax;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.common.xproc.XProcPipelineInfo;
import org.daisy.common.xproc.XProcPipelineInfo.Builder;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.converter.parser.XProcScriptConstants.Attributes;
import org.daisy.converter.parser.XProcScriptConstants.Elements;
import org.daisy.converter.parser.XProcScriptConstants.Values;

// TODO: Auto-generated Javadoc
/**
 * Parser the xproc pipeline info, not only for scripts but for regular steps.
 */
public class StaxXProcPipelineInfoParser {
	/** The xmlinputfactory. */
	private XMLInputFactory mFactory;

	/** The uri resolver. */
	private URIResolver mUriResolver;




	/**
	 * Sets the uri resolver.
	 *
	 * @param uriResolver the new uri resolver
	 */
	public void setUriResolver(URIResolver uriResolver) {
		mUriResolver = uriResolver;
	}


	/**
	 * Sets the factory.
	 *
	 * @param factory the new factory
	 */
	public void setFactory(XMLInputFactory factory) {
		mFactory = factory;
	}

	/**
	 * Parses the the pipeline file
	 *
	 * @param uri the uri
	 * @return the x proc pipeline info
	 */
	public XProcPipelineInfo parse(URI uri) {
		return new StatefulParser().parse(uri);
	}

	/**
	 *StatefulParser is thread safe.
	 */
	private class StatefulParser {

		/** The m ancestors. */
		private final LinkedList<XMLEvent> mAncestors = new LinkedList<XMLEvent>();

		/**
		 * Parses the info from the xproc pipeline located at the provided uri
		 *
		 * @param uri the uri
		 * @return the x proc pipeline info
		 */
		public XProcPipelineInfo parse(URI uri) {
			if (mFactory == null) {
				throw new IllegalStateException();
			}
			InputStream is = null;
			XMLEventReader reader = null;

			try {
				// init the XMLStreamReader
				// uRL descUrl = pipelineInfo.getURI().toURL();
				URL descUrl = uri.toURL();
				// if we have a url resolver resolve the
				if (mUriResolver != null) {
					Source src = mUriResolver.resolve(descUrl.toString(), "");
					descUrl = new URL(src.getSystemId());

				}
				is = descUrl.openConnection().getInputStream();
				reader = mFactory.createXMLEventReader(is);
				XProcPipelineInfo.Builder infoBuilder = new XProcPipelineInfo.Builder();
				infoBuilder.withURI(uri);
				parseInfoElements(reader, infoBuilder);
				return infoBuilder.build();
			} catch (XMLStreamException e) {
				throw new RuntimeException("Parsing error: " + e.getMessage(),
						e);
			} catch (IOException e) {
				throw new RuntimeException("io error while parsing"
						+ e.getMessage(), e);
			} catch (TransformerException te) {
				throw new RuntimeException("Error resolving url: "
						+ te.getMessage(), te);
			} finally {
				try {
					if (reader != null) {
						reader.close();
					}
					if (is != null) {
						is.close();
					}
				} catch (Exception e) {
					// ignore;
				}
			}

		}

		/**
		 * Checks if is first child.
		 *
		 * @return true, if is first child
		 */
		public boolean isFirstChild() {
			return mAncestors.size() == 2;
		}

		/**
		 * Reads the next element
		 *
		 * @param reader the reader
		 * @return the xML event
		 * @throws XMLStreamException the xML stream exception
		 */
		private XMLEvent readNext(XMLEventReader reader)
				throws XMLStreamException {
			XMLEvent event = reader.nextEvent();

			if (event.isStartElement()) {
				mAncestors.add(event);
			} else if (event.isEndElement()) {
				mAncestors.pollLast();
			}
			return event;
		}

		/**
		 * Parses the info elements (options, input and outputs)
		 *
		 * @param reader the reader
		 * @param infoBuilder the info builder
		 * @throws XMLStreamException the xML stream exception
		 */
		private void parseInfoElements(final XMLEventReader reader,
				final Builder infoBuilder) throws XMLStreamException {

			while (reader.hasNext()) {
				XMLEvent event = readNext(reader);
				if (event.isStartElement()) {
					if (event.asStartElement().getName()
							.equals(Elements.P_OPTION)) {
						parseOption(event, infoBuilder);
					} else if (isFirstChild()
							&& (event.asStartElement().getName()
									.equals(Elements.P_INPUT)
							|| event.asStartElement().getName()
									.equals(Elements.P_OUTPUT))) {
						parsePort(event, infoBuilder);
					}
				}
				//TODO break the loop when reached the subpipeline
			}

		}

		/**
		 * Parses a port.
		 *
		 * @param event the event
		 * @param infoBuilder the info builder
		 */
		private void parsePort(XMLEvent event, Builder infoBuilder) {
			QName elemName = event.asStartElement().getName();
			boolean primary = false;
			boolean sequence = false;
			Attribute portAttr = event.asStartElement().getAttributeByName(
					Attributes.PORT);
			Attribute kindAttr = event.asStartElement().getAttributeByName(
					Attributes.KIND);
			Attribute primaryAttr = event.asStartElement().getAttributeByName(
					Attributes.PRIMARY);
			Attribute sequenceAttr = event.asStartElement().getAttributeByName(
					Attributes.SEQUENCE);
			if (primaryAttr != null && Values.TRUE.equals(primaryAttr.getValue())) {
				primary = true;
			}
			if (sequenceAttr != null && Values.TRUE.equals(sequenceAttr.getValue())) {
				sequence = true;
			}
			XProcPortInfo info = null;
			if (portAttr != null) {
				if (kindAttr != null && elemName.equals(Elements.P_INPUT)
						&& Values.PARAMETER.equals(kindAttr.getValue())) {
					info = XProcPortInfo.newParameterPort(portAttr.getValue(),
							primary);
				} else if (elemName.equals(Elements.P_INPUT)) {
					info = XProcPortInfo.newInputPort(portAttr.getValue(),
							sequence, primary);
				} else if (elemName.equals(Elements.P_OUTPUT)) {
					info = XProcPortInfo.newOutputPort(portAttr.getValue(),
							sequence, primary);
				}
			}
			if (info != null) {
				infoBuilder.withPort(info);
			}

		}

		/**
		 * Parses an option.
		 *
		 * @param event the event
		 * @param infoBuilder the info builder
		 */
		private void parseOption(final XMLEvent event, final Builder infoBuilder) {
			QName name = null;
			boolean required = false;
			String select = null;
			Attribute nameAttr = event.asStartElement().getAttributeByName(
					Attributes.NAME);
			Attribute requiredAttr = event.asStartElement().getAttributeByName(
					Attributes.REQUIRED);
			Attribute selectAttr = event.asStartElement().getAttributeByName(
					Attributes.SELECT);
			if (nameAttr != null) {
				name = new QName(nameAttr.getValue());
			}
			if (requiredAttr != null) {
				if (Values.TRUE.equals(requiredAttr.getValue())) {
					required = true;
				}
			}
			if (selectAttr != null) {
				select = selectAttr.getValue();
			}
			infoBuilder.withOption(XProcOptionInfo.newOption(name, required,
					select));

		}
	}
}
