package org.kendar.ham;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReplayerStates   extends BaseStates{


    private static HamReplayerBuilder replayerBuilder;



    @Given("^user start replaying '(.+)'$")
    public void user_start_replaying(String string) {
        // Write code here that turns the phrase above into concrete actions\
    }
    @When("^user create a recording '(.+)'$")
    public void userCreateRecording(String name) throws HamException {
        var builder = hamBuilder.pluginBuilder(HamReplayerBuilder.class);
        builder.createRecording(name);
    }

    @When("^user start recording '(.+)'$")
    public void userStartsRecording(String name) throws HamException {
        replayerBuilder = hamBuilder.pluginBuilder(HamReplayerBuilder.class);
        replayerBuilder.startRecording(name);
    }

    @When("^user stop recording '(.+)'$")
    public void userStopRecording(String name) throws HamException {
        ((HamReplayerRecorderStop)replayerBuilder).stop();
    }


    @When("^user delete recording '(.+)'$")
    public void userDeleteRecording(String name) throws HamException {
        var replayerBuilder = hamBuilder.pluginBuilder(HamReplayerBuilder.class);
        replayerBuilder.deleteRecording(name);
    }

    @When("^user can download '(.+)' as '(.+)'$")
    public void userCanDownload(String recordingId,String destinationFile) throws HamException, IOException {
        var replayerBuilder = hamBuilder.pluginBuilder(HamReplayerBuilder.class);
        var content = replayerBuilder.downloadRecording(recordingId);
        Files.writeString(Path.of(destinationFile),content);

    }

    @Then("^file '(.+)' contains '(.+)'$")
    public void fileContains(String file,String content) throws HamException, IOException {
        var data = Files.readString(Path.of(file)).toLowerCase(Locale.ROOT);
        assertTrue(data.indexOf(content.toLowerCase(Locale.ROOT))>=0);
    }
    @Given("^user upload '(.+)' as '(.+)'$")
    public void users_upload(String file,String id) throws IOException, HamException {
        var data = Files.readString(Path.of(file)).toLowerCase(Locale.ROOT);
        var replayerBuilder = hamBuilder.pluginBuilder(HamReplayerBuilder.class);
        replayerBuilder.uploadRecording(id,data);
    }
}
