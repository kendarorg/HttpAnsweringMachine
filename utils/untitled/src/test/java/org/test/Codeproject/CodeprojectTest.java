package org.kendar.Codeproject;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Scanner;

public class CodeprojectTest {
	@Test
	void doTestNavigation() throws IOException{
		//UPLOAD THE REPLAYER RESULT
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		var request = new HttpPost("http://www.local.test");
		var data = this.getClass().getResourceAsStream("/org/kendar/Codeproject/recording.json").readAllBytes();
		var jsonFile="{\"name\":\"Codeproject.json\",\"data\":\""+Base64.getEncoder().encodeToString(data)+"\"}";
		HttpEntity entity = new StringEntity(jsonFile, ContentType.create("application/json"));
		((HttpEntityEnclosingRequestBase) request).setEntity(entity);
		var httpResponse = httpClient.execute(request);
		HttpEntity responseEntity = httpResponse.getEntity();

		//STARTREPLAYING
		d_0();
		d_1();
		d_2();
		d_3();
		d_4();
		d_5();
		d_6();
		d_7();
		d_8();
		d_9();
		d_10();
		d_11();
		d_12();
		d_13();
		d_14();
		d_15();
		d_16();
		d_17();
		d_18();
	}

	private void d_0() throws IOException{
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		var request = new HttpGet("https://www.codeproject.com/");
		request.addHeader("Accept-encoding","gzip, deflate, br");
		request.addHeader("Cookie","mguid=5853a8f0-f0cf-4346-a8f2-29ac4b4e1480; vk=ad52b0e4-a53f-4da5-a440-f3bd660f3d29; _ga=GA1.1.896389723.1643017258; __gads=ID=e72e7344db5d49c2:T=1643017258:S=ALNI_MbdF8AQaXePEV8YGN63Yerhm_a1cQ; cookieconsent_status=deny; SessionGUID=ca489c81-8eff-43cb-ada5-919ef8fe1a0a; g_state={\"i_p\":1645526177239,\"i_l\":3}; _ga_YZNPNQ0F2S=GS1.1.1644921371.7.1.1644921906.0");
		request.addHeader("Sec-ch-ua","\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"98\", \"Google Chrome\";v=\"98\"");
		request.addHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
		request.addHeader("Sec-fetch-dest","document");
		request.addHeader("Sec-fetch-user","?1");
		request.addHeader("Connection","keep-alive");
		request.addHeader("Host","www.codeproject.com");
		request.addHeader("Purpose","prefetch");
		request.addHeader("Sec-fetch-site","none");
		request.addHeader("Sec-ch-ua-platform","\"Windows\"");
		request.addHeader("Sec-fetch-mode","navigate");
		request.addHeader("User-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.82 Safari/537.36");
		request.addHeader("Accept-language","en-US,en;q=0.9");
		request.addHeader("Upgrade-insecure-requests","1");
		request.addHeader("Sec-ch-ua-mobile","?0");
		request.addHeader("Content-Type","application/octet-stream");
		var expectedResponseData = new String(this.getClass().getResourceAsStream("/org/kendar/Codeproject/row_0_res").readAllBytes());
		var expectedResponseCode = 200;
		var httpResponse = httpClient.execute(request);
		HttpEntity responseEntity = httpResponse.getEntity();
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		String contentType = responseEntity.getContentType().getValue();
		InputStream in = responseEntity.getContent();
		String result = IOUtils.toString(in, StandardCharsets.UTF_8);
	}

	private void d_1() throws IOException{
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		var request = new HttpPost("https://www.codeproject.com/script/Common/Webservices/CommonServices.aspx/GetPageViewsTimeSeriesData");
		request.addHeader("Origin","https://www.codeproject.com");
		request.addHeader("Accept-encoding","gzip, deflate, br");
		request.addHeader("Cookie","mguid=5853a8f0-f0cf-4346-a8f2-29ac4b4e1480; vk=ad52b0e4-a53f-4da5-a440-f3bd660f3d29; _ga=GA1.1.896389723.1643017258; __gads=ID=e72e7344db5d49c2:T=1643017258:S=ALNI_MbdF8AQaXePEV8YGN63Yerhm_a1cQ; cookieconsent_status=deny; SessionGUID=ca489c81-8eff-43cb-ada5-919ef8fe1a0a; g_state={\"i_p\":1645526177239,\"i_l\":3}; _ga_YZNPNQ0F2S=GS1.1.1644921371.7.1.1644922685.0");
		request.addHeader("Sec-ch-ua","\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"98\", \"Google Chrome\";v=\"98\"");
		request.addHeader("Accept","text/plain, */*; q=0.01");
		request.addHeader("Sec-fetch-dest","empty");
		request.addHeader("Connection","keep-alive");
		request.addHeader("Referer","https://www.codeproject.com/");
		request.addHeader("Host","www.codeproject.com");
		request.addHeader("Sec-fetch-site","same-origin");
		request.addHeader("Sec-ch-ua-platform","\"Windows\"");
		request.addHeader("Sec-fetch-mode","cors");
		request.addHeader("User-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.82 Safari/537.36");
		request.addHeader("Content-type","application/json");
		request.addHeader("Accept-language","en-US,en;q=0.9");
		request.addHeader("Content-length","32");
		request.addHeader("X-requested-with","XMLHttpRequest");
		request.addHeader("Sec-ch-ua-mobile","?0");
		var data = new String(this.getClass().getResourceAsStream("/org/kendar/Codeproject/row_1_req").readAllBytes());
		HttpEntity entity = new StringEntity(data, ContentType.create("application/json"));
		((HttpEntityEnclosingRequestBase) request).setEntity(entity);
		var expectedResponseData = new String(this.getClass().getResourceAsStream("/org/kendar/Codeproject/row_1_res").readAllBytes());
		var expectedResponseCode = 200;
		var httpResponse = httpClient.execute(request);
		HttpEntity responseEntity = httpResponse.getEntity();
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		String contentType = responseEntity.getContentType().getValue();
		InputStream in = responseEntity.getContent();
		String result = IOUtils.toString(in, StandardCharsets.UTF_8);
	}

