package com.gutotech.loteriasapi.consumer;

import com.gutotech.loteriasapi.util.SSLHelper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Default implementation of HttpConnectionService using SSLHelper with retry logic
 */
@Service
public class SSLHttpConnectionService implements HttpConnectionService {

    private static final Logger logger = LoggerFactory.getLogger(SSLHttpConnectionService.class);
    
    @Value("${caixa.api.retry.max-attempts:3}")
    private int maxRetries;
    
    @Value("${caixa.api.retry.initial-delay-ms:1000}")
    private long initialDelayMs;

    @Override
    public Document get(String url) throws IOException {
        IOException lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return executeRequest(url, attempt);
            } catch (IOException e) {
                lastException = e;
                
                if (attempt < maxRetries) {
                    long delayMs = calculateBackoff(attempt);
                    logger.warn("Attempt {}/{} failed for {}. Retrying in {}ms", 
                        attempt, maxRetries, url, delayMs);
                    
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Retry interrupted", ie);
                    }
                } else {
                    logger.error("All {} retry attempts failed for {}", maxRetries, url, e);
                }
            }
        }
        
        throw lastException;
    }
    
    private Document executeRequest(String url, int attemptNumber) throws IOException {
        Connection.Response r = SSLHelper.getConnection(url).execute();

        String responseBody = r.body() != null ? r.body() : "";
        int statusCode = r.statusCode();
        
        logger.info("[Attempt {}/{}] CAIXA Response: status={} url={} bodyLength={}", 
            attemptNumber, maxRetries, statusCode, url, responseBody.length());
        
        // Log response headers for debugging (debug level)
        logger.debug("Response headers: {}", r.headers());
        
        // Check for WAF/bot blocking (403 with HTML)
        if (statusCode == 403) {
            String errorPreview = responseBody.substring(0, Math.min(responseBody.length(), 150));
            if (responseBody.contains("<!DOCTYPE") || responseBody.contains("Forbidden") || 
                responseBody.contains("Your IP")) {
                logger.warn("IP Blocking detected (403) from CAIXA for {}. This is cloud/IP-based blocking.", url);
                throw new IOException("HTTP 403 - IP Blocking by CAIXA WAF. Error: " + errorPreview);
            }
        }
        
        // Detect other non-JSON responses (HTML, errors, etc)
        if (statusCode >= 400) {
            logger.warn("HTTP Error {} for {}: {}", statusCode, url, 
                responseBody.substring(0, Math.min(responseBody.length(), 200)));
            throw new IOException("HTTP " + statusCode + " for " + url);
        }
        
        if (!responseBody.trim().isEmpty() && !responseBody.trim().startsWith("{")) {
            logger.warn("Non-JSON response for {}: {}", url, 
                responseBody.substring(0, Math.min(responseBody.length(), 200)));
            throw new IOException("Non-JSON response from " + url);
        }

        return Jsoup.parse(responseBody);
    }
    
    private long calculateBackoff(int attemptNumber) {
        // Exponential backoff: 1s, 2s, 4s, etc. with some jitter
        long delay = initialDelayMs * (long) Math.pow(2, attemptNumber - 1);
        long jitter = (long) (Math.random() * 1000); // 0-1s random
        return delay + jitter;
    }
}

