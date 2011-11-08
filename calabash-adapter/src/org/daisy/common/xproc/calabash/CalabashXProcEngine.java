package org.daisy.common.xproc.calabash;

import java.net.URI;

import javax.xml.transform.URIResolver;

import org.daisy.common.messaging.MessageListenerFactory;
import org.daisy.common.xproc.XProcEngine;
import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcPipeline;
import org.daisy.common.xproc.XProcPipelineInfo;
import org.daisy.common.xproc.XProcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;


//TODO check thread safety
/**
 * Calabash xproc engine wrapper 
 */
public final class CalabashXProcEngine implements XProcEngine {

	/** The Constant logger. */
	private static final Logger logger = LoggerFactory
			.getLogger(CalabashXProcEngine.class);


	/** The uri resolver. */
	private URIResolver uriResolver = null;
	
	/** The entity resolver. */
	private EntityResolver entityResolver = null;
	
	/** The config factory. */
	private XProcConfigurationFactory configFactory = null;
	
	/** The message listener factory. */
	private MessageListenerFactory messageListenerFactory;

	

	/**
	 * Instantiates a new calabash x proc engine.
	 */
	public CalabashXProcEngine() {
		//FIXME: default entity resolver
		entityResolver= new CalabashXprocEntityResolver();
	}

	/**
	 * Activate (to be used by OSGI)
	 */
	public void activate(){
		logger.trace("Activating XProc Engine");
	}

	/* (non-Javadoc)
	 * @see org.daisy.common.xproc.XProcEngine#load(java.net.URI)
	 */
	@Override
	public XProcPipeline load(URI uri) {
		if (configFactory == null) {
			throw new IllegalStateException(
					"Calabash configuration factory unavailable");
		}
	
		return new CalabashXProcPipeline(uri, configFactory, uriResolver, entityResolver,messageListenerFactory);
	}

	/* (non-Javadoc)
	 * @see org.daisy.common.xproc.XProcEngine#getInfo(java.net.URI)
	 */
	@Override
	public XProcPipelineInfo getInfo(URI uri) {
		return load(uri).getInfo();
	}

	/* (non-Javadoc)
	 * @see org.daisy.common.xproc.XProcEngine#run(java.net.URI, org.daisy.common.xproc.XProcInput)
	 */
	@Override
	public XProcResult run(URI uri, XProcInput data) {
		return load(uri).run(data);
	}

	/**
	 * Sets the configuration factory to this engine
	 *
	 * @param configFactory the new configuration factory
	 */
	public void setConfigurationFactory(XProcConfigurationFactory configFactory) {
		this.configFactory = configFactory;
	}

	/**
	 * Sets the entity resolver to this engine
	 *
	 * @param entityResolver the new entity resolver
	 */
	public void setEntityResolver(EntityResolver entityResolver) {
		this.entityResolver = entityResolver;
	}

	/**
	 * Sets the uri resolver to this engine, the uri resolver is thought to be able to handle component uris
	 *
	 * @param uriResolver the new uri resolver
	 */
	public void setUriResolver(URIResolver uriResolver) {
		this.uriResolver = uriResolver;
	}
	
	/**
	 * Sets the message listener factory used to create new message listeners to catch and process the messages thrown while executing pipelines 
	 *
	 * @param factory the new message listener factory
	 */
	public void setMessageListenerFactory(MessageListenerFactory factory){
		this.messageListenerFactory=factory;
	}

}
