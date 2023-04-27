package org.kendar.servers.http;

import org.kendar.events.EventQueue;
import org.kendar.servers.db.HibernateSessionFactory;

public class JsUtils {
    private final HibernateSessionFactory sessionFactory;
    private final EventQueue queue;
    private final ExternalRequester externalRequester;
    //private final String rootPath;

    public JsUtils(HibernateSessionFactory sessionFactory, EventQueue queue, ExternalRequester externalRequester) {
        this.sessionFactory = sessionFactory;
        this.queue = queue;
        this.externalRequester = externalRequester;

    }

    public void handleEvent(String eventType, String jsonEvent) {
        queue.handle(eventType, jsonEvent);
    }

    public String loadFile(final String path, boolean binary) {
        try {
            var content = (String) sessionFactory.queryResult(e -> {
                return (String) e.createQuery("SELECT e.content FROM DbFilterFiles e " +
                                " WHERE " +
                                " e.name='" + path + "'")
                        .getResultList().get(0);
            });
            return content;
//            if (!binary) {
//                return content;
//            } else {
//                var bytes = Files.readAllBytes(of);
//                return Base64.encodeBase64String(bytes);
//            }
//            if (path.startsWith("/") || path.startsWith("\\")) {
//                path = path.substring(1);
//            }
//            path = rootPath + File.separator + path;
//
//            String absolute = new File(path).getCanonicalPath();
//            if (absolute.toLowerCase(Locale.ROOT).startsWith(rootPath.toLowerCase(Locale.ROOT))) {
//                Path of = Path.of(absolute);
//
//            }
        } catch (Exception e) {
            return null;
        }
    }

    public Response httpRequest(Request request) throws Exception {
        var response = new Response();
        externalRequester.callSite(request, response);
        return response;
    }
}
