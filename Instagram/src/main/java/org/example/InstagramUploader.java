package org.example;

// Required imports for Selenium and WebDriver functionality
import org.openqa.selenium.*;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;

public class InstagramUploader {

    // Instagram credentials and image path to upload
    private static final String USERNAME = "";
    private static final String PASSWORD = "";
    private static final String IMAGE_PATH = "D:\\Instagram\\image6.jpeg";

    public static void main(String[] args) {

        // WebDriver Setup for Edge
        System.setProperty("webdriver.edge.driver", "C:\\Users\\User\\Downloads\\edgedriver_win64\\msedgedriver.exe");

        // Configure Edge browser options
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--disable-notifications");
        options.addArguments("--start-maximized");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-blink-features=AutomationControlled");

        WebDriver driver = new EdgeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));

        try {
            // Navigate to Instagram
            driver.get("https://www.instagram.com/");
            System.out.println("Instagram opened");
            takeScreenshot(driver, "OpenedInstagram");

            // Handle cookie consent banner if present
            handleCookieBanner(driver, wait);
            // Log into Instagram account
            login(driver, wait);
            // Handle any post-login popups
            handlePopups(driver, wait);
            // Ensure the home page is fully loaded
            waitForHomePage(driver, wait);
            // Create and upload a new post
            createAndUploadPost(driver, wait);

            System.out.println("Post uploaded successfully!");
            takeScreenshot(driver, "PostUploaded");

        } catch (Exception e) {
            // Log and capture screenshot in case of error
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            takeScreenshot(driver, "ErrorOccurred");
        } finally {
            // Quit the browser session
            driver.quit();
        }
    }

    //Waits for the Instagram home page to be fully loaded.
    private static void waitForHomePage(WebDriver driver, WebDriverWait wait) throws Exception {
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(@href,'/home') or contains(@aria-label,'Home')]")),
                    ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@aria-label, 'New post') or contains(@href, '/create')]"))
            ));
            System.out.println("Home page fully loaded");
            Thread.sleep(2000);
        } catch (Exception e) {
            throw new Exception("Failed to load home page: " + e.getMessage());
        }
    }

    //Handle cookie banners if present.
    private static void handleCookieBanner(WebDriver driver, WebDriverWait wait) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(., 'Allow') or contains(., 'Accept')]"))).click();
            System.out.println("Cookie banner handled");
            Thread.sleep(1000);
        } catch (Exception e) {
            System.out.println("No cookie banner found");
        }
    }

    //Logs into the Instagram account using the provided credentials.
    private static void login(WebDriver driver, WebDriverWait wait) throws Exception {
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        usernameField.sendKeys(USERNAME);

        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys(PASSWORD);

        driver.findElement(By.xpath("//button[@type='submit']")).click();
        System.out.println("Login attempted");

        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("accounts/onetap"),
                    ExpectedConditions.urlContains("challenge"),
                    ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(@href,'/home') or contains(@aria-label,'Home')]"))
            ));

            if (driver.getCurrentUrl().contains("challenge") || driver.getPageSource().contains("Suspicious Login Attempt")) {
                throw new Exception("Login blocked - security challenge detected");
            }

            System.out.println("Login successful");
            takeScreenshot(driver, "LoginSuccessful");
        } catch (Exception e) {
            throw new Exception("Login failed: " + e.getMessage());
        }
    }

    //Dismisses post-login popups like "Save Info" and "Turn On Notifications.
    private static void handlePopups(WebDriver driver, WebDriverWait wait) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Not Now') or contains(text(), 'Not now')]"))).click();
            System.out.println("'Save info' popup dismissed");
            Thread.sleep(1000);
        } catch (Exception ignored) {}

        try {
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Not Now') or contains(text(), 'Not now')]"))).click();
            System.out.println("Notifications popup dismissed");
            Thread.sleep(1000);
        } catch (Exception ignored) {}
    }

    //Automates the post creation and image upload process.
    private static void createAndUploadPost(WebDriver driver, WebDriverWait wait) throws Exception {
        WebElement createButton = null;
        // Try multiple possible XPaths to find the "Create Post" button
        try {
            createButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[contains(@aria-label, 'New post')]")));
        } catch (Exception e) {
            try {
                createButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[contains(@href, '/create')]")));
            } catch (Exception ex) {
                createButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[contains(text(), 'Create')]/..")));
            }
        }

        createButton.click();
        System.out.println("Create post button clicked");

        // Upload the image
        WebElement fileInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@type='file']")));
        fileInput.sendKeys(IMAGE_PATH);
        System.out.println("Image selected - path: " + IMAGE_PATH);
        Thread.sleep(3000);

        // Confirm image upload
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[contains(@style, 'background-image') or contains(@src, 'blob')]")
            ));
            System.out.println("Image upload verified");
            takeScreenshot(driver, "ImageUploaded");
        } catch (Exception e) {
            throw new Exception("Image upload failed - check if file exists at: " + IMAGE_PATH);
        }

        // Click through "Next button" steps and share the post
        clickNextButton(wait, driver, "First");
        clickNextButton(wait, driver, "Second");

        WebElement shareButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[contains(text(), 'Share')]/..")));
        shareButton.click();
        System.out.println("Share button clicked");

        // Verify post was shared
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(., 'Your post has been shared')]")),
                    ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(., 'Post shared')]"))
            ));
            System.out.println("Post confirmed as shared");
            takeScreenshot(driver, "PostShared");
        } catch (Exception e) {
            System.out.println("Post might have been shared but confirmation not detected");
        }
    }

    //Handles the clicking of the "Next" button(s) during post creation.
    private static void clickNextButton(WebDriverWait wait, WebDriver driver, String stepName) throws Exception {
        try {
            WebElement nextButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button/div[text()='Next']/parent::button[not(@disabled)]")
            ));
            nextButton.click();
            System.out.println(stepName + " Next button clicking");
            Thread.sleep(2000);
            takeScreenshot(driver, stepName);
        } catch (Exception primary) {
            System.out.println(stepName + " Trying fallback XPath...");
            try {
                WebElement fallbackButton = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("/html/body/div[4]/div[1]/div/div[3]/div/div/div/div/div/div/div/div[1]/div/div/div/div[3]/div/div")
                ));
                fallbackButton.click();
                System.out.println(stepName + " Next button clicked");
                Thread.sleep(2000);
                takeScreenshot(driver, stepName + "_Fallback");
            } catch (Exception fallback) {
                System.err.println("Failed to click " + stepName + " Next button.");
                fallback.printStackTrace();
                throw new Exception("Could not click " + stepName + " Next button using any known XPath.");
            }
        }

        // Optionally wait for editor elements (Crop/Adjust screen) to load
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(text(),'Crop') or contains(text(),'Original') or contains(text(),'Adjust')]")
            ));
            Thread.sleep(1000);
        } catch (Exception ignored) {}
    }

    // Updated takeScreenshot method with timestamped filenames and saved in screenshots folder
    private static void takeScreenshot(WebDriver driver, String stepName) {
        try {
            TakesScreenshot ts = (TakesScreenshot) driver;
            File screenshotFile = ts.getScreenshotAs(OutputType.FILE);

            // Create screenshot folder if it doesn't exist
            String folderPath = "screenshots";
            File folder = new File("D:\\Instagram\\Screenshots");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // Generate file name with timestamp and step name
            String timestamp = java.time.LocalDateTime.now()
                    .toString().replace(":", "-")
                    .replace(".", "-");
            String fileName = folderPath + File.separator + stepName + "_" + timestamp + ".png";

            // Save screenshot
            File destinationFile = new File(fileName);
            org.openqa.selenium.io.FileHandler.copy(screenshotFile, destinationFile);

            System.out.println("Screenshot saved: " + destinationFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Failed to take screenshot: " + e.getMessage());
        }
    }
}
