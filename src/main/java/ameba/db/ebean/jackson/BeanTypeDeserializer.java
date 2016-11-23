package ameba.db.ebean.jackson;

import com.avaje.ebean.text.json.JsonContext;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Deserialize entity beans of a given type.
 *
 * @author icode
 * @version $Id: $Id
 */
public class BeanTypeDeserializer extends JsonDeserializer {

    private final JsonContext jsonContext;

    private final Class<?> beanType;

    /**
     * <p>Constructor for BeanTypeDeserializer.</p>
     *
     * @param jsonContext a {@link com.avaje.ebean.text.json.JsonContext} object.
     * @param beanType    a {@link java.lang.Class} object.
     */
    public BeanTypeDeserializer(JsonContext jsonContext, Class<?> beanType) {
        this.jsonContext = jsonContext;
        this.beanType = beanType;
    }

    /** {@inheritDoc} */
    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        return jsonContext.toBean(beanType, jsonParser);
    }
}
