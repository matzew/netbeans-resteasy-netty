
package de.niclashoyer.resteasytest.rdf;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;

public class MemoryStoreFactory implements StoreFactory {
    
    Dataset session;
    Dataset storage;
    
    public MemoryStoreFactory() {
        this.session = DatasetFactory.createMem();
        this.storage = DatasetFactory.createMem();
    }

    @Override
    public Dataset getSession() {
        return this.session;
    }

    @Override
    public Dataset getStorage() {
        return this.storage;
    }
    
}
