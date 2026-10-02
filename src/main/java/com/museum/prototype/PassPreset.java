package com.museum.prototype;

import java.util.ArrayList;
import java.util.List;

/**
 * A named prototype containing the option identifiers for a museum pass.
 */
public class PassPreset implements Cloneable {
    private final String name;
    private final List<String> options;

    /**
     * Creates a preset prototype with the supplied name and options.
     *
     * @param name the preset name
     * @param options the option identifiers included in the preset
     */
    public PassPreset(String name, List<String> options) {
        this.name = name;
        this.options = new ArrayList<>(options);
    }

    /**
     * Returns the name identifying this prototype.
     *
     * @return the preset name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the option identifiers held by this prototype.
     *
     * @return the mutable option list belonging to this preset
     */
    public List<String> getOptions() {
        return options;
    }

    /**
     * Creates an independent prototype whose option list can be changed without
     * changing this prototype.
     *
     * @return a deep copy of this preset
     */
    public PassPreset copy() {
        return new PassPreset(name, new ArrayList<>(options));
    }
}
