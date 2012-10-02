
package de.niclashoyer.resteasytest.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileRepresentation extends Representation {
    
    protected File file;

    @Override
    public OutputStream getOutputStream() {
        try {
            this.setOutputStream(new FileOutputStream(this.file));
            return super.getOutputStream();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileRepresentation.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public InputStream getInputStream() {
        try {
            this.setInputStream(new FileInputStream(this.file));
            return super.getInputStream();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileRepresentation.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public void setFileForStreams(File file) throws FileNotFoundException {
        this.file = file;
    }
}
