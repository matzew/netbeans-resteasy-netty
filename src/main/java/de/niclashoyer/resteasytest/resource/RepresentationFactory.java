
package de.niclashoyer.resteasytest.resource;

import java.util.Collection;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

@Provider
public interface RepresentationFactory {
    public Collection<MediaType> getTypes(String path);
    public Representation readRepresentation(String path, MediaType type);
    public Representation writeRepresentation(String path, MediaType type);
}
