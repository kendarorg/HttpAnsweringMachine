package org.kendar.cucumber;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.bs.A;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.kendar.globaltest.Sleeper;
import org.kendar.ham.HamBuilder;
import org.kendar.ham.HamException;
import org.kendar.ham.HamReplayerBuilder;
import org.kendar.ham.HamRequestBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.kendar.cucumber.Utils.*;


public class RecordingTasks {

    private static ObjectMapper mapper = new ObjectMapper();

    @And("^Navigate calendar ui$")
    public void navigateCalendarUi() throws Exception {
        var driver = (WebDriver) Utils.getCache("driver");
        driver.get("http://www.sample.test/");
        Sleeper.sleep(2000);
        takeSnapShot();
        doClick(() -> driver.findElement(By.id("appoint-add")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("role")));
        Sleeper.sleep(1000);
        sendKeys(By.id("role"),"Doctor");
        Sleeper.sleep(1000);
        sendKeys(By.id("name"),"John Doe");
        doClick(() -> driver.findElement(By.id("mod-save")));
        Sleeper.sleep(5000);
        doClick(() -> driver.findElement(By.id("grid-rowe-0-2")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("appoint-add")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("description")));
        Sleeper.sleep(1000);
        sendKeys(By.id("description"),"Visit");
        doClick(() -> driver.findElement(By.id("mod-save")));
        Sleeper.sleep(2000);
        waitForItem(() -> driver.findElement(By.id("grid-rowc-0-1")),
                (item) -> {
                    Sleeper.sleep(1000);
                    return item.getText().contains("CREATED");
                });

        doClick(() -> driver.findElement(By.id("grid-rowe-0-2")));
        Sleeper.sleep(2000);
        waitForItem(() -> driver.findElement(By.id("grid-rowc-0-1")),
                (item) -> {
                    Sleeper.sleep(1000);
                    return item.getText().contains("DRAFT");
                });
        doClick(() -> driver.findElement(By.id("grid-rowe-0-2")));
        Sleeper.sleep(2000);


        waitForItem(() -> driver.findElement(By.id("grid-rowc-0-1")),
                (item) -> {
                    Sleeper.sleep(1000);
                    return item.getText().contains("CONFIRMED");
                });
        doClick(() -> driver.findElement(By.id("grid-rowe-0-1")));
        Sleeper.sleep(2000);
        doClick(() -> driver.findElement(By.linkText("Employees")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("grid-rowe-0-0")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("name")));
        driver.findElement(By.id("name")).clear();
        Sleeper.sleep(1000);
        sendKeys(By.id("name"),"Jane Doe");
        doClick(() -> driver.findElement(By.id("mod-save")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("grid-rowe-0-1")));
        Sleeper.sleep(1000);
    }

    @And("^Stop action '(.+)'$")
    public void stopAction(String recordingName) throws Exception {
        var driver = (WebDriver) Utils.getCache("driver");
        var recordingId = Utils.getCache("recording_" + recordingName);
        driver.get("http://www.local.test/plugins/recording/script.html?id=" + recordingId);
        Sleeper.sleep(1000);
        takeSnapShot();
        doClick(() -> driver.findElement(By.id("recording-stop")));
        Sleeper.sleep(3000);
        doClick(() -> driver.findElement(By.id("recording-list-reload")));
        Sleeper.sleep(1000);
    }

    @And("^Start replaying '(.+)'$")
    public void startReplaying(String recordingName) throws Exception {
        var driver = (WebDriver) Utils.getCache("driver");
        doClick(() -> driver.findElement(By.id("recording-play")));
        Sleeper.sleep(1000);
    }

