package org.daisy.common.xproc.calabash;

import java.net.URI;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.base.Provider;
import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcMonitor;
import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.common.xproc.XProcPipeline;
import org.daisy.common.xproc.XProcPipelineInfo;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.common.xproc.XProcResult;
import org.daisy.pipeline.event.EventBusProvider;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.xmlcalabash.core.XProcConfiguration;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.model.DeclareStep;
import com.xmlcalabash.model.Input;
import com.xmlcalabash.model.Option;
import com.xmlcalabash.model.Output;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XPipeline;

/**
 * Calabash piplines allow to define and run xproc pipelines using calabash. The
 * pipelines supplied by this class are reusable.
 */
public class CalabashXProcPipeline implements XProcPipeline {

	/** The uri. */
	private final URI uri;

	/** The config factory. */
	private final XProcConfigurationFactory configFactory;

	/** The uri resolver. */
	private final URIResolver uriResolver;

	/** The entity resolver. */
	private final EntityResolver entityResolver;

	/** The message listener factory. */
	private final EventBusProvider eventBusProvider;



	/**
	 * The pipeline supplier returns a ready-to-go pipeline instance based on
	 * the XProcPipeline object
	 */
	private final Supplier<PipelineInstance> pipelineSupplier = new Supplier<PipelineInstance>() {
		/**
		 * configures the clone of the pipeline instance setting all the objects
		 * present in the XProcPipeline object
		 */
		@Override
		public PipelineInstance get() {
			XProcConfiguration config = configFactory.newConfiguration();
			XProcRuntime runtime = new XProcRuntime(config);
			runtime.setMessageListener(new slf4jXProcMessageListener());
			if (uriResolver != null) {
				runtime.setURIResolver(uriResolver);
			}
			if (entityResolver != null) {
				runtime.setEntityResolver(entityResolver);
			}

			XProcMessageListenerAggregator listeners = new XProcMessageListenerAggregator();
			listeners.add(new slf4jXProcMessageListener());
			// TODO: get rid of asAccessor as from now on it will be available
			// from the job monitor
			// listeners.addAsAccessor(new MessageListenerWrapper(
			// messageListenerFactory.createMessageListener()));
			runtime.setMessageListener(listeners);
			XPipeline xpipeline = null;

			try {
				xpipeline = runtime.load(uri.toString());
			} catch (SaxonApiException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
			return new PipelineInstance(xpipeline, config);
		}
	};

	/** Suplies the current Pipeline info for this pipeline object */
	private final Supplier<XProcPipelineInfo> info = Suppliers
			.memoize(new Supplier<XProcPipelineInfo>() {

				@Override
				public XProcPipelineInfo get() {
					XProcPipelineInfo.Builder builder = new XProcPipelineInfo.Builder();
					builder.withURI(uri);
					DeclareStep declaration = pipelineSupplier.get().xpipe
							.getDeclareStep();
					// input and parameter ports
					for (Input input : declaration.inputs()) {
						if (input.getParameterInput()) {
							builder.withPort(XProcPortInfo.newInputPort(
									input.getPort(), input.getSequence(),
									input.getPrimary()));
						} else {
							builder.withPort(XProcPortInfo.newParameterPort(
									input.getPort(), input.getPrimary()));
						}
					}
					// output ports
					for (Output output : declaration.outputs()) {
						builder.withPort(XProcPortInfo.newOutputPort(
								output.getPort(), output.getSequence(),
								output.getPrimary()));
					}
					// options
					for (Option option : declaration.options()) {
						builder.withOption(new XProcOptionInfo(new QName(option
								.getName().getNamespaceURI(), option.getName()
								.getLocalName(), option.getName().getPrefix()),
								option.getRequired(), option.getSelect()));
					}
					return builder.build();
				}
			});



	/**
	 * Instantiates a new calabash x proc pipeline.
	 *
	 * @param uri
	 *            the uri to load the xpl file
	 * @param configFactory
	 *            the configuration factory
	 * @param uriResolver
	 *            the uri resolver
	 * @param entityResolver
	 *            the entity resolver
	 * @param messageListenerFactory
	 *            the message listener factory used to process pipeline
	 *            execution related messages
	 */
	public CalabashXProcPipeline(URI uri,
			XProcConfigurationFactory configFactory, URIResolver uriResolver,
			EntityResolver entityResolver,
			EventBusProvider eventBusProvider) {
		this.uri = uri;
		this.configFactory = configFactory;
		this.uriResolver = uriResolver;
		this.entityResolver = entityResolver;
		this.eventBusProvider=eventBusProvider;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.daisy.common.xproc.XProcPipeline#getInfo()
	 */
	@Override
	public XProcPipelineInfo getInfo() {
		return info.get();
	}

	@Override
	public XProcResult run(XProcInput data) {
		return run(data, null,null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.daisy.common.xproc.XProcPipeline#run(org.daisy.common.xproc.XProcInput
	 * )
	 */
	@Override
	public XProcResult run(XProcInput data, XProcMonitor monitor,Properties props) {
		PipelineInstance pipeline = pipelineSupplier.get();
		// monitor.setMessageAccessor(pipeline.messageAccessor);
		((XProcMessageListenerAggregator) pipeline.xpipe.getStep().getXProc()
				.getMessageListener()).add(new EventBusMessageListener(eventBusProvider,props));
		// bind inputs
		for (String name : pipeline.xpipe.getInputs()) {
			for (Provider<Source> sourceProvider : data.getInputs(name)) {
				Source source = sourceProvider.provide();
				// TODO hack to set the entity resolver
				if (source instanceof SAXSource) {
					XMLReader reader = ((SAXSource) source).getXMLReader();
					if (reader == null) {
						try {
							reader = XMLReaderFactory.createXMLReader();
							((SAXSource) source).setXMLReader(reader);
							reader.setEntityResolver(entityResolver);
						} catch (SAXException se) {
							// nop?
						}
					}
				}
				pipeline.xpipe.writeTo(name,
						asXdmNode(pipeline.config.getProcessor(), source));
			}
		}
		// bind options
		for (QName optname : data.getOptions().keySet()) {
			RuntimeValue value = new RuntimeValue(data.getOptions()
					.get(optname));
			pipeline.xpipe.passOption(new net.sf.saxon.s9api.QName(optname),
					value);
		}

		// bind parameters
		for (String port : info.get().getParameterPorts()) {
			for (QName name : data.getParameters(port).keySet()) {
				RuntimeValue value = new RuntimeValue(data.getParameters(port)
						.get(name), null, null);
				pipeline.xpipe.setParameter(port, new net.sf.saxon.s9api.QName(
						name), value);
			}
		}

		// run
		try {
			pipeline.xpipe.run();
		} catch (SaxonApiException e) {
			e.printStackTrace();
		}
		// pipeline set to null to free its reference
		return CalabashXProcResult.newInstance(/* pipeline.xpipe */null,
				pipeline.config);
	}

	/**
	 * As xdm node.
	 *
	 * @param processor
	 *            the processor
	 * @param source
	 *            the source
	 * @return the xdm node
	 */
	private static XdmNode asXdmNode(Processor processor, Source source) {
		DocumentBuilder builder = processor.newDocumentBuilder();
		builder.setDTDValidation(false);
		builder.setLineNumbering(true);
		try {
			return builder.build(source);
		} catch (SaxonApiException sae) {
			// TODO better exception handling
			throw new RuntimeException(sae.getMessage(), sae);
		}
	}

	/**
	 * The Class PipelineInstance is just a holder for various objects to
	 * connect with the suppliers .
	 */
	private static final class PipelineInstance {

		/** The xpipe. */
		private final XPipeline xpipe;

		/** The config. */
		private final XProcConfiguration config;

		/**
		 * Instantiates a new pipeline instance.
		 *
		 * @param xpipe
		 *            the xpipe
		 * @param config
		 *            the config
		 */
		private PipelineInstance(XPipeline xpipe, XProcConfiguration config) {
			this.xpipe = xpipe;
			this.config = config;

		}
	}

}
