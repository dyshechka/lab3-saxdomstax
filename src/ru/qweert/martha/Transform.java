package ru.qweert.martha;

import ru.qweert.martha.domsax.DomSaxTransformer;
import ru.qweert.martha.sax.SaxParser;

import java.io.File;

public class Transform {

    public static void main(String[] args) {
        final File input = new File("resources/application.xml");
        final File schema = new File("resources/application2.xsd");

//        XmlTransformer transformer = new DomSaxTransformer();
        XmlTransformer transformer = new SaxParser();
        transformer.xmlToHtml(input, schema);
    }
}
