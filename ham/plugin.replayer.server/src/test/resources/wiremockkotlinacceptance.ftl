package ${test.package}

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import com.lastminute.farecatalog.util.ClassPathFileReader
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.test.context.ActiveProfiles
import java.net.HttpURLConnection
import java.util.*

@ExtendWith(WireMockExtension::class)
@WireMockTest(httpPort = 8077)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("${test.profile}")
public class ${test.name} {
    @LocalServerPort
    var port = 0

    @AfterEach(){
        //AFTER.START_EXTRA
        //AFTER.END_EXTRA
    }

    @BeforeEach
    fun before() {
        //BEFORE.START_EXTRA
        //BEFORE.END_EXTRA

        <#list test.responses as response>
        stubFor(
            ${response.method}(urlEqualTo("/${response.path}"))
                .willReturn(
                    aResponse()
                        .withStatus(${response.status})
                        .withHeader("Content-Type", "${response.getHeader("Content-Type")}")
                        .withBody(ClassPathFileReader.readText("/it/${test.name}/response${response.id}.json"))
                )
        )
        <#response>
    }

    @Test
    fun happy_path() {
        <#list test.requests as request>
        val response${request.id} = RestAssured.given()
            .baseUri("http://localhost:$port/${host}")
            .log().all()
            <#if request.method=="post" || request.method=="put" >
            .contentType(${request.getHeader("Content-Type")})
            .body(ClassPathFileReader.readText("/it/${test.name}/request${request.id}.json"))
            <#if>
            .${request.method}("/${request.path}")
            .andReturn()

        val expectedResponse${request.id} = ClassPathFileReader.readText("/it/${test.name}/response${request.id}.json")
        <#if response.isTextResponse >
        assertEquals(expectedResponse${request.id},response${request.id}.asString())
        <#if>
            //RESP_${request.id}.START_EXTRA
            //RESP_${request.id}.END_EXTRA
        <#list>
    }
}