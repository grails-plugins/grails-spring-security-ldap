import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver

// default is to use htmlunit
driver = {
    HtmlUnitDriver htmlUnitDriver = new HtmlUnitDriver()
    htmlUnitDriver.javascriptEnabled = true
    htmlUnitDriver
}

environments {

    htmlUnit {
        driver = {
            HtmlUnitDriver htmlUnitDriver = new HtmlUnitDriver()
            htmlUnitDriver.javascriptEnabled = true
            htmlUnitDriver
        }
    }
    // run via “./gradlew -Dgeb.env=chrome iT”
    chrome {
        driver = { new ChromeDriver() }
    }

    // run via “./gradlew -Dgeb.env=chromeHeadless iT”
    chromeHeadless {
        driver = {
            ChromeOptions o = new ChromeOptions()
            o.addArguments('headless')
            new ChromeDriver(o)
        }
    }

    // run via “./gradlew -Dgeb.env=firefox iT”
    firefox {
        driver = { new FirefoxDriver() }
    }
}
