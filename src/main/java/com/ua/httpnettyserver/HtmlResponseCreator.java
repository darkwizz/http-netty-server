package com.ua.httpnettyserver;

import com.ua.httpnettyserver.responseelements.Table;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by ARTUR on 05.09.2015.
 */
public class HtmlResponseCreator {
    private String responseBody;

    private HtmlResponseCreator() {
        InputStream stream = getClass().getResourceAsStream("/GetResponse.html");
        try(BufferedReader reader= new BufferedReader(
                new InputStreamReader(stream))) {
            responseBody = "";
            String line = reader.readLine();
            while (line != null) {
                responseBody += (line + "\n");
                line = reader.readLine();
            }
        }
        catch (IOException ex) {
            System.out.println("Can't find resource");
            responseBody = "<html><body>{body}</body></html>";
        }
    }

    public static HtmlResponseCreator create() {
        return new HtmlResponseCreator();
    }

    public HtmlResponseCreator fillBody(String body) {
        responseBody = responseBody.replace("{body}", body);
        return this;
    }

    public String createList(List<String> items) {
        String list = "";
        for (String item : items) {
            list += ("<li>" + item + "</li>");
        }
        list = "<ul>" + list + "</ul>";
        return list;
    }

    public String createTable(Map<String, ? extends Object> table) {
        String result = "";
        for (Map.Entry<String, ? extends Object> entry : table.entrySet()) {
            result += ("<td>" + entry.getKey() + "</td>" + "<td>" + entry.getValue() + "</td>");
            result = "<tr>" + result + "</tr>";
        }
        result = "<table border = \"2\">" + result + "</table>";
        return result;
    }

    public Table createTable(Collection<String> headers) {
        Table table = new Table();
        for (String header : headers) {
            table.addHeader(header);
        }
        return table;
    }

    @Override
    public String toString() {
        return responseBody;
    }
}
