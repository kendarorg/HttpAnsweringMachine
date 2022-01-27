package org.kendar.servers.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.servers.utils.models.ValidatorData;
import org.kendar.servers.utils.models.ValidatorResult;
import org.wiztools.xsdgen.ParseException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ValidatorAPITestXml {
    final String TEMPLATE ="<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\"> \n" +
            " <env:Header>\n" +
            "  <m:reservation xmlns:m=\"http://travelcompany.example.org/reservation\" \n" +
            "\t\tenv:role=\"http://www.w3.org/2003/05/soap-envelope/role/next\">\n" +
            "   <m:reference>uuid:093a2da1-q345-739r-ba5d-pqff98fe8j7d</m:reference>\n" +
            "   <m:dateAndTime>2007-11-29T13:20:00.000-05:00</m:dateAndTime>\n" +
            "  </m:reservation>\n" +
            "  <n:passenger xmlns:n=\"http://mycompany.example.com/employees\" \n" +
            "\t\tenv:role=\"http://www.w3.org/2003/05/soap-envelope/role/next\">\n" +
            "   <n:name>Fred Bloggs</n:name>\n" +
            "  </n:passenger>\n" +
            " </env:Header>\n" +
            " <env:Body>\n" +
            "  <p:itinerary xmlns:p=\"http://travelcompany.example.org/reservation/travel\">\n" +
            "   <p:departure>\n" +
            "     <p:departing>New York</p:departing>\n" +
            "     <p:arriving>Los Angeles</p:arriving>\n" +
            "     <p:departureDate>2007-12-14</p:departureDate>\n" +
            "     <p:departureTime>late afternoon</p:departureTime>\n" +
            "     <p:seatPreference>aisle</p:seatPreference>\n" +
            "   </p:departure>\n" +
            "   <p:return>\n" +
            "     <p:departing>Los Angeles</p:departing>\n" +
            "     <p:arriving>New York</p:arriving>\n" +
            "     <p:departureDate>2007-12-20</p:departureDate>\n" +
            "     <p:departureTime>mid-morning</p:departureTime>\n" +
            "     <p:seatPreference></p:seatPreference>\n" +
            "   </p:return>\n" +
            "  </p:itinerary>\n" +
            " </env:Body>\n" +
            "</env:Envelope>";
    final String VALID="<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\"> \n" +
            " <env:Header>\n" +
            "  <m:reservation xmlns:m=\"http://travelcompany.example.org/reservation\" \n" +
            "\t\tenv:role=\"http://www.w3.org/2003/05/soap-envelope/role/next\">\n" +
            "   <m:reference>uuid:093a2da1-q345-739r-ba5d-pqff98fe8j7d</m:reference>\n" +
            "   <m:dateAndTime>2010-11-29T13:20:00.000-05:00</m:dateAndTime>\n" +
            "  </m:reservation>\n" +
            "  <n:passenger xmlns:n=\"http://mycompany.example.com/employees\" \n" +
            "\t\tenv:role=\"http://www.w3.org/2003/05/soap-envelope/role/next\">\n" +
            "   <n:name>Fred Bloggs</n:name>\n" +
            "  </n:passenger>\n" +
            " </env:Header>\n" +
            " <env:Body>\n" +
            "  <p:itinerary xmlns:p=\"http://travelcompany.example.org/reservation/travel\">\n" +
            "   <p:departure>\n" +
            "     <p:departing>Milan</p:departing>\n" +
            "     <p:arriving>New York</p:arriving>\n" +
            "     <p:departureDate>2007-12-14</p:departureDate>\n" +
            "     <p:departureTime>late wetheaver</p:departureTime>\n" +
            "     <p:seatPreference>corridor</p:seatPreference>\n" +
            "   </p:departure>\n" +
            "   <p:return>\n" +
            "     <p:departing>Rome</p:departing>\n" +
            "     <p:arriving>Madrid</p:arriving>\n" +
            "     <p:departureDate>2008-12-20</p:departureDate>\n" +
            "     <p:departureTime>mid-evening</p:departureTime>\n" +
            "     <p:seatPreference>wetheaver</p:seatPreference>\n" +
            "   </p:return>\n" +
            "  </p:itinerary>\n" +
            " </env:Body>\n" +
            "</env:Envelope>";

    ObjectMapper mapper = new ObjectMapper();
    @Test
    public void shouldValidate() throws IOException, ParseException, ParserConfigurationException, SAXException {
        var target = new ValidatorAPI();
        var req = new Request();
        var res = new Response();
        var input = new ValidatorData();
        input.setTemplate(TEMPLATE);
        input.setSource(VALID);
        req.setRequestText(mapper.writeValueAsString(input));

        target.validateXml(req,res);
        var result = mapper.readValue(res.getResponseText(), ValidatorResult.class);
        assertFalse(result.isError());
        assertEquals(0,result.getErrors().size());
    }
}