	private void d_2() throws IOException{
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		var request = new HttpPost("https://www.codeproject.com/script/Common/Webservices/CommonServices.aspx/GetPageViewsTimeSeriesData");
		request.addHeader("Origin","https://www.codeproject.com");
		request.addHeader("Accept-encoding","gzip, deflate, br");
		request.addHeader("Cookie","mguid=5853a8f0-f0cf-4346-a8f2-29ac4b4e1480; vk=ad52b0e4-a53f-4da5-a440-f3bd660f3d29; _ga=GA1.1.896389723.1643017258; __gads=ID=e72e7344db5d49c2:T=1643017258:S=ALNI_MbdF8AQaXePEV8YGN63Yerhm_a1cQ; cookieconsent_status=deny; SessionGUID=ca489c81-8eff-43cb-ada5-919ef8fe1a0a; g_state={\"i_p\":1645526177239,\"i_l\":3}; _ga_YZNPNQ0F2S=GS1.1.1644921371.7.1.1644922685.0");
		request.addHeader("Sec-ch-ua","\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"98\", \"Google Chrome\";v=\"98\"");
		request.addHeader("Accept","text/plain, */*; q=0.01");
		request.addHeader("Sec-fetch-dest","empty");
		request.addHeader("Connection","keep-alive");
		request.addHeader("Referer","https://www.codeproject.com/");
		request.addHeader("Host","www.codeproject.com");
		request.addHeader("Sec-fetch-site","same-origin");
		request.addHeader("Sec-ch-ua-platform","\"Windows\"");
		request.addHeader("Sec-fetch-mode","cors");
		request.addHeader("User-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.82 Safari/537.36");
		request.addHeader("Content-type","application/json");
		request.addHeader("Accept-language","en-US,en;q=0.9");
		request.addHeader("Content-length","32");
		request.addHeader("X-requested-with","XMLHttpRequest");
		request.addHeader("Sec-ch-ua-mobile","?0");
		var data = new String(this.getClass().getResourceAsStream("/org/kendar/Codeproject/row_2_req").readAllBytes());
		HttpEntity entity = new StringEntity(data, ContentType.create("application/json"));
		((HttpEntityEnclosingRequestBase) request).setEntity(entity);
		var expectedResponseData = new String(this.getClass().getResourceAsStream("/org/kendar/Codeproject/row_2_res").readAllBytes());
		var expectedResponseCode = 200;
		var httpResponse = httpClient.execute(request);
		HttpEntity responseEntity = httpResponse.getEntity();
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		String contentType = responseEntity.getContentType().getValue();
		InputStream in = responseEntity.getContent();
		String result = IOUtils.toString(in, StandardCharsets.UTF_8);
	}

	private void d_3() throws IOException{
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		var request = new HttpGet("https://www.codeproject.com/search.aspx?q=nuget&sbo=kw&x=12&y=8");
		request.addHeader("Accept-encoding","gzip, deflate, br");
		request.addHeader("Cookie","mguid=5853a8f0-f0cf-4346-a8f2-29ac4b4e1480; vk=ad52b0e4-a53f-4da5-a440-f3bd660f3d29; _ga=GA1.1.896389723.1643017258; __gads=ID=e72e7344db5d49c2:T=1643017258:S=ALNI_MbdF8AQaXePEV8YGN63Yerhm_a1cQ; cookieconsent_status=deny; SessionGUID=ca489c81-8eff-43cb-ada5-919ef8fe1a0a; g_state={\"i_p\":1645526177239,\"i_l\":3}; _ga_YZNPNQ0F2S=GS1.1.1644921371.7.1.1644922685.0");
		request.addHeader("Sec-ch-ua","\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"98\", \"Google Chrome\";v=\"98\"");
		request.addHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
		request.addHeader("Sec-fetch-dest","document");
		request.addHeader("Sec-fetch-user","?1");
		request.addHeader("Connection","keep-alive");
		request.addHeader("Referer","https://www.codeproject.com/");
		request.addHeader("Host","www.codeproject.com");
		request.addHeader("Sec-fetch-site","same-origin");
		request.addHeader("Sec-ch-ua-platform","\"Windows\"");
		request.addHeader("Sec-fetch-mode","navigate");
		request.addHeader("User-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.82 Safari/537.36");
		request.addHeader("Accept-language","en-US,en;q=0.9");
		request.addHeader("Upgrade-insecure-requests","1");
		request.addHeader("Sec-ch-ua-mobile","?0");
		request.addHeader("Content-Type","application/octet-stream");
		var expectedResponseData = new String(this.getClass().getResourceAsStream("/org/kendar/Codeproject/row_3_res").readAllBytes());
		var expectedResponseCode = 200;
		var httpResponse = httpClient.execute(request);
		HttpEntity responseEntity = httpResponse.getEntity();
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		String contentType = responseEntity.getContentType().getValue();
		InputStream in = responseEntity.getContent();
		String result = IOUtils.toString(in, StandardCharsets.UTF_8);
	}

