package ru.qweert.martha.sax;

import org.xml.sax.SAXException;
import ru.qweert.martha.XmlTransformer;
import ru.qweert.martha.domsax.ApplicationSaxParser;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;

public class SaxParser implements XmlTransformer {
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
        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(input, new HtmlHandler());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse xml", e);
        }
        return null;
    }
}
