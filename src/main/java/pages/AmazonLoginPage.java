package pages;


import configuration.BaseDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import org.testng.Assert;


public class AmazonLoginPage extends AbstractionPOM{

    public static String GoToUrl = null;

    public AmazonLoginPage(BaseDriver bdriver) {
        super(bdriver);
    }

    @FindBy(xpath = "//button[@type='submit']")
    private WebElement LoginButton;

    @FindBy(xpath = "//input[@id='email']")
    private WebElement EmailIdInput;

    @FindBy(xpath = "//input[@id='password']")
    private WebElement PasswordInput;

    //will Navigate to Amazon Home Page
    public void LoginIn(String emailId,String password) {
        try {

        } catch (Exception e) {
            Assert.fail("Login Failed On " + GoToUrl+" because of ");
        }
    }


}
