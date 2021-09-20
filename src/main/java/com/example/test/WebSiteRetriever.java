package com.example.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import java.util.concurrent.CompletableFuture;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WebSiteRetriever {

    @Autowired
    private RestTemplate restTemplate;

    Logger logger = LoggerFactory.getLogger(WebSiteRetriever.class);

    @Async
    public CompletableFuture<WebsiteRetrievalResult> getWebSiteContent(String webSiteURL)
    // throws InterruptedException
    {
        // vrnemo error response, če pride do napake
        WebsiteRetrievalResult webSiteSummary = new WebsiteRetrievalResult(500, "An error has occurred");
        try {
            logger.info("Calling: getWebSiteContent for webSite URL {} with thread {}", webSiteURL,
                    Thread.currentThread().getName());

            // naredi GET HTTP request na določen url - webSiteURL in dobi http response kot
            // return
            HttpEntity<String> response = restTemplate.exchange(webSiteURL, HttpMethod.GET, null, String.class);
 
            String resultString = response.getBody();

            HttpHeaders headers = response.getHeaders();
            int statusCode = ((ResponseEntity<String>) response).getStatusCode().value();
            System.out.println(statusCode);
            System.out.println("Headers: " + headers);

            Document doc = Jsoup.parse(resultString);
            doc.title();

            webSiteSummary = new WebsiteRetrievalResult(statusCode, doc.title());
            return CompletableFuture.completedFuture(webSiteSummary);

        } catch (Exception ex) {

            logger.error("An error has occurred while calling getWebSiteContent", ex);
        }
        logger.info("Execution completed: getWebSiteContent for webSite URL {} with thread {}", webSiteURL,
                Thread.currentThread().getName());

        return CompletableFuture.completedFuture(webSiteSummary);
    }

}