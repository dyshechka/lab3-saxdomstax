package ru.qweert.martha.stax;

import ru.qweert.martha.DateUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class StaxParser {

    public void handle(File input, File schema, String outputFileName) {
        try {
            XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
            xmlFactory.setXMLReporter(new XMLReporter() {

                @Override
                public void report(String message, String errorType,
                                   Object relatedInformation, Location location)
                        throws XMLStreamException {
                    System.out.println(message + " " + errorType);
                }
            });
            XMLEventReader eventReader = xmlFactory.createXMLEventReader(new FileInputStream(input));

            StringBuilder str = new StringBuilder();
            int quantity = 0;
            int cost = 0;
            int total = 0;

            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                if (event.isStartDocument()) {
                    str.append("<html>");
                    str.append("<head>");
                    str.append("<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta.2/css/bootstrap.min.css\" integrity=\"sha384-PsH8R72JQ3SOdhVi3uxftmaW6Vc51MKb0q5P2rRUpPvrszuE4W1povHYgTpBfshb\" crossorigin=\"anonymous\">");
                    str.append("</head>");
                    str.append("<body>");
                    str.append("<div class=\"container\">");
                }
                if (event.isStartElement()) {
                    if(("goods").equals(event.asStartElement().getName().getLocalPart())) {
                        str.append("<table class=\"table\">");
                        str.append("<thead>");
                        str.append("<tr>");
                        str.append("<th>Тип</th>");
                        str.append("<th>Наименование</th>");
                        str.append("<th>Количество</th>");
                        str.append("<th>Стоимость за одну штуку</th>");
                        str.append("<th>Итого</th>");
                        str.append("</tr>");
                        str.append("</thead>");
                        str.append("<tbody>");
                    } else if (("name").equals(event.asStartElement().getName()
                            .getLocalPart())) {
                        str.append("<td>").append(eventReader.nextEvent().asCharacters().getData()).append("</td>");
                    } else if (("quantity").equals(event.asStartElement().getName()
                            .getLocalPart())) {
                        quantity = Integer.valueOf(eventReader.nextEvent()
                                .asCharacters().getData());
                        str.append("<td>").append(quantity).append("</td>");
                    } else if (("cost").equals(event.asStartElement().getName()
                            .getLocalPart())) {
                        cost = Integer.valueOf(eventReader.nextEvent()
                                .asCharacters().getData());
                        str.append("<td>").append(cost).append("</td>");
                    } else if(("good").equals(event.asStartElement().getName()
                            .getLocalPart())) {
                        str.append("<tr>");
                        Attribute goodType = event.asStartElement().getAttributeByName(new QName("goodType"));
                        str.append("<td>" + goodType.getValue() + "</td>");
                    } else if(("firstName").equals(event.asStartElement().getName().getLocalPart())) {
                        str.append("<div class=\"row\"><label>Имя:&nbsp</label>")
                                .append(eventReader.nextEvent().asCharacters().getData())
                                .append("</div>");
                    } else if(("lastName").equals(event.asStartElement().getName().getLocalPart())) {
                        str.append("<div class=\"row\"><label>Фамилия:&nbsp</label>")
                                .append(eventReader.nextEvent().asCharacters().getData())
                                .append("</div>");
                    } else if(("patronymic").equals(event.asStartElement().getName().getLocalPart())) {
                        str.append("<div class=\"row\"><label>Отчество:&nbsp</label>")
                                .append(eventReader.nextEvent().asCharacters().getData())
                                .append("</div>");
                    } else if(("birthDate").equals(event.asStartElement().getName().getLocalPart())) {
                        str.append("<div class=\"row\"><label>Дата рождения:&nbsp</label>")
                                .append(DateUtils.formatDate(eventReader.nextEvent().asCharacters().getData()))
                                .append("</div>");
                    }
                }
                if (event.isEndElement()) {
                    if (("good").equals(event.asEndElement().getName().getLocalPart())) {
                        str.append("<td>").append(String.valueOf(quantity * cost)).append("</td>");
                        total += quantity * cost;
                        str.append("</tr>");
                    } else if (("goods").equals(event.asEndElement().getName().getLocalPart())) {
                        str.append("</tbody>");
                        str.append("<tfoot>");
                        str.append("<tr>");
                        str.append("<th></th>");
                        str.append("<th></th>");
                        str.append("<th></th>");
                        str.append("<th></th>");
                        str.append("<th>" + total + "</th>");
                        str.append("</tr>");
                        str.append("</tfoot>");
                    }
                }
                if (event.isEndDocument()) {
                    str.append("</div>");
                    str.append("</body>");
                    str.append("</html>");
                }
            }

            try {
                PrintWriter out = new PrintWriter(outputFileName);
                out.println(str.toString());
                out.flush();
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Problem while saving to file");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
