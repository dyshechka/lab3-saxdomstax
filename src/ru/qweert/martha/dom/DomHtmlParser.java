package ru.qweert.martha.dom;

import org.w3c.dom.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import ru.qweert.martha.DateUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.text.ParseException;

public class DomHtmlParser {

    public void handle(File input, File schema, String outputFileName) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            documentBuilderFactory.setValidating(true);
            documentBuilderFactory.setAttribute(
                    "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                    "http://www.w3.org/2001/XMLSchema"
            );
            documentBuilderFactory.setAttribute(
                    "http://java.sun.com/xml/jaxp/properties/schemaSource",
                    new FileInputStream(schema));
            documentBuilderFactory.setIgnoringElementContentWhitespace(true);

            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            documentBuilder.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) throws SAXException {
                    System.out.println("Warning!");
                    System.out.println("line " + exception.getLineNumber() + " column " + exception.getColumnNumber());
                    System.out.println(exception.getLocalizedMessage());
                    exception.printStackTrace();
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    System.out.println("Fatal Error!");
                    System.out.println("line " + exception.getLineNumber() + " column " + exception.getColumnNumber());
                    System.out.println(exception.getLocalizedMessage());
                    exception.printStackTrace();
                }

                @Override
                public void error(SAXParseException exception) throws SAXException {
                    System.out.println("Error!");
                    System.out.println("line " + exception.getLineNumber() + " column " + exception.getColumnNumber());
                    System.out.println(exception.getLocalizedMessage());
                    exception.printStackTrace();
                }
            });

            Document sourceDocument = documentBuilder.parse(new FileInputStream(input));
            Document destinationDocument = documentBuilder.newDocument();
            Element html = destinationDocument.createElement("html");
            Element head = destinationDocument.createElement("head");
            Element link = destinationDocument.createElement("link");
            Attr linkType = destinationDocument.createAttribute("rel");
            linkType.setValue("stylesheet");
            link.setAttributeNode(linkType);
            Attr linkHref = destinationDocument.createAttribute("href");
            linkHref.setValue("https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css");
            link.setAttributeNode(linkHref);

            Element body = destinationDocument.createElement("body");
            Element container = destinationDocument.createElement("div");
            Attr containerClass = destinationDocument.createAttribute("class");
            containerClass.setValue("container");
            container.setAttributeNode(containerClass);
            body.appendChild(container);

            Element table = createTable(destinationDocument);

            destinationDocument.appendChild(html);
            head.appendChild(link);
            html.appendChild(head);
            html.appendChild(body);
            container.appendChild(table);

            Integer total = 0;

            NodeList goods = sourceDocument.getElementsByTagName("goods");
            if(goods != null && goods.item(0) != null) {
                NodeList goodsElements = goods.item(0).getChildNodes();
                for(int i = 0; i < goodsElements.getLength(); i++) {
                    Integer totalAmount = appendTableLine(goodsElements.item(i), table.getElementsByTagName("tbody").item(0), destinationDocument, total);
                    total += totalAmount;
                }
            }
            NodeList footerTHs = table.getElementsByTagName("tfoot").item(0).getChildNodes().item(0).getChildNodes();
            Node lastTHofFooter = footerTHs.item(footerTHs.getLength() - 1);
            lastTHofFooter.appendChild(destinationDocument.createTextNode(String.valueOf(total)));

            createRow(destinationDocument, container, "Имя", sourceDocument.getElementsByTagName("firstName").item(0).getTextContent());
            createRow(destinationDocument, container, "Фамилия", sourceDocument.getElementsByTagName("lastName").item(0).getTextContent());
            createRow(destinationDocument, container, "Отчество", sourceDocument.getElementsByTagName("patronymic").item(0).getTextContent());
            createRow(destinationDocument, container, "Дата рождения", DateUtils.formatDate(sourceDocument.getElementsByTagName("birthDate").item(0).getTextContent()));

            // Use a Transformer for output
            try {
                TransformerFactory tFactory = TransformerFactory.newInstance();
                Transformer transformer = tFactory.newTransformer();
                StreamResult result = new StreamResult(outputFileName);
                DOMSource destinationSource = new DOMSource(destinationDocument);
                transformer.transform(destinationSource, result);
            } catch (TransformerException e) {
                throw new RuntimeException("Failed to write to file", e);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void createRow(Document dest, Element container, String label, String value) {
        Element div = dest.createElement("div");
        Attr divClass = dest.createAttribute("class");
        divClass.setValue("row");
        Element labelElement = dest.createElement("label");
        labelElement.setTextContent(label + ": ");
        div.appendChild(labelElement);
        div.appendChild(dest.createTextNode(value));
        container.appendChild(div);
    }

    private Element createTable(Document destinationDocument) {
        Element table = destinationDocument.createElement("table");
        Attr tableClass = destinationDocument.createAttribute("class");
        tableClass.setValue("table");
        table.setAttributeNode(tableClass);
        Element thead = destinationDocument.createElement("thead");
        Element tr = destinationDocument.createElement("tr");
        Element typeColumn = destinationDocument.createElement("th");
        Element nameColumn = destinationDocument.createElement("th");
        Element quantityColumn = destinationDocument.createElement("th");
        Element costColumn = destinationDocument.createElement("th");
        Element totalColumn = destinationDocument.createElement("th");
        typeColumn.appendChild(destinationDocument.createTextNode("Тип"));
        nameColumn.appendChild(destinationDocument.createTextNode("Наименование"));
        quantityColumn.appendChild(destinationDocument.createTextNode("Количество"));
        costColumn.appendChild(destinationDocument.createTextNode("Стоимость за одну штуку"));
        totalColumn.appendChild(destinationDocument.createTextNode("Итого"));
        tr.appendChild(typeColumn);
        tr.appendChild(nameColumn);
        tr.appendChild(quantityColumn);
        tr.appendChild(costColumn);
        tr.appendChild(totalColumn);
        thead.appendChild(tr);
        table.appendChild(thead);
        Element tbody = destinationDocument.createElement("tbody");
        Element tfoot = destinationDocument.createElement("tfoot");
        Element trfoot = destinationDocument.createElement("tr");
        Element th = destinationDocument.createElement("th");
        trfoot.appendChild(th);
        trfoot.appendChild(th.cloneNode(false));
        trfoot.appendChild(th.cloneNode(false));
        trfoot.appendChild(th.cloneNode(false));
        trfoot.appendChild(th.cloneNode(false));
        tfoot.appendChild(trfoot);
        table.appendChild(tbody);
        table.appendChild(tfoot);
        return table;
    }

    private static Integer appendTableLine(
            Node goodsElement,
            Node tableNode,
            Document dest,
            Integer total) throws ParseException {
        Node tr = dest.createElement("tr");
        NodeList columns = goodsElement.getChildNodes();
        Node typeTD = dest.createElement("td");
        Node nameTD = dest.createElement("td");
        Node quantityTD = dest.createElement("td");
        Node costTD = dest.createElement("td");
        Node totalTD = dest.createElement("td");
        typeTD.appendChild(dest.createTextNode(goodsElement.getAttributes().getNamedItem("goodType").getTextContent()));
        nameTD.appendChild(dest.createTextNode(columns.item(0).getTextContent()));
        Integer quantity = Integer.parseInt(columns.item(1).getTextContent());
        Integer cost = Integer.parseInt(columns.item(2).getTextContent());
        quantityTD.appendChild(dest.createTextNode(String.valueOf(quantity)));
        costTD.appendChild(dest.createTextNode(String.valueOf(cost)));
        totalTD.appendChild(dest.createTextNode(String.valueOf(quantity * cost)));
        tr.appendChild(typeTD);
        tr.appendChild(nameTD);
        tr.appendChild(quantityTD);
        tr.appendChild(costTD);
        tr.appendChild(totalTD);
        tableNode.appendChild(tr);
        return quantity * cost;
    }
}
