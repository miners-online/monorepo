package uk.minersonline.games.server_bootstrap.feature;

import net.kyori.adventure.key.KeyPattern;
import org.intellij.lang.annotations.Subst;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Marks a class as a Feature implementation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FeatureImplementation {
    /** Unique identifier for the feature */
    @KeyPattern @Subst("default:unknown") String id();

    /** Version of the feature */
    String version();
}
