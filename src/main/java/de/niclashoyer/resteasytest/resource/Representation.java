
package de.niclashoyer.resteasytest.resource;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Variant;

public interface Representation {
    public OutputStream getOutputStream();
    public InputStream getInputStream();
    public Variant getVariant();
    public Date getLastModified();
    public Date getCreated();
    public EntityTag getEntityTag();
}
