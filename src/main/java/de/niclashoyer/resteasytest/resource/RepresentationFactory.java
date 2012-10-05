
package de.niclashoyer.resteasytest.resource;

import java.util.List;
import javax.ws.rs.core.Variant;
import javax.ws.rs.ext.Provider;

@Provider
public interface RepresentationFactory {
    public List<Variant> getVariants(String path);
    public Representation selectRepresentation(String path, Variant v);
    public Representation createRepresentation(String path, Variant v);
}
