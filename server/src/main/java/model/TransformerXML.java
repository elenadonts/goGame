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

    /**
     * Creates static transformer  object
     */
    public static void createTransformer() {
        try {
            transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
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
     * Writes xml document in file
     *
     * @param document for transform
     * @param file     for writing
     */
    public static void transformToFile(Document document, File file) {
        try {
            transformer.transform(new DOMSource(document), new StreamResult(file));
        } catch (TransformerException e) {
            LOGGER.error(e);
        }
    }


}
