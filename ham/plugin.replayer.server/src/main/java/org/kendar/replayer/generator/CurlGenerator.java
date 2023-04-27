package org.kendar.replayer.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.replayer.storage.CallIndex;
import org.kendar.replayer.storage.ReplayerRow;
import org.kendar.servers.db.HibernateSessionFactory;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.ConstantsHeader;
import org.kendar.utils.ConstantsMime;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class CurlGenerator implements SelectedGenerator {
    private final ObjectMapper mapper = new ObjectMapper();
    private final HibernateSessionFactory sessionFactory;

    public CurlGenerator(HibernateSessionFactory sessionFactory) {

        this.sessionFactory = sessionFactory;
    }

    @Override
    public String getId() {
        return "curl";
    }

    @Override
    public void generate(int recordingId, Request req, Response res, List<Long> ids) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);


        var unixCurl = new ArrayList<String>();
        unixCurl.add("#/bin/sh");
        unixCurl.add("");
        unixCurl.add("function pause {\n" +
                " read -s -n 1 -p \"Press any key to continue . . .\"\n" +
                " echo \"\"\n" +
                "}");
        var winCurl = new ArrayList<String>();
        for (var id : ids) {
            var callIndex = (CallIndex) sessionFactory.queryResult((em) -> em.createQuery("SELECT e FROM CallIndex e WHERE " +
                    " e.recordingId=" + recordingId + " AND " +
                    " e.id=" + id).getResultList().get(0));
            var replayerRow = (ReplayerRow) sessionFactory.queryResult((em) -> em.createQuery("SELECT e FROM ReplayerRow e WHERE " +
                    " e.recordingId=" + recordingId + " AND " +
                    " e.id=" + callIndex.getReference()).getResultList().get(0));
            var requ = replayerRow.getRequest();

            StringBuilder singleCurl = new StringBuilder("curl -v ");

            if (requ.bodyExists()) {
                singleCurl.append(" -d @").append(id).append(".data.bin ");

                byte[] result;
                if (requ.isBinaryRequest()) {
                    result = requ.getRequestBytes();
                } else {
                    result = requ.getRequestText().getBytes(StandardCharsets.UTF_8);
                }
                ZipEntry entry = new ZipEntry(id + ".data.bin");
                //entry.setSize(result.length);
                zos.putNextEntry(entry);
                zos.write(result);
                zos.closeEntry();
            }


            if (requ.getMethod().equalsIgnoreCase("post")) {
                singleCurl.append(" -X POST ");
            } else if (requ.getMethod().equalsIgnoreCase("put")) {
                singleCurl.append(" -X PUT ");
            } else if (requ.getMethod().equalsIgnoreCase("delete")) {
                singleCurl.append(" -X DELETE ");
            } else if (!requ.getMethod().equalsIgnoreCase("get")) {
                continue;
            }

            if (requ.getPort() > 0) {
                singleCurl.append(" ").append(requ.getProtocol()).append("://").append(requ.getHost()).append(":").append(requ.getPort()).append(requ.getPath());
            } else {
                singleCurl.append(" ").append(requ.getProtocol()).append("://").append(requ.getHost()).append(requ.getPath());
            }
            for (var h : requ.getHeaders().entrySet()) {
                singleCurl.append(" -H \"").append(h.getKey()).append(":").append(h.getValue()).append("\" ");
            }
            unixCurl.add("# Request " + id);
            unixCurl.add(singleCurl.toString());
            unixCurl.add("pause");
            winCurl.add("REM Request " + id);
            winCurl.add(singleCurl.toString());
            winCurl.add("pause");

        }

        var full = String.join("\r\n", unixCurl);
        var result = full.getBytes(StandardCharsets.UTF_8);
        ZipEntry entry = new ZipEntry("curls.sh");
        //entry.setSize(result.length);
        zos.putNextEntry(entry);
        zos.write(result);
        zos.closeEntry();

        full = String.join("\r\n", winCurl);
        result = full.getBytes(StandardCharsets.UTF_8);
        entry = new ZipEntry("curls.bat");
        //entry.setSize(result.length);
        zos.putNextEntry(entry);
        zos.write(result);
        zos.closeEntry();

        zos.close();

        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.ZIP);
        res.addHeader("Content-disposition", "inline;filename=curls.zip");
        res.setResponseBytes(baos.toByteArray());
        res.setBinaryResponse(true);
    }
}