	private void d_4() throws IOException{
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		var request = new HttpGet("https://www.codeproject.com/Reference/628210/An-Overview-of-the-NuGet-Ecosystem");
		request.addHeader("Accept-encoding","gzip, deflate, br");
		request.addHeader("Cookie","mguid=5853a8f0-f0cf-4346-a8f2-29ac4b4e1480; vk=ad52b0e4-a53f-4da5-a440-f3bd660f3d29; _ga=GA1.1.896389723.1643017258; __gads=ID=e72e7344db5d49c2:T=1643017258:S=ALNI_MbdF8AQaXePEV8YGN63Yerhm_a1cQ; cookieconsent_status=deny; SessionGUID=ca489c81-8eff-43cb-ada5-919ef8fe1a0a; g_state={\"i_p\":1645526177239,\"i_l\":3}; _ga_YZNPNQ0F2S=GS1.1.1644921371.7.1.1644922690.0");
		request.addHeader("Sec-ch-ua","\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"98\", \"Google Chrome\";v=\"98\"");
		request.addHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
		request.addHeader("Sec-fetch-dest","document");
		request.addHeader("Sec-fetch-user","?1");
		request.addHeader("Connection","keep-alive");
		request.addHeader("Referer","https://www.codeproject.com/");
		request.addHeader("Host","www.codeproject.com");
		request.addHeader("Sec-fetch-site","same-origin");
		request.addHeader("Sec-ch-ua-platform","\"Windows\"");
		request.addHeader("Sec-fetch-mode","navigate");
		request.addHeader("User-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.82 Safari/537.36");
		request.addHeader("Accept-language","en-US,en;q=0.9");
		request.addHeader("Upgrade-insecure-requests","1");
		request.addHeader("Sec-ch-ua-mobile","?0");
		request.addHeader("Content-Type","application/octet-stream");
		var expectedResponseData = new String(this.getClass().getResourceAsStream("/org/kendar/Codeproject/row_4_res").readAllBytes());
		var expectedResponseCode = 200;
		var httpResponse = httpClient.execute(request);
		HttpEntity responseEntity = httpResponse.getEntity();
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		String contentType = responseEntity.getContentType().getValue();
		InputStream in = responseEntity.getContent();
		String result = IOUtils.toString(in, StandardCharsets.UTF_8);
	}

	private void d_5() throws IOException{
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		var request = new HttpPost("https://www.codeproject.com/script/ratings/ajax/GetRatings.aspx");
		request.addHeader("Origin","https://www.codeproject.com");
		request.addHeader("Accept-encoding","gzip, deflate, br");
		request.addHeader("Cookie","mguid=5853a8f0-f0cf-4346-a8f2-29ac4b4e1480; vk=ad52b0e4-a53f-4da5-a440-f3bd660f3d29; _ga=GA1.1.896389723.1643017258; __gads=ID=e72e7344db5d49c2:T=1643017258:S=ALNI_MbdF8AQaXePEV8YGN63Yerhm_a1cQ; cookieconsent_status=deny; SessionGUID=ca489c81-8eff-43cb-ada5-919ef8fe1a0a; g_state={\"i_p\":1645526177239,\"i_l\":3}; _ga_YZNPNQ0F2S=GS1.1.1644921371.7.1.1644922692.0");
		request.addHeader("Sec-ch-ua","\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"98\", \"Google Chrome\";v=\"98\"");
		request.addHeader("Accept","*/*");
		request.addHeader("Sec-fetch-dest","empty");
		request.addHeader("Connection","keep-alive");
		request.addHeader("Referer","https://www.codeproject.com/");
		request.addHeader("Host","www.codeproject.com");
		request.addHeader("Sec-fetch-site","same-origin");
		request.addHeader("Sec-ch-ua-platform","\"Windows\"");
		request.addHeader("Sec-fetch-mode","cors");
		request.addHeader("User-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.82 Safari/537.36");
		request.addHeader("Content-type","application/x-www-form-urlencoded; charset=UTF-8");
		request.addHeader("Accept-language","en-US,en;q=0.9");
		request.addHeader("Content-length","16");
		request.addHeader("X-requested-with","XMLHttpRequest");
		request.addHeader("Sec-ch-ua-mobile","?0");
		var expectedResponseData = new String(this.getClass().getResourceAsStream("/org/kendar/Codeproject/row_5_res").readAllBytes());
		var expectedResponseCode = 200;
		var httpResponse = httpClient.execute(request);
		HttpEntity responseEntity = httpResponse.getEntity();
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		String contentType = responseEntity.getContentType().getValue();
		InputStream in = responseEntity.getContent();
		String result = IOUtils.toString(in, StandardCharsets.UTF_8);
	}

	private void d_6() throws IOException{
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		var request = new HttpPost("https://www.codeproject.com/script/Ratings/ajax/GetRatings.aspx");
		request.addHeader("Origin","https://www.codeproject.com");
		request.addHeader("Accept-encoding","gzip, deflate, br");
		request.addHeader("Cookie","mguid=5853a8f0-f0cf-4346-a8f2-29ac4b4e1480; vk=ad52b0e4-a53f-4da5-a440-f3bd660f3d29; _ga=GA1.1.896389723.1643017258; __gads=ID=e72e7344db5d49c2:T=1643017258:S=ALNI_MbdF8AQaXePEV8YGN63Yerhm_a1cQ; cookieconsent_status=deny; SessionGUID=ca489c81-8eff-43cb-ada5-919ef8fe1a0a; g_state={\"i_p\":1645526177239,\"i_l\":3}; _ga_YZNPNQ0F2S=GS1.1.1644921371.7.1.1644922692.0");
		request.addHeader("Sec-ch-ua","\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"98\", \"Google Chrome\";v=\"98\"");
		request.addHeader("Accept","*/*");
		request.addHeader("Sec-fetch-dest","empty");
		request.addHeader("Connection","keep-alive");
		request.addHeader("Referer","https://www.codeproject.com/");
		request.addHeader("Host","www.codeproject.com");
		request.addHeader("Sec-fetch-site","same-origin");
		request.addHeader("Sec-ch-ua-platform","\"Windows\"");
		request.addHeader("Sec-fetch-mode","cors");
		request.addHeader("User-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.82 Safari/537.36");
		request.addHeader("Content-type","application/x-www-form-urlencoded; charset=UTF-8");
		request.addHeader("Accept-language","en-US,en;q=0.9");
		request.addHeader("Content-length","16");
		request.addHeader("X-requested-with","XMLHttpRequest");
		request.addHeader("Sec-ch-ua-mobile","?0");
		var expectedResponseData = new String(this.getClass().getResourceAsStream("/org/kendar/Codeproject/row_6_res").readAllBytes());
		var expectedResponseCode = 200;
		var httpResponse = httpClient.execute(request);
		HttpEntity responseEntity = httpResponse.getEntity();
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		String contentType = responseEntity.getContentType().getValue();
		InputStream in = responseEntity.getContent();
		String result = IOUtils.toString(in, StandardCharsets.UTF_8);
	}

