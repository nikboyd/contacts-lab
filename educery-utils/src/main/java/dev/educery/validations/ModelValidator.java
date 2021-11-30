package dev.educery.validations;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;

import javax.validation.*;

import dev.educery.context.SpringContext;

/**
 * Validates a model using the bean validation framework.
 *
 * <h4>ModelValidator Responsibilities:</h4>
 * <ul>
 * <li>validates an instance of an annotated model class</li>
 * </ul>
 *
 * <h4>Client Responsibilities:</h4>
 * <ul>
 * <li>properly configure a validator</li>
 * </ul>
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class ModelValidator {

    private static final String Dot = ".";

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Properties messages = new Properties();

    /**
     * Configures the message map from a message file.
     *
     * @param messageFile a message file path
     * @throws Exception if raised within
     */
    public void setMessages(String messageFile) throws Exception {
        InputStream fileStream = getClass().getResourceAsStream(messageFile);
        if (fileStream != null) {
            messages.load(fileStream);
            fileStream.close();
        }
    }

    /**
     * Returns the configured validator.
     * @return a ModelValidator, or null
     */
    public static ModelValidator getConfiguredValidator() {
        return SpringContext.getConfigured(ModelValidator.class);
    }

    /**
     * Validates an annotated model.
     * @param <ModelType> a model type
     * @param model an annotated model
     * @return any constraint violation error messages discovered during entity validation
     */
    public <ModelType> String[] validate(ModelType model) {
        String[] empty = {};
        Set<ConstraintViolation<ModelType>> errors = factory.getValidator().validate(model);
        if (errors.isEmpty()) return empty;

        ArrayList<String> results = new ArrayList();
        for (ConstraintViolation<ModelType> error : errors) {
            String messageKey = buildMessageKey(error);
            if (messages.containsKey(messageKey)) {
                results.add(messages.getProperty(messageKey));
            } else {
                results.add(error.getMessage());
            }
        }

        return results.toArray(empty);
    }

    /**
     * Builds a message key from a constraint violation.
     *
     * @param error a constraint violation error
     * @return an error message key
     */
    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    private <EntityType> String buildMessageKey(ConstraintViolation<EntityType> error) {
        Class<?> errorType = error.getConstraintDescriptor().getAnnotation().annotationType();
        Class<?> beanType = error.getRootBeanClass();
        StringBuilder builder = new StringBuilder();
        builder.append(errorType.getSimpleName());
        builder.append(Dot + beanType.getSimpleName());
        builder.append(Dot + error.getPropertyPath());
        return builder.toString();
    }

} // ModelValidator
