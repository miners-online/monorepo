package uk.minersonline.games.server_bootstrap.feature;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import net.kyori.adventure.key.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class FeatureRegistry {
    private static final Logger logger = LoggerFactory.getLogger(FeatureRegistry.class);

    public static final FeatureRegistry INSTANCE = new FeatureRegistry();

    /** Stores all discovered dependencies by ID */
    private final Map<Key, FeatureInfo> features = new HashMap<>();

    private FeatureRegistry() {

    }

    public void scanAndRegister() {
        try (ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .scan()) {

            // Find all classes annotated with @FeatureImplementation
            scanResult.getClassesWithAnnotation(FeatureImplementation.class.getName())
                    .forEach(classInfo -> {
                        try {
                            Class<?> clazz = classInfo.loadClass();
                            if (!Feature.class.isAssignableFrom(clazz)) {
                                logger.error("Class {} is annotated with @FeatureImplementation but does not implement Feature", clazz.getName());
                                return;
                            }

                            FeatureImplementation annotation = clazz.getAnnotation(FeatureImplementation.class);

                            // Store in registry
                            Key id = Key.key(annotation.id());
                            features.put(id, new FeatureInfo(id, annotation.version(), clazz));

                        } catch (Throwable e) {
                            logger.error("Failed to load feature class {}", classInfo.getName(), e);
                        }
                    });
        }
    }

    /** Retrieve feature info by ID */
    public FeatureInfo getFeature(Key id) {
        return features.get(id);
    }

    /** Retrieve all registered dependencies */
    public Map<Key, FeatureInfo> getAllFeatures() {
        return features;
    }

    public static boolean isFeatureLoaded(Key id) {
        return INSTANCE.getFeature(id) != null;
    }

    /**
     * Simple container for feature metadata
     */
    public record FeatureInfo(Key id, String version, Class<?> clazz) {

        public Feature createInstance() throws ReflectiveOperationException {
            return (Feature) clazz.getDeclaredConstructor().newInstance();
        }
    }
}
