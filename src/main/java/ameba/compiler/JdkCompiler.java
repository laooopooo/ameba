package ameba.compiler;

import ameba.util.ClassLoaderUtils;
import ameba.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class JdkCompiler extends JavaCompiler {
    final Logger log = LoggerFactory.getLogger(JdkCompiler.class);
    private final boolean isJdk6;
    private javax.tools.JavaCompiler jc;
    private StandardJavaFileManager fileManager;
    private List<String> options; // 编译参数

    public JdkCompiler() {
        String version = System.getProperty("java.version");
        if (version != null && version.contains("1.6.")) {
            this.isJdk6 = true;
        } else {
            this.isJdk6 = false;
        }
    }

    @Override
    protected void initialize() {
        javax.tools.JavaCompiler jcc = ToolProvider.getSystemJavaCompiler();
        if (jcc == null) {
            // JDT 支持 ServiceLoader 方式载入。
            ServiceLoader<javax.tools.JavaCompiler> serviceLoader = ServiceLoader.load(javax.tools.JavaCompiler.class);
            Iterator<javax.tools.JavaCompiler> iterator = serviceLoader.iterator();
            if (iterator.hasNext()) {
                jcc = iterator.next();
            }
        }
        if (jcc == null) {
            throw new IllegalStateException("Can't get system java compiler. Please add jdk tools.jar to your classpath.");
        }

        this.jc = jcc;
        this.fileManager = jc.getStandardFileManager(null, null, null);
        this.options = Arrays.asList("-encoding", JavaSource.JAVA_FILE_ENCODING, "-g", "-nowarn", "-source", "1.6", "-target", "1.6");

        setDefaultClasspath(fileManager);
    }

    private void setDefaultClasspath(StandardJavaFileManager fileManager) {
        ClassLoader contextClassLoader = ClassLoaderUtils.getContextClassLoader();
        Collection<URL> classpath = ClassLoaderUtils.getClasspathURLs(contextClassLoader);

        // add dependences
        List<String> classlist = Arrays.asList(

        );
        for (String klass : classlist) {
            try {
                Class<?> cls = contextClassLoader.loadClass(klass);
                classpath.add(cls.getProtectionDomain().getCodeSource().getLocation());
            } catch (ClassNotFoundException e) {
            }
        }

        if (classpath.size() > 0) {
            try {
                Set<File> files = new LinkedHashSet<File>(classpath.size() + 16);
                for (URL url : classpath) {
                    File file = new File(url.getFile());
                    if (file.exists()) {
                        files.add(file);
                    }
                }
                Iterable<? extends File> list = fileManager.getLocation(StandardLocation.CLASS_PATH);
                for (File file : list) {
                    files.add(file);
                }
                fileManager.setLocation(StandardLocation.CLASS_PATH, files);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        // 输出编译用的 classpath
        if (debugEnabled) {
            if (log.isInfoEnabled()) {
                Iterable<? extends File> files = fileManager.getLocation(StandardLocation.CLASS_PATH);
                for (File file : files) {
                    log.info("Compilation classpath: " + file.getAbsolutePath());
                }
            }
        }
    }

    @Override
    protected void generateJavaClass(JavaSource source) {
        // 编译代码
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>(); // 编译器编译中的诊断信息
        Iterable<? extends JavaFileObject> files = fileManager.getJavaFileObjects(source.getJavaFile()); // 要编译的所有Java文件
        CompilationTask task = jc.getTask(null, fileManager, diagnostics, options, null, files);

        Boolean result;
        if (isJdk6) {
            // jdk6 的 compiler 是线程不安全的，需要手动同步
            synchronized (this) {
                result = task.call();
            }
        } else {
            // jdk7+ 的 compiler 是线程安全的
            result = task.call();
        }

        // 返回编译结果
        if ((result == null) || !result.booleanValue()) {
            String[] sourceCodeLines = source.getSourceCode().split("\r?\n", -1);
            StringBuilder sb = new StringBuilder();
            sb.append("Compilation failed.");
            sb.append('\n');
            for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
                sb.append(d.getMessage(Locale.ENGLISH)).append('\n')
                        .append(StringUtils.getPrettyError(sourceCodeLines, (int) d.getLineNumber(), (int) d.getColumnNumber(), (int) d.getPosition(), (int) d.getPosition(), 3));
            }
            sb.append(diagnostics.getDiagnostics().size());
            sb.append(" error(s)\n");
            throw new CompileErrorException(sb.toString());
        }
    }
}
