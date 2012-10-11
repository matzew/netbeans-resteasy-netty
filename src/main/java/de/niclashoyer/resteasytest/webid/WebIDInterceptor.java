
package de.niclashoyer.resteasytest.webid;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.util.FileManager;
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
    
    protected Dataset session;
    
    public WebIDInterceptor() {
        this(DatasetFactory.createMem());
    }
    
    public WebIDInterceptor(Dataset session) {
        this.session = session;
    }

    @Override
    public ServerResponse preProcess(HttpRequest req, ResourceMethod m) throws Failure, WebApplicationException {
        Model webid;
        RSAPublicKey rsa;
        PublicKey key;
        Certificate cert = (Certificate) req.getAttribute("webidcertificate");
        Collection<String> claims = (Collection<String>) req.getAttribute("webidclaims");
        if (claims == null || cert == null) {
            return null;
        }
        key = cert.getPublicKey();
        if (key.getAlgorithm().equals("RSA")) {
            rsa = (RSAPublicKey) key;
        } else {
            return null;
        }
        List<String> webids = new ArrayList<>();
        for (String claim : claims) {
            if (session.containsNamedModel(claim)) {
                webid = session.getNamedModel(claim);
            } else {
                webid = FileManager.get().loadModel(claim);
                session.addNamedModel(claim, webid);
            }
            //webid.begin().removeAll().add(webid).commit();
            String query = getAskQuery(claim, rsa.getModulus(), rsa.getPublicExponent());
            QueryExecution ask = QueryExecutionFactory.create(query, webid);
            boolean verified = ask.execAsk();
            if (verified) {
                webids.add(claim);
            }
        }
        req.setAttribute("webids", webids);
        return null;
    }
    
    protected String getAskQuery(String url, BigInteger modulus, BigInteger exponent) {
        String mod;
        String exp;
        mod = modulus.toString(16);
        exp = exponent.toString(10);
        return
            "PREFIX : <http://www.w3.org/ns/auth/cert#>\n" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
            "ASK {\n" +
            "   <"+url+"> :key [\n" +
            "      :modulus \""+mod+"\"^^xsd:hexBinary;\n" +
            "      :exponent \""+exp+"\"^^xsd:int;\n" +
            "   ] .\n"+
            "}";
    }
    
}
