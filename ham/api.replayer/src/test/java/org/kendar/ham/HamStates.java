package org.kendar.ham;

import com.sun.net.httpserver.HttpServer;
import io.cucumber.java.en.Given;

public class HamStates {
    private HttpServer httpServer = null;

    @Given("^I have a running HAM instance$")
    public void iHaveRunningHamInstance() throws HamTestException {
        HamStarter.runHamJar();
    }

    @Given("^I add a dns mapping from '(.+)' to '(.+)'$")
    public void iAddDnsMapping(String ip,String name) throws HamTestException {
    }

    @Given("^I add a proxy from '(.+)' to '(.+)' testing it with '(.+)'$")
    public void iAddAProxy(String from,String to,String test) throws HamTestException {
    }

    @Given("^I have a server listening on port '([0-9]+)'$")
    public void iHaveServerListemimgOn(int port){
        if(httpServer!=null)return;
    }

    @Given("^users upload '(.+)'$")
    public void users_upload(String string) {
        // Write code here that turns the phrase above into concrete actions

    }

    @Given("^user start replaying '(.+)'$")
    public void user_start_replaying(String string) {
        // Write code here that turns the phrase above into concrete actions\
    }

    @Given("^user calls '(.+)'$\"")
    public void user_calls_http_gateway_int_test_api_v2_$metadata(String url) {
        // Write code here that turns the phrase above into concrete actions
    }

    @Given("the response should be blahblah")
    public void the_response_should_be_blahblah() {
    }
}
