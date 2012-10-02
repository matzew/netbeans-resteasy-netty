
package de.niclashoyer.resteasytest.resource;

import java.sql.SQLException;
import javax.activation.MimeTypeParseException;
import javax.ws.rs.core.MediaType;

public class H2Test {
    public static void main(String[] args) throws SQLException, MimeTypeParseException {
        RepresentationFactory sf = new H2RepresentationFactory();
        Representation r;
        r = sf.readRepresentation("/test", new MediaType("application", "json"));
        System.out.println(r.getETag());
        r = sf.writeRepresentation("/test", new MediaType("text", "plain"));
        System.out.println(r.getETag());
        r = sf.writeRepresentation("/test", new MediaType("application", "json"));
        System.out.println(r.getETag());
        r = sf.readRepresentation("/test", new MediaType("application", "json"));
        System.out.println(r.getETag());
        r = sf.readRepresentation("/test", new MediaType("application", "*"));
        System.out.println(r.getETag());
        r = sf.readRepresentation("/test", new MediaType("*", "*"));
        System.out.println(r.getETag());
        System.out.println(sf.getTypes("/test"));
    }
}
