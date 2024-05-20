package configuration;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.Augmenter;

import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static configuration.BaseTest.fwConfigData;

public class BaseDriver {

	public  WebDriver driver = null;
	private final Browsers browser;
	private MutableCapabilities options = null;
	String outputPath;
	protected AutoLogger logger = new AutoLogger(BaseTest.class);
	private long limit;
	private static final ThreadLocal<RemoteWebDriver> REMOTE_WEB_DRIVER
			= new ThreadLocal<>();
	private String mainWindowHandle = null;

	protected Wait<WebDriver> newWait;

	public enum Browsers {
		FIREFOX, EDGE, CHROME, SAFARI
	}

	public BaseDriver(String browser,long newWaitImplicit,long newWaitExplicit,
                     String outputDir) {
		this.browser = parseBrowser(browser);
		this.options = parseMutableCapabilities();
		this.limit = newWaitImplicit;
		this.newWait = setWait(newWaitExplicit);
		this.outputPath = outputDir;
		loadWebDriverObject();
	}

	/**
	 * Set wati
	 * @param newWaitExplicit - The Explicit Wait
	 */
	public Wait<WebDriver> setWait(long newWaitExplicit) {
		newWait = new FluentWait<>(driver)
				.withTimeout(Duration.ofSeconds(newWaitExplicit))
				.ignoring(NoSuchElementException.class)
				.ignoring(StaleElementReferenceException.class)
				.ignoring(TimeoutException.class)
				.ignoring(InvalidElementStateException.class);
		return newWait;
	}

	/**
	 * Parse Browser
	 * @param browserStr - The browser name
	 */
	public Browsers parseBrowser(String browserStr) {
		Browsers browser;
		List<String> ffKeys = new ArrayList<>();
		ffKeys.add("firefox");
		ffKeys.add("ff");
		ffKeys.add("Firefox");
		ffKeys.add("MozilaFirefox");
		ffKeys.add("Mozilafirefox");
		ffKeys.add("mozilafirefox");

		List<String> edgeKeys = new ArrayList<>();
		edgeKeys.add("edge");
		edgeKeys.add("Edge");
		edgeKeys.add("MicrosoftEdge");

		List<String> chromeKeys = new ArrayList<>();
		chromeKeys.add("googlechrome");
		chromeKeys.add("chrome");
		chromeKeys.add("Chrome");

		if (ffKeys.contains(browserStr)) {
			browser = Browsers.FIREFOX;
		}
		else if (edgeKeys.contains(browserStr)) {
			browser = Browsers.EDGE;
		}else if (chromeKeys.contains(browserStr)) {
			browser = Browsers.CHROME;
		} else {
			browser = Browsers.CHROME;
		}
		return browser;
	}

	/**
	 * Set Mutable Capabilities
	 */
	public MutableCapabilities parseMutableCapabilities() {
		MutableCapabilities option = null;
		switch (browser.toString()) {
			case "firefox" -> option = new FirefoxOptions();
			case "chrome" -> option = new ChromeOptions();
			case "microsoftedge" -> option = new EdgeOptions();
		}

		return option;
	}

	/**
	 * Load Web Driver Object
	 */
	private void loadWebDriverObject() {
		try {
			String DOCKER_GRID_URL = fwConfigData.get("Configuration", "DOCKER_GRID_URL");
			if (null != options) {
				REMOTE_WEB_DRIVER.set(
						new RemoteWebDriver(new URL(DOCKER_GRID_URL), options));
			}
			
			// If the Web Driver has been set
			if (null != REMOTE_WEB_DRIVER.get()) {
				// Set this below to ensure that files can be found when using a dockerized grid
				REMOTE_WEB_DRIVER.get().setFileDetector(new LocalFileDetector());
			}

		} catch (MalformedURLException e) {
			logger.e("\nMalformed URL Exception while connecting to the Selenium GRID Hub\n" + e.getMessage());
		} catch (SessionNotCreatedException e) {
			logger.e("Selenium Grid was unable to create a session using the following capabilities: \n"
					+ "BrowserName = " + browser);
		}

		if (null != REMOTE_WEB_DRIVER.get()) {
			REMOTE_WEB_DRIVER
					.get()
					.manage()
					.timeouts()
					.pageLoadTimeout(Duration.ofSeconds(limit));
			REMOTE_WEB_DRIVER
					.get()
					.manage()
					.timeouts()
					.implicitlyWait(Duration.ofSeconds(limit));
			REMOTE_WEB_DRIVER
					.get()
					.manage()
					.timeouts()
					.scriptTimeout(
							Duration.ofSeconds(limit));

			mainWindowHandle = REMOTE_WEB_DRIVER.get()
					.getWindowHandle();

			REMOTE_WEB_DRIVER.get()
					.manage()
					.window()
					.maximize();
		}
	}

