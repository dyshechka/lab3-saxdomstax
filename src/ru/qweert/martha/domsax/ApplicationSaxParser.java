package ru.qweert.martha.domsax;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import ru.qweert.martha.DateUtils;

import java.util.*;

public class ApplicationSaxParser extends DefaultHandler {

    private Document html;
    private Node body;
    private String currentElement = "";

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

    public ApplicationSaxParser(Document html) {
        this.html = html;
        body = html.getElementsByTagName("div").item(0);
    }

    public Document getHtml() {
        return html;
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
        String text = new String(ch, start, length).trim();
        if(text.length() > 0) {
            if(DATE_FIELDS.contains(currentElement)) {
                text = DateUtils.formatDate(text);
            }
            if(goods) {
                addToMap(goodsData, goodsOrder, currentElement, text);
            } else if(documents) {
                addToMap(documentsData, documentsOrder, currentElement, text);
            } else if(contacts) {
                addToMap(contactsData, contactsOrder, currentElement, text);
            } else {
                Element div = html.createElement("div");
                Attr rowClass = html.createAttribute("class");
                rowClass.setValue("row");
                div.setAttributeNode(rowClass);
                div.appendChild(html.createTextNode(currentElement + " : " + text));
                body.appendChild(div);
            }
        }
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        throw e;
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

        Element table = html.createElement("table");
        Attr tableClass = html.createAttribute("class");
        tableClass.setValue("table");
        table.setAttributeNode(tableClass);
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
