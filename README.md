# RESULT---RESTful-API-WebsiteRetriever
This is a simple program written in Java. I also used the Spring framework for easy http requests and the RESTful API http response. The program also gets an integer through a query parameter and uses it as a variable that declares multiple concurrent threads, which are then used to access certain site URLs. From there, the program extracts the desired HTML elements from certain websites via previously specified URLs. It also counts successful and unsuccessful connections established via the RESTful API.

First, I made a test console app that works but doesn’t use the spring features of framweork. In this test application, I only used the external Jsoup library, which is a good tool for extracting the desired HTML elements.

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
