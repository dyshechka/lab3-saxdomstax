package ru.qweert.martha.sax;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import ru.qweert.martha.DateUtils;

public class GoodsHandler extends DefaultHandler {

    private StringBuilder result = new StringBuilder();
    private String currentElement;

    private Integer cost;
    private Integer quantity;
    private Integer total = 0;

    private boolean isGoods = false;
    private boolean isPersonalData = false;

    public String getResult() {
        return result.toString();
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
        System.out.println("Warning!");
        e.printStackTrace();

    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        System.out.println("Error!");
        e.printStackTrace();
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        System.out.println("Fatal error!");
        e.printStackTrace();
    }

    @Override
    public void startDocument() throws SAXException {
        result.append("<html>");
        result.append("<head>");
        result.append("<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta.2/css/bootstrap.min.css\" integrity=\"sha384-PsH8R72JQ3SOdhVi3uxftmaW6Vc51MKb0q5P2rRUpPvrszuE4W1povHYgTpBfshb\" crossorigin=\"anonymous\">");
        result.append("</head>");
        result.append("<body>");
        result.append("<div class=\"container\">");
    }

    @Override
    public void endDocument() throws SAXException {
        result.append("</div>");
        result.append("</body>");
        result.append("</html>");
    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        currentElement = qName;
        if (qName.equalsIgnoreCase("GOODS")) {
            isGoods = true;
            result.append("<table class=\"table\">");
            result.append("<thead>");
            result.append("<tr>");
            result.append("<th>Тип</th>");
            result.append("<th>Наименование</th>");
            result.append("<th>Количество</th>");
            result.append("<th>Стоимость за одну штуку</th>");
            result.append("<th>Итого</th>");
            result.append("</tr>");
            result.append("</thead>");
            result.append("<tbody>");
        } else if(isGoods == true) {
            if(qName.equalsIgnoreCase("GOOD")) {
                result.append("<tr>");
                String type = attributes.getValue(0);
                result.append("<td>" + type + "</td>");
            } else if(qName.equalsIgnoreCase("NAME") || qName.equalsIgnoreCase("QUANTITY") || qName.equalsIgnoreCase("COST")) {
                result.append("<td>");
            }
        } else if("firstName".equalsIgnoreCase(qName)) {
            isPersonalData = true;
            result.append("<div class=\"row\"><label>Имя:&nbsp</label>");
        } else if("lastName".equalsIgnoreCase(qName)) {
            isPersonalData = true;
            result.append("<div class=\"row\"><label>Фамилия:&nbsp</label>");
        } else if("patronymic".equalsIgnoreCase(qName)) {
            isPersonalData = true;
            result.append("<div class=\"row\"><label>Отчество:&nbsp</label>");
        } else if("birthDate".equalsIgnoreCase(qName)) {
            isPersonalData = true;
            result.append("<div class=\"row\"><label>Дата рождения:&nbsp</label>");
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("GOODS")) {
            isGoods = false;
            result.append("</tbody>");
            result.append("<tfoot>");
            result.append("<tr>");
            result.append("<th></th>");
            result.append("<th></th>");
            result.append("<th></th>");
            result.append("<th></th>");
            result.append("<th>" + total + "</th>");
            result.append("</tr>");
            result.append("</tfoot>");
            result.append("</table>");
        } else if(isGoods == true) {
            if(qName.equalsIgnoreCase("GOOD")) {
                result.append("<td>" + (quantity * cost) + "</td>");
                total += quantity * cost;
                result.append("</tr>");
            } else if(qName.equalsIgnoreCase("NAME") || qName.equalsIgnoreCase("QUANTITY") || qName.equalsIgnoreCase("COST")) {
                result.append("</td>");
            }
        } else if("firstName".equalsIgnoreCase(qName)) {
            result.append("</div>");
            isPersonalData = false;
        } else if("lastName".equalsIgnoreCase(qName)) {
            result.append("</div>");
            isPersonalData = false;
        } else if("patronymic".equalsIgnoreCase(qName)) {
            result.append("</div>");
            isPersonalData = false;
        } else if("birthDate".equalsIgnoreCase(qName)) {
            result.append("</div>");
            isPersonalData = false;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String str = new String(ch, start, length);
        str = str.trim();
        if (str.length() > 0) {
            if(isGoods == true) {
                result.append(str);
                if(currentElement.equalsIgnoreCase("QUANTITY")) {
                    quantity = Integer.parseInt(str);
                } else if(currentElement.equalsIgnoreCase("COST")) {
                    cost = Integer.parseInt(str);
                }
            } else if(isPersonalData) {
                if(currentElement.equalsIgnoreCase("birthDate")) {
                    result.append(DateUtils.formatDate(str));
                } else {
                    result.append(str);
                }
            }
        }
    }
}
