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
public class CurlGenerator implements SelectedGenerator{
    private ObjectMapper mapper = new ObjectMapper();
    private HibernateSessionFactory sessionFactory;

    public CurlGenerator(HibernateSessionFactory sessionFactory){

        this.sessionFactory = sessionFactory;
    }

    @Override
    public String getId() {
        return "curl";
    }

    @Override
    public void generate(int recordingId, Request req, Response res, List<Long> ids) throws Exception{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);



        var curls = new ArrayList<String>();
        for (var id :ids) {
            var callIndex = (CallIndex)sessionFactory.queryResult((em) -> {
                return em.createQuery("SELECT e FROM CallIndex e WHERE " +
                        " e.recordingId=" + recordingId+" AND "+
                        " e.id="+id).getResultList().get(0);
            });
            var replayerRow = (ReplayerRow)sessionFactory.queryResult((em) -> {
                return em.createQuery("SELECT e FROM ReplayerRow e WHERE " +
                        " e.recordingId=" + recordingId+" AND "+
                        " e.id="+callIndex.getReference()).getResultList().get(0);
            });
            var requ = replayerRow.getRequest();

            var singleCurl = "curl ";

            if(requ.bodyExists()){
                singleCurl+=" -d @"+id+".data.bin ";

                byte[] result;
                if(requ.isBinaryRequest()){
                    result=requ.getRequestBytes();
                }else{
                    result=requ.getRequestText().getBytes(StandardCharsets.UTF_8);
                }
                ZipEntry entry = new ZipEntry(id+".data.bin");
                //entry.setSize(result.length);
                zos.putNextEntry(entry);
                zos.write(result);
                zos.closeEntry();
            }


            if(requ.getMethod().equalsIgnoreCase("post")){
                singleCurl+=" -X POST ";
            }else if(requ.getMethod().equalsIgnoreCase("put")){
                singleCurl+=" -X PUT ";
            }else if(requ.getMethod().equalsIgnoreCase("delete")){
                singleCurl+=" -X DELETE ";
            }else if(requ.getMethod().equalsIgnoreCase("get")){

            }else{
                continue;
            }

            singleCurl+=" "+requ.getProtocol()+"://"+requ.getHost()+":"+requ.getPort()+requ.getPath();
            for(var h:requ.getHeaders().entrySet()){
                singleCurl+=" -H \""+h.getKey()+":"+h.getValue()+"\" ";
            }
            curls.add("# Request "+id);
            curls.add(singleCurl);

        }

        var full = String.join("\r\n",curls);
        var result = full.getBytes(StandardCharsets.UTF_8);
        ZipEntry entry = new ZipEntry("curls.txt");
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
