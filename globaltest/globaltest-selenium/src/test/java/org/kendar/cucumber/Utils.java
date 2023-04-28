package org.kendar.cucumber;

import org.apache.commons.io.FileUtils;
import org.kendar.globaltest.HttpChecker;
import org.kendar.globaltest.ProcessUtils;
import org.kendar.globaltest.Sleeper;
import org.openqa.selenium.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

public class Utils {
    private static int counter = 0;
    private static HashMap<String, Object> cache = new HashMap<>();
    private static String rootPath;
    private static boolean shutdownHookInitialized = false;

    public static HashMap<String, Object> getCache() {
        return cache;
    }

    public static <T> T getCache(String key) {
        return (T) cache.get(key.toLowerCase(Locale.ROOT));
    }

    public static void setCache(String key, Object value) {
        if (value == null) {
            cache.remove(key.toLowerCase(Locale.ROOT));
        } else {
            cache.put(key.toLowerCase(Locale.ROOT), value);
        }
    }

    public static void sendKeys(By by,String data){
        var driver = (WebDriver)getCache("driver");
        driver.findElement(by).clear();
        driver.findElement(by).sendKeys(data);
        takeSnapShot();
    }

    private static String recordingName="none";
    public static void setRecordingName(String val){
        recordingName=val;
        counter=0;
        takeMessageSnapshot("Start test "+val);
    }
    public static void takeSnapShot() {

        try {
            var root = getRootPath(RecordingTasks.class);
            var dest = Path.of(root, "release","recording");
            if(!Files.exists(dest)){
                Files.createDirectory(dest);
            }
            dest = Path.of(root, "release","recording",recordingName);
            if(!Files.exists(dest)){
                Files.createDirectory(dest);
            }
            counter++;
            TakesScreenshot scrShot = ((TakesScreenshot) getCache("driver"));
            File srcFile = scrShot.getScreenshotAs(OutputType.FILE);
            var destFilePath = Path.of(root, "release","recording",recordingName,"snap_" + String.format("%03d", counter) + ".png");
            File destFile = new File(destFilePath.toAbsolutePath().toString());
            FileUtils.copyFile(srcFile, destFile);
            Files.delete(srcFile.getAbsoluteFile().toPath());
        }catch (Exception ex){
            System.out.println(ex);
        }
    }

    public static void takeMessageSnapshot(String text ){
        try {
            var root = getRootPath(RecordingTasks.class);
            var dest = Path.of(root, "release","recording");
            if(!Files.exists(dest)){
                Files.createDirectory(dest);
            }
            dest = Path.of(root, "release","recording",recordingName);
            if(!Files.exists(dest)){
                Files.createDirectory(dest);
            }

            counter++;
            var destFilePath = Path.of(root, "release","recording",recordingName,"snap_" + String.format("%03d", counter) + ".png");

            var img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            var g2d = img.createGraphics();
            var font = new Font("Arial", Font.PLAIN, 48);
            g2d.setFont(font);
            FontMetrics fm = g2d.getFontMetrics();
            int width = 1024;//fm.stringWidth(text);
            int height = 768;//fm.getHeight();
            g2d.dispose();

            img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            g2d = img.createGraphics();
            g2d.setBackground(Color.WHITE);
            g2d.clearRect(0, 0, width, height);
            g2d.setFont(font);
            fm = g2d.getFontMetrics();
            g2d.setColor(Color.BLACK);
            g2d.drawString(text, 0+5, 5+fm.getAscent());
            g2d.dispose();

            ImageIO.write(img, "png", new File(destFilePath.toAbsolutePath().toString()));
        }catch (Exception ex){
            System.out.println(ex);
        }
    }

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

    public static WebElement scrollFind(Supplier<WebElement> supplier, long... extraLength) throws Exception {
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
            if (extraLength.length > 0) {
                js.executeScript("arguments[0].scrollIntoView(true);window.scrollBy(0,-100);", we);
                //js.executeScript("window.scrollTo(0," + (i+extraLength[0]) + ")");
            }
            takeSnapShot();
            return we;
        }
        throw new RuntimeException("Unable to find item!");
    }

    public static WebElement waitForItem(Supplier<WebElement> el, Function<WebElement, Boolean> c) {
        for (var i = 0; i < 30; i++) {
            try {
                var res = el.get();
                if (res == null) {
                    Sleeper.sleep(1000);
                    continue;
                } else {
                    if (c != null) {
                        Sleeper.sleep(1000);
                        if (c.apply(res)) {
                            takeSnapShot();
                            return res;
                        }
                    } else {
                        return res;
                    }
                }
            } catch (Exception ex) {

            }
        }
        return null;
    }

    public static void doClick(Supplier<WebElement> el) {
        for (var i = 0; i < 10; i++) {
            try {
                var res = el.get();
                if (res == null) {
                    Sleeper.sleep(1000);

                    continue;
                }
                try {
                    //res.click();
                    Sleeper.sleep(500);
                    try {
                        res = el.get();
                        if (res != null) {
                            res.click();
                        }
                    } catch (Exception ex) {
                        return;
                    }
                    takeSnapShot();
                    return;
                } catch (Exception e) {
                    System.out.println(e);
                }
            } catch (Exception ex) {

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

        takeSnapShot();
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
        takeSnapShot();
        return el;
    }



    public static void showMessage(String message) throws InterruptedException {
        var driver = (WebDriver) getCache("driver");
        var js = (JavascriptExecutor) driver;
        js.executeScript("alert(\"" + message + "\");");
        Sleeper.sleep(5000);
        takeMessageSnapshot(message);
        driver.switchTo().alert().dismiss();
    }

    public static String getVersion() {
        var path = Path.of(getRootPath(Utils.class), "scripts", "version.txt");
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void initShutdownHook() {
        if (shutdownHookInitialized) return;
        shutdownHookInitialized = true;
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {

                try {
                    var driver = (WebDriver) getCache("driver");
                    if (driver != null) {
                        try {
                            driver.close();
                        } catch (Exception ex) {
                        }
                    }
                    var pu = new ProcessUtils(new HashMap<>());
                    HttpChecker.checkForSite(60, "http://127.0.0.1/api/shutdown").noError().run();
                    pu.sigtermProcesses((str) -> str.contains("-Dloader.main=org.kendar.Main"));
                    pu.killProcesses((psLine) ->
                            psLine.contains("java") &&
                                    psLine.contains("httpanswering") &&
                                    !psLine.contains("globaltest"));
                    pu.killProcesses((psLine) ->
                            psLine.contains("java") &&
                                    psLine.contains("org.h2.tools.Server") &&
                                    !psLine.contains("globaltest"));
                } catch (Exception e) {

                }
            }

        });
    }

    public static boolean navigateTo(String url) {
        var driver = (WebDriver) Utils.getCache("driver");
        var current = driver.getCurrentUrl();
        if (current.equalsIgnoreCase(url)) {
            org.kendar.globaltest.Sleeper.sleep(1000);
            takeSnapShot();
            return true;
        }
        driver.get(url);
        takeSnapShot();
        return false;
    }

    public static void setTitle(String title) {
        var driver = (JavascriptExecutor) Utils.getCache("driver");
        driver.executeScript("document.title = '" + title + "'");
    }
}
