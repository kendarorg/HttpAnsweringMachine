package org.kendar.ham;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kendar.utils.Sleeper;
import org.tkendar.ham.HamStarter;
import org.tkendar.ham.HamTestException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class JsBuilderTest {
    @BeforeAll
    public static void beforeAll() throws HamTestException {
        HamStarter.runHamJar(JsBuilderTest.class);
    }

    public static final String HTTP_SIMPLE_TEST_TEST_THING = "http://simple.test/test/thing";
    public static final String HTTP_SIMPLE_TOAST_TEST_THONG = "http://simple.toast/test/thong";

    public static class ValueDate {
        private String value;
        private String date;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }
    }

    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testFilterWithoutRegexp() throws HamException, IOException, InterruptedException {
        var hamBuilder = (HamBuilder) GlobalSettings.builder();
        //Add dns
        var dnsNameId = hamBuilder.dns().addDnsName("127.0.0.1", "simple.test");
        var jsBuilder = hamBuilder.pluginBuilder(JsBuilder.class);
        var realid = jsBuilder.addFilter("test")
                .inPhase(FilterPhase.API)
                .withMethod(Methods.GET)
                .withHost("simple.test")
                .withPath("/test/thing")
                .withType(ScriptType.SCRIPT)
                .setBlocking()
                .withSource()
                .addLine("var today = new Date().toISOString();")
                .addLine("response.setResponseText('{\"value\":\"A value\",\"date\":\"'+today+'\"}');")
                .addLine("response.addHeader('Content-Type','application/json');")
                .addLine("response.setStatusCode(200);")
                .closeBlocking()
                .create();

        ValueDate result = requestJsApiTestThing(hamBuilder, HTTP_SIMPLE_TEST_TEST_THING);
        assertEquals(result.getValue(), "A value");
        Sleeper.sleep(500);

        ValueDate result2 = requestJsApiTestThing(hamBuilder, HTTP_SIMPLE_TEST_TEST_THING);
        assertNotEquals(result.getDate(), result2.getDate());

        hamBuilder.dns().removeDnsName(dnsNameId);
        jsBuilder.deleteFilter(realid);
    }

    private ValueDate requestJsApiTestThing(HamBuilder hamBuilder, String url) throws HamException, IOException {
        var httpGet = new HttpGet(url);
        var clientResponse = hamBuilder.execute(httpGet);
        var data = IOUtils.toString(clientResponse.getEntity().getContent(), StandardCharsets.UTF_8);
        var result = mapper.readValue(data, ValueDate.class);
        return result;
    }


    @Test
    public void testFilterWithRegexp() throws HamException, IOException, InterruptedException {
        var hamBuilder = (HamBuilder) GlobalSettings.builder();

        //Add dns
        var dnsNameId = hamBuilder.dns().addDnsName("127.0.0.1", "simple.test");
        var dnsNameId2 = hamBuilder.dns().addDnsName("127.0.0.1", "simple.toast");
        var jsBuilder = hamBuilder.pluginBuilder(JsBuilder.class);

        var builder = jsBuilder.addFilter("test2")
                .inPhase(FilterPhase.API)
                .withMethod(Methods.GET)
                .wihtHostPattern("simple.([a-z]+)")
                .withPathPattern("/test/([a-z]+)")
                .withType(ScriptType.SCRIPT)
                .setBlocking()
                .withSource()
                .addLine("var today = new Date().toISOString();")
                .addLine("response.setResponseText('{\"value\":\"A value\",\"date\":\"'+today+'\"}');")
                .addLine("response.addHeader('Content-Type','application/json');")
                .addLine("response.setStatusCode(200);")
                .closeBlocking();

        builder.verifyHostPattern("simple.thrust");
        builder.verifyPathPattern("/test/wetheaver");

        var filterId = builder.create();

        ValueDate result = requestJsApiTestThing(hamBuilder, HTTP_SIMPLE_TEST_TEST_THING);
        assertEquals(result.getValue(), "A value");
        Sleeper.sleep(500);

        ValueDate result2 = requestJsApiTestThing(hamBuilder, HTTP_SIMPLE_TOAST_TEST_THONG);
        assertEquals(result.getValue(), result2.getValue());
        assertNotEquals(result.getDate(), result2.getDate());

        hamBuilder.dns().removeDnsName(dnsNameId);
        hamBuilder.dns().removeDnsName(dnsNameId2);
        jsBuilder.deleteFilter(filterId);
    }
}
