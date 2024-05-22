package configuration;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.TestListenerAdapter;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import pages.AmazonSignInPage;

public class BaseTest extends TestListenerAdapter {

	public static BaseDriver bdriver = null;
	protected String outputDir;
	public static String browser;
	public static ReadXMLData fwConfigData = new ReadXMLData("./TestData/Configuration.xml");
	protected AutoLogger logger = new AutoLogger(BaseTest.class);

	@BeforeSuite(alwaysRun = true)
	@Parameters({"Browser"})
	public void beforeSuite(String browser,
			 @Optional ITestContext testContext) {
		outputDir = testContext.getOutputDirectory();

		// Open browser
		try {
			int implicitTimeout = Integer.parseInt(fwConfigData.get("Configuration", "ImplicitTimeout"));
			int explicitTimeout = Integer.parseInt(fwConfigData.get("Configuration", "ExplicitTimeout"));
			bdriver = new BaseDriver(browser, implicitTimeout,explicitTimeout, outputDir);
        }

		catch (Exception e) {
			Assert.fail("Open browser failed!" + e);
		}

		String url = fwConfigData.get("Configuration", "URL");
		bdriver.gotoUrl(url);
	}

	@AfterSuite(alwaysRun = true)
	public void afterSuite() {
		  //Close Browser
		  bdriver.driver.close();
	}

	private AmazonSignInPage obAmazonSignIn =null;

	public AmazonSignInPage ObAmazonSignIn() {
		try {
			if (obAmazonSignIn == null) {
				obAmazonSignIn = new AmazonSignInPage(bdriver);
			}
		} catch (Exception e) {
			logger.e(e.getMessage());
		}
		Assert.assertNotNull(obAmazonSignIn, "Login Page is not initialized!");
		return obAmazonSignIn;
	}
}
	
	