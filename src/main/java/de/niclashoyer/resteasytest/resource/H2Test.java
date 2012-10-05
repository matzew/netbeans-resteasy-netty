
package de.niclashoyer.resteasytest.resource;


import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import javax.activation.MimeTypeParseException;

public class H2Test {
    public static void main(String[] args) throws SQLException, MimeTypeParseException, IOException {
        //RepresentationFactory sf = new H2RepresentationFactory();
        System.out.println(Locale.GERMANY.toLanguageTag());
    }
}
