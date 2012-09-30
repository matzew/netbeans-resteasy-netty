
package de.niclashoyer.resteasytest.resource;

import java.sql.SQLException;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

public class H2Test {
    public static void main(String[] args) throws SQLException, MimeTypeParseException {
        RepresentationFactory sf = new H2RepresentationFactory();
        Representation r;
        r = sf.readRepresentation("/test", new MimeType("application", "json"));
        System.out.println(r.getETag());
        r = sf.writeRepresentation("/test", new MimeType("text", "plain"));
        System.out.println(r.getETag());
        r = sf.writeRepresentation("/test", new MimeType("application", "json"));
        System.out.println(r.getETag());
        r = sf.readRepresentation("/test", new MimeType("application", "json"));
        System.out.println(r.getETag());
        r = sf.readRepresentation("/test", new MimeType("application", "*"));
        System.out.println(r.getETag());
        r = sf.readRepresentation("/test", new MimeType("*", "*"));
        System.out.println(r.getETag());
        System.out.println(sf.getTypes("/test"));
    }
}
