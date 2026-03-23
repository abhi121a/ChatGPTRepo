package com.example.amazon.pages;

import com.example.amazon.core.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class SearchResultsPage extends BasePage {

    private static final By SEARCH_BOX = By.id("twotabsearchtextbox");
    private static final By RESULTS_CONTAINER = By.cssSelector("div.s-main-slot");

    public SearchResultsPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return waitForVisible(RESULTS_CONTAINER).isDisplayed();
    }

    public String getSearchTerm() {
        return readValue(SEARCH_BOX);
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
