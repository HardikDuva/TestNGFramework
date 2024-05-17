package pages;


import configuration.BaseDriver;
import configuration.BaseTest;
import org.openqa.selenium.support.PageFactory;

public class AbstractionPOM extends BaseTest {

	// Default constructor
	public AbstractionPOM(BaseDriver bdriver) {
		this.bdriver = bdriver;
		// This initElements method will create all WebElements
		PageFactory.initElements(bdriver.driver, this);
	}
	
	public void updateBDriver(BaseDriver bdriver) {
		this.bdriver = bdriver;
		// This initElements method will create all WebElements
				PageFactory.initElements(bdriver.driver, this);
	}
	
}
