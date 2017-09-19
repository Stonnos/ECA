package eca.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

/**
 * Velocity configuration.
 *
 * @author Roman Batygin
 */
@Slf4j
public class VelocityConfiguration {

    private static VelocityConfiguration velocityConfiguration;
    private static VelocityEngine velocityEngine;

    static {
        try {
            velocityEngine = new VelocityEngine();
            velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
            velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
            velocityEngine.init();
        } catch (Exception ex) {
            log.error("Error in init velocity engine: ", ex);
        }
    }

    /**
     * Returns {@link VelocityConfiguration} instance.
     *
     * @return {@link VelocityConfiguration} instance
     */
    public static VelocityConfiguration getVelocityConfiguration() {
        if (velocityConfiguration == null) {
            velocityConfiguration = new VelocityConfiguration();
        }
        return velocityConfiguration;
    }

    /**
     * Returns velocity template.
     *
     * @param name velocity template name
     * @return {@link Template} object
     */
    public Template getTemplate(String name) {
        return velocityEngine.getTemplate(name, "UTF-8");
    }
}
