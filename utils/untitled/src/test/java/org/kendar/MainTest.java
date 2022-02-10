package org.kendar;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Scanner;

public class MainTest {
    @BeforeAll
    static void setup() {
    }

    @BeforeEach
    void init() {

    }

    @Test
    void someTest() throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://www.tutorialspoint.com/");
        httpGet.addHeader("X-test","test");
        HttpResponse httpResponse = httpClient.execute(httpGet);
        Scanner sc = new Scanner(httpResponse.getEntity().getContent());
        StringBuilder sb = new StringBuilder();
        while(sc.hasNext()) {
            sb.append(sc.nextLine());
        }
        StatusLine sl = httpResponse.getStatusLine();
        String content = sb.toString();
        int statusCode = sl.getStatusCode();
        System.out.println(statusCode+" "+content);
    }
}
