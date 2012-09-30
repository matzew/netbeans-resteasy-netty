
package de.niclashoyer.resteasytest.resource;

import java.util.Collection;
import javax.activation.MimeType;
import javax.ws.rs.ext.Provider;

@Provider
public interface RepresentationFactory {
    public Collection<MimeType> getTypes(String path);
    public Representation readRepresentation(String path, MimeType type);
    public Representation writeRepresentation(String path, MimeType type);
}
