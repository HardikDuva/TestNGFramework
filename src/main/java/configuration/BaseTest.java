package configuration;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.TestListenerAdapter;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import pages.AmazonLoginPage;

public class BaseTest extends TestListenerAdapter {

	public static BaseDriver bdriver = null;
	protected String outputDir;
	public static String browser;
	public static ReadXMLData fwConfigData = new ReadXMLData("./TestData/Configuration.xml");
	AutoLogger logger = new AutoLogger(BaseDriver.class);

	@BeforeSuite(alwaysRun = true)
	@Parameters({"Project","Browser","Environment"})
	public void beforeSuite(String browser,
			 @Optional ITestContext testContext) {
		BaseTest.browser = browser;
		outputDir = testContext.getOutputDirectory();

		// Open browser
		try {
			int implicitTimeout = Integer.parseInt(fwConfigData.get("Configuration", "ImplicitTimeout"));
			int explicitTimeout = Integer.parseInt(fwConfigData.get("Configuration", "ExplicitTimeout"));
			bdriver = new BaseDriver(browser, implicitTimeout,explicitTimeout, outputDir);
			String url = fwConfigData.get("Configuration", "URL");
			bdriver.gotoUrl(url);
		}
		catch (Exception e) {
			Assert.fail("Open browser failed!" + e);
		}
	}

	@AfterSuite(alwaysRun = true)
	public void afterSuite() {
		  //Close Browser
		  bdriver.driver.close();
	}

	private AmazonLoginPage obAmazonLogin =null;

	public AmazonLoginPage ObAmazonLogin() {
		try {
			if (obAmazonLogin == null) {
				obAmazonLogin = new AmazonLoginPage(bdriver);
			}
		} catch (Exception e) {
			logger.e(e.getMessage());
		}
		Assert.assertNotNull(obAmazonLogin, "Login Page is not initialized!");
		return obAmazonLogin;
	}
}
	
	