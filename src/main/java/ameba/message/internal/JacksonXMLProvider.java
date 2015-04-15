package ameba.message.internal;

import ameba.core.Application;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.jaxrs.cfg.Annotations;
import com.fasterxml.jackson.jaxrs.xml.JacksonJaxbXMLProvider;
import org.glassfish.jersey.server.ContainerRequest;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.OutputStream;

/**
 * <p>JacksonXMLProvider class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
@Singleton
public class JacksonXMLProvider extends JacksonJaxbXMLProvider {
    @Inject
    private Provider<UriInfo> uriInfoProvider;
    @Inject
    private Provider<ContainerRequest> requestProvider;

    /**
     * <p>Constructor for JacksonXMLProvider.</p>
     *
     * @param app       a {@link ameba.core.Application} object.
     * @param xmlMapper a {@link com.fasterxml.jackson.dataformat.xml.XmlMapper} object.
     */
    @Inject
    public JacksonXMLProvider(Application app, XmlMapper xmlMapper) {
        this(app, xmlMapper, DEFAULT_ANNOTATIONS);

    }

    /**
     * <p>Constructor for JacksonXMLProvider.</p>
     *
     * @param app              a {@link ameba.core.Application} object.
     * @param mapper           a {@link com.fasterxml.jackson.dataformat.xml.XmlMapper} object.
     * @param annotationsToUse an array of {@link com.fasterxml.jackson.jaxrs.cfg.Annotations} objects.
     */
    public JacksonXMLProvider(Application app, XmlMapper mapper, Annotations[] annotationsToUse) {
        super(mapper, annotationsToUse);
        setAnnotationsToUse(annotationsToUse);
        JacksonUtils.configureMapper(app.getMode().isDev(), mapper);
    }

    @Override
    protected JsonGenerator _createGenerator(ObjectWriter writer, OutputStream rawStream, JsonEncoding enc) throws IOException {
        JsonGenerator generator = super._createGenerator(writer, rawStream, enc);
        if (requestProvider.get().getMethod().equalsIgnoreCase("get")) {
            JacksonUtils.configureGenerator(uriInfoProvider.get(), generator);
        }
        return generator;
    }

}
