package org.kendar.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.kendar.ham.HamException;
import org.kendar.ham.HamReplayerBuilder;
import org.kendar.ham.HamReplayerRecorderStop;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReplayerStates   extends BaseStates{


    private static HamReplayerBuilder replayerBuilder;



    @Given("^user start replaying$")
    public void user_start_replaying() throws HamException {
        replayerBuilder = hamBuilder.pluginBuilder(HamReplayerBuilder.class);
        replayerBuilder.startReplaying(recordingId);
        // Write code here that turns the phrase above into concrete actions\
    }
    public long recordingId;
    @When("^user create a recording '(.+)'$")
    public void userCreateRecording(String name) throws HamException {
        var builder = hamBuilder.pluginBuilder(HamReplayerBuilder.class);
        recordingId = builder.createRecording(name);
    }

    @When("^user start recording$")
    public void userStartsRecording() throws HamException {
        replayerBuilder = hamBuilder.pluginBuilder(HamReplayerBuilder.class);
        replayerBuilder.startRecording(recordingId);
    }

    @Given("^user stop replaying$")
    public void user_stop_replaying() throws HamException {
        ((HamReplayerRecorderStop)replayerBuilder).stop();
    }

    @When("^user stop recording$")
    public void userStopRecording() throws HamException {
        ((HamReplayerRecorderStop)replayerBuilder).stop();
    }


    @When("^user delete recording$")
    public void userDeleteRecording() throws HamException {
        var replayerBuilder = hamBuilder.pluginBuilder(HamReplayerBuilder.class);
        replayerBuilder.deleteRecording(recordingId);
    }

    @When("^user can download as '(.+)'$")
    public void userCanDownload(String destinationFile) throws HamException, IOException {
        var replayerBuilder = hamBuilder.pluginBuilder(HamReplayerBuilder.class);
        var content = replayerBuilder.downloadRecording(recordingId);
        Files.writeString(Path.of(destinationFile),content);

    }

    @Then("^file '(.+)' contains '(.+)'$")
    public void fileContains(String file,String content) throws HamException, IOException {
        var data = Files.readString(Path.of(file)).toLowerCase(Locale.ROOT);
        assertTrue(data.indexOf(content.toLowerCase(Locale.ROOT))>=0);
    }

    @Then("^file '(.+)' replace '(.+)' with '(.+)'$")
    public void fileReplace(String file,String replace,String content) throws HamException, IOException {
        var data = Files.readString(Path.of(file));
        var replaced = data.replaceAll(replace,content);
        Files.writeString(Path.of(file),replaced);
    }
    @Given("^user upload '(.+)' as '(.+)'$")
    public void users_upload(String file,String id) throws IOException, HamException {
        var data = Files.readString(Path.of(file));
        var replayerBuilder = hamBuilder.pluginBuilder(HamReplayerBuilder.class);
        recordingId = replayerBuilder.uploadRecording(id,data);
    }
}
