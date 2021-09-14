# RESULT---RESTful-API-WebsiteRetriever
This is a simple program written in Java. I also used the Spring framework for easy http requests and the RESTful API http response. The program also gets an integer through a query parameter and uses it as a variable that declares multiple concurrent threads, which are then used to access certain site URLs. From there, the program extracts the desired HTML elements from certain websites via previously specified URLs. It also counts successful and unsuccessful connections established via the RESTful API.
<pre>

</pre>
**First, I made a test console app that works but doesn’t use the spring features of framweork. In this test application, I only used the external Jsoup library, which is a good tool for extracting the desired HTML elements.**

```
package com.example.test;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ConsoleApp_Test {

static int successfulCalls = 0;

    private static String[] urls = { "https://www.result.si/projekti/", 
                                     "https://www.result.si/o-nas/", 
                                     "https://www.result.si/kariera/", 
                                     "https://www.resulst.si/blog/"};
                             
    public  static void main(String[] args) throws IOException {

        int stKlicev = 1;
         
        ExecutorService executor = Executors.newFixedThreadPool(stKlicev);

        for (String url : urls) {  
            executor.execute(() -> {
                try {

                    Document doc = Jsoup.connect(url).get();
                    doc.title(); 
                    
                    for (int i = 0; i < stKlicev; i++) {
                        String data[] = {doc.title()};
                        System.out.println(data[i]);  
                    } 
                    successfulCalls++; 
                    System.out.println("Stevilo uspelih klicev:" + successfulCalls);

                } catch (IOException e) {
                    int unSuccessfulCalls = 0;
                    unSuccessfulCalls++;
                    System.out.println("Stevilo neuspelih klicev:" + unSuccessfulCalls);
                }
            });
        }          
    }
}
```
**Which gives an console output like this:**
```
RESULT – Izzivi in rešitve
Stevilo uspelih klicev:1
O nas – RESULT
Stevilo uspelih klicev:2
Kariera – RESULT
Stevilo uspelih klicev:3
Stevilo neuspelih klicev:1
```
<pre>

</pre>
**Then I also made the RESTfull API application using the Java Spring framework, which is more complex but gives a JSON response to the http request.**

>ResultApplication.java
```
package com.example.test;
    
    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.SpringBootApplication;
    import org.springframework.context.annotation.Bean;
    import org.springframework.web.client.RestTemplate;
    
    @SpringBootApplication
    public class ResultApplication {
    
        @Bean
        public RestTemplate getRestTemplate(){
            return new RestTemplate();
        }
    
        public static void main(String[] args) {
            SpringApplication.run(ResultApplication.class, args);
        }
    }
```

>APIcontroller.java
```
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
```
>WebSiteRetriever.java
```
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
```

>WebsiteRetrievalResult.java
```
package com.example.test;

public class WebsiteRetrievalResult {

    
    private int httpStatusCode;
    /**
     * @param httpStatusCode
     * @param websiteData 
     */

    public WebsiteRetrievalResult(int httpStatusCode, String websiteData) {
        this.httpStatusCode = httpStatusCode;
        this.websiteData = websiteData;
    }

    private String websiteData;
    
    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getWebsiteData() {
        return websiteData;
    }

    
    public Boolean isSuccessful() {
        return this.httpStatusCode == 200;
    }
}
```

>WebsitesRetrievalResultWrapper.java
```
package com.example.test;

import java.util.List;

public class WebsitesRetrievalResultWrapper {

    private List<WebsiteRetrievalResult> websiteRetrievalResult;
    private Integer numberOfUnsuccessfulCalls;
    private Integer numberOfSuccessfulCalls;

    /**
     * @param httpStatusCode
     * @param websiteData
     */

    // konstrutor
    public WebsitesRetrievalResultWrapper(
        List<WebsiteRetrievalResult> websiteRetrievalResult,
            Integer numberOfSuccessfulCalls, 
            int numberOfUnsuccessfulCalls) {
        this.websiteRetrievalResult = websiteRetrievalResult;
        this.numberOfSuccessfulCalls = numberOfSuccessfulCalls;
        this.numberOfUnsuccessfulCalls = numberOfUnsuccessfulCalls;
    }

    /**
     * @return the websiteRetrievalResult
     */
    public List<WebsiteRetrievalResult> getWebsiteRetrievalResult() {
        return websiteRetrievalResult;
    }

    public Integer getNumberOfSuccessfulCalls() {
        return this.numberOfSuccessfulCalls;
    }

    public Integer getNumberOfUnsuccessfulCalls() {
        return this.numberOfUnsuccessfulCalls;
    }

}
```

**Which gives an JSON output like this:**
```
{
   "websiteRetrievalResult":[
      {
         "httpStatusCode":200,
         "websiteData":"RESULT – Izzivi in rešitve",
         "successful":true
      },
      {
         "httpStatusCode":200,
         "websiteData":"O nas – RESULT",
         "successful":true
      },
      {
         "httpStatusCode":200,
         "websiteData":"Kariera – RESULT",
         "successful":true
      },
      {
         "httpStatusCode":200,
         "websiteData":"RESULT – Blog z vrhunskimi vsebinami s področja optimizacije poslovanja",
         "successful":true
      }
   ],
   "numberOfUnsuccessfulCalls":0,
   "numberOfSuccessfulCalls":4
}
```
