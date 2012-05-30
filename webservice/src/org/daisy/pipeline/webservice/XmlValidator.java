package org.daisy.pipeline.webservice;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.thaiopensource.resolver.Identifier;
import com.thaiopensource.resolver.Input;
import com.thaiopensource.resolver.Resolver;
import com.thaiopensource.resolver.ResolverException;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.validate.rng.CompactSchemaReader;

public class XmlValidator {

	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(Validator.class.getName());

	public boolean validate(Document document, URL schemaUrl) {
		ErrorHandlerImpl errorHandler = new ErrorHandlerImpl();

        PropertyMapBuilder properties = new PropertyMapBuilder();
        properties.put(ValidateProperty.ERROR_HANDLER, errorHandler);
        properties.put(ValidateProperty.RESOLVER, BasicResolver.getInstance());

		try {
			InputSource documentInputSource = DOMtoInputSource(document);
			InputSource schemaInputSource = new InputSource(schemaUrl.toURI().toString());

	        ValidationDriver driver = new ValidationDriver(properties.toPropertyMap(), CompactSchemaReader.getInstance());
	        if (!driver.loadSchema(schemaInputSource)) {
	        	logger.error("Could not load schema " + schemaUrl.toString());
	        	return false;
	        }
			driver.validate(documentInputSource);

		} catch (TransformerConfigurationException e) {
			logger.error(e.getMessage());
			return false;
		} catch (TransformerException e) {
			logger.error(e.getMessage());
			return false;
		} catch (TransformerFactoryConfigurationError e) {
			logger.error(e.getMessage());
			return false;
		} catch (URISyntaxException e) {
			logger.error(e.getMessage());
			return false;
		} catch (SAXException e) {
			logger.error(e.getMessage());
			return false;
		} catch (IOException e) {
			logger.error(e.getMessage());
			return false;
		};

        return !errorHandler.hasErrors();
	}


	// create an input source from a DOM document
	private InputSource DOMtoInputSource(Document doc) throws TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {
		DOMSource source = new DOMSource(doc);
		StringWriter xmlAsWriter = new StringWriter();
		StreamResult result = new StreamResult(xmlAsWriter);
		TransformerFactory.newInstance().newTransformer().transform(source, result);
		StringReader xmlReader = new StringReader(xmlAsWriter.toString());
		InputSource is = new InputSource(xmlReader);
		return is;
	}

	public class ErrorHandlerImpl implements ErrorHandler {
		List<SAXParseException> errors = new ArrayList<SAXParseException>();

		public boolean hasErrors() {
			return errors.size() > 0;
		}

		public List<SAXParseException> getErrors() {
			return errors;
		}

		@Override
		public void error(SAXParseException exception) throws SAXException {
			errors.add(exception);
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			errors.add(exception);
		}

		@Override
		public void warning(SAXParseException exception) throws SAXException {
			errors.add(exception);
		}
	}

	/**
	 * Basic Resolver from Jing modified to add support for resolving zip and
	 * jar relative locations.
	 *
	 * @author george@oxygenxml.com
	 */
	static public class BasicResolver implements Resolver {
		static private final BasicResolver theInstance = new BasicResolver();

		protected BasicResolver() {
		}

		public static BasicResolver getInstance() {
			return theInstance;
		}

		@Override
		public void resolve(Identifier id, Input input) throws IOException,
				ResolverException {
			if (!input.isResolved()) {
				input.setUri(resolveUri(id));
			}
		}

		@Override
		public void open(Input input) throws IOException, ResolverException {
			if (!input.isUriDefinitive()) {
				return;
			}
			URI uri;
			try {
				uri = new URI(input.getUri());
			} catch (URISyntaxException e) {
				throw new ResolverException(e);
			}
			if (!uri.isAbsolute()) {
				throw new ResolverException("cannot open relative URI: " + uri);
			}
			URL url = new URL(uri.toASCIIString());
			// XXX should set the encoding properly
			// XXX if this is HTTP and we've been redirected, should do
			// input.setURI with the new URI
			input.setByteStream(url.openStream());
		}

		public static String resolveUri(Identifier id) throws ResolverException {
			try {
				String uriRef = id.getUriReference();
				URI uri = new URI(uriRef);
				if (!uri.isAbsolute()) {
					String base = id.getBase();
					if (base != null) {
						// OXYGEN PATCH START
						// Use class URL in order to resolve protocols like zip
						// and jar.
						URI baseURI = new URI(base);
						if ("zip".equals(baseURI.getScheme())
								|| "jar".equals(baseURI.getScheme())) {
							uriRef = new URL(new URL(base), uriRef)
									.toExternalForm();
							// OXYGEN PATCH END
						} else {
							uriRef = baseURI.resolve(uri).toString();
						}
					}
				}

				return uriRef;
			} catch (URISyntaxException e) {
				throw new ResolverException(e);
			} catch (MalformedURLException e) {
				throw new ResolverException(e);
			}
		}
	}
}
