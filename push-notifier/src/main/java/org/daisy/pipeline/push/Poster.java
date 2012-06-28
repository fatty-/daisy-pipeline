package org.daisy.pipeline.push;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URI;

import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.webserviceutils.Authenticator;
import org.daisy.pipeline.webserviceutils.callback.Callback;
import org.daisy.pipeline.webserviceutils.clients.Client;
import org.daisy.pipeline.webserviceutils.xml.JobXmlWriter;
import org.daisy.pipeline.webserviceutils.xml.XmlUtils;
import org.daisy.pipeline.webserviceutils.xml.XmlWriterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class Poster {

	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(Poster.class.getName());

	public static void postMessage(Job job, int msgStart, int msgEnd, Callback callback) {
		URI url = callback.getHref();
		JobXmlWriter writer = XmlWriterFactory.createXmlWriter(job);
		writer.withMessageRange(msgStart, msgEnd);
		Document doc = writer.getXmlDocument();
		postXml(doc, url, callback.getClient());
	}

	public static void postStatusUpdate(Job job, Callback callback) {
		URI url = callback.getHref();
		JobXmlWriter writer = XmlWriterFactory.createXmlWriter(job);
		Document doc = writer.getXmlDocument();
		postXml(doc, url, callback.getClient());
	}

	private static void postXml(Document doc, URI url, Client client) {
		URI requestUri = url;
		if (client != null) {
			requestUri = Authenticator.createUriWithCredentials(url.toString(), client);
		}

		// from http://code.geek.sh/2009/10/simple-post-in-java/
		HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) requestUri.toURL().openConnection();
        } catch (IOException e) {
        	logger.error(e.getMessage());
			return;
        }

        connection.setDoInput (true);
        connection.setDoOutput (true);
        connection.setUseCaches (false);

        try {
            connection.setRequestMethod("POST");
        } catch (ProtocolException e) {
        	logger.error(e.getMessage());
        	return;
        }

        try {
            connection.connect();
        } catch (IOException e) {
        	logger.error(e.getMessage());
        	return;
        }

        DataOutputStream output = null;
        DataInputStream input = null;

        try {
            output = new DataOutputStream(connection.getOutputStream());
        } catch (IOException e) {
        	logger.error(e.getMessage());
        	return;
        }

        // Send the request data.
        try {
            output.writeBytes(XmlUtils.DOMToString(doc));
            output.flush();
            output.close();
        } catch (IOException e) {
        	logger.error(e.getMessage());
        	return;
        }

        // Get response data. We're not doing anything with it but if we don't retrieve it, the callback doesn't appear to work.
        try {
            input = new DataInputStream (connection.getInputStream());
            input.close ();
        } catch (IOException e) {
        	logger.warn(e.getMessage());
        }
	}


}