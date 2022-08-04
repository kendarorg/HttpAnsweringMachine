package org.kendar.freemaker;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;
import org.junit.jupiter.api.Test;
import org.kendar.freemaker.ham.AcceptanceModel;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class NewFreeMakerTest {

    @Test
    public void doTest() throws IOException, TemplateException {
        Configuration cfg = new Configuration(new Version("2.3.23"));

        cfg.setClassForTemplateLoading(FreeMakerTest.class, "/");
        cfg.setDefaultEncoding("UTF-8");

        Template template = cfg.getTemplate("wiremockkotlinacceptance.ftl");
        Map<String, Object> templateData = new HashMap<>();

        var model = new AcceptanceModel();
        model.setName("TestName");
        model.setPackageName("org.kendar.test");
        model.setProfile("acceptance");
        templateData.put("test", model);
        try (StringWriter out = new StringWriter()) {

            template.process(templateData, out);
            System.out.println(out.getBuffer().toString());

            out.flush();
        }
    }
}
