package com.example.amazon.tests;

import com.example.amazon.core.TestBase;
import com.example.amazon.pages.AmazonHomePage;
import com.example.amazon.pages.SearchResultsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AmazonHomePageTest extends TestBase {

    @Test(description = "Verify the main controls on the Amazon homepage are visible")
    public void shouldDisplayKeyHomepageElements() {
        AmazonHomePage homePage = new AmazonHomePage(driver).open(baseUrl);

        Assert.assertTrue(homePage.isLogoDisplayed(), "Amazon logo should be visible.");
        Assert.assertTrue(homePage.isSearchBoxDisplayed(), "Search box should be visible.");
        Assert.assertTrue(homePage.isSearchButtonEnabled(), "Search button should be enabled.");
        Assert.assertTrue(homePage.isAccountListMenuDisplayed(), "Account & Lists menu should be visible.");
        Assert.assertTrue(homePage.isDeliveryLocationDisplayed(), "Delivery location selector should be visible.");
        Assert.assertTrue(homePage.getPageTitle().toLowerCase().contains("amazon"), "Page title should contain Amazon.");
    }

    @Test(description = "Verify a user can search from the Amazon homepage")
    public void shouldSearchForAProductFromHomepage() {
        SearchResultsPage searchResultsPage = new AmazonHomePage(driver)
                .open(baseUrl)
                .searchFor("laptop");

        Assert.assertTrue(searchResultsPage.isLoaded(), "Search results page should load.");
        Assert.assertEquals(searchResultsPage.getSearchTerm(), "laptop", "Search box should keep the submitted keyword.");
        Assert.assertTrue(searchResultsPage.getCurrentUrl().contains("s?k=laptop"), "URL should contain the Amazon search query.");
    }
}
