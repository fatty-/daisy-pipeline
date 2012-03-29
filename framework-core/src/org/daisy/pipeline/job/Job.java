package org.daisy.pipeline.job;

import java.io.IOException;

import org.daisy.common.messaging.MessageAccessor;
import org.daisy.common.xproc.XProcEngine;
import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcMonitor;
import org.daisy.common.xproc.XProcPipeline;
import org.daisy.common.xproc.XProcResult;
import org.daisy.pipeline.script.XProcScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	/**
	 * Creates a new job.
	 *
	 * @param script the script to be executed
	 * @param input the input
	 * @return the job
	 */
	public static Job newJob(XProcScript script, XProcInput input) {
		// TODO validate input
		return new Job(JobIdFactory.newId(), script, input, null);
	}

	/**
	 * Creates a new job attached to a context.
	 *
	 * @param script the script
	 * @param input the input
	 * @param context the context
	 * @return the job
	 */
	public static Job newJob(XProcScript script, XProcInput input,
			ResourceCollection context) {
		// TODO check arguments
		JobId id = JobIdFactory.newId();
		// FIXME "common path"+id.toString

		try {

			IOBridge bridge = new IOBridge(id);
			XProcInput resolvedInput = bridge.resolve(script, input, context);

			// TODO validate input
			return new Job(id, script, resolvedInput, bridge);
		} catch (IOException e) {
			throw new RuntimeException("Error resolving pipeline info", e);
		}
	}

	/** The id. */
	private final JobId id;

	/** The input. */
	private final XProcInput input;

	/** The script. */
	private final XProcScript script;

	/** The output. */
	private XProcResult output;

	/** The results. */
	private JobResult results;

	/** The io bridge. */
	private final IOBridge ioBridge;

	/** The status. */
	private Status status = Status.IDLE;

	private final XProcMonitor monitor= new XProcMonitor() {
		MessageAccessor accessor = null;
		@Override
		public MessageAccessor getMessageAccessor() {

			return accessor;
		}

		@Override
		public void setMessageAccessor(MessageAccessor accessor) {
			this.accessor=accessor;

		}
	};

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
	private Job(JobId id, XProcScript script, XProcInput input,
			IOBridge ioBridge) {
		// TODO check arguments
		this.id = id;
		this.script = script;
		this.input = input;
		this.ioBridge = ioBridge;
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

	/**
	 * Runs the job using the XProcEngine as script loader.
	 *
	 * @param engine the engine
	 */
	public void run(XProcEngine engine) {
		status = Status.RUNNING;
		// TODO use a pipeline cache
		XProcPipeline pipeline = engine.load(script.getURI());
		try{
			output = pipeline.run(input,monitor);
			status=Status.DONE;
		}catch(Exception e){
			logger.error("job finished with error state",e);
			status=Status.ERROR;
		}


		JobResult.Builder builder = new JobResult.Builder();
		builder.withMessageAccessor(output.getMessages());
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

}
