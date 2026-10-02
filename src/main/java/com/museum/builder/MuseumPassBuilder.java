package com.museum.builder;

import com.museum.decorator.AccessibilityDecorator;
import com.museum.decorator.AudioGuideDecorator;
import com.museum.decorator.VipAccessDecorator;
import com.museum.decorator.VirtualTourDecorator;
import com.museum.model.MuseumPass;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Concrete builder of the Builder pattern.
 *
 * <p>This class plays the role of the <em>Builder</em>: it separates the step by step
 * construction of a decorated {@link MuseumPass} from its final representation. The client
 * chains fluent calls to choose the extra services it wants and only the final
 * {@link #build()} call assembles the object graph.</p>
 *
 * <p>Internally the builder does not create the decorators eagerly. It only records the
 * selected option ids, preserving the order in which they were added and ignoring
 * duplicates. When {@link #build()} is invoked, the recorded options are translated into
 * the existing Decorator pattern classes, each one wrapping the previous result, so the
 * Builder pattern acts as a readable facade over the decorator chain.</p>
 */
public class MuseumPassBuilder {

    /** Option id of the audio guide service. */
    private static final String OPTION_AUDIO = "audio";

    /** Option id of the virtual tour service. */
    private static final String OPTION_VIRTUAL = "virtual";

    /** Option id of the VIP access service. */
    private static final String OPTION_VIP = "vip";

    /** Option id of the accessibility support service. */
    private static final String OPTION_ACCESSIBILITY = "accessibility";

    /** Component of the Decorator pattern that every selected decorator will wrap. */
    private final MuseumPass base;

    /** Selected option ids kept in insertion order; the set nature discards duplicates. */
    private final Set<String> selectedOptions = new LinkedHashSet<>();

    /**
     * Creates a builder for the given base pass.
     *
     * <p>The base pass is the concrete component of the Decorator pattern (for example a
     * basic or a student pass, which in this project is produced by the Factory Method
     * pattern). The builder never replaces it, it only decorates it.</p>
     *
     * @param base the pass to decorate, must not be {@code null}
     * @throws IllegalArgumentException if {@code base} is {@code null}
     */
    public MuseumPassBuilder(MuseumPass base) {
        if (base == null) {
            throw new IllegalArgumentException("Base museum pass must not be null.");
        }
        this.base = base;
    }

    /**
     * Adds the audio guide service to the pass under construction.
     *
     * <p>Build step of the Builder pattern. It schedules an
     * {@code AudioGuideDecorator} for the final assembly and returns the builder itself so
     * the steps can be chained fluently.</p>
     *
     * @return this builder, to allow method chaining
     */
    public MuseumPassBuilder withAudioGuide() {
        return withOption(OPTION_AUDIO);
    }

    /**
     * Adds the virtual tour service to the pass under construction.
     *
     * <p>Build step of the Builder pattern that schedules a
     * {@code VirtualTourDecorator} for the final assembly.</p>
     *
     * @return this builder, to allow method chaining
     */
    public MuseumPassBuilder withVirtualTour() {
        return withOption(OPTION_VIRTUAL);
    }

    /**
     * Adds the VIP access service to the pass under construction.
     *
     * <p>Build step of the Builder pattern that schedules a
     * {@code VipAccessDecorator} for the final assembly.</p>
     *
     * @return this builder, to allow method chaining
     */
    public MuseumPassBuilder withVipAccess() {
        return withOption(OPTION_VIP);
    }

    /**
     * Adds the accessibility support service to the pass under construction.
     *
     * <p>Build step of the Builder pattern that schedules an
     * {@code AccessibilityDecorator} for the final assembly.</p>
     *
     * @return this builder, to allow method chaining
     */
    public MuseumPassBuilder withAccessibility() {
        return withOption(OPTION_ACCESSIBILITY);
    }

    /**
     * Adds a service identified by its option id.
     *
     * <p>Generic build step of the Builder pattern. It is the entry point used when the
     * options are not known at compile time, for example when they come from an HTTP query
     * parameter or from a cloned preset of the Prototype pattern. Repeating an option id
     * that was already added is ignored, so a pass is never wrapped twice by the same
     * decorator.</p>
     *
     * @param id one of {@code audio}, {@code virtual}, {@code vip} or {@code accessibility}
     * @return this builder, to allow method chaining
     * @throws IllegalArgumentException if {@code id} is not a known option id
     */
    public MuseumPassBuilder withOption(String id) {
        if (!isKnownOption(id)) {
            throw new IllegalArgumentException("Unknown museum pass option: " + id);
        }
        selectedOptions.add(id);
        return this;
    }

    /**
     * Assembles and returns the final decorated pass.
     *
     * <p>Final step of the Builder pattern: it builds the product. The base component is
     * wrapped by one decorator per selected option, applied in the same order in which the
     * options were added, so the resulting description, price and service list follow that
     * order.</p>
     *
     * @return the decorated pass, or the untouched base pass when no option was selected
     */
    public MuseumPass build() {
        MuseumPass pass = base;
        for (String option : selectedOptions) {
            pass = decorate(pass, option);
        }
        return pass;
    }

    /**
     * Tells whether the given id matches one of the supported options.
     *
     * @param id the option id to validate, may be {@code null}
     * @return {@code true} if the id is supported, {@code false} otherwise
     */
    private static boolean isKnownOption(String id) {
        return OPTION_AUDIO.equals(id)
                || OPTION_VIRTUAL.equals(id)
                || OPTION_VIP.equals(id)
                || OPTION_ACCESSIBILITY.equals(id);
    }

    /**
     * Wraps the given pass with the decorator that corresponds to an option id.
     *
     * <p>This is the single point where the Builder pattern delegates to the Decorator
     * pattern classes already present in the project.</p>
     *
     * @param pass   the pass to wrap
     * @param option the option id of the decorator to apply
     * @return the pass wrapped by the matching decorator
     */
    private static MuseumPass decorate(MuseumPass pass, String option) {
        switch (option) {
            case OPTION_AUDIO:
                return new AudioGuideDecorator(pass);
            case OPTION_VIRTUAL:
                return new VirtualTourDecorator(pass);
            case OPTION_VIP:
                return new VipAccessDecorator(pass);
            case OPTION_ACCESSIBILITY:
                return new AccessibilityDecorator(pass);
            default:
                throw new IllegalArgumentException("Unknown museum pass option: " + option);
        }
    }
}
