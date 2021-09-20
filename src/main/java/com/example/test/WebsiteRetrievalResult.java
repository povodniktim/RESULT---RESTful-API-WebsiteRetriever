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
