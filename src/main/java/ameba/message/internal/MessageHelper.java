package ameba.message.internal;

import ameba.core.Requests;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.message.internal.HeaderUtils;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.RuntimeDelegate;

/**
 * <p>MessageHelper class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
public class MessageHelper {

    /**
     * Constant <code>STREAMING_RANGE_ENABLED="streaming.range.enabled"</code>
     */
    public static final String STREAMING_RANGE_ENABLED = "streaming.range.enabled";

    private MessageHelper() {
    }

    /**
     * <p>disableCurrentStreamingRange.</p>
     */
    public static void disableCurrentStreamingRange() {
        Requests.setProperty(STREAMING_RANGE_ENABLED, false);
    }

    /**
     * <p>enableCurrentStreamingRange.</p>
     */
    public static void enableCurrentStreamingRange() {
        Requests.setProperty(STREAMING_RANGE_ENABLED, true);
    }

    /**
     * <p>getStreamingProcesses.</p>
     *
     * @param locator a {@link org.glassfish.hk2.api.ServiceLocator} object.
     * @return a {@link java.lang.Iterable} object.
     */
    public static Iterable<StreamingProcess> getStreamingProcesses(ServiceLocator locator) {
        return Providers.getAllProviders(locator, StreamingProcess.class);
    }

    /**
     * <p>getStreamingProcess.</p>
     *
     * @param entity a T object.
     * @param locator a {@link org.glassfish.hk2.api.ServiceLocator} object.
     * @param <T> a T object.
     * @return a {@link ameba.message.internal.StreamingProcess} object.
     */
    @SuppressWarnings("unchecked")
    public static <T> StreamingProcess<T> getStreamingProcess(T entity, ServiceLocator locator) {
        for (StreamingProcess process : getStreamingProcesses(locator)) {
            if (process.isSupported(entity)) {
                return process;
            }
        }
        return null;
    }


    /**
     * <p>getHeaderString.</p>
     *
     * @param headers a {@link javax.ws.rs.core.MultivaluedMap} object.
     * @param name a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getHeaderString(MultivaluedMap<String, Object> headers, String name) {
        return HeaderUtils.asHeaderString(headers.get(name), RuntimeDelegate.getInstance());
    }
}
