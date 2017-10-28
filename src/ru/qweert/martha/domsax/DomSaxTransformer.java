package ru.qweert.martha.domsax;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import ru.qweert.martha.XmlTransformer;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;

public class DomSaxTransformer implements XmlTransformer {

    private static final String OUTPUT_FILE_NAME = "result.html";

    @Override
    public File xmlToHtml(File input, File schema) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SchemaFactory schemaFactory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schemaInstance;
        try {
            schemaInstance = schemaFactory.newSchema(schema);
        } catch (SAXException e) {
            throw new RuntimeException("Couldn't read schema from file", e);
        }

        // Валидируем
        Validator validator = schemaInstance.newValidator();
        try {
            validator.validate(new StreamSource(input));
        } catch (SAXException | IOException e) {
            throw new RuntimeException("Failed to validate document", e);
        }

        // Читаем & Пишем в новый документ
        ApplicationSaxParser saxp;
        try {
            SAXParser parser = factory.newSAXParser();
            saxp = new ApplicationSaxParser(getHtmlDocument());
            parser.parse(input, saxp);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse xml", e);
        }

        // Handling output file
        File resultFile = new File(OUTPUT_FILE_NAME);
        if(resultFile.exists()) {
            resultFile.delete();
        }

        // Use a Transformer for output
        try {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            DOMSource source = new DOMSource(saxp.getHtml());
            StreamResult result = new StreamResult(resultFile);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw new RuntimeException("Failed to write to file", e);
        }
        return resultFile;
    }

    private static Document getHtmlDocument() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("html");
        doc.appendChild(rootElement);
        rootElement.appendChild(createHead(doc));
        rootElement.appendChild(createBody(doc));
        return doc;
    }

    private static Element createHead(Document doc) {
        Element head = doc.createElement("head");
        Element link = doc.createElement("link");
        Attr linkType = doc.createAttribute("rel");
        linkType.setValue("stylesheet");
        link.setAttributeNode(linkType);
        Attr linkHref = doc.createAttribute("href");
        linkHref.setValue("https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css");
        link.setAttributeNode(linkHref);
        Attr linkIntegrity = doc.createAttribute("integrity");
        linkIntegrity.setValue("sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u");
        link.setAttributeNode(linkIntegrity);
        Attr linkCrossorigin = doc.createAttribute("crossorigin");
        linkCrossorigin.setValue("anonymous");
        link.setAttributeNode(linkCrossorigin);
        head.appendChild(link);
        return head;
    }

    private static Element createBody(Document doc) {
        Element body = doc.createElement("body");
        Element container = doc.createElement("div");
        Attr containerClass = doc.createAttribute("class");
        containerClass.setValue("container");
        container.setAttributeNode(containerClass);
        body.appendChild(container);
        return body;
    }
}
