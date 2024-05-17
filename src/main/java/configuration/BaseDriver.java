package configuration;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.*;

import static configuration.BaseTest.fwConfigData;

public class BaseDriver {

	public  WebDriver driver = null;
	private final Browsers browser;
	private MutableCapabilities options = null;
	private final long limit;
	String outputPath;
	protected AutoLogger logger = new AutoLogger(BaseTest.class);

	private static final ThreadLocal<RemoteWebDriver> REMOTE_WEB_DRIVER
			= new ThreadLocal<>();

	public enum Browsers {
		FIREFOX, EDGE, CHROME, SAFARI
	}

	public BaseDriver(String browser,
                      long limit, String outputDir) {
		this.browser = parseBrowser(browser);
		this.options = parseMutableCapabilities();
		this.limit = limit;
		this.outputPath = outputDir;
		loadWebDriverObject();
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

			REMOTE_WEB_DRIVER.get()
					.manage()
					.window()
					.maximize();
		}
	}

	/**
	 * Go to the URL
	 * @param urlAddress - The URL Address
	 */
	public void gotoUrl(String urlAddress) {
		logger.i("gotoUrl: " + urlAddress);
		driver.get(urlAddress);
	}
}