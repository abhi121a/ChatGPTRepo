package com.example.amazon.pages;

import com.example.amazon.core.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class AmazonHomePage extends BasePage {

    private static final By DELIVERY_LOCATION = By.id("nav-global-location-popover-link");
    private static final By SEARCH_BOX = By.id("twotabsearchtextbox");
    private static final By SEARCH_BUTTON = By.id("nav-search-submit-button");
    private static final By ACCOUNT_LIST_MENU = By.id("nav-link-accountList");
    private static final By AMAZON_LOGO = By.id("nav-logo-sprites");

    public AmazonHomePage(WebDriver driver) {
        super(driver);
    }

    public AmazonHomePage open(String baseUrl) {
        driver.get(baseUrl);
        waitForVisible(SEARCH_BOX);
        return this;
    }

    public boolean isSearchBoxDisplayed() {
        return waitForVisible(SEARCH_BOX).isDisplayed();
    }

    public boolean isSearchButtonEnabled() {
        return waitForClickable(SEARCH_BUTTON).isEnabled();
    }

    public boolean isAccountListMenuDisplayed() {
        return waitForVisible(ACCOUNT_LIST_MENU).isDisplayed();
    }

    public boolean isDeliveryLocationDisplayed() {
        return waitForVisible(DELIVERY_LOCATION).isDisplayed();
    }

    public boolean isLogoDisplayed() {
        return waitForVisible(AMAZON_LOGO).isDisplayed();
    }

    public String getPageTitle() {
        return driver.getTitle();
    }

    public SearchResultsPage searchFor(String keyword) {
        waitForClickable(SEARCH_BOX).sendKeys(keyword);
        waitForClickable(SEARCH_BUTTON).click();
        return new SearchResultsPage(driver);
    }
}
