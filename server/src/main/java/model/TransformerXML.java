package model;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.StringWriter;

public class TransformerXML {
    private static final Logger LOGGER = Logger.getLogger(TransformerXML.class);
    private static TransformerFactory transformerFactory = TransformerFactory.newInstance();
    private static Transformer transformer;

    private TransformerXML() {

    }

    public static void createTransformer() {
        try {
            transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
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

    public static StreamResult transformToFile(Document document, File file) {
        StreamResult streamResult = new StreamResult(file);
        try {
            transformer.transform(new DOMSource(document), streamResult);
        } catch (TransformerException e) {
            LOGGER.error(e);
        }
        return streamResult;
    }


}
