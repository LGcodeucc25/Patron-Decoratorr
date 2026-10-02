package com.museum.factory;

import com.museum.model.MuseumPass;
import com.museum.model.BasicMuseumPass;

/** Concrete Factory Method creator for the standard pass product. */
public class StandardPassCreator extends PassCreator {
    /** Creates a fresh standard product for the pass builder.
     * @return a new BasicMuseumPass
     */
    @Override
    public MuseumPass createBasePass() {
        return new BasicMuseumPass();
    }
}
