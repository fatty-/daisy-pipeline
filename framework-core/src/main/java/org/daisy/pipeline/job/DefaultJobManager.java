package org.daisy.pipeline.job;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.daisy.common.xproc.XProcInput;
import org.daisy.pipeline.script.XProcScript;


/**
 * DefaultJobManager allows to manage the jobs submitted to the daisy pipeline 2
 */
public class DefaultJobManager implements JobManager {

	/** The jobs. */
	private final Map<JobId, Job> jobs= new HashMap<JobId, Job>();

	/* (non-Javadoc)
	 * @see org.daisy.pipeline.job.JobManager#newJob(org.daisy.pipeline.script.XProcScript, org.daisy.common.xproc.XProcInput, org.daisy.pipeline.job.ResourceCollection)
	 */
	@Override
	public Job newJob(XProcScript script, XProcInput input,
			ResourceCollection context) {
		Job job = JobFactory.getInstance().newJob(script, input, context);
		jobs.put(job.getId(), job);
		return job;
	}

	/* (non-Javadoc)
	 * @see org.daisy.pipeline.job.JobManager#newJob(org.daisy.pipeline.script.XProcScript, org.daisy.common.xproc.XProcInput)
	 */
	@Override
	public Job newJob(XProcScript script, XProcInput input) {
		return newJob(script, input, null);
	}

	/* (non-Javadoc)
	 * @see org.daisy.pipeline.job.JobManager#getJobs()
	 */
	@Override
	public Iterable<Job> getJobs() {
		return Collections.unmodifiableCollection(jobs.values());
	}

	/* (non-Javadoc)
	 * @see org.daisy.pipeline.job.JobManager#deleteJob(org.daisy.pipeline.job.JobId)
	 */
	@Override
	public Job deleteJob(JobId id) {
		jobs.get(id).cleanUp();
		return jobs.remove(id);
	}

	/* (non-Javadoc)
	 * @see org.daisy.pipeline.job.JobManager#getJob(org.daisy.pipeline.job.JobId)
	 */
	@Override
	public Job getJob(JobId id) {
		return jobs.get(id);
	}
}