	public void changeStyleAttrWithElementID(String elementID, String TagName, String newValue) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		String executeScriptText = "document.getElementById('" + elementID + "').setAttribute('" + TagName + "', '"
				+ newValue + "')";
		js.executeScript(executeScriptText);
	}

	public void setFocusOnBrowser() {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("window.focus();");
	}

	public void resetBrowserSize() {
		Dimension d = new Dimension(1024, 768);
		driver.manage().window().setSize(d);
	}

	public void newWaitUntilPageIsLoaded() {
		
		ExpectedCondition<Boolean> pageLoadCondition = driver -> {
			assert driver != null;
			return "complete".equals(((JavascriptExecutor) driver).executeScript("return document.readyState"));
		};

		newWait.until(pageLoadCondition);

	}

	public void newWaitForPageLoad() {
		
		ExpectedCondition<Boolean> pageLoadCondition = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				return "complete".equals(((JavascriptExecutor) driver).executeScript("return document.readyState"));
			}
		};

		newWait.until(pageLoadCondition);

	}

	public void switchToFrame(WebElement we) {

		logger.i("switchToFrame");
		driver.switchTo().frame(we);
	}

	public void switchToDefault() {
		logger.i("switch To Default Frame");
		driver.switchTo().defaultContent();
	}

	public void inputTextToiFrame(WebElement we, String expText) {

		logger.i("inputTextToiFrame");
		switchToFrame(we);
		switchToInnerFrame(we, expText);
		switchToDefault();
	}

	public void switchToInnerFrame(WebElement we, String expText) {
		logger.i("switchToInnerFrame");
		we.click();
		Actions act = new Actions(driver);
		act.sendKeys(we, expText).build().perform();
	}

	public void verifyLabelPresentInList(WebElement we, String expLabel) {
		logger.i("verifyLabelPresentInList");
		String[] actList = getAllListItems(we);
		Assert.assertTrue(Arrays.asList(actList).contains(expLabel), "Text" + expLabel + "is not present in list!");
	}

	public void verifyLabelNotPresentInList(WebElement we, String expLabel) {
		logger.i("verifyLabelNotPresentInList");
		String[] actList = getAllListItems(we);
		Assert.assertFalse(Arrays.asList(actList).contains(expLabel), "Text" + expLabel + "is present in list!");
	}

	public void clickAndWait(WebElement we, boolean newWaitForPageLoad) {
		we.click();
		if (newWaitForPageLoad) {
			newWaitUntilPageIsLoaded();
		}
	}

	public void clickAndWait(WebElement we) {
		logger.i("Click On "+we.getText());
		clickAndWait(we, true);
	}

	public void doubleClickAndWait(WebElement we, boolean newWaitForPageLoad) {
		logger.i("doubleClickAndWait, newWait?=%s");
		(new Actions(driver)).doubleClick(we).perform();
		if (newWaitForPageLoad) {
			newWaitUntilPageIsLoaded();
		}

	}

	public void doubleClickAndWait(WebElement we) {
		doubleClickAndWait(we, true);
	}

	public void inputText(WebElement we, String text) {
		logger.i("inputText, text=%s", text);
		we.clear();
		we.sendKeys(text);
	}

	public void pressTab(WebElement we) {
		logger.i("pressTab");
		we.click();
		we.sendKeys(Keys.TAB);
	}

	public void inputTextAndDefocus(WebElement we, String text) {
		logger.i("inputTextAndDefocus, text=%s", text);
		we.clear();
		we.sendKeys(text);
		((JavascriptExecutor) driver).executeScript("arguments[0].onblur();", we);
		// element.sendKeys(Keys.TAB);
		newWaitUntilPageIsLoaded();
	}

	public void executeBlurEvent(WebElement we) {
		((JavascriptExecutor) driver).executeScript("arguments[0].onblur();", we);
	}

	public void inputTextAndChangefocus(WebElement we, String text) {
		logger.i("inputTextAndDefocus, text=%s", text);
		we.clear();
		we.sendKeys(text);
		we.sendKeys(Keys.TAB);
		newWaitUntilPageIsLoaded();
	}

	public void sendKey(WebElement we, Keys key) {
		logger.i("sendkey, key=%s", key.name());
		we.sendKeys(key);
		newWaitUntilPageIsLoaded();
	}

	public void setText(WebElement we, String text) {
		logger.i("setText, text=%s", text);
		JavascriptExecutor executor = (JavascriptExecutor) driver;
		executor.executeScript("arguments[0].value='" + text + "';", we);
	}

	public void setInnerHTML(WebElement we, String text) {
		logger.i("setInnerHTML, text=%s", text);
		JavascriptExecutor executor = (JavascriptExecutor) driver;
		executor.executeScript("arguments[0].innerHTML='" + text + "';", we);
	}

	public void setTextContent(WebElement we, String text) {
		logger.i("setTextContent, text=%s", text);
		JavascriptExecutor executor = (JavascriptExecutor) driver;
		executor.executeScript("arguments[0].textContent='" + text + "';", we);
	}

	public void newWaitForDropDownToLoad(final WebElement elementId) {

		logger.i("newWaitForDropDownToLoad");
		
		
		ExpectedCondition<Boolean> newWaitCondition = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				Object ret = ((JavascriptExecutor) driver)
						.executeScript("return document.getElementById('" + elementId + "').length > 1");
				return ret.equals(true);
			}
		};
		newWait.until(newWaitCondition);
		
	}

	public void newWaitForElementToBeClickable(WebElement webElement, int implicitTime) {		
		logger.i("newWaitForElementToBeClickable");
		
		newWait.until(ExpectedConditions.elementToBeClickable(webElement));
	}

	public void newWaitForElementState(WebElement webElement, int implicitTime, boolean elementState) {

		logger.i("newWaitForElementState");

		newWait.until(ExpectedConditions.elementSelectionStateToBe(webElement, elementState));
	}

	public void newWaitForTextChangeInElement(WebElement we) {
		logger.i("newWaitForTextChangeInElement");

		String actText = getText(we);

		newWaitForTextNotInElement(we, actText);
	}

	public void newWaitForTextNotInElement(WebElement we, String text) {

		logger.i("newWaitForTextNotInElement");

		
		

		newWait.until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElement(we, text)));

		

	}

	public void newWaitForElement(WebElement we) {


		
		

		newWait.until(ExpectedConditions.elementToBeClickable(we));

		

	}

	public void newWaitForValueNotInElement(WebElement we, String text) {

		logger.i("newWaitForValueNotInElement");

		
		

		newWait.until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElementValue(we, text)));

		

	}

	public void newWaitForTextInElement(WebElement we, String expText) {

		logger.i("newWaitForTextInElement");

		
		

		newWait.until(ExpectedConditions.textToBePresentInElement(we, expText));

		

	}

	public void newWaitForTextToBePresentInElement(WebElement we, String expText) {

		logger.i("newWaitForTextInElementWithAttribute");

		
		

		newWait.until(ExpectedConditions.textToBePresentInElement(we, expText));

		

	}

	public void newWaitForTextToBePresentInElementWithAttribute(WebElement we, String expText, String attributeName) {

		logger.i("newWaitForTextToBePresentInElementWithAttribute");

		

		int attempts = 0;
		while (attempts < 500) {
			try {
				if (we.getAttribute(attributeName).contains(expText)) {
					break;
				}
			} catch (Exception e) {
				logger.e(e);
			}
			attempts++;
		}

		
	}

	public void newWaitForValueInElement(WebElement we, String expText) {

		logger.i("newWaitForValueInElement");

		
		

		newWait.until(ExpectedConditions.textToBePresentInElementValue(we, expText));

		
	}

	public void newWaitForJSCondition(final String jsCondition) {
		logger.i("newWaitForJSCondition: %s", jsCondition);
		
		
		ExpectedCondition<Boolean> newWaitCondition = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				Object ret = ((JavascriptExecutor) driver).executeScript("return " + jsCondition);
				return ret.equals(true);
			}
		};
		newWait.until(newWaitCondition);
		
	}

	public boolean isVisible(WebElement we) {
		logger.i("isVisible");
		return we.isDisplayed();
	}

	public String getCurrentUrl() {
		logger.i("getCurrentUrl");
		return driver.getCurrentUrl();
	}

	public boolean isSelected(WebElement we) {
		logger.i("isSelected");
		return we.isSelected();
	}

	public void selectCheckbox(WebElement we) {
		logger.i("selectCheckbox");
		if (!isSelected(we)) {
			logger.d("Element not checked, perform click");
			clickAndWait(we, false);
		} else {
			logger.d("Element already checked, skipping click");
		}
	}

	public void unselectCheckbox(WebElement we) {
		logger.i("unselectCheckbox");
		if (isSelected(we)) {
			logger.d("Element checked, perform click");
			clickAndWait(we, false);
		} else {
			logger.d("Element already unchecked, skipping click");
		}
	}

	public String getText(WebElement we) {
		logger.i(we.getText());
		String message = we.getText();
		return message;
	}

	public WebDriver wd() {
		return driver;
	}

	public void quit() {
		logger.i("Driver Quit");
		if (driver != null) {
			driver.quit();
			driver = null;
		}
	}

	public String getTextByAttribute(WebElement we, String attribute) {
		String message = we.getAttribute(attribute);
		logger.i("getTextByAttribute=%s", message);
		return message;
	}

	public void selectByLabel(WebElement we, String label) {
		logger.i("selectByLabel, label=%s", label);
		Select select = new Select(we);
		select.selectByVisibleText(label);
	}

	public void selectByIndex(WebElement we, int index) {
		logger.i("selectByIndex, index=%s", index);
		Select select = new Select(we);
		select.selectByIndex(index);
	}

	public String getSelectedLabel(WebElement we) {
		logger.i("getSelectedLabel");
		Select select = new Select(we);
		String actLabel = select.getFirstSelectedOption().getText();
		logger.d("getSelectedLabel, actualLabel=%s", actLabel);
		return actLabel;
	}

	public void verifySelectedLabel(WebElement we, String expLabel) {
		logger.i("verifySelectedLabel, expLabel=%s", expLabel);
		String actLabel = getSelectedLabel(we);

		Assert.assertEquals(actLabel, expLabel, "Selected label mismatch!");
	}

	public void verifyText(WebElement we, String expText) {
		logger.i("verifyText");
		String actText = getText(we);
		Assert.assertEquals(actText, expText, "Element text mismatch!");
	}

	public void verifyTextNotInElement(WebElement we, String expText) {
		logger.i("verifyTextNotInElement");
		String actText = getText(we);
		Assert.assertNotEquals(actText, expText, "Element text mismatch!");
	}

	public String getValue(WebElement we) {
		logger.i("getValue");
		String actValue = we.getAttribute("value");
		logger.d("getValue=%s", actValue);
		return actValue;
	}

	public void verifyValue(WebElement we, String expValue) {
		logger.i("verifyValue");
		String actValue = getValue(we);
		Assert.assertEquals(actValue, expValue, "Element value mismatch!");
	}
	
	public void takeScreenshot(String fileSuffix) {
		try {

			WebDriver augmentedDriver = new Augmenter().augment(wd());
			File screenshot = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);
			String relativePath = "UI" + File.separator + "_" + fileSuffix + ".png";

			File outputDir = new File(outputPath);
			String parentDir = outputDir.getParent();
			File finalPath = new File(parentDir, relativePath);
			
			FileUtils.copyFile(screenshot, finalPath);

		} catch (Exception e) {
			logger.e("Error while taking screenshot", e);
		}
	}

	public void captureScreenshot(String fileSuffix) {
		try {

			WebDriver augmentedDriver = new Augmenter().augment(wd());
			File screenshot = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);

			String timestamp = new SimpleDateFormat("YYYYMMdd_hhmmss").format(new Date());
			String nameScreenshot = browser.toString().toUpperCase() + "_" + timestamp + "_" + fileSuffix + ".png";

			String relativePath = "FailedScenarioScreenShots" + File.separator + nameScreenshot;
			String reportSrcPath = "." + File.separator + ".." + File.separator + relativePath;

			File outputDir = new File(outputPath);
			String parentDir = outputDir.getParent();
			File finalPath = new File(parentDir, relativePath);
			FileUtils.copyFile(screenshot, finalPath);

			logger.i("ScreenShot: <br/> <a target=\"_blank\" href=\"" + reportSrcPath + "\"><img width=\"500\" src=\""
					+ reportSrcPath + "\" alt=\"" + fileSuffix + "\"/></p></a><br />");
		} catch (Exception e) {
			logger.e("Error while taking screenshot", e);
		}
	}

	public void newWaitForAllElementsVisible(List<WebElement> weLst) throws InterruptedException {
		
		Thread.sleep(1000);
		
		

		newWait.until(ExpectedConditions.visibilityOfAllElements(weLst));

		

	}

	public void newWaitForElementVisible(WebElement we) {
		
		
		newWait.until(ExpectedConditions.visibilityOf(we));
		

	}
	public void newWaitForElementNotVisible(WebElement we) {

		
		
		newWait.until(ExpectedConditions.not(ExpectedConditions.visibilityOf(we)));

		

	}
	public void verifyIsNotVisible(WebElement we) {
		logger.i("verifyIsNotVisible");

		if (!isVisible(we)) {
			Assert.assertFalse(isVisible(we), "Element '" + we + "' is found visible!");
		} else {
			Assert.assertTrue(isVisible(we), "Element count is non-zero!");
		}
	}

	public void verifyIsSelected(WebElement we) {

		logger.i("verifyIsSelected");
		Assert.assertTrue(isSelected(we), "Element '" + we + "' is not selected!");
	}

	public void verifyIsNotSelected(WebElement we) {
		logger.i("verifyIsNotSelected");
		Assert.assertFalse(isSelected(we), "Element '" + we + "' is found selected!");
	}

	public void verifyTextBoxNotEmpty(WebElement we) {
		logger.i("verifyTextBoxNotEmpty");
		Assert.assertNotSame(we.getAttribute("value"), "", "Element '" + we + "' Text Box is empty ");
	}

	public void verifyElementTextNotEmpty(WebElement we) {
		logger.i("verifyElementTextNotEmpty");
		Assert.assertNotSame(we.getText(), "", "Element '" + we + "' Text is empty ");
	}

	public void scrollBottom() {
		logger.i("scrollBottom");
		((JavascriptExecutor) driver).executeScript(
				"window.scrollTo(0,Math.max(document.documentElement.scrollHeight,document.body.scrollHeight,document.documentElement.clientHeight))");
	}

	public void alertConfirm(boolean switchBackToMainWindow) {
		logger.i("alertConfirm: backToMainWaindow=%s", switchBackToMainWindow);

		try {
			Alert alert = driver.switchTo().alert();
			alert.accept();

			sleep(2000, "Required time to settle down after alert in IE");
		} catch (Exception e) {
			logger.e(e);
		}

		if (switchBackToMainWindow) {
			driver.switchTo().defaultContent();
		}
	}

	public void promptConfirm(String sendKeys) {
		try {
			Alert promptAlert  = driver.switchTo().alert();
			//Send some text to the alert
			promptAlert .sendKeys(sendKeys);
			sleep(2000, "Required time to settle down after alert in IE");
		}

		catch (Exception e) {
			logger.e(e);
		}
	}
	public void alertConfirm() {
		alertConfirm(true);
	}

	public void alertDismiss() {
		logger.i("alertDismiss");

		try {
			Alert alert = driver.switchTo().alert();
			alert.dismiss();

			// Required time to settle down after alert in IE
			sleep(2000, "Required time to settle down after alert in IE");
		} catch (Exception e) {
			logger.e(e);
		}

		driver.switchTo().defaultContent();
	}

	public void switchToNewWindow() {

		logger.i("switchToNewWindow");

		int trials = 3;

		Set<String> windowIds;
		do {
			windowIds = driver.getWindowHandles();
			if (trials != 3) {
				sleep(1000, "Requires time to select newly opened window");
			}
		} while (windowIds.size() <= 1 && --trials > 0);

		// Switch to new window opened
		for (String winHandle : windowIds) {
			if (!winHandle.equals(mainWindowHandle)) {
				logger.d("Switching to '%s'", winHandle);
				driver.switchTo().window(winHandle);
			}
		}
	}

	public void switchToMainWindow() {
		logger.i("switchToMainWindow, mainWindowHandle=%s", mainWindowHandle);

		Assert.assertNotNull(mainWindowHandle, "Main Window Handle not initialized!");

		driver.switchTo().window(mainWindowHandle);
	}

	public void closeWindow() {
		logger.i("closeWindow");

		driver.close();
	}

	public void verifyWindowTitle(String title) {
		logger.i("verifyWindowTitle, expTitle=%s", title);

		String actTitle = getTitle();

		logger.d("verifyWindowTitle, actTitle=%s", actTitle);

		Assert.assertEquals(actTitle, title, "Window title mismatch!");
	}

	public void mouseHoverClickOnElement(WebElement ele) {

		Actions action = new Actions(driver);
		logger.d("mouseHover And Click On "+ele.getText());
		action.moveToElement(ele).click().build().perform();
	}

	public void clickOnElementOnSpecificDimesion(WebElement clickOnTopSelectMenu) {
		Actions action = new Actions(driver);
		int xyard=clickOnTopSelectMenu.getSize().width-10;
		int yard=clickOnTopSelectMenu.getSize().height-5;
		action.moveToElement(clickOnTopSelectMenu,xyard,yard).perform();
		action.moveToElement(clickOnTopSelectMenu,xyard,yard).clickAndHold().release().build().perform();

	}

	public void clickOnCheckBoxOnSpecificDimesion(WebElement clickOnTopSelectMenu) {
		Actions action = new Actions(driver);
		int xyard=clickOnTopSelectMenu.getSize().width/2;
		int yard=clickOnTopSelectMenu.getSize().height/2;
		action.moveToElement(clickOnTopSelectMenu,xyard,yard).perform();
		action.moveToElement(clickOnTopSelectMenu,xyard,yard).clickAndHold().release().build().perform();
	}

	public void dragAndDrop(WebElement sourceWE, WebElement targetWE) {
		logger.i("dragAndDrop");

		(new Actions(driver)).dragAndDrop(sourceWE, targetWE).perform();
	}

	public void refreshPage() {
		logger.i("refreshPage");
		driver.navigate().refresh();
	}

	public void bakcPage() {
		driver.navigate().back();
	}

	public void sleep(long milliseconds, String reasonForSleep) {
		Assert.assertNotEquals("", reasonForSleep.trim(), "Reason for sleep not specified!");

		try {
			Thread.sleep(milliseconds);
		} catch (Exception e) {
			logger.e(e);
		}
	}

	public void gotoUrl(String urlAddress) {
		logger.i("gotoUrl: " + urlAddress);
		driver.get(urlAddress);
	}

	public void maximizeWindow() {
		logger.i("Maximize window");
		driver.manage().window().maximize();
	}

	public void verifyElementContainsText(WebElement we, String expLabel) {
		logger.i("verifySelectedLabel, expLabel=%s", expLabel);
		String actText = getText(we);

		Assert.assertTrue(actText.contains(expLabel), "Expected String is not present");

	}

	public void verifyElementTextIsSubstring(WebElement we, String expLabel) {
		logger.i("verifySelectedLabel, expLabel=%s", expLabel);
		String actText = getText(we);

		Assert.assertTrue(expLabel.contains(actText), "Expected String is not present");

	}

	public String alertGetMessage() {
		logger.i("getAlertMessage");
		String msg = "";
		try {
			Alert alert = driver.switchTo().alert();
			msg = alert.getText();
			logger.d("getAlertMessage: %s", msg);
		} catch (Exception e) {
			logger.e(e);
		}

		return msg;

	}

	public void verifyElementTextMatchesRegex(WebElement we, String regex) {
		logger.i("verifyElementTextMatchesRegex, regex=%s", regex);
		String actText = getText(we);
		Assert.assertTrue(actText.matches(regex), "Actual String does not matches Regex");
	}

	public String[] getAllListItems(WebElement we) {
		logger.i("getAllListItems");

		Select select = new Select(we);
		List<WebElement> options = select.getOptions();
		int j = 0;
		String[] val = new String[options.size()];
		for (WebElement weOption : options) {
			val[j++] = weOption.getText();
		}

		logger.d("getAllListItems: %s", Utilities.getStringFromArray(val));

		return val;
	}

	public void verifyAllListItems(WebElement we, String[] items) {
		String expItemsStr = Utilities.getStringFromArray(items);
		logger.i("verifyAllListItems: expItemsLen=%d, expItems=%s", items.length, expItemsStr);
		String[] actItems = getAllListItems(we);

		Assert.assertEquals(actItems.length, items.length, "List items length mismatch!");
		Assert.assertEquals(Utilities.getStringFromArray(actItems), expItemsStr, "List items mismatch!");
	}

	public boolean isPresent(WebElement we) {
		logger.i("isPresent");
		boolean present = we.isDisplayed();
		logger.d("isPresent: %s", present);
		return present;
	}

	public String getTitle() {
		logger.i("getTitle");
		String actTitle = driver.getTitle();
		logger.d("getTitle: %s", actTitle);
		return actTitle;
	}

	public void newWaitForAlert() {
		logger.i("newWaitForAlert");
		
		newWait.until(ExpectedConditions.alertIsPresent());
	}

	public void verifyColor(WebElement we, String expcolor) {

		String color = we.getCssValue("color");

		String[] numbers = color.replace("rgba(", "").replace(")", "").split(",");
		int r = Integer.parseInt(numbers[0].trim());
		int g = Integer.parseInt(numbers[1].trim());
		int b = Integer.parseInt(numbers[2].trim());
		String hex = "#" + Integer.toHexString(r) + Integer.toHexString(g) + Integer.toHexString(b);
		Assert.assertEquals(hex.toUpperCase(), expcolor.toUpperCase(),
				"Element text color mismatch! Expected:" + expcolor.toUpperCase() + "actual:" + hex.toUpperCase());

	}

	public void verifyBackgroundColor(WebElement we, String expcolor) {

		String color = we.getCssValue("background-color");
		String[] numbers = color.replace("rgba(", "").replace(")", "").split(",");
		int r = Integer.parseInt(numbers[0].trim());
		int g = Integer.parseInt(numbers[1].trim());
		int b = Integer.parseInt(numbers[2].trim());
		String hex = "#" + Integer.toHexString(r) + Integer.toHexString(g) + Integer.toHexString(b);
		Assert.assertEquals(hex.toUpperCase(), expcolor.toUpperCase(), "Element background color mismatch! Expected:"
				+ expcolor.toUpperCase() + "actual:" + hex.toUpperCase());

	}

	public void verifyElementValueIsEmpty(WebElement we) {
		logger.i("verifyElementValueIsEmpty");

		Assert.assertEquals(getValue(we), "", "Element '" + we + "' Value is not empty");

	}

	public void openDuplicateWindow() {
		logger.i("openDuplicateWindow");
		((JavascriptExecutor) driver).executeScript("(window.open(document.URL))");

	}



	public void verifyTextBoxIsNotEditable(WebElement we) {
		logger.i("verifyTextBoxIsNotEditable");
		String value = we.getAttribute("readonly");
		Assert.assertTrue(value.contentEquals("true"), "Element '" + we + "' Is Editable");

	}

	public void verifyTextIsUnderLined(WebElement we) {
		logger.i("verifyTextBoxIsNotEditable");
		String value = we.getAttribute("style");
		Assert.assertTrue(value.contains("text-decoration: underline"),
				"Text of Element '" + we + "' Is not Underlined");

	}

	public void uploadFile(WebElement we, String path) {
		logger.i("uploadFile");
		we.sendKeys(path);
	}

	public void newWaitForElementTextNotEmpty(WebElement we) {

		logger.i("newWaitForElementTextNotEmpty");

		int trials = 10;
		int len;

		do {
			String text = getText(we);
			len = text.length();
			if (trials != 10) {
				logger.d("Waiting");
				sleep(2000, "Requires time for text to be displayed");
			}
		} while (len <= 1 && --trials > 0);

	}

	public void newWaitForElementValueNotEmpty(WebElement we) {

		logger.i("newWaitForElementValueNotEmpty");

		int trials = 10;
		int len;

		do {
			String text = getValue(we);
			len = text.length();
			if (trials != 10) {
				logger.d("Waiting");
				sleep(2000, "Requires time for text to be displayed");
			}
		} while (len <= 1 && --trials > 0);

	}

	public void clearText(WebElement we) {
		logger.i("clearText");
		we.clear();
	}

	public void verifyElementContainsValue(WebElement we, String expLabel) {
		logger.i("verifySelectedLabel, expLabel=%s", expLabel);

		String actValue = getValue(we);
		Assert.assertTrue(actValue.contains(expLabel), "Expected String is not present");

	}

	public void verifyElementValueNotEmpty(WebElement we) {
		logger.i("verifyElementValueNotEmpty");

		Assert.assertNotEquals(getValue(we), "", "Element '" + we + "' Value is empty");

	}

	public void selectRadioButton(List<WebElement> weLst, int optionNo) {
		logger.i("selectRadioButton");
		if (optionNo > 0 && optionNo <= weLst.size()) {
			weLst.get(optionNo - 1).click();
		} else {
			throw new NotFoundException("option " + optionNo + " not found");
		}
	}

	public void verifySelectionBoxIsMultiple(String elementId) {

		logger.i("verifyIsMultipleSelectionBox");

		Object isMultiple = ((JavascriptExecutor) driver)
				.executeScript("return document.getElementById('" + elementId + "').multiple");
		Assert.assertEquals(isMultiple, true, "Element is not Multiple Selection Box");
	}

	public int getselectedRadioButton(List<WebElement> weLst) {

		logger.i("getselectedRadioButton");

		int optionNo = 0;
		for (int i = 0; i <= weLst.size(); i++) {
			if (weLst.get(i).isSelected()) {
				optionNo = (i + 1);
				break;
			}
		}

		return optionNo;
	}

	public void verifyJSCondition(final String jsCondition, boolean status) {

		logger.i("verifyJSCondition: %s", jsCondition);
		Object ret = ((JavascriptExecutor) driver).executeScript("return " + jsCondition);
		Assert.assertEquals(status, ret, "Status for jsCondition :" + jsCondition + " is not '" + status + "'");

	}

	public void executeJSCondition(final String jsCondition) {

		logger.i("executeJSCondition: %s", jsCondition);
		((JavascriptExecutor) driver).executeScript(jsCondition);
	}

	public void verifyScrollBarPresent(WebElement elementId) {
		logger.i("verifyScrollBarPresent");
		String condition = "document.getElementById('" + elementId + "').scrollHeight > document.getElementById('"
				+ elementId + "').clientHeight";
		Object ret = ((JavascriptExecutor) driver).executeScript("return " + condition);
		Assert.assertEquals(ret, true, "Scroll Bar with 'element Id' :" + elementId + " is not present!");

	}


	public void scrollUntilElementIsView(WebElement ele) {
		this.newWaitForElementVisible(ele);
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", ele);
	}

	public void newWaitForChangeInLengthOfValue(final WebElement we, final int valueLen) {

		logger.i("newWaitForChangeInLengthOfValue");
		

		

		ExpectedCondition<Boolean> newWaitCondition = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				boolean flag = false;
				if (we.getAttribute("value").length() != valueLen) {
					flag = true;
				}
				return flag;
			}
		};

		newWait.until(newWaitCondition);

		
	}

	public void appendText(WebElement we, String text) {
		logger.i("appendText, text=%s", text);
		we.sendKeys(text);
	}

	public void verifyScrollBarNotPresent(String elementId) {
		logger.i("verifyScrollBarPresent");
		String condition = "document.getElementById('" + elementId + "').scrollHeight > document.getElementById('"
				+ elementId + "').clientHeight";
		Object ret = ((JavascriptExecutor) driver).executeScript("return " + condition);
		Assert.assertEquals(ret, false, "Scroll Bar with 'element Id' :" + elementId + " is present!");
	}

	public void newWaitForWindowToClose() {

		final int windowCount = driver.getWindowHandles().size();

		logger.i("newWaitForWindowToClose");
		if (windowCount > 1) {

			

			

			ExpectedCondition<Boolean> windowClosedCondition = new ExpectedCondition<>() {
				public Boolean apply(WebDriver driver) {
					assert driver != null;
					return driver.getWindowHandles().size() < windowCount;
				}
			};

			newWait.until(windowClosedCondition);

		}

		switchToMainWindow();
		

	}

	public boolean isAlertPresent() {

		boolean presentFlag = false;

		try {
			logger.i("isAlertPresent");

			if (newWait.until(ExpectedConditions.alertIsPresent()) != null) {
				// Alert present; set the flag
				presentFlag = true;
			}  // Alert present; set the flag

			// Check the presence of alert
			// driver.switchTo().alert();
			// Alert present; set the flag
			// presentFlag = true;
			// driver.switchTo().defaultContent();

		} catch (NoAlertPresentException ex) {
			// Alert not present
			logger.e(ex);
		}
		return presentFlag;
	}

	public void selectByValue(WebElement we, String value) {
		this.sleep(2000,"For showing case Create page");
		logger.i("selectByValue, label=%s", value);
		Select select = new Select(we);
		select.selectByValue(value);
	}

	public void selectByVisibleText(WebElement we, String value) {
		logger.i("selectByValue, label=%s", value);
		Select select = new Select(we);
		select.selectByVisibleText(value);
	}

	public void verifyIsEnabled(WebElement we) {

		logger.i("verifyIsEnabled");

		Assert.assertTrue(isEnabled(we), "Element '" + we + "' is not enabled!");
	}

	public void verifyIsDisabled(WebElement we) {

		logger.i("verifyIsDisabled");
		Assert.assertFalse(isEnabled(we), "Element '" + we + "' is not enabled!");
	}

	public boolean isEnabled(WebElement we) {
		logger.i("isEnabled");
		return we.isEnabled();
	}

	public boolean isReadOnly(WebElement we) {
		logger.i("isReadOnly");
		return Boolean.parseBoolean(we.getAttribute("readonly"));
	}

	public boolean findElementByXpath(String object) {
		logger.i("findElementByXpath");
		try {
			driver.findElement(By.xpath(object));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public String getTextColor(WebElement we) {
		logger.i("getTextColor");
		String color = we.getCssValue("color");

		if (!color.contains("#")) {
			String[] numbers = color.replace("rgba(", "").replace(")", "").split(",");
			int r = Integer.parseInt(numbers[0].trim());
			int g = Integer.parseInt(numbers[1].trim());
			int b = Integer.parseInt(numbers[2].trim());
			color = "#" + Integer.toHexString(r) + Integer.toHexString(g) + Integer.toHexString(b);
		}
		return color;
	}

	public void mouseHoverOnElement(WebElement we) {

		Actions action = new Actions(driver);
		action.moveToElement(we).moveToElement(we).build().perform();
		this.sleep(2000,"Check Is Hover Show Detail");
	}


	public void moveOnElement(WebElement we) {
		logger.i("moveOnElement "+we.getText());

		Actions actions = new Actions(driver);
		actions.moveToElement(we);
		actions.perform();

	}

	public WebElement FindElementByCss(String object) {
		logger.i("findElementByCSS");
		try {
            return driver.findElement(By.cssSelector(object));
		} catch (Exception e) {
			return null;
		}
	}

	public List<WebElement> FindElementsByCss(String object) {
		List<WebElement> element;
		try {
			element = driver.findElements(By.cssSelector(object));
			return element;
		} catch (Exception e) {
			return null;
		}

	}

	public List<String> getListItemFromString(String stringArray) {

		List<String> secondDropDownItemList;
		try {
			secondDropDownItemList = new ArrayList<>(Arrays.asList(stringArray.split(",")));
			return secondDropDownItemList;
		} catch (Exception e) {
			return null;
		}
	}

	public void selectItemFromListItem(List<WebElement> we,String selectItem) {
		try {
			boolean isitemGet=false;
			for(WebElement DropDownItem:we){
				if(DropDownItem.getText().trim().contains(selectItem)) {
					Actions action = new Actions(driver);
					logger.i("mouseHoverClickOnElement "+selectItem);
					isitemGet=true;
					action.moveToElement(DropDownItem).click().build().perform();
					break;
				}
			}

			if(!isitemGet) {
				logger.e("Selected item is Not Found");
			}

		} catch (Exception e) {
			Assert.fail(e.getMessage());
			logger.e(e.getMessage());
		}
	}

	public void scrollHorizontalUntillElementIsView(WebElement ele) {
		((JavascriptExecutor) driver).executeScript("window.scrollBy(1000,0)", ele);
		logger.i("Scroll Horzontal");
		this.sleep(2000,"Element find");
	}

	public void scrollHorizontalUntillListElementsIsView(List<WebElement> eleList) {
		((JavascriptExecutor) driver).executeScript("window.scrollBy(1000,0)", eleList);
		logger.i("Scroll Horzontal");
		this.sleep(2000,"Element find");

	}


	public boolean isClickable(WebElement el) {
		try{
			newWait.until(ExpectedConditions.elementToBeClickable(el));
			return true;
		}
		catch (Exception e){
			return false;
		}
	}
}