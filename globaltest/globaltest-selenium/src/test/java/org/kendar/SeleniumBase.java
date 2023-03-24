package org.kendar;

import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.kendar.globaltest.HttpChecker;
import org.kendar.globaltest.LocalFileUtils;
import org.kendar.globaltest.ProcessRunner;
import org.kendar.globaltest.ProcessUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.SessionId;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.kendar.globaltest.LocalFileUtils.pathOf;

public class SeleniumBase implements BeforeAllCallback, ExtensionContext.Store.CloseableResource, AfterAllCallback {
    private static final Function<String, Boolean> findHamProcesses = (psLine) ->
            psLine.contains("java") &&
                    psLine.contains("httpanswering") &&
                    !psLine.contains("globaltest");
    private static final Function<String, Boolean> findFirefoxHidden = (psLine) ->
            (psLine.contains("--marionette") &&
                    psLine.contains("--remote-debugging-port")) || psLine.contains("geckodriver");
    private static final String INSTALLED_PROGRAMS = "powershell -command \"Get-ItemProperty HKLM:\\\\Software\\\\Wow6432Node\\\\Microsoft\\\\Windows\\\\CurrentVersion\\\\Uninstall\\\\* | Select-Object DisplayName, InstallLocation | Format-Table –AutoSize\"";
    public static boolean shutdownHookInitialized = false;
    private static ProcessUtils _processUtils = new ProcessUtils(new HashMap<>());
    private static FirefoxDriver driver;
    private static JavascriptExecutor js;
    private static boolean started = false;
    private static String rootPath = null;
    private String tmpdir;
    private SessionId sessionId;

    public static ProcessRunner run(String root, Map<String, String> env, String script) throws Exception {
        return new ProcessRunner(env).
                asShell().
                withCommand(script + LocalFileUtils.execScriptExt()).
                withStartingPath(pathOf(root, "release", "calendar", "scripts")).
                runBackground();
    }

    public static FirefoxDriver getDriver() {
        return driver;
    }

    public static void showMessage(FirefoxDriver driver,String message) throws InterruptedException {
        var js = (JavascriptExecutor)driver;
        js.executeScript("alert(\""+message+"\");");
        Thread.sleep(5000);
        driver.switchTo().alert().dismiss();
    }

    public static void doClick(Supplier<WebElement> el) {
        for (var i = 0; i < 10; i++) {
            var res = el.get();
            if (res == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {

                }
                continue;
            }
            try {
                res.click();
                Thread.sleep(500);
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

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }
        var res = el.get();

        throw new RuntimeException("Unable to click " + res.getText());
    }

    public static WebElement checkCheckBox(FirefoxDriver driver, Supplier<WebElement> supplier) {
        var el = supplier.get();
        if (!el.isSelected()) {
            var js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].click();", el);
        }
        return el;
    }

