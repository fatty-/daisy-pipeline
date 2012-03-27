package org.daisy.pipeline.persistence.messaging.Message;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.daisy.common.messaging.Message;
import org.daisy.pipeline.job.JobId;

@Entity
public class PersistentMessage implements org.daisy.common.messaging.Message{
	@Column(name="throwable")
	/** The m throwable. */
	 Throwable throwable;
	@Column(name="message")
	/** The m msg. */
	String msg;

	@Column(name="level")
	/** The m level. */
	Level level;
	@Column(name="timestamp")
	/** The m time stamp. */
	Date timeStamp;
	@Column(name="sequence")
	int sequence;
	@Column(name="jobId")
	JobId jobId;
	
	
	
	public PersistentMessage(Throwable throwable, String msg, Level level,
			 int sequence, JobId jobId) {
		super();
		this.throwable = throwable;
		this.msg = msg;
		this.level = level;
		this.timeStamp = new Date();
		this.sequence = sequence;
		this.jobId = jobId;
	}
	public Throwable getThrowable() {
		return throwable;
	}
	public void setThrowable(Throwable throwable) {
		this.throwable = throwable;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public Level getLevel() {
		return level;
	}
	public void setLevel(Level level) {
		this.level = level;
	}
	public Date getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
	public int getSequence() {
		return sequence;
	}
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	public JobId getJobId() {
		return jobId;
	}
	public void setJobId(JobId jobId) {
		this.jobId = jobId;
	}
	
	public static List<Message> getMessages(EntityManager em,JobId id,int from,List<Level> levels){
		
		StringBuilder sqlBuilder=new StringBuilder("select m from Message m where jobId='%s' and  sequence > %s and level in (");
		
		for (Level l:levels){
			sqlBuilder.append(String.format("'%s',", l));
		}
		sqlBuilder.append(",'1') order by sequence");
		String sql=String.format(sqlBuilder.toString(), id.toString(),from);
		
		Query q=em.createNativeQuery(sql);
		@SuppressWarnings("unchecked") //just how persistence works
		List<Message> result = q.getResultList();
		return result;
	}
	
	
	/**
	 * Builder for creating new messages
	 */
	public static final class Builder {

		/** The m throwable. */
		Throwable mThrowable;

		/** The m msg. */
		String mMsg;

		/** The m level. */
		Level mLevel;

		int mSequence;

		JobId mJobId;
		/**
		 * With message.
		 *
		 * @param message
		 *            the message
		 * @return the builder
		 */
		public Builder withMessage(String message) {
			mMsg = message;
			return this;
		}

		/**
		 * With level.
		 *
		 * @param level
		 *            the level
		 * @return the builder
		 */
		public Builder withLevel(Level level) {
			mLevel = level;
			return this;
		}

		/**
		 * With throwable.
		 *
		 * @param throwable
		 *            the throwable
		 * @return the builder
		 */
		public Builder withThrowable(Throwable throwable) {
			mThrowable = throwable;
			return this;
		}
		public Builder withSequence(int i) {
			mSequence=i;
			return this;
		}
		public Builder withJobId(JobId id) {
			mJobId=id;
			return this;
		}
		/**
		 * Builds the message based on the objects provided using the "with" methods.
		 *
		 * @return the message
		 */
		public PersistentMessage build() {
			return new PersistentMessage(mThrowable, mMsg,mLevel,mSequence,mJobId);
		}

		
	}

}
