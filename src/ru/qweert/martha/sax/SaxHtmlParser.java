package ru.qweert.martha.sax;

import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class SaxHtmlParser {

    public void handle(File input, File schema, String outputFileName) {
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

        // Читаем & Пишем в строку
        GoodsHandler goodsSaxHandler = new GoodsHandler();
        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(input, goodsSaxHandler);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse xml", e);
        }

        try {
            PrintWriter out = new PrintWriter(outputFileName);
            out.println(goodsSaxHandler.getResult());
            out.flush();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Problem while saving to file");
        }
    }
}