	private void d_7() throws IOException{
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		var request = new HttpPost("https://www.codeproject.com/script/Forums/WebServices/ForumSubscriptionServices.aspx/GetSubscriptions");
		request.addHeader("Origin","https://www.codeproject.com");
		request.addHeader("Accept-encoding","gzip, deflate, br");
		request.addHeader("Cookie","mguid=5853a8f0-f0cf-4346-a8f2-29ac4b4e1480; vk=ad52b0e4-a53f-4da5-a440-f3bd660f3d29; _ga=GA1.1.896389723.1643017258; __gads=ID=e72e7344db5d49c2:T=1643017258:S=ALNI_MbdF8AQaXePEV8YGN63Yerhm_a1cQ; cookieconsent_status=deny; SessionGUID=ca489c81-8eff-43cb-ada5-919ef8fe1a0a; g_state={\"i_p\":1645526177239,\"i_l\":3}; _ga_YZNPNQ0F2S=GS1.1.1644921371.7.1.1644922692.0");
		request.addHeader("Sec-ch-ua","\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"98\", \"Google Chrome\";v=\"98\"");
		request.addHeader("Accept","application/json, text/javascript, */*; q=0.01");
		request.addHeader("Sec-fetch-dest","empty");
		request.addHeader("Connection","keep-alive");
		request.addHeader("Referer","https://www.codeproject.com/");
		request.addHeader("Host","www.codeproject.com");
		request.addHeader("Sec-fetch-site","same-origin");
		request.addHeader("Sec-ch-ua-platform","\"Windows\"");
		request.addHeader("Sec-fetch-mode","cors");
		request.addHeader("User-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.82 Safari/537.36");
		request.addHeader("Content-type","application/json; charset=UTF-8");
		request.addHeader("Accept-language","en-US,en;q=0.9");
		request.addHeader("Content-length","19");
		request.addHeader("X-requested-with","XMLHttpRequest");
		request.addHeader("Sec-ch-ua-mobile","?0");
		var data = new String(this.getClass().getResourceAsStream("/org/kendar/Codeproject/row_7_req").readAllBytes());
		HttpEntity entity = new StringEntity(data, ContentType.create("application/json"));
		((HttpEntityEnclosingRequestBase) request).setEntity(entity);
		var expectedResponseData = new String(this.getClass().getResourceAsStream("/org/kendar/Codeproject/row_7_res").readAllBytes());
		var expectedResponseCode = 200;
		var httpResponse = httpClient.execute(request);
		HttpEntity responseEntity = httpResponse.getEntity();
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		String contentType = responseEntity.getContentType().getValue();
		InputStream in = responseEntity.getContent();
		String result = IOUtils.toString(in, StandardCharsets.UTF_8);
	}

	private void d_8() throws IOException{
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		var request = new HttpGet("https://www.codeproject.com/script/Articles/ListVersions.aspx?aid=628210");
		request.addHeader("Accept-encoding","gzip, deflate, br");
		request.addHeader("Cookie","mguid=5853a8f0-f0cf-4346-a8f2-29ac4b4e1480; vk=ad52b0e4-a53f-4da5-a440-f3bd660f3d29; _ga=GA1.1.896389723.1643017258; __gads=ID=e72e7344db5d49c2:T=1643017258:S=ALNI_MbdF8AQaXePEV8YGN63Yerhm_a1cQ; cookieconsent_status=deny; SessionGUID=ca489c81-8eff-43cb-ada5-919ef8fe1a0a; g_state={\"i_p\":1645526177239,\"i_l\":3}; _ga_YZNPNQ0F2S=GS1.1.1644921371.7.1.1644922693.0");
		request.addHeader("Sec-ch-ua","\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"98\", \"Google Chrome\";v=\"98\"");
		request.addHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
		request.addHeader("Sec-fetch-dest","document");
		request.addHeader("Sec-fetch-user","?1");
		request.addHeader("Connection","keep-alive");
		request.addHeader("Referer","https://www.codeproject.com/");
		request.addHeader("Host","www.codeproject.com");
		request.addHeader("Sec-fetch-site","same-origin");
		request.addHeader("Sec-ch-ua-platform","\"Windows\"");
		request.addHeader("Sec-fetch-mode","navigate");
		request.addHeader("User-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.82 Safari/537.36");
		request.addHeader("Accept-language","en-US,en;q=0.9");
		request.addHeader("Upgrade-insecure-requests","1");
		request.addHeader("Sec-ch-ua-mobile","?0");
		request.addHeader("Content-Type","application/octet-stream");
		var expectedResponseData = new String(this.getClass().getResourceAsStream("/org/kendar/Codeproject/row_8_res").readAllBytes());
		var expectedResponseCode = 200;
		var httpResponse = httpClient.execute(request);
		HttpEntity responseEntity = httpResponse.getEntity();
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		String contentType = responseEntity.getContentType().getValue();
		InputStream in = responseEntity.getContent();
		String result = IOUtils.toString(in, StandardCharsets.UTF_8);
	}

