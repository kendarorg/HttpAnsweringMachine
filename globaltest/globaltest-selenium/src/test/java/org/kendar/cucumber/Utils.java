package org.kendar.cucumber;

import org.kendar.SeleniumBase;
import org.kendar.globaltest.HttpChecker;
import org.kendar.globaltest.ProcessUtils;
import org.kendar.globaltest.Sleeper;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

public class Utils {
    public static HashMap<String, Object> getCache() {
        return cache;
    }

    public static <T> T getCache(String key) {
        return (T)cache.get(key.toLowerCase(Locale.ROOT));
    }

    public static void setCache(String key,Object value) {
        if(value==null){
            cache.remove(key.toLowerCase(Locale.ROOT));
        }else {
            cache.put(key.toLowerCase(Locale.ROOT), value);
        }
    }

    private static HashMap<String,Object> cache = new HashMap<>();

    private static String rootPath;
    public static String getRootPath(Class<?> caller) {
        if (rootPath != null) {
            return rootPath;
        }
        final File jarFile =
                new File(caller.getProtectionDomain().getCodeSource().getLocation().getPath());
        var path = Path.of(jarFile.getAbsolutePath());
        return path.getParent()    //target
                .getParent()    //globaltest-selenium
                .getParent()    //globaltest
                .getParent().toAbsolutePath().toString();
    }

    public static boolean deleteDirectory(File directoryToBeDeleted) {
        if (!directoryToBeDeleted.exists()) {
            return true;
        }
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    public static WebElement scrollFind( Supplier<WebElement> supplier, long ... extraLength) throws Exception {
        var js = (JavascriptExecutor) getCache("driver");
        var result = js.executeScript("return Math.max(" +
                "document.body.scrollHeight," +
                "document.body.offsetHeight," +
                "document.body.clientHeight," +
                "document.documentElement.scrollHeight," +
                "document.documentElement.offsetHeight," +
                "document.documentElement.clientHeight" +
                ");").toString();
        var length = Integer.parseInt(result);
        for (int i = 0; i < length; i += 100) {
            js.executeScript("window.scrollTo(0," + i + ")");
            var we = supplier.get();
            if (we == null) {
                continue;
            }
            if(extraLength.length>0){
                js.executeScript("arguments[0].scrollIntoView(true);window.scrollBy(0,-100);", we);
                //js.executeScript("window.scrollTo(0," + (i+extraLength[0]) + ")");
            }
            return we;
        }
        throw new RuntimeException("Unable to find item!");
    }

    public static void waitForText(Supplier<WebElement> el){
        for (var i = 0; i < 10; i++) {
            var res = el.get();
            if (res == null) {
                Sleeper.sleep(1000);
                continue;
            }
        }
    }
    public static void doClick(Supplier<WebElement> el) {
        for (var i = 0; i < 10; i++) {
            var res = el.get();
            if (res == null) {
                Sleeper.sleep(1000);

                continue;
            }
            try {
                res.click();
                Sleeper.sleep(500);
                try {
                    res = el.get();
                    if (res != null) {
                        res.click();
                    }
                } catch (Exception ex) {
                    return;
                }
                return;
            } catch (Exception e) {
                System.out.println(e);
            }

            Sleeper.sleep(1000);

        }
        var res = el.get();

        throw new RuntimeException("Unable to click " + res.getText());
    }

    public static WebElement checkCheckBox(Supplier<WebElement> supplier) throws Exception {
        var driver = (JavascriptExecutor) getCache("driver");
        var el = supplier.get();
        if (!el.isSelected()) {
            var js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].click();", el);
            Thread.sleep(100);
        }
        return el;
    }

    public static WebElement uncheckCheckBox(Supplier<WebElement> supplier) throws InterruptedException {
        var driver = (JavascriptExecutor) getCache("driver");
        var el = supplier.get();
        if (el.isSelected()) {
            var js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].click();", el);
            Thread.sleep(100);
        }
        return el;
    }

    public static void showMessage( String message) throws InterruptedException {
        var driver = (WebDriver) getCache("driver");
        var js = (JavascriptExecutor)driver;
        js.executeScript("alert(\""+message+"\");");
        Sleeper.sleep(5000);
        driver.switchTo().alert().dismiss();
    }

    public static String getVersion() {
        var path = Path.of(getRootPath(SeleniumBase.class), "scripts", "version.txt");
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean shutdownHookInitialized = false;
    public static void initShutdownHook() {
        if (shutdownHookInitialized) return;
        shutdownHookInitialized = true;
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {

                try {
                    var driver = (WebDriver)getCache("driver");
                    if(driver!=null){
                        try {
                            driver.close();
                        }catch (Exception ex){}
                    }
                    var pu = new ProcessUtils(new HashMap<>());
                    HttpChecker.checkForSite(60, "http://127.0.0.1/api/shutdown").noError().run();
                    pu.sigtermProcesses((str) -> str.contains("-Dloader.main=org.kendar.Main"));
                    pu.killProcesses( (psLine) ->
                            psLine.contains("java") &&
                                    psLine.contains("httpanswering") &&
                                    !psLine.contains("globaltest"));
                    pu.killProcesses( (psLine) ->
                            psLine.contains("java") &&
                                    psLine.contains("org.h2.tools.Server") &&
                                    !psLine.contains("globaltest"));
                } catch (Exception e) {

                }
            }

        });
    }

    public static boolean navigateTo(String url){
        var driver = (WebDriver)Utils.getCache("driver");
        var current = driver.getCurrentUrl();
        if(current.equalsIgnoreCase(url)) return true;
        driver.get(url);
        return false;
    }

    public static void setTitle(String title){
        var driver = (JavascriptExecutor)Utils.getCache("driver");
        driver.executeScript("document.title = '"+title+"'");
    }
}
