package com.ua.httpnettyserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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

    @Override
    public String toString() {
        return responseBody;
    }
}
