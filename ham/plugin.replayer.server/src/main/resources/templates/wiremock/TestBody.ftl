package ${getPackage}

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import io.restassured.RestAssured
import io.restassured.http.ContentType
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
@ActiveProfiles("acceptance")
virtual class ${name} {
    @LocalServerPort
    var port = 0

    abstract void resetDatastore();

    @BeforeEach
    fun before() {
        resetDatastore()



<#list rows as row>
        stubFor(
            ${row.getLowMethod()}(urlEqualTo("/${row.data.requestPath}"))
                .willReturn(
                    aResponse()
                        //.withStatus(HttpURLConnection.HTTP_CREATED)
                        .withStatus(${row.data.responseStatusCode})
                        .withHeader("Content-Type", "${row.contentType}")
                        .withBody(ClassPathFileReader.read("/${getResourcePackage}/${row.index}.json"))
                )
        )
</#list>
    }
}