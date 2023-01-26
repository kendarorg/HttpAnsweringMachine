package org.kendar.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.kendar.ham.*;
import org.kendar.utils.Sleeper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReplayerStates   extends BaseStates{





    @Given("^user start replaying$")
    public void user_start_replaying() throws HamException {
       recordingId.startReplaying();
        // Write code here that turns the phrase above into concrete actions\
    }
    @When("^user create a recording '(.+)'$")
    public void userCreateRecording(String name) throws HamException {
        var builder = hamBuilder.pluginBuilder(HamReplayerBuilder.class);
        recordingId = builder.setupRecording().withName(name).createRecording();
    }

    @When("^user start recording$")
    public void userStartsRecording() throws HamException {
        recordingId.startRecording();
        Sleeper.sleep(2000);
    }

    @Given("^user stop replaying$")
    public void user_stop_replaying() throws HamException {
        ((HamReplayerRecorderStop)recordingId).stop();
    }

    @When("^user stop recording$")
    public void userStopRecording() throws HamException {
        ((HamReplayerRecorderStop)recordingId).stop();
    }


    @When("^user delete recording$")
    public void userDeleteRecording() throws HamException {
        recordingId.delete();
    }

    @When("^user can download as '(.+)'$")
    public void userCanDownload(String destinationFile) throws HamException, IOException {
        var replayerBuilder = hamBuilder.pluginBuilder(HamReplayerBuilder.class);
        var content = replayerBuilder.downloadRecording(recordingId.getId());
        Files.writeString(Path.of(destinationFile),content);

    }

    @Then("^file '(.+)' contains '(.+)'$")
    public void fileContains(String file,String content) throws HamException, IOException {
        var data = Files.readString(Path.of(file)).toLowerCase(Locale.ROOT);
        assertTrue(data.indexOf(content.toLowerCase(Locale.ROOT))>=0);
    }

    @Then("^file '(.+)' does not contains '(.+)'$")
    public void fileNotContains(String file,String content) throws HamException, IOException {
        var data = Files.readString(Path.of(file)).toLowerCase(Locale.ROOT);
        assertFalse(data.indexOf(content.toLowerCase(Locale.ROOT))>=0);
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
