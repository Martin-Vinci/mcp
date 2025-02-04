package com.greybox.mediums.inter_switch.utils;

import com.greybox.mediums.utils.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Map;


public class HttpUtil {
    public static String postHTTPRequest(String resourceUrl, Map<String, String> headers, String data) throws Exception {
        StringBuilder responseString = new StringBuilder();
        StringBuilder logBuilder = new StringBuilder();
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        int responseCode = 0;

        try {
            // Log and store outgoing request details
            logBuilder.append("Inter-switch Outgoing HTTP Request: \n")
                    .append("URL: ").append(resourceUrl).append("\n")
                    .append("Request Body: ").append(data).append("\n")
                    .append("Headers: ").append(headers.toString()).append("\n");

            client = HttpClients.custom()
                    .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
            HttpPost httpPost = new HttpPost(resourceUrl);

            StringEntity entity = new StringEntity(data);
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // Set headers
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }

            // Execute request and capture the response
            response = client.execute(httpPost);
            HttpEntity httpEntity = response.getEntity();
            responseCode = response.getStatusLine().getStatusCode();

            // Read response content
            if (httpEntity != null) {
                InputStream inputStream = httpEntity.getContent();
                int c;
                while ((c = inputStream.read()) != -1) {
                    responseString.append((char) c);
                }
            }

        } finally {
            if (client != null) {
                client.close();  // Ensure client is closed in case of exceptions
            }

            // Log and store response details in the logBuilder
            logBuilder.append("Response Code: ").append(responseCode).append("\n")
                    .append("Response Body: ").append(responseString.toString());

            // Log everything in one go
            Logger.logInfo(logBuilder.toString());

            // Ensure the response is also closed
            if (response != null) {
                response.close();
            }
        }

        return responseString.toString();
    }


    public static String getHTTPRequest(String resourceUrl, Map<String, String> headers) throws Exception {


        Logger.logInfo("http outgoing phoenix request url {} " + resourceUrl);

        CloseableHttpClient client = HttpClients
                .custom()
                .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();
        HttpGet httpGet = new HttpGet(resourceUrl);
        httpGet.setHeader("Accept", "application/json");
        httpGet.setHeader("Content-type", "application/json");
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            httpGet.setHeader(key, value);
        }
        CloseableHttpResponse response = client.execute(httpGet);
        HttpEntity httpEntity = response.getEntity();
        StringBuilder resposeString = new StringBuilder();
        int responseCode = response.getStatusLine().getStatusCode();

        if (httpEntity != null) {
            InputStream inputStream = httpEntity.getContent();
            int c;
            while ((c = inputStream.read()) != -1) {
                resposeString.append((char) c);
            }
        }
        client.close();
        Logger.logInfo("http response code {} " + responseCode);
        Logger.logInfo("http phoenix response body {} " + resposeString);
        return resposeString.toString();
    }

}
