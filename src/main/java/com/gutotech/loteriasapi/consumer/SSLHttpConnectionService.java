package com.gutotech.loteriasapi.consumer;

import com.gutotech.loteriasapi.util.SSLHelper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Default implementation of HttpConnectionService using SSLHelper
 */
@Service
public class SSLHttpConnectionService implements HttpConnectionService {

    private static final Logger logger = LoggerFactory.getLogger(SSLHttpConnectionService.class);

    @Override
    public Document get(String url) throws IOException {
        Connection.Response r = SSLHelper.getConnection(url).execute();

        String responseBody = r.body() != null ? r.body() : "";
        int statusCode = r.statusCode();
        
        logger.info("CAIXA Response: status={} url={} bodyLength={}", statusCode, url, responseBody.length());
        
        // Log response headers for debugging
        logger.debug("Response headers: {}", r.headers());
        
        // Detect non-JSON responses (HTML, 403, etc)
        if (statusCode >= 400) {
            logger.warn("HTTP Error {} for {}: {}", statusCode, url, 
                responseBody.substring(0, Math.min(responseBody.length(), 200)));
        }
        
        if (!responseBody.trim().isEmpty() && !responseBody.trim().startsWith("{")) {
            logger.warn("Non-JSON response for {}: {}", url, 
                responseBody.substring(0, Math.min(responseBody.length(), 200)));
        }

        return Jsoup.parse(responseBody);
    }
}