    public static WebElement uncheckCheckBox(FirefoxDriver driver, Supplier<WebElement> supplier) {
        var el = supplier.get();
        if (el.isSelected()) {
            var js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].click();", el);
        }
        return el;
    }

    public static void setupSize(FirefoxDriver driver){
        driver.manage().window().setSize(new Dimension(1366, 900));
    }

    public static WebElement scrollFind(FirefoxDriver driver, Supplier<WebElement> supplier,long ... extraLength) throws Exception {
        var js = (JavascriptExecutor) driver;
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

    public static String findFirefox() throws Exception {
        var env = new HashMap<String, String>();
        if (env.containsKey("FIREFOX_PATH")) {
            return env.get("FIREFOX_PATH");
        }
        if (SystemUtils.IS_OS_WINDOWS) {
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
                if (lower.contains("firefox")) {
                    var index = col.indexOf(":\\");
                    if (index > 0) {
                        index--;
                        return col.substring(index).trim() + "\\Firefox.exe";
                    }
                }
            }
        } else {
            var queue = new ConcurrentLinkedQueue<String>();
            new ProcessRunner(env).
                    withCommand("whereis").
                    withParameter("firefox").
                    withStorage(queue).
                    run();
            var allJavaProcesses = String.join(" ", queue.stream().
                    collect(Collectors.toList()));
            var all = allJavaProcesses.split("\\s+");
            for (var col : all) {
                var trimmed = col.trim().toLowerCase(Locale.ROOT);
                if (trimmed.endsWith("/firefox")) {
                    return col;
                }
            }
        }
        throw new Exception("Firefox not found!");
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

    private static boolean deleteDirectory(File directoryToBeDeleted) {
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

    public static String highlight(WebElement element, String... style) {
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        var oldStile = element.getAttribute("style");
        if (style.length == 0) {
            jsExecutor.executeScript("arguments[0].setAttribute('style', 'border:2px solid red; background:yellow')", element);
        } else {
            jsExecutor.executeScript("arguments[0].setAttribute('style', '" + style[0] + "')", element);
        }
        return oldStile;
    }

    private static void initShutdownHook() {
        if (shutdownHookInitialized) return;
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {

                try {

                    killHam();
                } catch (Exception e) {

                }
            }

        });
    }

    public static void killHam() throws Exception {
        var pu = new ProcessUtils(new HashMap<>());
        HttpChecker.checkForSite(60, "http://127.0.0.1/api/shutdown").noError().run();
        pu.sigtermProcesses((str) -> str.contains("-Dloader.main=org.kendar.Main"));
        _processUtils.killProcesses(findHamProcesses);
    }

    public static String getVersion() {
        var path = Path.of(getRootPath(SeleniumBase.class), "scripts", "version.txt");
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void runHamJar(Class<?> caller) throws Exception {
        try {
            if (HttpChecker.checkForSite(5, "http://127.0.0.1/api/dns/lookup/test").noError().run()) {
                return;
            }
        } catch (Exception e) {
            throw new Exception(e);
        }
        var commandLine = new ArrayList<String>();

        deleteDirectory(Path.of(getRootPath(caller), "data", "tmp").toFile());
        deleteDirectory(Path.of(getRootPath(caller), "release", "calendar", "data").toFile());
        var java = "java";
        //var agentPath = Path.of(getRootPath(caller), "ham", "api.test", "org.jacoco.agent-0.8.8-runtime.jar");
        //var jacocoExecPath = Path.of(getRootPath(caller), "ham", "api.test", "target", "jacoco_starter.exec");
        var externalJsonPath = Path.of(getRootPath(caller), "ham", "test.external.json").toString();
        var libsPath = Path.of(getRootPath(caller), "ham", "libs").toString();
        var appPathRootPath = Path.of(getRootPath(caller), "ham", "app", "target");

        if (!appPathRootPath.toFile().exists()) {
            throw new Exception("WRONG STARTING PATH " + appPathRootPath);
        }
        File[] matchingFiles = appPathRootPath.toFile().listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name == null) return false;
                return name.startsWith("app-") && name.endsWith(".jar");
            }
        });
        var appPathRoot = Path.of(matchingFiles[0].getAbsolutePath()).toString();
        initShutdownHook();

        var pr = new ProcessRunner(new ConcurrentHashMap<>()).
                asShell().
                withCommand(java).
                withParameter("-Djsonconfig=" + externalJsonPath).
                withParameter("-Dloader.path=" + libsPath).
                withParameter("-Dham.tempdb=data/tmp").
                withParameter("-Dloader.main=org.kendar.Main").
                //withParameter("-javaagent:" + agentPath + "=destfile=" + jacocoExecPath + ",includes=org.kendar.**").
                        withParameter("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:9863").
                withParameter("-jar").
                withParameter(appPathRoot);
        try {
            pr.runBackground();
            if (!HttpChecker.checkForSite(60, "http://127.0.0.1/api/dns/lookup/test").run()) {
                throw new Exception("NOT STARTED");
            }
        } catch (Exception e) {
            throw new Exception(e);
        }

    }

    @Override
    public void close() throws Throwable {
        if (started == false) return;
        try {
            //driver.close();
            driver.quit();
            driver = null;
            js = null;
            _processUtils.killProcesses(findFirefoxHidden);
            //deleteDirectory(new File(tmpdir));
        } catch (Exception ex) {

        }
        started = false;
    }

    public void restart() throws Throwable {
        close();
        beforeAll(null);
    }

    public static void unzip(String src,String dst) throws IOException {
        String fileZip = src;
        File destDir = new File(dst);
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = newFile(destDir, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();
    }
    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    protected static void setEnv(Map<String, String> newenv) throws Exception {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newenv);
        } catch (NoSuchFieldException e) {
            Class[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for(Class cl : classes) {
                if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    Map<String, String> map = (Map<String, String>) obj;
                    map.clear();
                    map.putAll(newenv);
                }
            }
        }
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        if (started) return;
        started = true;
        try {

            _processUtils.killProcesses(findFirefoxHidden);
            var firefoxExecutable = SeleniumBase.findFirefox();

            Proxy proxy = new Proxy();
            proxy.setSocksProxy("127.0.0.1:1080");
            proxy.setSocksVersion(5);
            File pathBinary = new File(firefoxExecutable);
            FirefoxBinary firefoxBinary = new FirefoxBinary(pathBinary);
            DesiredCapabilities desired = new DesiredCapabilities();
            FirefoxOptions options = new FirefoxOptions();
            desired.setCapability(FirefoxOptions.FIREFOX_OPTIONS, options.setBinary(firefoxBinary));
            desired.setCapability(FirefoxOptions.FIREFOX_OPTIONS, options.setProxy(proxy));;
            FirefoxProfile profile = new FirefoxProfile();
            profile.setAcceptUntrustedCertificates(true);
            profile.setAssumeUntrustedCertificateIssuer(true);
            profile.setPreference("network.proxy.socks_remote_dns", true);
            desired.setCapability(FirefoxOptions.FIREFOX_OPTIONS, options.setProfile(profile));
            driver = new FirefoxDriver((new GeckoDriverService.Builder() {
                @Override
                protected GeckoDriverService createDriverService(File exe, int port,
                                                                 Duration timeout,
                                                                 List<String> args, Map<String, String> environment) {
                    return super.createDriverService(exe, port, timeout, args, environment);
                }
            }).build(), options);

            sessionId = driver.getSessionId();
            js = (JavascriptExecutor) driver;


            System.out.println("here it is " + firefoxExecutable);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        try {
            driver.quit();
        } catch (Exception ex) {

        }
        started = false;
    }
}
