
package de.niclashoyer.resteasytest.resource;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import javax.activation.MimeTypeParseException;
import javax.ws.rs.core.MediaType;
import org.apache.commons.io.IOUtils;

public class H2Test {
    public static void main(String[] args) throws SQLException, MimeTypeParseException, IOException {
        RepresentationFactory sf = new H2RepresentationFactory();
        Representation r;
        /*
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
        r = sf.readRepresentation("/hallo/welt", new MediaType("*", "*"));
        System.out.println(r.getETag());
        System.out.println(IOUtils.toString(r.getInputStream()));*/
        FileOutputStream s = new FileOutputStream("representations/da6c7e1bd75f3d14b28a52c26cd7f27597a5386e.bin");
        //System.out.println(s.available());
    }
}
