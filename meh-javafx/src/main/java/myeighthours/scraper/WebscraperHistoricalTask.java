package myeighthours.scraper;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import myeighthours.Fichaje;
import myeighthours.MehOptions;
import org.joda.time.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class WebscraperHistoricalTask extends WebscraperTaskAbstract {

    private static final Logger LOG = LoggerFactory.getLogger(WebscraperHistoricalTask.class);

    public static final String PHANTOM_EXE_PATH = "drivers/phantomjs-2.1.1-windows/bin/phantomjs.exe";

    public static final String CHROMEDRIVER_EXE_PATH = "drivers/chromedriver-2.34-win32/chromedriver.exe";

    private static final String PATRON_FORMATO_FECHA_FICHAJE_WEB = "dd/MM/yyyy HH:mm";

    private static final DateTimeZone DATETIMEZONE_LOCAL = DateTimeZone.getDefault();

    private static final Locale DATETIMEZONE_PARSE_LOCALE = Locale.getDefault();

    public static final DateTimeFormatter FORMATO_FECHA_FICHAJE_WEB = DateTimeFormat.forPattern(PATRON_FORMATO_FECHA_FICHAJE_WEB).withLocale(DATETIMEZONE_PARSE_LOCALE).withZone(DATETIMEZONE_LOCAL);

    private MehOptions options;

    private long timestampStart = 0;

    private long timestampFinish = 0;

    private DateTime timeStart;
    private DateTime timeEnd;

    public WebscraperHistoricalTask(MehOptions options, DateTime timeStart, DateTime timeEnd) {
        this.options = options;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
    }

    private WebscraperHistoricalTask() {
        throw new UnsupportedOperationException("Private constructor nor supported");
    }

    @Override
    protected List<Fichaje> call() throws Exception {
        LOG.info("Webscraper task start");
        List<Fichaje> fichajes = new ArrayList<>();
        timestampStart = System.currentTimeMillis();
        try {
            fichajes.addAll(escarbarFichajes());
        } finally {
            timestampFinish = System.currentTimeMillis();
        }
        return fichajes;
    }

    private List<Fichaje> escarbarFichajes() throws Exception {
        List<Fichaje> fichajes = new ArrayList<>();
        List<Fichaje> fichajesRecolectados = new ArrayList<>();
        WebDriver driver = null;
        try {
            driver = createDriver(options);
            driver.get("https://portaldelempleado.teltronic.es/epsilonnet");
            webscraperWebLoginIfNeeded(driver);
            webscraperNavigateToFichajes(driver);
            for (int i = 0; i<11; i++) {
                LOG.info("Webscraper Historical Data start: Page #"+i);
                List<Fichaje> fichajesParseados = webscraperParseFichajes(driver);
                fichajesRecolectados.addAll(fichajesParseados);
                boolean hayMasPaginas = calculaHayMasPaginasDeFichajes(driver);
                boolean heSobrepasadoLasFechasBuscadas = calculaSiHeSobrepasadoLasFechasBuscadas(fichajesParseados, timeStart, timeEnd);
                if (hayMasPaginas && !heSobrepasadoLasFechasBuscadas) {
                    navegarSiguientePaginaFichajes(driver);
                } else{
                    LOG.info("Webscraper Historical Data stop.");
                    LOG.info("hayMasPaginas"+hayMasPaginas);
                    LOG.info("heSobrepasadoLasFechasBuscadas"+heSobrepasadoLasFechasBuscadas);
                    break; //Salir del bucle
                }
            }
            List<Fichaje> fichajesFiltrados = filtrarFichajesAUnaSemana(fichajesRecolectados, timeStart, timeEnd);
            fichajes.addAll(fichajesFiltrados);
        } finally {
            LOG.info("Webscraper historical task finish");
            if (driver != null) {
                driver.close();
                driver.quit();
            }
        }
        return fichajes;
    }

    private List<Fichaje> filtrarFichajesAUnaSemana(List<Fichaje> fichajesRecolectados, DateTime timeStart, DateTime timeEnd) {
        List<Fichaje> fichajesFiltrados = new ArrayList<>();
        long timeStartMillis = timeStart.toDateTime(DATETIMEZONE_LOCAL).getMillis();
        long timeEndMillis = timeEnd.toDateTime(DATETIMEZONE_LOCAL).getMillis();

        for ( Fichaje fichajesRecolectado : fichajesRecolectados ){
            long fechaMarcaje = fichajesRecolectado.getFechaMarcaje();
            if (fechaMarcaje>=timeStartMillis && fechaMarcaje<=timeEndMillis) {
                fichajesFiltrados.add(fichajesRecolectado);
            }
        }
        return fichajesFiltrados;
    }

    private void navegarSiguientePaginaFichajes(WebDriver driver) {
        WebElement pageForwardLink = driver.findElement(By.id("ctl02_GRD_SELECT_MARCAJE_pageFORWARD"));
        pageForwardLink.click();
    }

    public boolean webelementHasClass( WebElement element, String className) {
        String classes = element.getAttribute("class");
        for (String c : classes.split(" ")) {
            if (c.equals(className)) {
                return true;
            }
        }
        return false;
    }

    private boolean calculaHayMasPaginasDeFichajes(WebDriver driver) {
        WebElement pageForwardLink = driver.findElement(By.id("ctl02_GRD_SELECT_MARCAJE_pageFORWARD"));
        boolean pageForwardLinkIsDisabled = webelementHasClass(pageForwardLink, "aspNetDisabled");
        boolean hayMasPaginas = !pageForwardLinkIsDisabled;
        return hayMasPaginas;
    }

    private boolean calculaSiHeSobrepasadoLasFechasBuscadas(List<Fichaje> fichajesParseados, DateTime timeStart, DateTime timeEnd) {
        //Aqui hay que recorrer los fichajes, y ver si la fecha del marcaje es mas antigua que las parseadas.
        //Si es asi, significa que ya tenemos los fichajes de las fechas buscadas, y hay que detener la busqueda (heSobrepasado=true)
        long timeStartMillis = timeStart.toDateTime(DATETIMEZONE_LOCAL).getMillis();
        long timeEndMillis = timeEnd.toDateTime(DATETIMEZONE_LOCAL).getMillis();

        boolean heSobrepasadoLasFechas = false;
        for (Fichaje fichajesParseado : fichajesParseados) {
            long fechaMarcaje = fichajesParseado.getFechaMarcaje();
            boolean fechaMarcajeEsMasAntiguoQueTimeStart = fechaMarcaje<timeStartMillis;
            if (fechaMarcajeEsMasAntiguoQueTimeStart) {
                heSobrepasadoLasFechas = true;
                break;
            }
        }
        return heSobrepasadoLasFechas;
    }

    private WebDriver createDriver(MehOptions options) {
        switch (options.getWebdriver()) {
            case PHANTOMJS:
                return createPhantomjsDriver();
            case HTMLUNIT:
                return createHtmlunitDriver();
            default:
                throw new UnsupportedOperationException("Driver type \"" + options.getWebdriver() + "\" not supported.");
        }
    }

    private WebDriver createHtmlunitDriver() {
        HtmlUnitDriver driver = new HtmlUnitDriver(BrowserVersion.CHROME);
        driver.setJavascriptEnabled(true);
        return driver;
    }

    private WebDriver createFirefoxDriver() {
        WebDriver driver = new FirefoxDriver();
        return driver;
    }

    private WebDriver createChromeHeadlessDriver() {
        System.setProperty("webdriver.chrome.driver", CHROMEDRIVER_EXE_PATH);
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless");
        WebDriver driver = new ChromeDriver(chromeOptions);
        return driver;
    }

    private WebDriver createChromeDriver() {
        System.setProperty("webdriver.chrome.driver", CHROMEDRIVER_EXE_PATH);
        ChromeOptions chromeOptions = new ChromeOptions();
        //chromeOptions.addArguments("--headless");
        WebDriver driver = new ChromeDriver(chromeOptions);
        return driver;
    }

    private WebDriver createPhantomjsDriver() {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setJavascriptEnabled(true);
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, PHANTOM_EXE_PATH);
        WebDriver driver = new PhantomJSDriver(caps);
        driver.manage().window().setSize(new Dimension(1280, 720));
        return driver;
    }

    private void webscraperWebLoginIfNeeded(WebDriver driver) throws Exception {
        boolean usernameInputExists = !(driver.findElements(By.id("SignIn1_tbUserName")).isEmpty());
        boolean passwordInputExists = !(driver.findElements(By.id("SignIn1_tbPassword")).isEmpty());
        boolean loginButtonExists = !(driver.findElements(By.id("SignIn1_Img_Entrar_img")).isEmpty());

        boolean currentPageIsLoginWeb = (usernameInputExists && passwordInputExists && loginButtonExists);

        if (currentPageIsLoginWeb) {
            webscraperWebLogin(driver);
        }
    }

    private void webscraperWebLogin(WebDriver driver) throws Exception {
        WebElement usernameInput = driver.findElement(By.id("SignIn1_tbUserName"));
        WebElement passwordInputExists = driver.findElement(By.id("SignIn1_tbPassword"));
        WebElement loginButtonExists = driver.findElement(By.id("SignIn1_Img_Entrar_img"));

        //Send login
        usernameInput.sendKeys(options.getUsername());
        passwordInputExists.sendKeys(options.getPassword());
        loginButtonExists.click();

        //Miramos a ver si volvemos a la web de login con error
        boolean loginErrorLabelExists = !(driver.findElements(By.id("SignIn1_ErrorLabel")).isEmpty());
        if (loginErrorLabelExists) {
            WebElement loginErrorLabel = driver.findElement(By.id("SignIn1_ErrorLabel"));
            String loginErrorLabelText = loginErrorLabel.getText();
            throw new Exception("Error en login: " + loginErrorLabelText);
        }
    }

    @Override
    public long getTimestampFinish() {
        return timestampFinish;
    }

    @Override
    public long getTimestampStart() {
        return timestampStart;
    }

    private void webscraperNavigateToFichajes(WebDriver driver) throws Exception {

        //Primer clic
        WebElement linkGestion = driver.findElement(By.id("Menu_GestSolic_internallink"));
        linkGestion.click();

        //Segundo clic
        String linkSolicitudesId = "ctl06_repGrupos_ctl03_repOpc_ctl00_HyperLink1";
        WebElement linkSolicitudes = driver.findElement(By.id(linkSolicitudesId));
        linkSolicitudes.click();

        //Tercer clic
        List<WebElement> todasImagenes1 = driver.findElements(By.tagName("img"));
        String imagenRelojScr = "images/tip_solic/clock.png";
        for (WebElement imagen : todasImagenes1) {
            String imageScr = imagen.getAttribute("src");
            if (imageScr != null && imageScr.endsWith(imagenRelojScr)) {
                imagen.click();
                break;
            }
        }

        //Clic en nueva modificacion de fichaje
        List<WebElement> todasImagenes2 = driver.findElements(By.tagName("img"));
        String imagenNuevoFichajeScr = "images/newlook/form_blue_add.png";
        for (WebElement imagen : todasImagenes2) {
            String imageScr = imagen.getAttribute("src");
            if (imageScr != null && imageScr.endsWith(imagenNuevoFichajeScr)) {
                imagen.click();
                break;
            }
        }

        //Entrar en el frame de fichajes
        driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
        driver.switchTo().frame(driver.findElement(By.xpath("//iframe[contains(@src,'Generic.aspx')]")));
    }

    private static Fichaje parseFichajesInsideTrAutogenerado(WebDriver driver, List<WebElement> tableColumns) {
        Fichaje.Builder fichajeBuilder = new Fichaje.Builder();
        fichajeBuilder.tipo(Fichaje.TIPO.AUTOGENERADO);
        return fichajeBuilder.build();
    }

    private static Fichaje parseFichajesInsideTrReloj(WebDriver driver, List<WebElement> tableColumns) throws Exception {

        Fichaje.Builder fichajeBuilder = new Fichaje.Builder();
        fichajeBuilder.tipo(Fichaje.TIPO.RELOJ);

        String fechaMarcaje = tableColumns.get(0).findElement(By.tagName("span")).getAttribute("innerHTML");
        String imagenDireccion = tableColumns.get(1).findElement(By.tagName("img")).getAttribute("src");
        String horaMarcaje = tableColumns.get(2).findElement(By.tagName("span")).getAttribute("innerHTML");
        String estado = tableColumns.get(3).findElement(By.tagName("span")).getAttribute("innerHTML");
        //String tipo = tableColumns.get(4).getText();
        String terminal = tableColumns.get(5).findElement(By.tagName("span")).getAttribute("innerHTML");

        if (fechaMarcaje.isEmpty() || horaMarcaje.isEmpty()) throw new Exception("Hora o fecha de marcaje vacío");

        //Parseo de fecha marcaje
        DateTime dateTime;
        String dateString = "";
        try {
            dateString = fechaMarcaje + " " + horaMarcaje;
            dateTime = FORMATO_FECHA_FICHAJE_WEB.parseDateTime(dateString);
        } catch (IllegalArgumentException iae) {
            LOG.error("No ha sido posible parsear \"" + dateString + "\" usando el patron de fecha \"" + PATRON_FORMATO_FECHA_FICHAJE_WEB + "\"");
            throw iae;
        }

        long absoluteMillis = dateTime.getMillis();
        fichajeBuilder.fechaMarcaje(absoluteMillis);

        //Parseo de indicador entrada-salida
        int index = imagenDireccion.lastIndexOf('/');
        String imageName = imagenDireccion.substring(index + 1);
        if (imageName.contains("in")) {
            fichajeBuilder.direccion(Fichaje.DIRECCION.ENTRADA);
        } else {
            fichajeBuilder.direccion(Fichaje.DIRECCION.SALIDA);
        }

        //Terminal
        int terminalParsed = -1;
        try {
            terminalParsed = Integer.parseInt(terminal);
        } catch (NumberFormatException nfe) {
            LOG.error("Excepción parseando terminal \"" + terminal + "\"");
            terminalParsed = -1;
        }
        fichajeBuilder.terminal(terminalParsed);

        return fichajeBuilder.build();
    }

    private static Fichaje parseFichajesInsideTr(WebDriver driver, WebElement tableRow) throws Exception {

        List<WebElement> rowColumns = tableRow.findElements(By.tagName("td"));
        WebElement rowColumns_type = rowColumns.get(4);
        WebElement typeColumnSpan = rowColumns_type.findElement(By.tagName("span"));
        String spanHtml = String.valueOf(typeColumnSpan.getAttribute("innerHTML"));
        Fichaje fichaje;
        String tipo = spanHtml;
        if ("RELOJ".equalsIgnoreCase(tipo)) {
            fichaje = parseFichajesInsideTrReloj(driver, rowColumns);
        } else if ("AUTOGENERADO".equalsIgnoreCase(tipo)) {
            fichaje = parseFichajesInsideTrAutogenerado(driver, rowColumns);
        } else {
            String tdHtmlError = String.valueOf(rowColumns_type.getAttribute("innerHTML"));
            throw new Exception("Tipo de fichaje desconocido: \"" + tipo + "\"");
        }
        return fichaje;
    }

    private List<Fichaje> webscraperParseFichajes(WebDriver driver) throws Exception {
        return parseFichajes(driver);
    }

    private static boolean isHeaderRow(WebElement webdriverTableRow) {
        return "GridHeaderStyle".equals(webdriverTableRow.getAttribute("class"));
    }

    public static List<Fichaje> parseFichajesInsideIframe(WebDriver driver) throws Exception {
        List<Fichaje> fichajes = new ArrayList<>();
        WebElement gridDataDiv = driver.findElement(By.className("GridData"));
        WebElement table = gridDataDiv.findElement(By.tagName("table"));
        List<WebElement> tableRows = table.findElements(By.tagName("tr"));
        for (int i = 0; i < tableRows.size(); i++) {
            WebElement tableRow = tableRows.get(i);
            if (isHeaderRow(tableRow)) continue;
            try {
                Fichaje fichaje = parseFichajesInsideTr(driver, tableRow);
                if (fichaje.getTipo().equals(Fichaje.TIPO.RELOJ)) {
                    fichajes.add(fichaje);
                }
            } catch (Exception e) {
                String tableRowToHtml = String.valueOf(tableRow.getAttribute("innerHTML"));
                LOG.error("Información de fila de fichaje no válida: \n" + tableRowToHtml, e);
            }

        }
        return fichajes;
    }

    public static List<Fichaje> parseFichajes(WebDriver driver) throws Exception {
        return parseFichajesInsideIframe(driver);
    }

}
