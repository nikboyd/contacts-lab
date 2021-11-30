package dev.educery.context;

import java.util.HashMap;
import org.apache.commons.lang3.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.*;
import dev.educery.utils.Logging;

/**
 * Loads configured beans from a Spring context.
 *
 * <h4>SpringContext Responsibilities:</h4>
 * <ul>
 * <li>supports bean context configuration on the class path</li>
 * <li>supports bean context configuration in the file system</li>
 * <li>caches loaded contexts by name</li>
 * </ul>
 *
 * <h4>Client Responsibilities:</h4>
 * <ul>
 * <li>supply a bean type, and possibly a bean name</li>
 * </ul>
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class SpringContext implements Logging {

    static final String Dot = ".";
    static final String Dollar = "$";

    /**
     * Returns a bean loaded from a configured context.
     *
     * @param <BeanType> a bean type
     * @param beanType a kind of bean
     * @return a bean, or null
     */
    public static <BeanType> BeanType getConfigured(Class<BeanType> beanType) {
        return standardContext().getBean(beanType);
    }

    /**
     * Returns a bean loaded from a configured context.
     *
     * @param <BeanType> a bean type
     * @param beanType a kind of bean
     * @param beanName a bean name
     * @return a bean, or null
     */
    public static <BeanType> BeanType getConfigured(Class<BeanType> beanType, String beanName) {
        return standardContext().getBean(beanType, beanName);
    }

    /**
     * The standard context file.
     */
    static final String StandardFile = "spring-context.xml";
    public static SpringContext standardContext() {
        return SpringContext.named(StandardFile);
    }

    /**
     * Returns a specific named context.
     * @param contextName a context (file) name
     * @return a specific named context
     */
    public static SpringContext named(String contextName) {
        if (StringUtils.defaultString(contextName).isEmpty()) {
            return null; // no such context
        }
        if (ContextMap.containsKey(contextName)) {
            // already cached context
            return ContextMap.get(contextName);
        }

        // cache the named context
        SpringContext result = new SpringContext();
        result.contextName = contextName;
        ContextMap.put(contextName, result);
        return result;
    }

    /**
     * The loaded context cache.
     */
    static final HashMap<String, SpringContext> ContextMap = new HashMap();

    private String contextName;
    private ApplicationContext cachedContext;

    /**
     * Configures this context to load data from the class path.
     * @return this context
     */
    public SpringContext fromClassPath() {
        this.cachedContext = new ClassPathXmlApplicationContext(this.contextName);
        return this;
    }

    /**
     * Configures this context to load data from the file system.
     * @return this context
     */
    public SpringContext fromFileSystem() {
        this.cachedContext = new FileSystemXmlApplicationContext(this.contextName);
        return this;
    }

    public <BeanType> BeanType getBean(Class<BeanType> beanType) {
        return beanType == null ? null : getBean(beanType, Empty);
    }

    @SuppressWarnings("unchecked")
    public <BeanType> BeanType getBean(Class<BeanType> beanType, String beanName) {
        if (beanType == null) return null; // unknown class
        String lookup = beanName;
        if (StringUtils.isEmpty(beanName)) {
            lookup = getStandardBeanName(beanType);
        }

        // try locating with bean name
        if (getContext().containsBean(lookup)) {
            return (BeanType) getContext().getBean(lookup);
        }

        try { // create a missing bean
            return beanType.getConstructor().newInstance();
        } catch(Exception ex) {
            // report total failure to locate bean
            String className = beanType.getName();
            if (StringUtils.isEmpty(beanName)) {
                reportMissing(className, lookup);
            } else {
                reportMissing(className, beanName);
            }
            return null;
        }
    }

    static final Object[] NoArgs = {};

    /**
     * Returns the standard bean name for a given class.
     *
     * @param beanType a bean type
     * @return a bean name
     */
    private static String getStandardBeanName(Class<?> beanType) {
        String packageName = beanType.getPackage().getName();
        if (packageName.length() > 0) packageName += Dot;
        int index = packageName.length();
        return beanType.getName().substring(index).replace(Dollar, Dot);
    }

    /**
     * Reports that an expected bean was missing from its configuration.
     *
     * @param className a class name
     * @param beanName a bean name
     */
    private void reportMissing(String className, String beanName) {
        warn("can't find a configured bean named '" + beanName + "' in "
                + this.contextName + " type " + className);
    }

    /**
     * Returns the configured context.
     *
     * @return the configured context
     */
    public ApplicationContext getContext() {
        if (this.cachedContext == null) {
            fromClassPath();
        }
        return this.cachedContext;
    }
}
