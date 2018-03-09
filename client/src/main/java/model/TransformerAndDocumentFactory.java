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

public class TransformerAndDocumentFactory {
    private static final Logger LOGGER = Logger.getLogger(TransformerAndDocumentFactory.class);
    private static TransformerFactory transformerFactory = TransformerFactory.newInstance();
    private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private static DocumentBuilder documentBuilder;
    private static Transformer transformer;

    private TransformerAndDocumentFactory() {
    }

    public static void createTransformerAndBuilder() {
        try {
            transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (TransformerConfigurationException | ParserConfigurationException e) {
            LOGGER.error(e);
        }
    }

    public static StringWriter transform(Document document) {
        StringWriter writer = new StringWriter();
        try {
            transformer.transform(new DOMSource(document), new StreamResult(writer));
        } catch (TransformerException e) {
            LOGGER.error(e);
        }
        return writer;
    }

    public static Document newDocument() {
        return documentBuilder.newDocument();
    }

    public static DocumentBuilder getDocumentBuilder() {
        return documentBuilder;
    }
}
