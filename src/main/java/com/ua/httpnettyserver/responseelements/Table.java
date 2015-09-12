package com.ua.httpnettyserver.responseelements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ARTUR on 06.09.2015.
 */
public class Table {
    private List<List<String>> rows;
    private List<String> headers;

    public Table() {
        headers = new ArrayList<>();
        rows = new ArrayList<>();
    }

    public void addHeader(String header) {
        headers.add(header);
    }

    public void addRow(List<String> cells) {
        int headersCount = headers.size();
        int cellsCount = cells.size();
        if (headersCount > cellsCount) {
            for (int i = 0; i < headersCount - cellsCount; i++) {
                cells.add("");
            }
        }
        List<String> row = new ArrayList<>();
        for (int i = 0; i < headersCount; i++) {
            row.add(cells.get(i));
        }
        rows.add(row);
    }

    @Override
    public String toString() {
        String table = "";
        for (String header : headers) {
            table += ("<th>" + header + "</th>\n");
        }
        table = "<tr>\n" + table + "\n</tr>\n";
        String tableRows = "";
        for (List<String> row : rows) {
            String line = "";
            for (String cell : row) {
                line += ("<td>" + cell + "</td>\n");
            }
            tableRows += ("<tr>\n" + line + "</tr>\n");
        }
        table = "<table border = \"2\">\n" + table + tableRows + "\n</table>\n";
        return table;
    }
}
