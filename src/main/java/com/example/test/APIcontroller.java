package com.example.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


@RestController
public class APIcontroller {

    // dependency injection
    @Autowired
    private WebSiteRetriever webSiteRetriever;

    private List<String> websiteUrls = Arrays.asList("https://www.result.si/projekti/", "https://www.result.si/o-nas/",
            "https://www.result.si/kariera/", "https://www.result.si/blog/");

    private int successfulCalls;
    private int unsuccessfulCalls;

    // konstruktor
    public APIcontroller() {
        successfulCalls = unsuccessfulCalls = 0;

    }

    @GetMapping("/podatki")
    public WebsitesRetrievalResultWrapper getData(@RequestParam(required = true) int numberOfWebsites)
            throws IOException {

        // TODO: handle exception

        // PRIMER - 2 theada sočasno: http://localhost:8080/podatki?numberOfWebsites=2
        // Število 2 pove koliko sočasnih klicev se bo izvajalo naenkrat
        // Naredili smo custom pool, ki bo izvajal taske
        ExecutorService executor = Executors.newFixedThreadPool(numberOfWebsites);

        // Pomoč - Viri:
        // https://stackoverflow.com/questions/22129471/what-is-the-best-most-elegant-way-to-limit-the-number-of-concurrent-evaluation
        // https://stackoverflow.com/questions/36569775/how-to-set-forkjoinpool-with-the-desired-number-of-worker-threads-in-completable
        // https://www.callicoder.com/java-8-completablefuture-tutorial/
        List<WebsiteRetrievalResult> websitesWithTitlesList = websiteUrls.stream()
                .map(website -> CompletableFuture.supplyAsync(() -> {

                    System.out.println("processing " + website);
                    // retrieval spletnih strani paralelno
                    // servis WebSiteRetriever poskusi retriavat posamezno spletno stran
                    // {"httpStatusCode":200,"websiteData":"RESULT – Izzivi in rešitve"}
                    var scrapedWebsiteResult = webSiteRetriever.getWebSiteContent(website);

                    return scrapedWebsiteResult;

                }, executor)).collect(Collectors.toList()) // force-submita
                                                           // List<CompletableFuture<CompletableFuture<WebsiteRetrievalResult>>
                .stream().map(CompletableFuture::join) // počaka na posamezen
                                                       // List<<CompletableFuture<WebsiteRetrievalResult>>
                .collect(Collectors.toList()).stream().map(CompletableFuture::join) // počaka na posamezen
                                                                                    // List<WebsiteRetrievalResult>
                .collect(Collectors.toList());

        for (WebsiteRetrievalResult result : websitesWithTitlesList) {

            if (result.isSuccessful()) {
                successfulCalls++;

            } else {
                unsuccessfulCalls++;
            }
        }

        WebsitesRetrievalResultWrapper resultWrapper = new WebsitesRetrievalResultWrapper(websitesWithTitlesList, successfulCalls, unsuccessfulCalls);

        return resultWrapper;

    }
}