	private void d_9() throws IOException{
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		var request = new HttpPost("https://www.codeproject.com/script/Ratings/ajax/GetRatings.aspx");
		request.addHeader("Origin","https://www.codeproject.com");
		request.addHeader("Accept-encoding","gzip, deflate, br");
		request.addHeader("Cookie","mguid=5853a8f0-f0cf-4346-a8f2-29ac4b4e1480; vk=ad52b0e4-a53f-4da5-a440-f3bd660f3d29; _ga=GA1.1.896389723.1643017258; __gads=ID=e72e7344db5d49c2:T=1643017258:S=ALNI_MbdF8AQaXePEV8YGN63Yerhm_a1cQ; cookieconsent_status=deny; SessionGUID=ca489c81-8eff-43cb-ada5-919ef8fe1a0a; g_state={\"i_p\":1645526177239,\"i_l\":3}; _ga_YZNPNQ0F2S=GS1.1.1644921371.7.1.1644922696.0");
		request.addHeader("Sec-ch-ua","\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"98\", \"Google Chrome\";v=\"98\"");
		request.addHeader("Accept","*/*");
		request.addHeader("Sec-fetch-dest","empty");
		request.addHeader("Connection","keep-alive");
		request.addHeader("Referer","https://www.codeproject.com/");
		request.addHeader("Host","www.codeproject.com");
		request.addHeader("Sec-fetch-site","same-origin");
		request.addHeader("Sec-ch-ua-platform","\"Windows\"");
		request.addHeader("Sec-fetch-mode","cors");
		request.addHeader("User-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.82 Safari/537.36");
		request.addHeader("Content-type","application/x-www-form-urlencoded; charset=UTF-8");
		request.addHeader("Accept-language","en-US,en;q=0.9");
		request.addHeader("Content-length","16");
		request.addHeader("X-requested-with","XMLHttpRequest");
		request.addHeader("Sec-ch-ua-mobile","?0");
		var expectedResponseData = new String(this.getClass().getResourceAsStream("/org/kendar/Codeproject/row_9_res").readAllBytes());
		var expectedResponseCode = 200;
		var httpResponse = httpClient.execute(request);
		HttpEntity responseEntity = httpResponse.getEntity();
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		String contentType = responseEntity.getContentType().getValue();
		InputStream in = responseEntity.getContent();
		String result = IOUtils.toString(in, StandardCharsets.UTF_8);
	}

	private void d_10() throws IOException{
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		var request = new HttpGet("https://www.codeproject.com/");
		request.addHeader("Accept-encoding","gzip, deflate, br");
		request.addHeader("Cookie","mguid=5853a8f0-f0cf-4346-a8f2-29ac4b4e1480; vk=ad52b0e4-a53f-4da5-a440-f3bd660f3d29; _ga=GA1.1.896389723.1643017258; __gads=ID=e72e7344db5d49c2:T=1643017258:S=ALNI_MbdF8AQaXePEV8YGN63Yerhm_a1cQ; cookieconsent_status=deny; SessionGUID=ca489c81-8eff-43cb-ada5-919ef8fe1a0a; g_state={\"i_p\":1645526177239,\"i_l\":3}; _ga_YZNPNQ0F2S=GS1.1.1644921371.7.1.1644922696.0");
		request.addHeader("Sec-ch-ua","\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"98\", \"Google Chrome\";v=\"98\"");
		request.addHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
		request.addHeader("Sec-fetch-dest","document");
		request.addHeader("Sec-fetch-user","?1");
		request.addHeader("Connection","keep-alive");
		request.addHeader("Referer","https://www.codeproject.com/");
		request.addHeader("Host","www.codeproject.com");
		request.addHeader("Sec-fetch-site","same-origin");
		request.addHeader("Sec-ch-ua-platform","\"Windows\"");
		request.addHeader("Sec-fetch-mode","navigate");
		request.addHeader("User-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.82 Safari/537.36");
		request.addHeader("Accept-language","en-US,en;q=0.9");
		request.addHeader("Upgrade-insecure-requests","1");
		request.addHeader("Sec-ch-ua-mobile","?0");
		request.addHeader("Content-Type","application/octet-stream");
		var expectedResponseData = new String(this.getClass().getResourceAsStream("/org/kendar/Codeproject/row_10_res").readAllBytes());
		var expectedResponseCode = 200;
		var httpResponse = httpClient.execute(request);
		HttpEntity responseEntity = httpResponse.getEntity();
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		String contentType = responseEntity.getContentType().getValue();
		InputStream in = responseEntity.getContent();
		String result = IOUtils.toString(in, StandardCharsets.UTF_8);
	}

	private void d_11() throws IOException{
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		var request = new HttpPost("https://www.codeproject.com/script/Common/Webservices/CommonServices.aspx/GetPageViewsTimeSeriesData");
		request.addHeader("Origin","https://www.codeproject.com");
		request.addHeader("Accept-encoding","gzip, deflate, br");
		request.addHeader("Cookie","mguid=5853a8f0-f0cf-4346-a8f2-29ac4b4e1480; vk=ad52b0e4-a53f-4da5-a440-f3bd660f3d29; _ga=GA1.1.896389723.1643017258; __gads=ID=e72e7344db5d49c2:T=1643017258:S=ALNI_MbdF8AQaXePEV8YGN63Yerhm_a1cQ; cookieconsent_status=deny; SessionGUID=ca489c81-8eff-43cb-ada5-919ef8fe1a0a; g_state={\"i_p\":1645526177239,\"i_l\":3}; _ga_YZNPNQ0F2S=GS1.1.1644921371.7.1.1644922697.0");
		request.addHeader("Sec-ch-ua","\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"98\", \"Google Chrome\";v=\"98\"");
		request.addHeader("Accept","text/plain, */*; q=0.01");
		request.addHeader("Sec-fetch-dest","empty");
		request.addHeader("Connection","keep-alive");
		request.addHeader("Referer","https://www.codeproject.com/");
		request.addHeader("Host","www.codeproject.com");
		request.addHeader("Sec-fetch-site","same-origin");
		request.addHeader("Sec-ch-ua-platform","\"Windows\"");
		request.addHeader("Sec-fetch-mode","cors");
		request.addHeader("User-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.82 Safari/537.36");
		request.addHeader("Content-type","application/json");
		request.addHeader("Accept-language","en-US,en;q=0.9");
		request.addHeader("Content-length","32");
		request.addHeader("X-requested-with","XMLHttpRequest");
		request.addHeader("Sec-ch-ua-mobile","?0");
		var data = new String(this.getClass().getResourceAsStream("/org/kendar/Codeproject/row_11_req").readAllBytes());
		HttpEntity entity = new StringEntity(data, ContentType.create("application/json"));
		((HttpEntityEnclosingRequestBase) request).setEntity(entity);
		var expectedResponseData = new String(this.getClass().getResourceAsStream("/org/kendar/Codeproject/row_11_res").readAllBytes());
		var expectedResponseCode = 200;
		var httpResponse = httpClient.execute(request);
		HttpEntity responseEntity = httpResponse.getEntity();
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		String contentType = responseEntity.getContentType().getValue();
		InputStream in = responseEntity.getContent();
		String result = IOUtils.toString(in, StandardCharsets.UTF_8);
	}

