package de.niclashoyer.resteasytest;

import de.niclashoyer.resteasytest.webid.WebIDInterceptor;
import de.niclashoyer.resteasytest.webid.netty.WebIDNettyJaxrsServer;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.ResteasyDeployment;

@Path("/")
public class App {

    public static void main(String[] args) throws Exception {
        ResteasyDeployment deployment = new ResteasyDeployment();
        deployment.setResourceClasses(Collections.singletonList(App.class.getName()));
        deployment.setProviderClasses(Collections.singletonList(WebIDInterceptor.class.getName()));
        final WebIDNettyJaxrsServer server = new WebIDNettyJaxrsServer();
        server.setDeployment(deployment);

        server.setKeyManagers(App.getKeyManager());

        int port = 3000;
        server.setPort(port);
        server.start();
        System.out.println("Server listening on port " + port);

    }

    protected static KeyManager[] getKeyManager() throws NoSuchAlgorithmException, FileNotFoundException, KeyStoreException, IOException, UnrecoverableKeyException, CertificateException {
        TrustManagerFactory tmFactory = TrustManagerFactory.getInstance("PKIX");
        KeyStore tmpKS = null;
        tmFactory.init(tmpKS);
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(App.class.getClassLoader().getResourceAsStream("cert.jks"),"secret".toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks,"secret".toCharArray());
        return kmf.getKeyManagers();
    }

    @GET
    public Response get(@Context HttpRequest req) {
        String str = "Hello World!\n";
        Object claims = req.getAttribute("webidclaims");
        if (claims != null) {
            Collection<String> uris = (Collection<String>) claims;
            str += "You claimed the following WebIDs:\n";
            int i = 1;
            for (String uri : uris) {
                str += i+". "+uri+"\n";
                i++;
            }
        }
        return Response.status(200).entity(str).type(MediaType.TEXT_PLAIN).build();
    }

    @GET
    @Path("{any}")
    public Response catchAll(@Context HttpRequest req) {
        return this.get(req);
    }
}