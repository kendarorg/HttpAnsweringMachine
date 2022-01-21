package org.kendar;

import java.io.*;
import java.net.*;

public class HttpsTest {
    public static void main(String[] args) throws IOException {
        //extracted("https://www.google.com");
        extracted("https://www.local.test");
    }

    private static void extracted(String urla) throws IOException {
        //System.setProperty("https.protocols", "TLSv1.2");
        URL url = new URL(urla);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        int status = con.getResponseCode();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        System.out.println(content.toString());
        in.close();
        con.disconnect();
    }
}