	private void d_12() throws IOException{
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		var request = new HttpPost("https://www.codeproject.com/script/Common/Webservices/CommonServices.aspx/GetPageViewsTimeSeriesData");
		request.addHeader("Origin","https://www.codeproject.com");
		request.addHeader("Accept-encoding","gzip, deflate, br");
		request.addHeader("Cookie","mguid=5853a8f0-f0cf-4346-a8f2-29ac4b4e1480; vk=ad52b0e4-a53f-4da5-a440-f3bd660f3d29; _ga=GA1.1.896389723.1643017258; __gads=ID=e72e7344db5d49c2:T=1643017258:S=ALNI_MbdF8AQaXePEV8YGN63Yerhm_a1cQ; cookieconsent_status=deny; SessionGUID=ca489c81-8eff-43cb-ada5-919ef8fe1a0a; g_state={\"i_p\":1645526177239,\"i_l\":3}; _ga_YZNPNQ0F2S=GS1.1.1644921371.7.1.1644922697.0");
		request.addHeader("Sec-ch-ua","\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"98\", \"Google Chrome\";v=\"98\"");
		request.addHeader("Accept","text/plain, */*; q=0.01");
		request.addHeader("Sec-fetch-dest","empty");
		request.addHeader("Connection","keep-alive");
		request.addHeader("Referer","https://www.codeproject.com/");
		request.addHeader("Host","www.codeproject.com");
		request.addHeader("Sec-fetch-site","same-origin");
		request.addHeader("Sec-ch-ua-platform","\"Windows\"");
		request.addHeader("Sec-fetch-mode","cors");
		request.addHeader("User-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.82 Safari/537.36");
		request.addHeader("Content-type","application/json");
		request.addHeader("Accept-language","en-US,en;q=0.9");
		request.addHeader("Content-length","32");
		request.addHeader("X-requested-with","XMLHttpRequest");
		request.addHeader("Sec-ch-ua-mobile","?0");
		var data = new String(this.getClass().getResourceAsStream("/org/kendar/Codeproject/row_12_req").readAllBytes());
		HttpEntity entity = new StringEntity(data, ContentType.create("application/json"));
		((HttpEntityEnclosingRequestBase) request).setEntity(entity);
		var expectedResponseData = new String(this.getClass().getResourceAsStream("/org/kendar/Codeproject/row_12_res").readAllBytes());
		var expectedResponseCode = 200;
		var httpResponse = httpClient.execute(request);
		HttpEntity responseEntity = httpResponse.getEntity();
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		String contentType = responseEntity.getContentType().getValue();
		InputStream in = responseEntity.getContent();
		String result = IOUtils.toString(in, StandardCharsets.UTF_8);
	}

	private void d_13() throws IOException{
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		var request = new HttpGet("https://www.codeproject.com/Articles/5286393/Cplusplus-Windows-Toast-Notification");
		request.addHeader("Accept-encoding","gzip, deflate, br");
		request.addHeader("Cookie","mguid=5853a8f0-f0cf-4346-a8f2-29ac4b4e1480; vk=ad52b0e4-a53f-4da5-a440-f3bd660f3d29; _ga=GA1.1.896389723.1643017258; __gads=ID=e72e7344db5d49c2:T=1643017258:S=ALNI_MbdF8AQaXePEV8YGN63Yerhm_a1cQ; cookieconsent_status=deny; SessionGUID=ca489c81-8eff-43cb-ada5-919ef8fe1a0a; g_state={\"i_p\":1645526177239,\"i_l\":3}; _ga_YZNPNQ0F2S=GS1.1.1644921371.7.1.1644922697.0");
		request.addHeader("Sec-ch-ua","\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"98\", \"Google Chrome\";v=\"98\"");
		request.addHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
		request.addHeader("Sec-fetch-dest","document");
		request.addHeader("Sec-fetch-user","?1");
		request.addHeader("Connection","keep-alive");
		request.addHeader("Referer","https://www.codeproject.com/");
		request.addHeader("Host","www.codeproject.com");
		request.addHeader("Sec-fetch-site","same-origin");
		request.addHeader("Sec-ch-ua-platform","\"Windows\"");
		request.addHeader("Sec-fetch-mode","navigate");
		request.addHeader("User-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.82 Safari/537.36");
		request.addHeader("Accept-language","en-US,en;q=0.9");
		request.addHeader("Upgrade-insecure-requests","1");
		request.addHeader("Sec-ch-ua-mobile","?0");
		request.addHeader("Content-Type","application/octet-stream");
		var expectedResponseData = new String(this.getClass().getResourceAsStream("/org/kendar/Codeproject/row_13_res").readAllBytes());
		var expectedResponseCode = 200;
		var httpResponse = httpClient.execute(request);
		HttpEntity responseEntity = httpResponse.getEntity();
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		String contentType = responseEntity.getContentType().getValue();
		InputStream in = responseEntity.getContent();
		String result = IOUtils.toString(in, StandardCharsets.UTF_8);
	}

