package org.daisy.pipeline.job;

import java.io.IOException;

import java.util.Properties;

import org.daisy.common.xproc.XProcEngine;
import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcMonitor;
import org.daisy.common.xproc.XProcPipeline;
import org.daisy.common.xproc.XProcResult;

import org.daisy.pipeline.job.StatusMessage;
import org.daisy.pipeline.job.StatusMessage;
import org.daisy.pipeline.job.StatusMessage;
import org.daisy.pipeline.script.XProcScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

// TODO: Auto-generated Javadoc
//TODO check thread safety
/**
 * The Class Job defines the execution unit.
 */
public class Job {
	private static final Logger logger = LoggerFactory
			.getLogger(Job.class);
	/**
	 * The Enum Status.
	 */
	public static enum Status {

		/** The IDLE. */
		IDLE,
		/** The RUNNING. */
		RUNNING,
		/** The DONE. */
		DONE,
		ERROR
	}


	private EventBus eventBus;
	/** The id. */
	private final JobId id;

	/** The input. */
	private final XProcInput input;

	/** The script. */
	private final XProcScript script;

	//private XProcResult output;

	/** The results. */
	private JobResult results;

	/** The io bridge. */
	private final IOBridge ioBridge;

	/** The status. */
	private Status status = Status.IDLE;

	private final XProcMonitor monitor;

	/**
	 * Instantiates a new job.
	 *
	 * @param id
	 *            the id
	 * @param script
	 *            the script
	 * @param input
	 *            the input
	 * @param ioBridge
	 *            the io bridge
	 */
	Job(JobId id, XProcScript script, XProcInput input,
			IOBridge ioBridge,JobMonitor monitor,EventBus eventBus) {
		// TODO check arguments
		this.id = id;
		this.script = script;
		this.input = input;
		this.ioBridge = ioBridge;
		this.monitor=monitor;
		this.eventBus=eventBus;
		this.results=new JobResult.Builder().withMessageAccessor(monitor.getMessageAccessor()).withZipFile(null).withLogFile(null).build();
		this.postStatus();
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public JobId getId() {
		return id;
	}

	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * Gets the script.
	 *
	 * @return the script
	 */
	public XProcScript getScript() {
		return script;
	}

	/**
	 * Gets the x proc output.
	 *
	 * @return the x proc output
	 */
	XProcResult getXProcOutput() {
		return null;
	}
	private void postStatus(){
		this.eventBus.post(new StatusMessage.Builder().withJobId(this.id).withStatus(this.status).build());
	}
	/**
	 * Runs the job using the XProcEngine as script loader.
	 *
	 * @param engine the engine
	 */
	public void run(XProcEngine engine) {
		status = Status.RUNNING;
		this.postStatus();
		XProcPipeline pipeline = null;
		try{
		pipeline = engine.load(script.getURI());
		}catch (Exception e){
			logger.error("Error while loading the script:"+this.script.getName());
			throw new RuntimeException(e);
		}
		try{
			Properties props=new Properties();
			props.setProperty("JOB_ID", id.toString());
			pipeline.run(input,monitor,props);
			buildResults();
			status=Status.DONE;
			this.postStatus();
		}catch(Exception e){
			logger.error("job finished with error state",e);
			//buildResults();
			status=Status.ERROR;
			this.postStatus();
		}

	}
	private void buildResults() {
		JobResult.Builder builder = new JobResult.Builder();
		builder.withMessageAccessor(monitor.getMessageAccessor());
		builder.withLogFile(ioBridge.getLogFile());
		builder = (ioBridge != null) ? builder.withZipFile(ioBridge
				.zipOutput()) : builder;
		results = builder.build();
	}
	/**
	 * Gets the result.
	 *
	 * @return the result
	 */
	public JobResult getResult() {
		return results;
	}

	public XProcMonitor getMonitor(){
		return monitor;
	}

	public boolean cleanUp(){
		boolean clean=true;
		clean&=this.getMonitor().getMessageAccessor().delete();	
		return	clean&this.ioBridge.cleanUp();
	}

}
