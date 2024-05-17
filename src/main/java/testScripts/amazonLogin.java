package testScripts;

import configuration.BaseTest;
import configuration.ReadXMLData;
import org.testng.annotations.Test;

public class amazonLogin extends BaseTest {

	protected ReadXMLData LoginData = new ReadXMLData(
			"./TestData/Login/LoginData.xml");

	String UserName = LoginData.get("UserDetails", "UserName");
	String Password = LoginData.get("UserDetails", "Password");

	@Test(enabled = true)
	public void LoginIntoAmazon()  {

	}
}


