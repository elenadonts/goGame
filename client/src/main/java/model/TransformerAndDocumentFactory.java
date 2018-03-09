package model;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

/**
 * Class for helping transform and create xml document
 *
 * @author Eugene Lobin
 * @version 1.0 09 Mar 2018
 */
public class TransformerAndDocumentFactory {
    private static final Logger LOGGER = Logger.getLogger(TransformerAndDocumentFactory.class);
    private static TransformerFactory transformerFactory = TransformerFactory.newInstance();
    private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private static DocumentBuilder documentBuilder;
    private static Transformer transformer;

    private TransformerAndDocumentFactory() {
    }

    /**
     * Creates static transformer and builder object
     */
    public static void createTransformerAndBuilder() {
        try {
            transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (TransformerConfigurationException | ParserConfigurationException e) {
            LOGGER.error(e);
        }
    }

    /**
     * Transforms xml document to string writer
     * and returns him
     *
     * @param document for transform
     * @return new string writer object
     */
    public static StringWriter transform(Document document) {
        StringWriter writer = new StringWriter();
        try {
            transformer.transform(new DOMSource(document), new StreamResult(writer));
        } catch (TransformerException e) {
            LOGGER.error(e);
        }
        return writer;
    }

    /**
     * Creates new xml document and
     * return him
     *
     * @return new xml document
     */
    public static Document newDocument() {
        return documentBuilder.newDocument();
    }

    /**
     * Returns object document builder
     *
     * @return object document builder
     */
    public static DocumentBuilder getDocumentBuilder() {
        return documentBuilder;
    }
}
