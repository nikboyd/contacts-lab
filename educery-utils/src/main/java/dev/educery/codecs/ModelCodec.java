package dev.educery.codecs;

import java.io.*;
import javax.xml.bind.*;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import static dev.educery.utils.Exceptional.*;
import dev.educery.utils.Logging;

/**
 * Converts a properly annotated model to (or from) XML or JSON.
 *
 * <h4>ModelCodec Responsibilities:</h4>
 * <ul>
 * <li>encodes a model to JSON or XML</li>
 * <li>decodes a model from JSON or XML</li>
 * </ul>
 *
 * <h4>Client Responsibilities:</h4>
 * <ul>
 * <li>provides a model (and/or its class) to be converted</li>
 * </ul>
 * @param <ModelType> a model type
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
@SuppressWarnings("unchecked")
public class ModelCodec<ModelType> implements Logging {

    static final String XML_ENCODING = "UTF-8";

    private Class<ModelType> entityClass;
    private ModelType entity;

    /**
     * Returns a new ModelCodec.
     * @param <ModelType> a model type
     * @param modelClass a model class
     * @return a new ModelCodec
     */
    public static <ModelType> ModelCodec<ModelType> to(Class<ModelType> modelClass) {
        ModelCodec<ModelType> result = new ModelCodec();
        result.entityClass = modelClass;
        return result;
    }

    /**
     * Returns a new model instance.
     * @param modelXML a model in XML format
     * @return a new model, or null
     */
    public ModelType fromXML(String modelXML) {
        if (StringUtils.isEmpty(modelXML)) return null;
        return nullOrTryLoudly(() -> {
            byte[] xmlData = modelXML.getBytes(XML_ENCODING);
            ByteArrayInputStream stream = new ByteArrayInputStream(xmlData);
            return (ModelType) buildJAXBContext().createUnmarshaller().unmarshal(stream);
        });
    }

    /**
     * Returns a new model instance.
     * @param modelJSON a model in JSON format
     * @return a new model, or null
     */
    public ModelType fromJSON(String modelJSON) {
        if (StringUtils.isEmpty(modelJSON)) return null;
        return nullOrTryLoudly(() -> buildObjectMapper().readValue(modelJSON, this.entityClass));
    }

    /**
     * Returns a new ModelCodec.
     * @param <ModelType> a model type
     * @param model a model instance to serialize
     * @return a new ModelCodec
     */
    public static <ModelType> ModelCodec<ModelType> from(ModelType model) {
        ModelCodec<ModelType> result = new ModelCodec();
        result.entity = model;
        result.entityClass = (Class<ModelType>) model.getClass();
        return result;
    }

    /**
     * Converts a model to XML.
     * @return model XML, or empty
     */
    public String toXML() {
        return emptyOrTryLoudly(() -> {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Marshaller m = buildJAXBContext().createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            m.marshal(this.entity, stream);
            return stream.toString(XML_ENCODING).trim();
        });
    }

    /**
     * Converts a model to JSON.
     * @return model JSON, or empty
     */
    public String toJSON() {
        return emptyOrTryLoudly(() -> buildObjectMapper().writeValueAsString(this.entity));
    }

    /**
     * Returns a new JAXB context.
     * @return a JAXBContext
     * @throws JAXBException if raised during construction
     */
    private JAXBContext buildJAXBContext() throws JAXBException {
        return JAXBContext.newInstance(this.entityClass);
    }

    /**
     * Returns a new JSON object mapper.
     */
    private ObjectMapper buildObjectMapper() {
        ObjectMapper result = new ObjectMapper();
        result.setAnnotationIntrospector(new JaxbAnnotationIntrospector(result.getTypeFactory()));
        result.enable(SerializationFeature.INDENT_OUTPUT);
        return result;
    }

} // ModelCodec
