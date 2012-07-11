package org.daisy.pipeline.webserviceutils.xml;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class XmlUtils {
	/** The Constant NS_PIPELINE_DATA. */
	public static final String NS_PIPELINE_DATA = "http://www.daisy.org/ns/pipeline/data";

	
	/**
	 * DOM to string.
	 *
	 * @param doc
	 *            the doc
	 * @return the string
	 */
	/*
	 * from:
	 * http://www.journaldev.com/71/utility-java-class-to-format-xml-document
	 * -to-xml-string-and-xml-to-document
	 */
	public static String DOMToString(Document doc) {
		String xmlString = "";
		if (doc != null) {
			try {
				TransformerFactory transfac = TransformerFactory.newInstance();
				Transformer trans = transfac.newTransformer();
				trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				trans.setOutputProperty(OutputKeys.INDENT, "yes");
				StringWriter sw = new StringWriter();
				StreamResult result = new StreamResult(sw);
				DOMSource source = new DOMSource(doc);
				trans.transform(source, result);
				xmlString = sw.toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return xmlString;
	}

	
	/**
	 * Node to string.
	 *
	 * @param node
	 *            the node
	 * @return the string
	 */
	public static String nodeToString(Node node) {
		Document doc = node.getOwnerDocument();
		DOMImplementationLS domImplLS = (DOMImplementationLS) doc
				.getImplementation();
		LSSerializer serializer = domImplLS.createLSSerializer();
		serializer.getDomConfig().setParameter("xml-declaration", false);
		String string = serializer.writeToString(node);
		return string.trim();
	}

	/**
	 * Creates the dom.
	 *
	 * @param documentElementName
	 *            the document element name
	 * @return the document
	 */
	public static Document createDom(String documentElementName) {
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory
					.newDocumentBuilder();
			DOMImplementation domImpl = documentBuilder.getDOMImplementation();
			Document document = domImpl.createDocument(NS_PIPELINE_DATA,
					documentElementName, null);

			return document;

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}

	}

	
}