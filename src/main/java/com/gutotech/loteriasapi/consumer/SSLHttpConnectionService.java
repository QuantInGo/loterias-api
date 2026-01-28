package com.gutotech.loteriasapi.consumer;

import com.gutotech.loteriasapi.util.SSLHelper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Default implementation of HttpConnectionService using SSLHelper
 */
@Service
public class SSLHttpConnectionService implements HttpConnectionService {

    @Override
    public Document get(String url) throws IOException {
        Connection.Response r = SSLHelper.getConnection(url).execute();

        System.out.println(
            "CAIXA " + r.statusCode() + " for " + url +
            " | len=" + (r.body() != null ? r.body().length() : 0)
        );

        return Jsoup.parse(r.body());
    }
}

