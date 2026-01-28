package com.gutotech.loteriasapi.util;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

public class SSLHelper {

    static public Connection getConnection(String url) {
        return Jsoup.connect(url)
                // Core browser identification
                .userAgent(
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/124.0.0.0 Safari/537.36"
                )
                .referrer("https://loterias.caixa.gov.br/")
                
                // Accept headers - critical for API detection
                .header("Accept", "application/json, text/plain, */*")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Accept-Language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7")
                
                // Security headers - required by cloud WAF/bot detection
                .header("Sec-Fetch-Dest", "empty")
                .header("Sec-Fetch-Mode", "cors")
                .header("Sec-Fetch-Site", "cross-site")
                .header("Origin", "https://loterias.caixa.gov.br")
                
                // Client hints for browser identification
                .header("Sec-Ch-Ua", "\"Not A(Brand\";v=\"99\", \"Google Chrome\";v=\"124\", \"Chromium\";v=\"124\"")
                .header("Sec-Ch-Ua-Mobile", "?0")
                .header("Sec-Ch-Ua-Platform", "\"Windows\"")
                
                // Cache and connection control
                .header("Cache-Control", "no-cache")
                .header("Pragma", "no-cache")
                .header("Connection", "keep-alive")
                .header("TE", "trailers")
                
                // Additional headers for cloud/proxy bypass attempts
                .header("DNT", "1") // Do Not Track
                .header("Upgrade-Insecure-Requests", "1")
                
                // Timeout and parsing configuration
                .timeout(25000)
                .followRedirects(true)
                .maxBodySize(0) // Allow large responses
                .ignoreContentType(true)
                .ignoreHttpErrors(true) // <- importante
                .sslSocketFactory(SSLHelper.socketFactory());
    }

    static private SSLSocketFactory socketFactory() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};

        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Failed to create a SSL socket factory", e);
        }
    }
}