	private void d_14() throws IOException{
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		var request = new HttpPost("https://www.codeproject.com/script/Ratings/ajax/GetRatings.aspx");
		request.addHeader("Origin","https://www.codeproject.com");
		request.addHeader("Accept-encoding","gzip, deflate, br");
		request.addHeader("Cookie","mguid=5853a8f0-f0cf-4346-a8f2-29ac4b4e1480; vk=ad52b0e4-a53f-4da5-a440-f3bd660f3d29; _ga=GA1.1.896389723.1643017258; __gads=ID=e72e7344db5d49c2:T=1643017258:S=ALNI_MbdF8AQaXePEV8YGN63Yerhm_a1cQ; cookieconsent_status=deny; SessionGUID=ca489c81-8eff-43cb-ada5-919ef8fe1a0a; g_state={\"i_p\":1645526177239,\"i_l\":3}; _ga_YZNPNQ0F2S=GS1.1.1644921371.7.1.1644922700.0");
		request.addHeader("Sec-ch-ua","\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"98\", \"Google Chrome\";v=\"98\"");
		request.addHeader("Accept","*/*");
		request.addHeader("Sec-fetch-dest","empty");
		request.addHeader("Connection","keep-alive");
		request.addHeader("Referer","https://www.codeproject.com/");
		request.addHeader("Host","www.codeproject.com");
		request.addHeader("Sec-fetch-site","same-origin");
		request.addHeader("Sec-ch-ua-platform","\"Windows\"");
		request.addHeader("Sec-fetch-mode","cors");
		request.addHeader("User-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.82 Safari/537.36");
		request.addHeader("Content-type","application/x-www-form-urlencoded; charset=UTF-8");
		request.addHeader("Accept-language","en-US,en;q=0.9");
		request.addHeader("Content-length","16");
		request.addHeader("X-requested-with","XMLHttpRequest");
		request.addHeader("Sec-ch-ua-mobile","?0");
		var expectedResponseData = new String(this.getClass().getResourceAsStream("/org/kendar/Codeproject/row_14_res").readAllBytes());
		var expectedResponseCode = 200;
		var httpResponse = httpClient.execute(request);
		HttpEntity responseEntity = httpResponse.getEntity();
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		String contentType = responseEntity.getContentType().getValue();
		InputStream in = responseEntity.getContent();
		String result = IOUtils.toString(in, StandardCharsets.UTF_8);
	}

	private void d_15() throws IOException{
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		var request = new HttpPost("https://www.codeproject.com/script/ratings/ajax/GetRatings.aspx");
		request.addHeader("Origin","https://www.codeproject.com");
		request.addHeader("Accept-encoding","gzip, deflate, br");
		request.addHeader("Cookie","mguid=5853a8f0-f0cf-4346-a8f2-29ac4b4e1480; vk=ad52b0e4-a53f-4da5-a440-f3bd660f3d29; _ga=GA1.1.896389723.1643017258; __gads=ID=e72e7344db5d49c2:T=1643017258:S=ALNI_MbdF8AQaXePEV8YGN63Yerhm_a1cQ; cookieconsent_status=deny; SessionGUID=ca489c81-8eff-43cb-ada5-919ef8fe1a0a; g_state={\"i_p\":1645526177239,\"i_l\":3}; _ga_YZNPNQ0F2S=GS1.1.1644921371.7.1.1644922700.0");
		request.addHeader("Sec-ch-ua","\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"98\", \"Google Chrome\";v=\"98\"");
		request.addHeader("Accept","*/*");
		request.addHeader("Sec-fetch-dest","empty");
		request.addHeader("Connection","keep-alive");
		request.addHeader("Referer","https://www.codeproject.com/");
		request.addHeader("Host","www.codeproject.com");
		request.addHeader("Sec-fetch-site","same-origin");
		request.addHeader("Sec-ch-ua-platform","\"Windows\"");
		request.addHeader("Sec-fetch-mode","cors");
		request.addHeader("User-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.82 Safari/537.36");
		request.addHeader("Content-type","application/x-www-form-urlencoded; charset=UTF-8");
		request.addHeader("Accept-language","en-US,en;q=0.9");
		request.addHeader("Content-length","256");
		request.addHeader("X-requested-with","XMLHttpRequest");
		request.addHeader("Sec-ch-ua-mobile","?0");
		var expectedResponseData = new String(this.getClass().getResourceAsStream("/org/kendar/Codeproject/row_15_res").readAllBytes());
		var expectedResponseCode = 200;
		var httpResponse = httpClient.execute(request);
		HttpEntity responseEntity = httpResponse.getEntity();
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		String contentType = responseEntity.getContentType().getValue();
		InputStream in = responseEntity.getContent();
		String result = IOUtils.toString(in, StandardCharsets.UTF_8);
	}

	private void d_16() throws IOException{
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		var request = new HttpPost("https://www.codeproject.com/script/Forums/WebServices/ForumSubscriptionServices.aspx/GetSubscriptions");
		request.addHeader("Origin","https://www.codeproject.com");
		request.addHeader("Accept-encoding","gzip, deflate, br");
		request.addHeader("Cookie","mguid=5853a8f0-f0cf-4346-a8f2-29ac4b4e1480; vk=ad52b0e4-a53f-4da5-a440-f3bd660f3d29; _ga=GA1.1.896389723.1643017258; __gads=ID=e72e7344db5d49c2:T=1643017258:S=ALNI_MbdF8AQaXePEV8YGN63Yerhm_a1cQ; cookieconsent_status=deny; SessionGUID=ca489c81-8eff-43cb-ada5-919ef8fe1a0a; g_state={\"i_p\":1645526177239,\"i_l\":3}; _ga_YZNPNQ0F2S=GS1.1.1644921371.7.1.1644922700.0");
		request.addHeader("Sec-ch-ua","\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"98\", \"Google Chrome\";v=\"98\"");
		request.addHeader("Accept","application/json, text/javascript, */*; q=0.01");
		request.addHeader("Sec-fetch-dest","empty");
		request.addHeader("Connection","keep-alive");
		request.addHeader("Referer","https://www.codeproject.com/");
		request.addHeader("Host","www.codeproject.com");
		request.addHeader("Sec-fetch-site","same-origin");
		request.addHeader("Sec-ch-ua-platform","\"Windows\"");
		request.addHeader("Sec-fetch-mode","cors");
		request.addHeader("User-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.82 Safari/537.36");
		request.addHeader("Content-type","application/json; charset=UTF-8");
		request.addHeader("Accept-language","en-US,en;q=0.9");
		request.addHeader("Content-length","19");
		request.addHeader("X-requested-with","XMLHttpRequest");
		request.addHeader("Sec-ch-ua-mobile","?0");
		var data = new String(this.getClass().getResourceAsStream("/org/kendar/Codeproject/row_16_req").readAllBytes());
		HttpEntity entity = new StringEntity(data, ContentType.create("application/json"));
		((HttpEntityEnclosingRequestBase) request).setEntity(entity);
		var expectedResponseData = new String(this.getClass().getResourceAsStream("/org/kendar/Codeproject/row_16_res").readAllBytes());
		var expectedResponseCode = 200;
		var httpResponse = httpClient.execute(request);
		HttpEntity responseEntity = httpResponse.getEntity();
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		String contentType = responseEntity.getContentType().getValue();
		InputStream in = responseEntity.getContent();
		String result = IOUtils.toString(in, StandardCharsets.UTF_8);
	}

