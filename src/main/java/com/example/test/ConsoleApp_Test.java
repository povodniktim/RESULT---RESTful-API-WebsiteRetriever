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
