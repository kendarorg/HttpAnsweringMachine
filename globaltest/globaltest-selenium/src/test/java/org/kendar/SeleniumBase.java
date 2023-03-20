package org.kendar;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.kendar.globaltest.ProcessRunner;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class SeleniumBase implements BeforeAllCallback,ExtensionContext.Store.CloseableResource, AfterAllCallback {
    public static FirefoxDriver getDriver() {
        return driver;
    }

    private static FirefoxDriver driver;
    private static JavascriptExecutor js;
    private static boolean started = false;

    @Override
    public void close() throws Throwable {
        try {
            driver.quit();
        }catch (Exception ex){

        }
        started=false;
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        if(started)return;
        started = true;
        try {
            var firefoxExecutable = SeleniumBase.findFirefox();
            //WebDriverManager.firefoxdriver().setup();
            File pathBinary = new File(firefoxExecutable);
            FirefoxBinary firefoxBinary = new FirefoxBinary(pathBinary);
            DesiredCapabilities desired = new DesiredCapabilities();
            FirefoxOptions options = new FirefoxOptions();
            desired.setCapability(FirefoxOptions.FIREFOX_OPTIONS, options.setBinary(firefoxBinary));
            //driver = new HtmlUnitDriver();
            driver = new FirefoxDriver(options);
            js = (JavascriptExecutor) driver;

            System.out.println("here it is " + firefoxExecutable);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    private static final String INSTALLED_PROGRAMS = "powershell -command \"Get-ItemProperty HKLM:\\\\Software\\\\Wow6432Node\\\\Microsoft\\\\Windows\\\\CurrentVersion\\\\Uninstall\\\\* | Select-Object DisplayName, InstallLocation | Format-Table –AutoSize\"";
    public static String findFirefox() throws Exception {
        var env = new HashMap<String,String>();
        if(env.containsKey("FIREFOX_PATH")){
            return env.get("FIREFOX_PATH");
        }
        if(SystemUtils.IS_OS_WINDOWS){
            var queue = new ConcurrentLinkedQueue<String>();
            new ProcessRunner(env).
                    withCommand("powershell").
                    withParameter("-command").
                    withParameter("Get-ItemProperty HKLM:\\Software\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\* | Select-Object DisplayName, InstallLocation | Format-Table –AutoSize").
                    withStorage(queue).
                    limitOutput(5).
                    run();
            new ProcessRunner(env).
                    withCommand("powershell").
                    withParameter("-command").
                    withParameter("Get-ItemProperty HKLM:\\Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\* | Select-Object DisplayName, InstallLocation | Format-Table –AutoSize").
                    withStorage(queue).
                    limitOutput(5).
                    run();
            var allJavaProcesses = queue.stream().
                    collect(Collectors.toList());

            for (var col : allJavaProcesses) {
                var lower = col.toLowerCase(Locale.ROOT);
                if(lower.contains("firefox")){
                    var index = col.indexOf(":\\");
                    if(index>0){
                        index--;
                        return col.substring(index).trim()+"\\Firefox.exe";
                    }
                }
            }
        }else{
            var queue = new ConcurrentLinkedQueue<String>();
            new ProcessRunner(env).
                    withCommand("whereis").
                    withParameter("firefox").
                    withStorage(queue).
                    run();
            var allJavaProcesses = String.join(" ",queue.stream().
                    collect(Collectors.toList()));
            var all = allJavaProcesses.split("\\s+");
            for (var col : all) {
                var trimmed = col.trim().toLowerCase(Locale.ROOT);
                if(trimmed.endsWith("/firefox")){
                    return col;
                }
            }
        }
        throw new Exception("Firefox not found!");
    }


    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        try {
            driver.quit();
        }catch (Exception ex){

        }
        started=false;
    }
}
