package org.kendar.ham;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ReplayerStates   extends BaseStates{


    private static HamReplayerBuilder replayerBuilder;

    @Given("^user upload '(.+)'$")
    public void users_upload(String string) {
        // Write code here that turns the phrase above into concrete actions

    }

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

    @Then("$user can download '(.+)' as '(.+)'")
    public void user_can_download_as_test_json(String string,String path) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }


    @When("^user delete recording '(.+)'$")
    public void userDeleteRecording(String name) throws HamException {
        replayerBuilder = hamBuilder.pluginBuilder(HamReplayerBuilder.class);
        replayerBuilder.deleteRecording(name);
    }

    @When("^user calls '(.+)'$")
    public void userCalls(String url){

    }

    @When("^user can download '(.+)' as '(.+)'$")
    public void userCanDownload(String recordingId,String destinationFile){

    }
}
