package ru.qweert.martha.dom;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        final File input = new File("resources/application.xml");
        final File schema = new File("resources/application2.xsd");
        final String outputFileName = "result.html";

        DomHtmlParser parser = new DomHtmlParser();
        parser.handle(input, schema, outputFileName);
    }
}