    @And("^Start calendar recording '(.+)'$")
    public void startCalendarRecording(String recordingName) throws Exception {
        var driver = (WebDriver) Utils.getCache("driver");

        driver.get("http://www.local.test/index.html");
        Sleeper.sleep(1000);
        takeSnapShot();
        doClick(() -> driver.findElement(By.linkText("Replayer web")));
        Sleeper.sleep(1000);

        doClick(() -> driver.findElement(By.id("main-recording-addnew")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("createScriptName")));
        Sleeper.sleep(1000);
        sendKeys(By.id("createScriptName"),recordingName);
        Sleeper.sleep(1000);
        scrollFind(() -> driver.findElement(By.id("createScriptBt"))).click();
        Sleeper.sleep(2000);

        driver.get("http://www.local.test/plugins/recording/script.html?id=1");

        try {
            Sleeper.sleep(1000);
            takeSnapShot();
            scrollFind(() -> driver.findElement(By.id("scriptstab_0"))).click();
        } catch (Exception ex) {

        }
        Sleeper.sleep(1000);
        scrollFind(() -> driver.findElement(By.id("extdbname"))).click();
        driver.findElement(By.id("extdbname")).clear();
        Sleeper.sleep(1000);
        sendKeys(By.id("extdbname"),"be");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("description")));
        Sleeper.sleep(1000);
        sendKeys(By.id("description"),"Full recording sample");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-saverglobscriptdata")));
        Sleeper.sleep(1000);
        showMessage("Starting recording");
        doClick(() -> driver.findElement(By.id("recording-startrecord")));
        Sleeper.sleep(1000);
        var result = driver.findElement(By.id("id")).getAttribute("value");
        Utils.setCache("recording_" + recordingName, result);
    }

    @Then("^The recording  '(.+)' contains '(.+)'$")
    public void thePageContains(String recordingName,String what) throws HamException, IOException {
        var recordingId = Integer.parseInt(Utils.getCache("recording_" + recordingName));

        var root = getRootPath(RecordingTasks.class);
//        var builder = HamBuilder
//                .newHam("www.local.test")
//                .withSocksProxy("127.0.0.1", 1080)
//                .withDns("127.0.0.1");
//
//        var request = HamRequestBuilder.
//                newRequest("http", "www.local.test").
//                withPath("/api/plugins/replayer/recording/" + recordingId + "/full").
//                withMethod("GET").
//                build();
//        var response = builder.call(request);

        var data = Files.readString(Path.of(root, "release", recordingName + ".json"));
        assertTrue(data.contains(what),"Missing "+what+" in recording "+recordingName);
    }

    @And("^Download recording '(.+)'$")
    public void downloadRecording(String recordingName) throws Exception {
        var recordingId = Integer.parseInt(Utils.getCache("recording_" + recordingName));

        var root = getRootPath(RecordingTasks.class);
        var builder = HamBuilder
                .newHam("www.local.test")
                .withSocksProxy("127.0.0.1", 1080)
                .withDns("127.0.0.1");

        var request = HamRequestBuilder.
                newRequest("http", "www.local.test").
                withPath("/api/plugins/replayer/recording/" + recordingId + "/full").
                withMethod("GET").
                build();
        var response = builder.call(request);

        Files.writeString(Path.of(root, "release", recordingName + ".json"), response.getResponseText());
    }

    @And("^Upload recording '(.+)'$")
    public void uploadRecording(String recordingName) throws Exception {

        var root = getRootPath(RecordingTasks.class);
        var toUploadPath = Path.of(root, "release", recordingName + ".json");
        var result = "";
        //if(!Files.exists(toUploadPath)){
        var in = this.getClass().getResourceAsStream("/recordings/" + recordingName + ".json");
        var streamReader = new InputStreamReader(in, StandardCharsets.UTF_8);
        var reader = new BufferedReader(streamReader);
        for (String line; (line = reader.readLine()) != null; ) {
            result += line;
        }
        var builder = HamBuilder
                .newHam("www.local.test")
                .withSocksProxy("127.0.0.1", 1080)
                .withDns("127.0.0.1");
        var replayer = builder.pluginBuilder(HamReplayerBuilder.class);
        var uoloadedRecording = replayer.uploadRecording(recordingName, result);
        Utils.setCache("recording_" + recordingName, "" + uoloadedRecording.getId());
    }

    @And("^Clone recording '(.+)' into '(.+)'$")
    public void cloneRecording(String source, String destName) throws Exception {
        var sourceId = (String) Utils.getCache("recording_" + source);
        var driver = (WebDriver) Utils.getCache("driver");
        navigateTo("http://www.local.test/plugins/recording/index.html");
        Sleeper.sleep(1000);
        for (var element : driver.findElements(By.cssSelector("[id^=\"grid-rowc-\"][id$=\"-0\"]"))) {
            if (element.getText().equalsIgnoreCase(sourceId)) {
                var itemId = element.getAttribute("id");
                var row = itemId.split("-")[2];
                doClick(() -> driver.findElement(By.id("grid-rowe-" + row + "-2")));
                Sleeper.sleep(1000);
                break;
            }
        }
        doClick(() -> driver.findElement(By.id("newname")));
        Sleeper.sleep(1000);
        sendKeys(By.id("newname"),destName);
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("mod-save")));
        Sleeper.sleep(5000);

        for (var element : driver.findElements(By.cssSelector("[id^=\"grid-rowc-\"][id$=\"-2\"]"))) {
            if (element.getText().equalsIgnoreCase(destName)) {
                var itemId = element.getAttribute("id");
                var row = itemId.split("-")[2];
                doClick(() -> driver.findElement(By.id("grid-rowe-" + row + "-0")));
                Sleeper.sleep(1000);
                break;
            }
        }

        var destId = driver.findElement(By.id("id")).getAttribute("value");
        Utils.setCache("recording_" + destName, destId);
    }

    @And("^Start null playing '(.+)'$")
    public void startNullPlaying(String recordingName) throws Exception {
        var driver = (WebDriver) Utils.getCache("driver");
        scrollFind(() -> driver.findElement(By.id("recording-playstim"))).click();
        Sleeper.sleep(1000);
    }

    @When("^Prepare HAM setup")
    public void setupTestConfigProgrammatically() throws HamException {
        var hamBuilder = HamBuilder
                .newHam("www.local.test")
                .withSocksProxy("127.0.0.1", 1080)
                .withDns("127.0.0.1");
        hamBuilder
                .dns()
                .addDnsName("127.0.0.1", "www.sample.test");
        hamBuilder
                .dns()
                .addDnsName("127.0.0.1", "be.sample.test");
        hamBuilder
                .dns()
                .addDnsName("127.0.0.1", "gateway.sample.test");

        hamBuilder
                .certificates()
                .addAltName("sample.test");

        hamBuilder
                .certificates()
                .addAltName("*.sample.test");

        hamBuilder
                .proxies()
                .addForcedProxy("http://localhost/int/gateway.sample.test",
                        "http://127.0.0.1:8090", "127.0.0.1:8090");

        hamBuilder
                .proxies()
                .addForcedProxy("http://localhost/int/be.sample.test",
                        "http://127.0.0.1:8100", "127.0.0.1:8100");

        hamBuilder
                .proxies()
                .addForcedProxy("http://www.sample.test",
                        "http://127.0.0.1:8080", "127.0.0.1:8080");

        hamBuilder
                .proxies()
                .addRemoteDbProxy(
                        "jdbc:h2:tcp://localhost:9123/./data/be;MODE=MYSQL;",
                        "sa", "sa",
                        "org.h2.Driver")
                .asLocal("be", "login", "password");
    }


    @And("^Load results '(.+)' for '(.+)'$")
    public void loadResultsFor(String trueFalse, String recordingName) throws Exception {
        var success = trueFalse.equalsIgnoreCase("true");
        var driver = (WebDriver) Utils.getCache("driver");
        var idRecording = (String) Utils.getCache("recording_" + recordingName);

        doClick(() -> driver.findElement(By.linkText("RESULTS")));
        Sleeper.sleep(1000);
        var builder = HamBuilder
                .newHam("www.local.test")
                .withSocksProxy("127.0.0.1", 1080)
                .withDns("127.0.0.1");
        var replayer = builder.pluginBuilder(HamReplayerBuilder.class);
        int count = 10;
        while (count > 10) {
            if (replayer.retrieveRecordings().stream().anyMatch(l -> {
                try {
                    return !l.isCompleted();
                } catch (HamException e) {
                    return false;
                }
            })) {

            }
            Sleeper.sleep(1000);
            count--;
        }

        scrollFind(() -> driver.findElement(By.id("scriptstab_2")), 100).click();
        scrollFind(() -> driver.findElement(By.id("recording-grid-result-reload")), 100).click();
        Sleeper.sleep(1000);

        replayer = builder.pluginBuilder(HamReplayerBuilder.class);
        var results = replayer.retrieveResults(Integer.parseInt(idRecording));
        var result = results.get(0);
        var resultIndex = result.getFileId();
        Sleeper.sleep(1000);
        driver.get("http://www.local.test/api/plugins/replayer/results/" + resultIndex);
        String source = driver.getPageSource();
        takeSnapShot();
        assertTrue(source.contains("successful\":" + (success ? "true" : "false")));
        Sleeper.sleep(2000);

    }


    @And("^Wait for termination$")
    public void waitForTermination() throws Exception {
        var builder = HamBuilder
                .newHam("www.local.test")
                .withSocksProxy("127.0.0.1", 1080)
                .withDns("127.0.0.1");
        var replayer = builder.pluginBuilder(HamReplayerBuilder.class);
        while (true) {
            var res = replayer.retrieveRecordings().stream().findFirst();
            if (res.get().isCompleted()) {
                break;
            }
            Sleeper.sleep(1000);
        }
        Sleeper.sleep(1000);
    }

    @And("^Start simple proxy recording '(.+)'$")
    public void startSimpleProxyRecording(String recordingName) throws Exception {
        var driver = (WebDriver) Utils.getCache("driver");

        driver.get("http://www.local.test/index.html");
        Sleeper.sleep(1000);
        takeSnapShot();
        doClick(() -> driver.findElement(By.linkText("Replayer web")));
        Sleeper.sleep(1000);

        doClick(() -> driver.findElement(By.id("main-recording-addnew")));
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("createScriptName")));
        Sleeper.sleep(1000);
        sendKeys(By.id("createScriptName"),recordingName);
        Sleeper.sleep(1000);
        scrollFind(() -> driver.findElement(By.id("createScriptBt"))).click();
        Sleeper.sleep(2000);

        driver.get("http://www.local.test/plugins/recording/script.html?id=1");

        try {
            Sleeper.sleep(1000);
            takeSnapShot();
            scrollFind(() -> driver.findElement(By.id("scriptstab_0"))).click();
        } catch (Exception ex) {

        }
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("description")));
        Sleeper.sleep(1000);
        sendKeys(By.id("description"),"Full recording sample");
        Sleeper.sleep(1000);
        doClick(() -> driver.findElement(By.id("recording-saverglobscriptdata")));
        Sleeper.sleep(1000);
        showMessage("Starting recording");
        doClick(() -> driver.findElement(By.id("recording-startrecord")));
        Sleeper.sleep(1000);
        var result = driver.findElement(By.id("id")).getAttribute("value");
        Utils.setCache("recording_" + recordingName, result);
    }

}
