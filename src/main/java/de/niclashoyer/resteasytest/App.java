package de.niclashoyer.resteasytest;

import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.jboss.resteasy.plugins.server.netty.NettyJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;

@Path("/")
public class App 
{
    public static void main( String[] args ) throws NoSuchAlgorithmException
    {
        ResteasyDeployment deployment = new ResteasyDeployment();
        deployment.setResourceClasses(Collections.singletonList(App.class.getName()));
        final NettyJaxrsServer server = new NettyJaxrsServer();
        server.setDeployment(deployment);
        
        //SSLContext ssl = SSLContext.getInstance("TLS");
        //ssl.init(kms, tms, null);
        
        int port = 3000;
        server.setPort(port);
        server.start();
        System.out.println("Server listening on port "+port);
    }
    
    @GET
    public Response get() {
        return Response.status(200).entity("Hello World!").build();
    }
    
    @GET
    @Path("{any}")
    public Response catchAll() {
        return this.get();
    }
}