	private void d_17() throws IOException{
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		var request = new HttpGet("https://www.codeproject.com/script/Articles/Statistics.aspx?aid=5286393");
		request.addHeader("Accept-encoding","gzip, deflate, br");
		request.addHeader("Cookie","mguid=5853a8f0-f0cf-4346-a8f2-29ac4b4e1480; vk=ad52b0e4-a53f-4da5-a440-f3bd660f3d29; _ga=GA1.1.896389723.1643017258; __gads=ID=e72e7344db5d49c2:T=1643017258:S=ALNI_MbdF8AQaXePEV8YGN63Yerhm_a1cQ; cookieconsent_status=deny; SessionGUID=ca489c81-8eff-43cb-ada5-919ef8fe1a0a; g_state={\"i_p\":1645526177239,\"i_l\":3}; _ga_YZNPNQ0F2S=GS1.1.1644921371.7.1.1644922700.0");
		request.addHeader("Sec-ch-ua","\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"98\", \"Google Chrome\";v=\"98\"");
		request.addHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
		request.addHeader("Sec-fetch-dest","document");
		request.addHeader("Sec-fetch-user","?1");
		request.addHeader("Connection","keep-alive");
		request.addHeader("Referer","https://www.codeproject.com/");
		request.addHeader("Host","www.codeproject.com");
		request.addHeader("Sec-fetch-site","same-origin");
		request.addHeader("Sec-ch-ua-platform","\"Windows\"");
		request.addHeader("Sec-fetch-mode","navigate");
		request.addHeader("User-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.82 Safari/537.36");
		request.addHeader("Accept-language","en-US,en;q=0.9");
		request.addHeader("Upgrade-insecure-requests","1");
		request.addHeader("Sec-ch-ua-mobile","?0");
		request.addHeader("Content-Type","application/octet-stream");
		var expectedResponseData = new String(this.getClass().getResourceAsStream("/org/kendar/Codeproject/row_17_res").readAllBytes());
		var expectedResponseCode = 200;
		var httpResponse = httpClient.execute(request);
		HttpEntity responseEntity = httpResponse.getEntity();
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		String contentType = responseEntity.getContentType().getValue();
		InputStream in = responseEntity.getContent();
		String result = IOUtils.toString(in, StandardCharsets.UTF_8);
	}

	private void d_18() throws IOException{
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		var request = new HttpPost("https://www.codeproject.com/script/Ratings/ajax/GetRatings.aspx");
		request.addHeader("Origin","https://www.codeproject.com");
		request.addHeader("Accept-encoding","gzip, deflate, br");
		request.addHeader("Cookie","mguid=5853a8f0-f0cf-4346-a8f2-29ac4b4e1480; vk=ad52b0e4-a53f-4da5-a440-f3bd660f3d29; _ga=GA1.1.896389723.1643017258; __gads=ID=e72e7344db5d49c2:T=1643017258:S=ALNI_MbdF8AQaXePEV8YGN63Yerhm_a1cQ; cookieconsent_status=deny; SessionGUID=ca489c81-8eff-43cb-ada5-919ef8fe1a0a; g_state={\"i_p\":1645526177239,\"i_l\":3}; _ga_YZNPNQ0F2S=GS1.1.1644921371.7.1.1644922703.0");
		request.addHeader("Sec-ch-ua","\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"98\", \"Google Chrome\";v=\"98\"");
		request.addHeader("Accept","*/*");
		request.addHeader("Sec-fetch-dest","empty");
		request.addHeader("Connection","keep-alive");
		request.addHeader("Referer","https://www.codeproject.com/");
		request.addHeader("Host","www.codeproject.com");
		request.addHeader("Sec-fetch-site","same-origin");
		request.addHeader("Sec-ch-ua-platform","\"Windows\"");
		request.addHeader("Sec-fetch-mode","cors");
		request.addHeader("User-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.82 Safari/537.36");
		request.addHeader("Content-type","application/x-www-form-urlencoded; charset=UTF-8");
		request.addHeader("Accept-language","en-US,en;q=0.9");
		request.addHeader("Content-length","16");
		request.addHeader("X-requested-with","XMLHttpRequest");
		request.addHeader("Sec-ch-ua-mobile","?0");
		var expectedResponseData = new String(this.getClass().getResourceAsStream("/org/kendar/Codeproject/row_18_res").readAllBytes());
		var expectedResponseCode = 200;
		var httpResponse = httpClient.execute(request);
		HttpEntity responseEntity = httpResponse.getEntity();
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		String contentType = responseEntity.getContentType().getValue();
		InputStream in = responseEntity.getContent();
		String result = IOUtils.toString(in, StandardCharsets.UTF_8);
	}

}