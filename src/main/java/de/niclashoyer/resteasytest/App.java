package de.niclashoyer.resteasytest;

import de.niclashoyer.resteasytest.resource.H2RepresentationFactory;
import de.niclashoyer.resteasytest.resource.Representation;
import de.niclashoyer.resteasytest.resource.RepresentationFactory;
import de.niclashoyer.resteasytest.webid.WebIDInterceptor;
import de.niclashoyer.resteasytest.webid.netty.WebIDNettyJaxrsServer;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.ResteasyDeployment;

@Path("/")
public class App {

    @Context
    protected RepresentationFactory rf;
    protected SecureRandom random = new SecureRandom();

    public static void main(String[] args) throws Exception {
        ResteasyDeployment deployment = new ResteasyDeployment();
        deployment.setResourceClasses(Collections.singletonList(App.class.getName()));
        deployment.setProviderClasses(Collections.singletonList(WebIDInterceptor.class.getName()));
        HashMap<Class, Object> context = new HashMap<>();
        context.put(RepresentationFactory.class, new H2RepresentationFactory());
        deployment.setDefaultContextObjects(context);
        final WebIDNettyJaxrsServer server = new WebIDNettyJaxrsServer();
        server.setDeployment(deployment);
        server.setKeyManagers(App.getKeyManager());
        int port = 3210;
        server.setPort(port);
        server.start();
        System.out.println("Server listening on port " + port);
    }

    protected static KeyManager[] getKeyManager() throws NoSuchAlgorithmException, FileNotFoundException, KeyStoreException, IOException, UnrecoverableKeyException, CertificateException {
        TrustManagerFactory tmFactory = TrustManagerFactory.getInstance("PKIX");
        KeyStore tmpKS = null;
        tmFactory.init(tmpKS);
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(App.class.getClassLoader().getResourceAsStream("cert.jks"), "secret".toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, "secret".toCharArray());
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
                str += i + ". " + uri + "\n";
                i++;
            }
        }
        return Response.status(200).entity(str).type(MediaType.TEXT_PLAIN).build();
    }

    @GET
    @Path("{path:.*}")
    public Response getAll(@PathParam("path") String path, InputStream body, @Context Request req) throws IOException {
        ResponseBuilder resp;
        Variant selected;
        List<Variant> variants;
        Representation rep;
        path = "/" + path;
        variants = rf.getVariants(path);
        if (variants.isEmpty()) {
            resp = Response.status(Response.Status.NOT_FOUND);
            return resp.build();
        }
        selected = req.selectVariant(variants);
        if (selected == null) {
            resp = Response.notAcceptable(variants);
            return resp.build();
        }
        rep = rf.selectRepresentation(path, selected);
        resp = req.evaluatePreconditions(rep.getLastModified(), rep.getEntityTag());
        if (resp != null) {
            return resp.build();
        } else {
            resp = Response.ok(rep.getInputStream(), selected);
            resp.tag(rep.getEntityTag());
            resp.lastModified(rep.getLastModified());
            resp.variants(variants);
            return resp.build();
        }
    }

    @PUT
    @Path("{path:.*}")
    public Response putAll(@Context Request req, @HeaderParam("Content-Type") MediaType type, @HeaderParam("Content-Language") Locale loc, @PathParam("path") String path, InputStream body) throws IOException {
        ResponseBuilder resp;
        Variant variant;
        List<Variant> variants;
        Representation rep;
        path = "/" + path;
        variant = new Variant(type, loc, "deflate");
        rep = rf.selectRepresentation(path, variant);
        if (rep == null) {
            variants = rf.getVariants(path);
            if (variants.isEmpty()) {
                resp = Response.noContent().status(Response.Status.CONFLICT);
            } else {
                rep = rf.createRepresentation(path, variant);
                IOUtils.copy(body, rep.getOutputStream());
                resp = Response.noContent().status(Response.Status.OK);
                resp.tag(rep.getEntityTag());
            }
        } else {
            resp = req.evaluatePreconditions(rep.getLastModified(), rep.getEntityTag());
            if (resp == null) {
                IOUtils.copy(body, rep.getOutputStream());
                resp = Response.noContent().status(Response.Status.OK);
                resp.tag(rep.getEntityTag());
            }
        }
        return resp.build();
    }

    @POST
    public Response postAll(@HeaderParam("Content-Type") MediaType type, @HeaderParam("Content-Language") Locale loc, @Context UriInfo uri, InputStream body) throws IOException {
        ResponseBuilder resp;
        Variant variant;
        Representation rep;
        String path = "/" + new BigInteger(130, random).toString(32).substring(0, 20);
        if (loc == null) {
            loc = Locale.ROOT;
        }
        variant = new Variant(type, loc, "deflate");
        rep = rf.createRepresentation(path, variant);
        IOUtils.copy(body, rep.getOutputStream());
        resp = Response.noContent().status(Response.Status.CREATED);
        resp.tag(rep.getEntityTag());
        resp.location(uri.getAbsolutePathBuilder().path(path).build());
        return resp.build();
    }
}