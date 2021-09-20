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