
package de.niclashoyer.resteasytest.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Variant;

public class FileRepresentation implements Representation {

    protected File file;
    protected Date modified;
    protected Date created;
    protected Variant variant;
    protected EntityTag tag;
    
    public FileRepresentation(File file, Date modified, Date created, Variant variant, EntityTag tag) {
        this.file = file;
        this.modified = modified;
        this.created = created;
        this.variant = variant;
        this.tag = tag;
    }
    
    @Override
    public OutputStream getOutputStream() {
        try {
            return new FileOutputStream(this.file);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileRepresentation.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public InputStream getInputStream() {
        try {
            return new FileInputStream(this.file);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileRepresentation.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public Variant getVariant() {
        return this.variant;
    }

    @Override
    public Date getLastModified() {
        return this.modified;
    }

    @Override
    public Date getCreated() {
        return this.created;
    }

    @Override
    public EntityTag getEntityTag() {
        return this.tag;
    }
    
}
