package com.museum.factory;

import com.museum.model.MuseumPass;

/** Concrete Factory Method creator for the student pass product. */
public class StudentPassCreator extends PassCreator {
    /** Creates a fresh student product for the pass builder.
     * @return a new StudentMuseumPass
     */
    @Override
    public MuseumPass createBasePass() {
        return new StudentMuseumPass();
    }
}
