package com.museum.factory;

import com.museum.model.MuseumPass;

/** Creator in Factory Method; subclasses choose the base pass product. */
public abstract class PassCreator {
    /** Factory method that supplies a fresh base product for decoration.
     * @return a new museum pass
     */
    public abstract MuseumPass createBasePass();
}
