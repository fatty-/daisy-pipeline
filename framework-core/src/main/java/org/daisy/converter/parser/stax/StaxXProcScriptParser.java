package org.daisy.converter.parser.stax;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.daisy.common.stax.EventProcessor;
import org.daisy.common.stax.StaxEventHelper;
import org.daisy.common.stax.StaxEventHelper.EventPredicates;
import org.daisy.converter.parser.XProcScriptConstants;
import org.daisy.converter.parser.XProcScriptConstants.Attributes;
import org.daisy.converter.parser.XProcScriptConstants.Elements;
import org.daisy.converter.parser.XProcScriptConstants.Values;
import org.daisy.pipeline.script.XProcOptionMetadata;
import org.daisy.pipeline.script.XProcPortMetadata;
import org.daisy.pipeline.script.XProcScript;
import org.daisy.pipeline.script.XProcScriptParser;
import org.daisy.pipeline.script.XProcScriptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

// TODO: Auto-generated Javadoc
/**
 * StaxXProcScriptParser parses the xpl file extracting the metadata and buiding the XProcScript object
 */
public class StaxXProcScriptParser implements XProcScriptParser {

	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(StaxXProcScriptParser.class);
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
	 * Activate (OSGI)
	 */
	public void activate(){
		logger.trace("Activating XProc script parser");
	}

	/* (non-Javadoc)
	 * @see org.daisy.pipeline.script.XProcScriptParser#parse(java.net.URI)
	 */
	@Override
	public XProcScript parse(final XProcScriptService descriptor) {
		return new StatefulParser().parse(descriptor);
	}

	/**
	 *StatefulParser makes the parsing process thread safe.
	 */
	private class StatefulParser {

		/** The m ancestors. */
		private final LinkedList<XMLEvent> mAncestors = new LinkedList<XMLEvent>();

		/** The m port builders. */
		private final LinkedList<XProcPortMetadataBuilderHolder> mPortBuilders = new LinkedList<XProcPortMetadataBuilderHolder>();

		/** The m option builders. */
		private final LinkedList<XProcOptionMetadataBuilderHolder> mOptionBuilders = new LinkedList<XProcOptionMetadataBuilderHolder>();

		/** The script builder. */
		private XProcScript.Builder scriptBuilder ;

