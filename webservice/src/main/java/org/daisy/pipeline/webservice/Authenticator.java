package org.daisy.pipeline.webservice;

import java.security.SignatureException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.daisy.pipeline.webservice.clients.Client;
import org.daisy.pipeline.webservice.requestlog.RequestLog;
import org.daisy.pipeline.webservice.requestlog.RequestLogEntry;
import org.daisy.pipeline.webservice.requestlog.SimpleRequestLogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Authenticator {

	private static Logger logger = LoggerFactory.getLogger(Authenticator.class.getName());

	public static boolean authenticate(PipelineWebService webservice, String authid, String hash, String timestamp, String nonce, String URI, long maxRequestTime) {
		// rules for hashing: use the whole URL string, minus the hash part (&sign=<some value>)
		// important!  put the sign param last so we can easily strip it out

		int idx = URI.indexOf("&sign=", 0);

		if (idx > 1) {
			Client client = webservice.getClientStore().get(authid);
			// make sure the client exists
			if (client==null) {
				logger.error(String.format("Client with auth ID %s not found", authid));
				return false;
			}
			String hashuri = URI.substring(0, idx);
			String clientSecret = getClientSecret(client,authid);
			String serverHash = "";
			try {
				serverHash = calculateRFC2104HMAC(hashuri, clientSecret);

				SimpleDateFormat UTC_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
				UTC_FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));

				Date serverTimestamp = new Date(System.currentTimeMillis());
				Date clientTimestamp;
				try {
					clientTimestamp = UTC_FORMATTER.parse(timestamp);
				} catch (ParseException e) {
					logger.error(String.format("Could not parse timestamp: %s", timestamp));
					e.printStackTrace();
					return false;
				}
				if(!hash.equals(serverHash)) {
					logger.error("Hash values do not match");
					return false;
				}
				if (serverTimestamp.getTime() - clientTimestamp.getTime() > maxRequestTime) {
					logger.error("Request expired");
					return false;
				}
				if (!checkValidNonce(webservice.getRequestLog(), client, nonce, timestamp)) {
					logger.error("Invalid nonce");
					return false;
				}
				return true;

			} catch (SignatureException e) {
				logger.error("Could not generate hash");
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}


	// nonces, along with timestamps, protect against replay attacks
	private static boolean checkValidNonce(RequestLog requestLog, Client client, String nonce, String timestamp) {
		if (client == null) {
			throw new IllegalArgumentException("Client is null");
		}

		RequestLogEntry entry = new SimpleRequestLogEntry(client.getId(), nonce, timestamp);

		// if this nonce was already used with this timestamp, don't accept it again
		if (requestLog.contains(entry)) {
			logger.warn("Duplicate nonce detected.");
			return false;
		} else {
			// else, it is unique and therefore ok
			requestLog.add(entry);
			return true;
		}
	}

	private static String getClientSecret(Client client, String authid) {
		if (client != null) {
			return client.getSecret();
		}
		return "";
	}

	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";


	// adapted slightly from
	// http://docs.amazonwebservices.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/index.html?AuthJavaSampleHMACSignature.html
	/**
	* Computes RFC 2104-compliant HMAC signature.
	* * @param data
	* The data to be signed.
	* @param secret
	* The signing secret.
	* @return
	* The Base64-encoded RFC 2104-compliant HMAC signature.
	* @throws
	* java.security.SignatureException when signature generation fails
	*/
	private static String calculateRFC2104HMAC(String data, String secret) throws java.security.SignatureException {
		String result;
		try {
			// get an hmac_sha1 key from the raw key bytes
			SecretKeySpec signingSecret = new SecretKeySpec(secret.getBytes(), HMAC_SHA1_ALGORITHM);

			// get an hmac_sha1 Mac instance and initialize with the signing key
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingSecret);

			// compute the hmac on input data bytes
			byte[] rawHmac = mac.doFinal(data.getBytes());

			// base64-encode the hmac
			result = Base64.encodeBase64String(rawHmac);

			} catch (Exception e) {
				throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
		}
		return result;
	}



}