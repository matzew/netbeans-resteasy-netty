
package de.niclashoyer.resteasytest.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import javax.activation.MimeType;

public class Representation {
    protected OutputStream outputStream;
    protected InputStream inputStream;
    protected String path;
    protected String ETag;
    protected int version;
    protected Date updated;
    protected Date created;
    protected MimeType mimetype;

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getETag() {
        return ETag;
    }

    public void setETag(String ETag) {
        this.ETag = ETag;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Date getUpdate() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public MimeType getMimetype() {
        return mimetype;
    }

    public void setMimetype(MimeType mimetype) {
        this.mimetype = mimetype;
    }
    
    public void setFileForStreams(File file) throws FileNotFoundException {
        this.setOutputStream(new FileOutputStream(file));
        this.setInputStream(new FileInputStream(file));
    }
    
}
