package com.github.karma.trino;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


@Slf4j
public class TrinoPoolingHttpClientUtil {

    /**
     * get请求
     * @param url
     * @param headers
     * @return
     */
    public static String get(String url, Map<String, String> headers) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        setHeaders(httpGet, headers);

        CloseableHttpResponse response = null;
        HttpEntity entity = null;
        String result = null;
        try {
            response = getHttpClient().execute(httpGet);
            log.info("GET {}", url);
            log.info(String.valueOf(response.getStatusLine().getStatusCode()));
            entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            }
        } finally {
            EntityUtils.consume(entity);
            IOUtils.closeQuietly(response);
        }
        return result;
    }

    /**
     * deleet请求
     * @param url
     * @param headers
     * @return
     */
    public static String delete(String url, Map<String, String> headers) throws IOException {
        HttpDelete httpDelete = new HttpDelete(url);
        setHeaders(httpDelete, headers);

        CloseableHttpResponse response = null;
        HttpEntity entity = null;
        String result = null;
        try {
            response = getHttpClient().execute(httpDelete);
            log.info("DELETE {}", url);
            log.info(String.valueOf(response.getStatusLine().getStatusCode()));
            entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            }
        } finally {
            EntityUtils.consume(entity);
            IOUtils.closeQuietly(response);
        }
        return result;
    }

    /**
     * post请求
     * @param url
     * @param headers
     * @param sql
     * @return
     * @throws IOException
     */
    public static String post(String url, Map<String, String> headers, String sql) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        setHeaders(httpPost, headers);
        setHttpPostParams(httpPost, sql);

        CloseableHttpResponse response = null;
        HttpEntity entity = null;
        String result = null;
        try {
            response = getHttpClient().execute(httpPost);
            log.info("POST {}", url);
            log.info(String.valueOf(response.getStatusLine().getStatusCode()));
            entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            }
        } finally {
            EntityUtils.consume(entity);
            IOUtils.closeQuietly(response);
        }
        return result;
    }

    /**
     * 设置 HttpPost 请求参数
     * @param httpPost
     * @param sql
     */
    private static void setHttpPostParams(HttpPost httpPost, String sql) {
        if (StringUtils.isBlank(sql)) {
            return;
        }
        httpPost.setEntity(new StringEntity(sql, Consts.UTF_8));
    }

    /**
     * 设置请求头
     * @param httpRequestBase
     * @param headers
     */
    private static void setHeaders(HttpRequestBase httpRequestBase, Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return;
        }
        headers.forEach(httpRequestBase::addHeader);
    }

    private static CloseableHttpClient getHttpClient() {
        return HttpClientHolder.httpClient;
    }

    private static final class HttpClientHolder {
        static final CloseableHttpClient httpClient = createHttpClient();
    }

    @SuppressWarnings("all")
    private static CloseableHttpClient createHttpClient() {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };
        SSLConnectionSocketFactory csf;
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            csf = new SSLConnectionSocketFactory(
                    sslContext,
                    SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
            );
        }catch (Exception ignored){
            return null;
        }

        Registry<ConnectionSocketFactory> registry = RegistryBuilder
                .<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", csf)
                .build();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
        cm.setMaxTotal(200);
        cm.setDefaultMaxPerRoute(50);

        RequestConfig requestConfig = RequestConfig
                .custom()
                .setConnectionRequestTimeout(20 * 1000)     // 20秒
                .setConnectTimeout(10 * 1000)               // 10秒
                .setSocketTimeout(10 * 1000)                // 10秒
                .build();

        return HttpClients.custom().setConnectionManager(cm).setDefaultRequestConfig(requestConfig).build();
    }

}
