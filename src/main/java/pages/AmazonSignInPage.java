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

    @FindBy(xpath = "//div[@id=\"nav-al-signin\"]" +
            "//a[@class=\"nav-action-signin-button\"]")
    private WebElement signInNavActionButton;

    @FindBy(xpath = "//input[@id=\"continue\"]")
    private WebElement continueButton;

    @FindBy(xpath = "//input[@id=\"signInSubmit\"]")
    private WebElement signInButton;

    @FindBy(xpath = "//input[@id='ap_email']")
    private WebElement EmailIdInput;

    @FindBy(xpath = "//input[@id='ap_password']")
    private WebElement PasswordInput;

    //will Navigate to Amazon Home Page
    public void LoginIn(String emailId,String password) {
        try {
            bdriver.waitForElementVisible(EmailIdInput);
            bdriver.inputText(EmailIdInput,emailId);

            bdriver.waitForElementVisible(continueButton);
            bdriver.clickAndWait(continueButton);

            bdriver.waitForElementVisible(PasswordInput);
            bdriver.inputText(PasswordInput,password);

            bdriver.waitForElementVisible(signInButton);
            bdriver.clickAndWait(signInButton);

        } catch (Exception e) {
            Assert.fail("Login Failed On " + GoToUrl);
        }
    }

    //will Navigate to Amazon Sign in Page
    public void clickOnSignInButton() {
        try {
            bdriver.waitForElementVisible(signInNavActionButton);
            bdriver.clickAndWait(signInNavActionButton);

        } catch (Exception e) {
            Assert.fail("Sign in page is not opened " + GoToUrl);
        }
    }
}
