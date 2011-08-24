package org.daisy.pipeline.webservice;

import org.daisy.pipeline.job.JobManager;
import org.daisy.pipeline.script.ScriptRegistry;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PipelineWebService extends Application {
	private static Logger logger = LoggerFactory.getLogger(PipelineWebService.class.getName());

	// TODO make port and address configurable
	private final String serverAddress = "http://localhost:8182/ws";
	private final int portNumber = 8182;
	private JobManager jobManager;
	private ScriptRegistry scriptRegistry;

	@Override
	public Restlet createInboundRoot() {
		Router router = new Router(getContext());
		router.attach("/scripts", ScriptsResource.class);
		// TODO: is there any way to route this as "/converter{?id=uri}" in
		// order to be more explicit about the params?
		router.attach("/script", ScriptResource.class);
		router.attach("/jobs", JobsResource.class);
		router.attach("/jobs/{id}", JobResource.class);
		router.attach("/jobs/{id}/log", LogResource.class);
		router.attach("/jobs/{id}/result", ResultResource.class);
		return router;
	}

	public void init() {
		logger.info("Starting webservice on port 8182.");
		Component component = new Component();
		component.getServers().add(Protocol.HTTP, portNumber);
		component.getDefaultHost().attach("/ws", this);
		try {
			component.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {
		logger.info("Webservice stopped.");
	}

	public String getServerAddress() {
		return this.serverAddress;
	}

	public JobManager getJobManager() {
		return jobManager;
	}

	public void setJobManager(JobManager jobManager) {
		this.jobManager = jobManager;
	}

	public ScriptRegistry getScriptRegistry() {
		return scriptRegistry;
	}

	public void setScriptRegistry(ScriptRegistry scriptRegistry) {
		this.scriptRegistry = scriptRegistry;
	}

}