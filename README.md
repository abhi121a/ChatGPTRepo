# Amazon Homepage Automation Project

This repository contains a Java 17 Selenium automation project built with Maven and TestNG for Amazon homepage validation.

## Tech stack

- Java 17
- Maven
- Selenium WebDriver 4
- TestNG

## Project structure

```text
src
├── main
│   └── java
│       └── com/example/amazon
│           ├── config
│           │   └── TestConfig.java
│           ├── core
│           │   ├── BasePage.java
│           │   ├── DriverFactory.java
│           │   └── TestBase.java
│           └── pages
│               ├── AmazonHomePage.java
│               └── SearchResultsPage.java
└── test
    ├── java
    │   └── com/example/amazon/tests
    │       └── AmazonHomePageTest.java
    └── resources
        └── testng.xml
```

## Coverage

The suite currently includes:

- a homepage smoke test for the Amazon logo, search box, search button, account menu, delivery selector, and page title
- a homepage search flow that submits `laptop` and verifies the search results page loads correctly

## Run the suite

```bash
mvn test
```

### Optional runtime properties

```bash
mvn test -Dbrowser=chrome -Dheadless=true -DbaseUrl=https://www.amazon.com/
```

## Notes

- Chrome is the supported browser in this starter project.
- Headless mode is enabled by default for CI/container execution.
- The project relies on Selenium Manager through Selenium 4 for driver discovery.
