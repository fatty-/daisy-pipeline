package org.daisy.pipeline.webserviceutils.xml;

import java.util.List;

import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.script.XProcScript;
import org.daisy.pipeline.webserviceutils.clients.Client;

public class XmlWriterFactory {
	
	public static JobXmlWriter createXmlWriter(Job job) {
		return new JobXmlWriter(job);
	}
	
	public static JobsXmlWriter createXmlWriter(Iterable<Job> jobs) {
		return new JobsXmlWriter(jobs);
	}
	
	public static ScriptXmlWriter createXmlWriter(XProcScript script) {
		return new ScriptXmlWriter(script);
	}
	
	// TODO ugly but otherwise get erasure error
	public static ScriptsXmlWriter createXmlWriterForScripts(Iterable<XProcScript> scripts) {
		return new ScriptsXmlWriter(scripts);
	}
	
	public static ClientXmlWriter createXmlWriter(Client client) {
		return new ClientXmlWriter(client);
	}
	
	public static ClientsXmlWriter createXmlWriter(List<? extends Client> clients) {
		return new ClientsXmlWriter(clients);
	}
	
 }