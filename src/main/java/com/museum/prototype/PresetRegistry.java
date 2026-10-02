package com.museum.prototype;

import java.util.List;
import java.util.Map;

/**
 * Registry of named pass prototypes used to create independent preset copies.
 */
public final class PresetRegistry {
    private static final Map<String, PassPreset> PRESETS = Map.of(
            "explorer", new PassPreset("explorer", List.of("audio", "virtual")),
            "premium", new PassPreset("premium", List.of("audio", "vip")),
            "inclusive", new PassPreset("inclusive", List.of("audio", "accessibility")));
    private static final List<String> PRESET_NAMES = List.of("explorer", "premium", "inclusive");

    private PresetRegistry() {
    }

    /**
     * Returns a deep copy of the named registered prototype.
     *
     * @param name the registered preset name
     * @return an independent copy of the named preset
     * @throws IllegalArgumentException if no preset has the supplied name
     */
    public static PassPreset getClone(String name) {
        PassPreset preset = PRESETS.get(name);
        if (preset == null) {
            throw new IllegalArgumentException("Unknown preset: " + name);
        }

        // A deep copy prevents changes to the clone's options from affecting the registry.
        return preset.copy();
    }

    /**
     * Returns the names of all registered presets.
     *
     * @return the registered preset names
     */
    public static List<String> getPresetNames() {
        return PRESET_NAMES;
    }
}
