package ru.qweert.martha.stax;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        final File input = new File("resources/application.xml");
        final File schema = new File("resources/application2.xsd");
        final String outputFileName = "result.html";

        StaxParser parser = new StaxParser();
        parser.handle(input, schema, outputFileName);
    }
}
