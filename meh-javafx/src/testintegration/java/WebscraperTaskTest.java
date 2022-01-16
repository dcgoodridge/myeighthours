import junit.framework.TestCase;
import myeighthours.Fichaje;
import myeighthours.scraper.WebscraperTask;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.net.URL;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static myeighthours.scraper.WebscraperTask.CHROMEDRIVER_EXE_PATH;
import static myeighthours.scraper.WebscraperTask.PHANTOM_EXE_PATH;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class WebscraperTaskTest extends TestCase {

    private static final DateTimeZone DATETIMEZONE_LOCAL = DateTimeZone.getDefault();

    private static ZoneId ZONE_ID = ZoneId.systemDefault();

    public static List<Fichaje> leerFichajesTest(File htmlFile) throws Exception {
        List<Fichaje> fichajes;
        WebDriver driver = null;
        try {
            driver = createPhantomjsDriver();
            String localhost_file_url = "file:///" + htmlFile.getAbsolutePath();
            driver.get(localhost_file_url);
            fichajes = WebscraperTask.parseFichajesInsideIframe(driver);
        } finally {
            if (driver != null) {
                driver.close();
                driver.quit();
            }
        }
        return fichajes;
    }

    @Test
    public void testParseHtmlFechaMarcajeNormal() throws Exception {
        URL urlResource = getClass().getResource("/tabla_marcajes_normal.html");
        File file = new File(urlResource.getFile());
        List<Fichaje> fichajes = leerFichajesTest(file);
        assertThat(fichajes.get(0).getFechaMarcaje(), is(new DateTime(2016, 3, 16, 14, 19).getMillis()));
        assertThat(fichajes.get(1).getFechaMarcaje(), is(new DateTime(2016, 3, 16, 13, 30).getMillis()));
        assertThat(fichajes.get(2).getFechaMarcaje(), is(new DateTime(2016, 3, 16, 8, 48).getMillis()));
        assertThat(fichajes.get(3).getFechaMarcaje(), is(new DateTime(2016, 3, 15, 18, 43).getMillis()));
        assertThat(fichajes.get(4).getFechaMarcaje(), is(new DateTime(2016, 3, 15, 14, 13).getMillis()));
        assertThat(fichajes.get(5).getFechaMarcaje(), is(new DateTime(2016, 3, 15, 13, 25).getMillis()));
        assertThat(fichajes.get(6).getFechaMarcaje(), is(new DateTime(2016, 3, 15, 8, 54).getMillis()));
        assertThat(fichajes.get(7).getFechaMarcaje(), is(new DateTime(2016, 3, 14, 18, 13).getMillis()));
        assertThat(fichajes.get(8).getFechaMarcaje(), is(new DateTime(2016, 3, 14, 14, 14).getMillis()));
        assertThat(fichajes.get(9).getFechaMarcaje(), is(new DateTime(2016, 3, 14, 13, 28).getMillis()));
        assertThat(fichajes.size(),is(10));
    }

    @Test
    public void testParseHtmlFechaMarcajeVacaciones() throws Exception {
        URL urlResource = getClass().getResource("/tabla_marcajes_vacaciones.html");
        File file = new File(urlResource.getFile());
        List<Fichaje> fichajes = leerFichajesTest(file);
        assertThat(fichajes.get(0).getFechaMarcaje(), is(new DateTime(2018, 5, 2, 14, 13).getMillis()));
        assertThat(fichajes.get(1).getFechaMarcaje(), is(new DateTime(2018, 5, 2, 13, 54).getMillis()));
        assertThat(fichajes.get(2).getFechaMarcaje(), is(new DateTime(2018, 5, 2, 9, 9).getMillis()));
    }

    @Test
    public void testParseHtmlFechaMarcajeFilasErroneas() throws Exception {
        URL urlResource = getClass().getResource("/tabla_marcajes_filaserroneas.html");
        File file = new File(urlResource.getFile());
        List<Fichaje> fichajes = leerFichajesTest(file);
        assertThat(fichajes.size(), is(0));
    }

    @Test
    public void testNumeroFichajes() throws Exception {
        URL urlResource = getClass().getResource("/tabla_marcajes_normal.html");
        File file = new File(urlResource.getFile());
        List<Fichaje> fichajes = leerFichajesTest(file);
        assertThat(fichajes.size(), is(10));
    }

    private static WebDriver createPhantomjsDriver() {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setJavascriptEnabled(true);
        caps.setCapability("takesScreenshot", true);
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, PHANTOM_EXE_PATH);
        WebDriver driver = new PhantomJSDriver(caps);
        return driver;
    }

    @Test
    public void testChromeDriver_1() throws Exception {
        String websiteUrl = "http://www.google.com";
        System.setProperty("webdriver.chrome.driver", CHROMEDRIVER_EXE_PATH);
        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.get(websiteUrl);
        driver.close();
    }

    @Test
    public void testChromeDriver_headless() throws Exception {
        System.setProperty("webdriver.chrome.driver", CHROMEDRIVER_EXE_PATH);
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless");
        WebDriver driver = new ChromeDriver(chromeOptions);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.get("http://www.google.com");
        driver.close();
    }

    @Test
    public void testPhantomJS_headless() throws Exception {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setJavascriptEnabled(true);
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, PHANTOM_EXE_PATH);
        WebDriver driver = new PhantomJSDriver(caps);
        driver.manage().window().setSize(new Dimension(1280, 720));
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.get("http://www.google.com");
        driver.close();
    }

    @Test
    public void testHtmlunit_seleniumdriver() throws Exception {
        HtmlUnitDriver driver = new HtmlUnitDriver();
        driver.setJavascriptEnabled(true);
        driver.get("http://www.google.com");
        driver.close();
    }

}
