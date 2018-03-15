package model;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

public class TransformerXML {
    private static final Logger LOGGER = Logger.getLogger(Transformer.class);
    private static TransformerFactory transformerFactory = TransformerFactory.newInstance();
    private static Transformer transformer;
    private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private static DocumentBuilder documentBuilder;

    private TransformerXML() {
    }

    /**
     * Creates static transformer  object
     */
    public static void createTransformer() {
        try {
            transformer = transformerFactory.newTransformer();
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (TransformerConfigurationException | ParserConfigurationException e) {
            LOGGER.error(e);
        }
    }

    /**
     * Transforms xml document to string writer
     * and returns him
     *
     * @param document for transformToString
     * @return new string writer object
     */
    public static String transformToString(Document document) {
        StringWriter writer = new StringWriter();
        try {
            transformer.transform(new DOMSource(document), new StreamResult(writer));
        } catch (TransformerException e) {
            LOGGER.error(e);
        }
        return writer.toString();
    }

    /**
     * Writes xml document in file
     *
     * @param document for transformToString
     * @param file     for writing
     */
    public static void transformToFile(Document document, File file) {
        try {
            transformer.transform(new DOMSource(document), new StreamResult(file));
        } catch (TransformerException e) {
            LOGGER.error(e);
        }
    }

    /**
     * @return new xml document
     */
    public static Document newDocument() {
        return documentBuilder.newDocument();
    }

    /**
     * @return object document builder
     */
    public static DocumentBuilder getDocumentBuilder() {
        return documentBuilder;
    }

    /**
     * Parse file to xml document
     *
     * @param file for parse
     * @return object document builder
     */
    public static Document parseFile(File file) {
        Document document = null;
        try {
            document = documentBuilder.parse(file);
        } catch (SAXException | IOException e) {
            LOGGER.error(e);
        }
        return document;
    }

    /**
     * Creates xml element with tag name and data
     *
     * @param data xml element value
     * @param document for created
     * @param tagName xml element name
     * @return new element
     */
    public static Element createElement(Document document,String tagName, String data) {
        Element element = document.createElement(tagName);
        element.appendChild(document.createTextNode(data));
        return element;
    }

    public static TransformerFactory getTransformerFactory() {
        return transformerFactory;
    }
}
