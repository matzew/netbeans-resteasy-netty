
package de.niclashoyer.resteasytest.resource;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;
import javax.ws.rs.ext.Provider;

@Provider
public interface RepresentationFactory {
    public List<Variant> getVariants(String path);
    public OutputStream writeRepresentation(Variant v);
    public InputStream selectRepresentation(Variant v);
    @Deprecated
    public Collection<MediaType> getTypes(String path);
    @Deprecated
    public Representation readRepresentation(String path, MediaType type);
    @Deprecated
    public Representation writeRepresentation(String path, MediaType type);
}
