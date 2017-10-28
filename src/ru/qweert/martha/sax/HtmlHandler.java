package ru.qweert.martha.sax;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;

public class HtmlHandler extends DefaultHandler {

    private StringBuilder result = new StringBuilder();
    private String currentElement;

    private boolean goods = false;
    private boolean documents = false;
    private boolean contacts = false;

    private Map<String, List<String>> goodsData = new HashMap<>();
    private List<String> goodsOrder = new ArrayList<>();
    private Map<String, List<String>> documentsData = new HashMap<>();
    private List<String> documentsOrder = new ArrayList<>();
    private Map<String, List<String>> contactsData = new HashMap<>();
    private List<String> contactsOrder = new ArrayList<>();

    private List<String> DATE_FIELDS = Arrays.asList("birthDate", "issueDate");

    @Override
    public void startDocument() throws SAXException {
        result.append("<html>");
        result.append("<body>");
    }

    @Override
    public void endDocument() throws SAXException {
        result.append("</body>");
        result.append("</html>");
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentElement = qName;
        if(qName.equalsIgnoreCase("GOODS")) {
            goods = true;
        } else if(qName.equalsIgnoreCase("DOCUMENTS")) {
            documents = true;
        } else if(qName.equalsIgnoreCase("CONTACTS")) {
            contacts = true;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(qName.equalsIgnoreCase("GOODS")) {
            goods = false;
            createGoodsTable();
        } else if(qName.equalsIgnoreCase("DOCUMENTS")) {
            documents = false;
            createDocumentsTable();
        } else if(qName.equalsIgnoreCase("CONTACTS")) {
            contacts = false;
            createContactsTable();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
    }

    private void createContactsTable() { createTable(contactsData, contactsOrder, null); }

    private void createDocumentsTable() { createTable(documentsData, documentsOrder, null); }

    private void createGoodsTable() {
        int rowsCount = goodsData.get(goodsData.keySet().iterator().next()).size();
        goodsData.put("TOTAL", new ArrayList<>(rowsCount));
        goodsOrder.add("TOTAL");
        for (int index = 0 ; index < rowsCount; index++) {
            String quantity = goodsData.get("quantity").get(index);
            String cost = goodsData.get("cost").get(index);
            long total = Long.parseLong(quantity) * Long.parseLong(cost);
            goodsData.get("TOTAL").add(String.valueOf(total));
        }
        Map<String, String> footer = new HashMap<>();
        for (String key : goodsData.keySet()) {
            if (key.equalsIgnoreCase("TOTAL")) {
                Long totalOfTotal = 0L;
                List<String> total = goodsData.get("TOTAL");
                for (int i = 0; i < total.size(); i++) {
                    String goodTotal = total.get(i);
                    totalOfTotal += Long.parseLong(goodTotal);
                }
                footer.put(key, String.valueOf(totalOfTotal));
            } else {
                footer.put(key, "");
            }
        }
        createTable(goodsData, goodsOrder, footer);
    }

    private void createTable(Map<String, List<String>> data, List<String> order, Map<String, String> footer) {
        // получаем количество колонок в таблице
        int rowsCount = data.get(data.keySet().iterator().next()).size();

        result.append("<table class=\"table\">");
        result.append("<thead>");
        result.append("<tr>");
        result.append("<tbody>");
        Element thead = html.createElement("thead");
        Element tr = html.createElement("tr");
        Element tbody = html.createElement("tbody");
        List<Element> tableRows = new ArrayList<>(rowsCount);
        for (int index = 0 ; index < rowsCount; index++) {
            Element tabletr = html.createElement("tr");
            tableRows.add(tabletr);
            tbody.appendChild(tabletr);
        }
        thead.appendChild(tr);
        table.appendChild(thead);
        table.appendChild(tbody);
        for (int i = 0; i < order.size(); i++) {
            String key = order.get(i);
            Element th = html.createElement("th");
            th.appendChild(html.createTextNode(key));
            tr.appendChild(th);
            List<String> datas = data.get(key);
            for (int idx = 0; idx < datas.size(); idx++) {
                Element td = html.createElement("td");
                td.appendChild(html.createTextNode(datas.get(idx)));
                tableRows.get(idx).appendChild(td);
            }
        }

        if(footer != null) {
            Element tfoot = html.createElement("tfooter");
            Element tfoottr = html.createElement("tr");
            tfoot.appendChild(tfoottr);
            for (int i = 0; i < order.size(); i++) {
                String key = order.get(i);
                Element td = html.createElement("td");
                td.appendChild(html.createTextNode(footer.get(key)));
                tfoottr.appendChild(td);
            }
            table.appendChild(tfoot);
        }

        body.appendChild(table);
    }

    private void addToMap(Map<String, List<String>> data, List<String> order, String currentElement, String text) {
        if(!data.containsKey(currentElement)) {
            data.put(currentElement, new ArrayList<>());
            order.add(currentElement);
        }
        data.get(currentElement).add(text);
    }
}
