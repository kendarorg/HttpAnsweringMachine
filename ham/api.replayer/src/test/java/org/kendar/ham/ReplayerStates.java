package org.kendar.ham;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ReplayerStates   extends BaseStates{



    @Given("^users upload '(.+)'$")
    public void users_upload(String string) {
        // Write code here that turns the phrase above into concrete actions

    }

    @Given("^user start replaying '(.+)'$")
    public void user_start_replaying(String string) {
        // Write code here that turns the phrase above into concrete actions\
    }
    @When("^user create a recording '(.+)'$")
    public void userCreateRecording(String name){
        var builder = hamBuilder.pluginBuilder(HamReplayerBuilder.class);
        builder.createRecording(name);

    }

    @When("^user start recording '(.+)'$")
    public void userStartsRecording(String name){

    }

    @When("^user stop recording '(.+)'$")
    public void userStopRecording(String name){

    }

    @Given("^users create a recording '(.+)'$")
    public void users_create_a_recording(String string) {

    }

    @Then("$user can download '(.+)' as '(.+)'")
    public void user_can_download_as_test_json(String string,String path) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }


    @When("^user delete recording '(.+)'$")
    public void userDeleteRecording(String name){

    }

    @When("user calls '(.+)'$")
    public void userCalls(String url){

    }

    @When("^user can download '(.+)' as '(.+)'$")
    public void userCanDownload(String recordingId,String destinationFile){

    }
}
