
package de.niclashoyer.resteasytest.rdf;

import com.hp.hpl.jena.query.Dataset;

public interface StoreFactory {
    public Dataset getSession();
    public Dataset getStorage();
}
