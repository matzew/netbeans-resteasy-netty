
package de.niclashoyer.resteasytest.webid;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.Provider;
import org.jboss.resteasy.annotations.interception.Precedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;

@Provider
@ServerInterceptor
@Precedence("SECURITY")
public class WebIDInterceptor implements PreProcessInterceptor {

    @Override
    public ServerResponse preProcess(HttpRequest req, ResourceMethod m) throws Failure, WebApplicationException {
        Object claims = req.getAttribute("webidclaims");
        return null;
    }
    
}
