package com.museum.builder;

import com.museum.model.MuseumPass;

/**
 * Director of the Builder pattern.
 *
 * <p>This class plays the role of the <em>Director</em>: it knows the recipes, that is the
 * fixed sequences of build steps that produce the museum pass configurations the museum
 * sells most often. Clients that want one of these ready made combinations call a director
 * method instead of repeating the same chain of {@link MuseumPassBuilder} calls, while
 * clients that need a custom combination keep using the builder directly.</p>
 *
 * <p>The director never creates the base pass and never touches the decorators itself; it
 * only drives a {@link MuseumPassBuilder}, which keeps the construction logic in one
 * place.</p>
 */
public final class PassDirector {

    /**
     * Prevents instantiation: the director only exposes static construction recipes.
     */
    private PassDirector() {
    }

    /**
     * Builds the full experience pass: audio guide, virtual tour, VIP access and
     * accessibility support.
     *
     * <p>Director recipe of the Builder pattern that applies every available build step,
     * in that order, over the received base pass.</p>
     *
     * @param base the pass to decorate, must not be {@code null}
     * @return a pass decorated with the four available services
     * @throws IllegalArgumentException if {@code base} is {@code null}
     */
    public static MuseumPass buildFullExperience(MuseumPass base) {
        return new MuseumPassBuilder(base)
                .withAudioGuide()
                .withVirtualTour()
                .withVipAccess()
                .withAccessibility()
                .build();
    }

    /**
     * Builds the cultural visit pass: audio guide and virtual tour.
     *
     * <p>Director recipe of the Builder pattern aimed at visitors interested in the
     * cultural content of the museum rather than in premium services.</p>
     *
     * @param base the pass to decorate, must not be {@code null}
     * @return a pass decorated with the audio guide and the virtual tour
     * @throws IllegalArgumentException if {@code base} is {@code null}
     */
    public static MuseumPass buildCulturalVisit(MuseumPass base) {
        return new MuseumPassBuilder(base)
                .withAudioGuide()
                .withVirtualTour()
                .build();
    }
}
