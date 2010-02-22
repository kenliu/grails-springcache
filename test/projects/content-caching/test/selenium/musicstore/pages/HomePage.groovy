package musicstore.pages

import grails.plugins.selenium.pageobjects.GrailsPage

class HomePage extends GrailsPage {

	static HomePage open() {
		def page = new HomePage()
		page.selenium.open("/")
		return page
	}

	HomePage refresh() {
		selenium.refreshAndWait()
		return new HomePage()
	}

	String getLoggedInMessage() {
		return isUserLoggedIn() ? selenium.getText("loggedInUser") : null
	}

	boolean isUserLoggedIn() {
		selenium.isElementPresent("loggedInUser")
	}

	LoginPage goToLogin() {
		if (isUserLoggedIn()) {
			throw new PageStateException("Already logged in")
		} else {
			selenium.clickAndWait("css=#loginLink a")
			return new LoginPage()
		}
	}

	List<String> getLatestAlbums() {
		def list = []
		int i = 1
		while (selenium.isElementPresent("//div[@id='latestAlbums']/ol/li[$i]")) {
			list << selenium.getText("//div[@id='latestAlbums']/ol/li[$i]")
			i++
		}
		return list
	}

}