		/**
		 * Parses the xpl file extracting the metadata attached to options,ports and the step
		 *
		 * @param uri the uri
		 * @return the x proc script
		 */
		public XProcScript parse(final XProcScriptService descriptor) {
			if (mFactory == null) {
				throw new IllegalStateException();
			}
			InputStream is = null;
			XMLEventReader reader = null;
			scriptBuilder = new XProcScript.Builder();
			logger.debug("Parsing with descriptor:"+descriptor);
			scriptBuilder=scriptBuilder.withDescriptor(descriptor);
			StaxXProcPipelineInfoParser infoParser = new StaxXProcPipelineInfoParser();
			infoParser.setFactory(mFactory);
			infoParser.setUriResolver(mUriResolver);
			try {
				// init the XMLStreamReader
				// uRL descUrl = pipelineInfo.getURI().toURL();
				scriptBuilder.withPipelineInfo(infoParser.parse(descriptor.getURI()));
				URL descUrl = descriptor.getURI().toURL();
				// if we have a url resolver resolve the
				if (mUriResolver != null) {
					Source src = mUriResolver.resolve(descUrl.toString(), "");
					descUrl = new URL(src.getSystemId());

				}
				is = descUrl.openConnection().getInputStream();
				reader = mFactory.createXMLEventReader(is);

				parseStep(reader);
				for (XProcOptionMetadataBuilderHolder bHolder : mOptionBuilders) {
					XProcOptionMetadata opt = bHolder.mBuilder.build();
					scriptBuilder.withOptionMetadata(new QName(bHolder.mName),
							opt);
				}
				for (XProcPortMetadataBuilderHolder bHolder : mPortBuilders) {
					XProcPortMetadata opt = bHolder.mBuilder.build();
					scriptBuilder.withPortMetadata(bHolder.mName, opt);
				}

			} catch (XMLStreamException e) {
				throw new RuntimeException("Parsing error: " + e.getMessage(),
						e);
			} catch (IOException e) {
				throw new RuntimeException(
						"Couldn't access package descriptor: " + e.getMessage(),
						e);
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
			return scriptBuilder.build();
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
		 * Parses the step.
		 *
		 * @param reader the reader
		 * @throws XMLStreamException the xML stream exception
		 */
		public void parseStep(final XMLEventReader reader) throws XMLStreamException {
			while (reader.hasNext()) {
				XMLEvent event = readNext(reader);
				if (event.isStartElement()
						&& event.asStartElement().getName()
								.equals(Elements.P_DOCUMENTATION)) {
					DocumentationHolder dHolder = new DocumentationHolder();
					parseDocumentation(reader, dHolder);
					if (isFirstChild()) {
						scriptBuilder.withDescription(dHolder.mDetail);
						scriptBuilder.withShortName(dHolder.mShort);
						scriptBuilder.withHomepage(dHolder.mHomepage);
					} else if (mAncestors.get(mAncestors.size() - 2)
							.asStartElement().getName()
							.equals(Elements.P_INPUT)
							|| mAncestors.get(mAncestors.size() - 2)
									.asStartElement().getName()
									.equals(Elements.P_OUTPUT)) {
						mPortBuilders.peekLast().mBuilder
								.withDescription(dHolder.mDetail);
						mPortBuilders.peekLast().mBuilder
								.withNiceName(dHolder.mShort);
					} else if (mAncestors.get(mAncestors.size() - 2)
							.asStartElement().getName()
							.equals(Elements.P_OPTION)) {
						mOptionBuilders.peekLast().mBuilder
								.withDescription(dHolder.mDetail);
						mOptionBuilders.peekLast().mBuilder
								.withNiceName(dHolder.mShort);
					}
				} else if (isFirstChild()
						&& event.isStartElement()
						&& (event.asStartElement().getName()
								.equals(Elements.P_INPUT) || event
								.asStartElement().getName()
								.equals(Elements.P_OUTPUT))) {

					// parse output
					Attribute name = event.asStartElement().getAttributeByName(
							new QName("port"));

					XProcPortMetadata.Builder portBuilder = new XProcPortMetadata.Builder();
					XProcPortMetadataBuilderHolder bHolder = new XProcPortMetadataBuilderHolder();
					bHolder.mName = name.getValue();
					bHolder.mBuilder = portBuilder;
					mPortBuilders.add(bHolder);
					parsePort(event.asStartElement(), portBuilder);

				} else if (event.isStartElement()
						&& event.asStartElement().getName()
								.equals(Elements.P_OPTION)) {
					XProcOptionMetadata.Builder optBuilder = new XProcOptionMetadata.Builder();
					Attribute name = event.asStartElement().getAttributeByName(
							XProcScriptConstants.Attributes.NAME);
					if (name != null) {
						XProcOptionMetadataBuilderHolder bHolder = new XProcOptionMetadataBuilderHolder();
						bHolder.mName = name.getValue();
						bHolder.mBuilder = optBuilder;
						parseOption(event.asStartElement(), optBuilder);
						mOptionBuilders.add(bHolder);

					}
				}

			}
		}

		/**
		 * Parses the option.
		 *
		 * @param optionElement the option element
		 * @param optionBuilder the option builder
		 * @throws XMLStreamException the xML stream exception
		 */
		protected void parseOption(final StartElement optionElement,
				final XProcOptionMetadata.Builder optionBuilder)
				throws XMLStreamException {

			Attribute type = optionElement
					.getAttributeByName(XProcScriptConstants.Attributes.PX_TYPE);
			/*Attribute dir = optionElement
					.getAttributeByName(XProcScriptConstants.Attributes.PX_DIR);
			*/
			Attribute mediaType = optionElement
					.getAttributeByName(XProcScriptConstants.Attributes.PX_MEDIA_TYPE);
			Attribute output = optionElement
					.getAttributeByName(XProcScriptConstants.Attributes.PX_OUTPUT);
			Attribute sequence = optionElement
					.getAttributeByName(XProcScriptConstants.Attributes.PX_SEQUENCE);
			Attribute ordered = optionElement
					.getAttributeByName(XProcScriptConstants.Attributes.PX_ORDERED);
			Attribute separator = optionElement
					.getAttributeByName(XProcScriptConstants.Attributes.PX_SEPARATOR);

			if (mediaType != null) {
				optionBuilder.withMediaType(mediaType.getValue());
			}
			if (type != null) {
				optionBuilder.withType(type.getValue());
			}
			/*if (dir != null) {
				optionBuilder.withDirection(dir.getValue());
			}*/
			if (output != null) {
				optionBuilder.withOutput(output.getValue());
			}
			if (sequence != null) {
				optionBuilder.withSequence(sequence.getValue());
			}
			if (ordered != null) {
				optionBuilder.withOrdered(ordered.getValue());
			}
			if (separator != null) {
				optionBuilder.withSeparator(separator.getValue());
			}
		}

		/**
		 * Parses the documentation.
		 *
		 * @param reader the reader
		 * @param dHolder the d holder
		 * @return the documentation holder
		 * @throws XMLStreamException the xML stream exception
		 */
		private DocumentationHolder parseDocumentation(
				final XMLEventReader reader, final DocumentationHolder dHolder)
				throws XMLStreamException {

			Predicate<XMLEvent> pred = Predicates.or(EventPredicates.IS_START_ELEMENT,
					EventPredicates.IS_END_ELEMENT);

			// the tricky thing here is that you have to ignore blocks of markup for px:role="author" and px:role="maintainer"
			StaxEventHelper.loop(reader, pred,
					EventPredicates.getChildOrSiblingPredicate(),
					new EventProcessor() {

						// keep track of when we enter and exit a block that we want to ignore with these vars
						boolean processChildren = true;
						int elemsToIgnore = 0;

						@Override
						public void process(XMLEvent event) throws XMLStreamException {

							if (event.isStartElement()) {

								// in cases where we need to ignore the child elements, just keep count of how many open
								// elements we're seeing so that we can note when they have been closed
								if (processChildren == false && elemsToIgnore > 0) {
									elemsToIgnore++;
								}
								// we got past the block we wanted to skip
								else if (elemsToIgnore == 0) {
									processChildren = true;
								}

								if (processChildren) {
									StartElement elm = event.asStartElement();
									Attribute attr = elm.getAttributeByName(Attributes.PX_ROLE);
									String role = (attr != null ? attr.getValue() : "");

									// ignore blocks of author and maintainer data
									if (role.contains(Values.AUTHOR) || role.contains(Values.MAINTAINER)) {
										elemsToIgnore++;
										processChildren = false;
										return;
									}

									else if (role.equals(Values.NAME)) {
										reader.next();
										dHolder.mShort = reader.peek().asCharacters().getData();
									}

									else if (role.equals(Values.DESC)) {
										reader.next();
										dHolder.mDetail = reader.peek().asCharacters().getData();
									}
									else if (role.equals(Values.HOMEPAGE)) {
										reader.next();
										// if @href is present, use that
										if (elm.getAttributeByName(Attributes.HREF) != null) {
											dHolder.mHomepage = elm.getAttributeByName(Attributes.HREF).getValue();
										}
										// otherwise just use the text contents
										else {
											dHolder.mHomepage = reader.peek().asCharacters().getData();
										}
									}

								}
							}
							else if (event.isEndElement()) {

								if (processChildren == false) {
									elemsToIgnore--;
								}
							}
						}
					});
			return dHolder;
		}

		/**
		 * Parses the port.
		 *
		 * @param portElement the port element
		 * @param portBuilder the port builder
		 * @throws XMLStreamException the xML stream exception
		 */
		private void parsePort(final StartElement portElement,
				final XProcPortMetadata.Builder portBuilder)
				throws XMLStreamException {

			Attribute mediaType = portElement
					.getAttributeByName(XProcScriptConstants.Attributes.PX_MEDIA_TYPE);
			if (mediaType != null) {
				portBuilder.withMediaType(mediaType.getValue());
			}

		}
	}

	/**
	 * DocumentationHolde holds documentation elements
	 */
	private static class DocumentationHolder {

		/** The m short. */
		String mShort;

		/** The m detail. */
		String mDetail;

		/** The m homepage. */
		String mHomepage;
	}

	/**
	 * The Class XProcOptionMetadataBuilderHolder holds metadata for options
	 */
	private static class XProcOptionMetadataBuilderHolder {

		/** The m name. */
		String mName;

		/** The m builder. */
		XProcOptionMetadata.Builder mBuilder;
	}

	/**
	 * The Class XProcPortMetadataBuilderHolder holds metadata for ports
	 */
	private static class XProcPortMetadataBuilderHolder {

		/** The m name. */
		String mName;

		/** The m builder. */
		XProcPortMetadata.Builder mBuilder;
	}


}
