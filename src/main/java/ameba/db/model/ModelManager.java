package ameba.db.model;

import ameba.container.Container;
import ameba.core.AddOn;
import ameba.core.Application;
import ameba.db.DataSource;
import ameba.event.Listener;
import ameba.exception.AmebaException;
import ameba.util.ClassUtils;
import ameba.util.IOUtils;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import javassist.ClassPool;
import javassist.CtClass;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.server.ResourceFinder;
import org.glassfish.jersey.server.internal.scanning.PackageNamesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Configuration;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author icode
 */
public class ModelManager extends AddOn {

    public static final String MODULE_MODELS_KEY_PREFIX = "db.default.models.";
    private static Logger logger = LoggerFactory.getLogger(ModelManager.class);
    private static String DEFAULT_DB_NAME = null;
    private static Map<String, Set<Class>> modelMap = Maps.newLinkedHashMap();

    public static String getDefaultDBName() {
        return DEFAULT_DB_NAME;
    }

    public static Set<Class> getModels(String name) {
        return modelMap.get(name);
    }

    @Override
    public void setup(final Application application) {
        Configuration config = application.getConfiguration();
        DEFAULT_DB_NAME = (String) config.getProperty("db.default");

        if (StringUtils.isBlank(DEFAULT_DB_NAME)) {
            DEFAULT_DB_NAME = Model.DB_DEFAULT_SERVER_NAME;
        } else {
            DEFAULT_DB_NAME = StringUtils.deleteWhitespace(DEFAULT_DB_NAME).split(",")[0];
        }


        Set<String> defaultModelsPkg = Sets.newLinkedHashSet();
        //db.default.models.pkg=
        for (String key : config.getPropertyNames()) {
            if (key.startsWith(MODULE_MODELS_KEY_PREFIX)) {
                String modelPackages = (String) config.getProperty(key);
                if (StringUtils.isNotBlank(modelPackages)) {
                    Collections.addAll(defaultModelsPkg, StringUtils.deleteWhitespace(modelPackages).split(","));
                }
            }
        }

        for (String name : DataSource.getDataSourceNames()) {
            String modelPackages = (String) config.getProperty("db." + name + ".models");
            if (StringUtils.isNotBlank(modelPackages)) {
                final Set<String> pkgs = Sets.newHashSet(StringUtils.deleteWhitespace(modelPackages).split(","));

                //db.default.models.pkg=
                //db.default.models+=
                if (getDefaultDBName().equalsIgnoreCase(name)) {
                    pkgs.addAll(defaultModelsPkg);
                }

                application.packages(pkgs.toArray(new String[pkgs.size()]));

                final Set<Class> classes = Sets.newHashSet();
                subscribeSystemEvent(Container.ReloadEvent.class, new Listener<Container.ReloadEvent>() {
                    @Override
                    public void onReceive(Container.ReloadEvent event) {
                        classes.clear();
                    }
                });

                subscribeSystemEvent(Application.ClassFoundEvent.class, new Listener<Application.ClassFoundEvent>() {
                    @Override
                    public void onReceive(Application.ClassFoundEvent event) {
                        event.accept(new Application.ClassFoundEvent.ClassAccept() {
                            @Override
                            public boolean accept(Application.ClassFoundEvent.ClassInfo info) {
                                for (String st : pkgs) {
                                    if (!st.endsWith(".")) st += ".";
                                    String className = info.getClassName();
                                    if (className.startsWith(st)) {
                                        logger.trace("load class : {}", className);
                                        classes.add(info.toClass());
                                        return true;
                                    }
                                }

                                return false;
                            }
                        });
                    }
                });
                modelMap.put(name, classes);
            }
        }

        defaultModelsPkg.clear();
    }

}
