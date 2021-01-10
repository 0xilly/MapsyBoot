package net.minecraftforge.mapsy.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.ContextSelectorStaticBinder;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.OutputStreamAppender;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import net.minecraftforge.mapsy.dao.*;
import net.minecraftforge.mapsy.repository.mapping.*;
import net.minecraftforge.mapsy.util.Utils;
import net.minecraftforge.mapsy.util.mcp.MCPConfig;
import net.minecraftforge.mapsy.util.mcp.MCPConfigImporter;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by covers1624 on 18/12/20.
 */
@Service
public class ImportService {

    public static final Executor EXECUTOR = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder()
                    .setNameFormat("Importer")
                    .setDaemon(true)
                    .build()
    );

    private static final Logger logger = ContextSelectorStaticBinder.getSingleton()
            .getContextSelector()
            .getDefaultLoggerContext()
            .getLogger(ImportService.class);

    private final ClassNameRepo classNameRepo;
    private final FieldNameRepo fieldNameRepo;
    private final FieldChangeRepo fieldChangeRepo;
    private final MethodNameRepo methodNameRepo;
    private final MethodChangeRepo methodChangeRepo;
    private final ParameterNameRepo parameterNameRepo;
    private final ParameterChangeRepo parameterChangeRepo;
    private final MinecraftVersionRepo versionRepo;
    private final VersionCacheService cacheService;

    public ImportService(
            ClassNameRepo classNameRepo,
            FieldNameRepo fieldNameRepo,
            FieldChangeRepo fieldChangeRepo,
            MethodNameRepo methodNameRepo,
            MethodChangeRepo methodChangeRepo,
            ParameterNameRepo parameterNameRepo,
            ParameterChangeRepo parameterChangeRepo,
            MinecraftVersionRepo versionRepo,
            VersionCacheService cacheService
    ) {
        this.classNameRepo = classNameRepo;
        this.fieldNameRepo = fieldNameRepo;
        this.fieldChangeRepo = fieldChangeRepo;
        this.methodNameRepo = methodNameRepo;
        this.methodChangeRepo = methodChangeRepo;
        this.parameterNameRepo = parameterNameRepo;
        this.parameterChangeRepo = parameterChangeRepo;
        this.versionRepo = versionRepo;
        this.cacheService = cacheService;
    }

    @Transactional
    public ImportReport importMCPConfig(InputStream mcpConfig, Optional<MinecraftVersion> forkFrom) throws IOException {
        Context ctx = ContextSelectorStaticBinder.getSingleton().getContextSelector().getDefaultLoggerContext();
        ByteArrayOutputStream logCapture = new ByteArrayOutputStream();
        OutputStreamAppender<ILoggingEvent> appender = new OutputStreamAppender<>();
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        {
            encoder.setPattern("[%d{HH:mm:ss.SSS}] [%thread/%level]: %msg%n");
            encoder.setCharset(StandardCharsets.UTF_8);
            encoder.setContext(ctx);
            encoder.start();

            appender.setName("logcapture");
            appender.setContext(ctx);
            appender.setEncoder(encoder);
            appender.setOutputStream(logCapture);
            appender.start();
        }
        logger.addAppender(appender);

        ImportReport report = new ImportReport();

        try {
            Map<String, byte[]> files = Utils.loadZip(mcpConfig);
            MCPConfig config = Utils.gson.fromJson(new InputStreamReader(new ByteArrayInputStream(files.get("config.json"))), MCPConfig.class);
            config.zipFiles = files;
            logger.info("Importing MCP Config for minecraft {}, Forking from minecraft: {}", config.version, forkFrom.map(MinecraftVersion::getName).orElse(null));

            Optional<MinecraftVersion> mcVersionOpt = versionRepo.findLatestRevisionOf(config.version);

            MinecraftVersion mcVersion;
            if (mcVersionOpt.isEmpty()) {
                mcVersion = new MinecraftVersion(config.version);
            } else {
                MinecraftVersion existing = mcVersionOpt.get();
                mcVersion = new MinecraftVersion(config.version);
                mcVersion.setRevision(existing.getRevision() + 1);
                logger.warn("Minecraft version {} already exists. Revision is now: {}", config.version, mcVersion.getRevision());
            }
            versionRepo.save(mcVersion);

            logger.info("Loading existing mappings..");
            logger.info("Loading existing classes..");
            Map<String, ClassName> existingClasses = forkFrom
                    .map(mc -> classNameRepo.findAllByMinecraftVersion(mc).parallel()
                            .collect(Collectors.toMap(ClassName::getSrg, Function.identity())))
                    .orElse(Collections.emptyMap());
            logger.info("Loading existing fields..");
            Map<String, FieldName> existingFields = forkFrom
                    .map(mc -> fieldNameRepo.findAllByMinecraftVersion(mc).parallel()
                            .collect(Collectors.toMap(FieldName::getSrg, Function.identity())))
                    .orElse(Collections.emptyMap());
            logger.info("Loading existing fieldChanges..");
            Map<FieldName, List<FieldChange>> existingFieldChanges = existingFields.values().stream()
                    .collect(Collectors.toMap(Function.identity(), e -> new ArrayList<>()));
            forkFrom.ifPresent(v -> fieldChangeRepo.findAllByMinecraftVersion(v)
                    .forEach(e -> existingFieldChanges.computeIfAbsent(e.getField(), e2 -> new ArrayList<>())
                            .add(e)));

            logger.info("Loading existing methods..");
            Map<String, MethodName> existingMethods = forkFrom
                    .map(mc -> methodNameRepo.findAllByMinecraftVersion(mc).parallel()
                            .filter(e -> !e.isConstructor())
                            .collect(Collectors.toMap(MethodName::getSrg, Function.identity())))
                    .orElse(Collections.emptyMap());
            logger.info("Loading existing method changes..");
            Map<MethodName, List<MethodChange>> existingMethodChanges = existingMethods.values().stream()
                    .collect(Collectors.toMap(Function.identity(), e -> new ArrayList<>()));
            forkFrom.ifPresent(v -> methodChangeRepo.findAllByMinecraftVersion(v)
                    .forEach(e -> existingMethodChanges.computeIfAbsent(e.getMethod(), e2 -> new ArrayList<>())
                            .add(e)));

            logger.info("Loading existing parameters..");
            Map<String, ParameterName> existingParameters = forkFrom
                    .map(mc -> parameterNameRepo.findAllByMinecraftVersion(mc).parallel()
                            .collect(Collectors.toMap(ParameterName::getSrg, Function.identity())))
                    .orElse(Collections.emptyMap());

            logger.info("Loading existing parameter changes..");
            Map<ParameterName, List<ParameterChange>> existingParameterChanges = existingParameters.values().stream()
                    .collect(Collectors.toMap(Function.identity(), e -> new ArrayList<>()));
            forkFrom.ifPresent(v -> parameterChangeRepo.findAllByMinecraftVersion(v)
                    .forEach(e -> existingParameterChanges.computeIfAbsent(e.getParameter(), e2 -> new ArrayList<>())
                            .add(e)));

            MCPConfigImporter importer = new MCPConfigImporter(
                    config,
                    mcVersion,
                    cacheService,
                    existingClasses,
                    existingFields,
                    existingFieldChanges,
                    existingMethods,
                    existingMethodChanges,
                    existingParameters,
                    existingParameterChanges
            );

            importer.process(appender);

            insert("classes", classNameRepo, importer.getClassMap());
            insert("fields", fieldNameRepo, importer.getFieldMap());
            insertLists("field changes", fieldChangeRepo, importer.getFieldChangeMap());
            insert("methods", methodNameRepo, importer.getMethodMap());
            insertLists("method changes", methodChangeRepo, importer.getMethodChangeMap());
            insert("constructors", methodNameRepo, importer.getConstructorMap());
            insert("parameters", parameterNameRepo, importer.getMethodParameterMap());
            insertLists("parameter changes", parameterChangeRepo, importer.getParameterChangeMap());

            logger.info("Done.");

            report.importedClasses = importer.getClassMap().size();
            report.importedFields = importer.getFieldMap().size();
            report.importedFieldChanges = importer.getFieldChangeMap().values().stream().mapToInt(List::size).sum();
            report.importedMethods = importer.getMethodMap().size();
            report.importedMethodChanges = importer.getMethodChangeMap().values().stream().mapToInt(List::size).sum();
            report.importedConstructors = importer.getConstructorMap().size();
            report.importedMethodParameters = importer.getMethodParameterMap().size();
            report.importedMethodParameterChanges = importer.getParameterChangeMap().values().stream().mapToInt(List::size).sum();
        } catch (Throwable e) {
            logger.error("Error occurred whilst importing.", e);
        } finally {
            logger.detachAppender(appender);
            appender.stop();
            encoder.stop();
        }
        report.log = logCapture.toByteArray();
        return report;
    }

    private <T> void insert(String desc, CrudRepository<T, ?> repo, Map<?, T> things) {
        logger.info("Inserting {} {}.", things.size(), desc);
        repo.saveAll(things.values());
    }

    private <T> void insertLists(String desc, CrudRepository<T, ?> repo, Map<?, List<T>> things) {
        List<T> flatThings = things.values().stream().flatMap(List::stream).collect(Collectors.toList());
        logger.info("Inserting {} {}.", flatThings.size(), desc);
        repo.saveAll(flatThings);
    }

    public void importMCPSnapshot(InputStream snapshot, MinecraftVersion mcVersion) throws IOException {
        Map<String, byte[]> files = Utils.loadZip(snapshot);
        logger.info("Importing methods.");
        parseCSV(new ByteArrayInputStream(files.get("methods.csv")), line -> {
            Optional<MethodName> methodOpt = methodNameRepo.findBySrgAndMinecraftVersion(line[0], mcVersion);
            if (methodOpt.isEmpty()) {
                logger.warn("SRG {} not found for {}, ignoring..", line[0], mcVersion.getName());
                return;
            }
            MethodName method = methodOpt.get();
            method.setMcp(line[1]);
            method.setDescription(line[3]);
            methodNameRepo.save(method);
        });
        logger.info("Importing fields.");
        parseCSV(new ByteArrayInputStream(files.get("fields.csv")), line -> {
            Optional<FieldName> methodOpt = fieldNameRepo.findBySrgAndMinecraftVersion(line[0], mcVersion);
            if (methodOpt.isEmpty()) {
                logger.warn("SRG {} not found for {}, ignoring..", line[0], mcVersion.getName());
                return;
            }
            FieldName field = methodOpt.get();
            field.setMcp(line[1]);
            field.setDescription(line[3]);
            fieldNameRepo.save(field);
        });
        logger.info("Importing parameters.");
        parseCSV(new ByteArrayInputStream(files.get("params.csv")), line -> {
            Optional<ParameterName> methodOpt = parameterNameRepo.findBySrgAndMinecraftVersion(line[0], mcVersion);
            if (methodOpt.isEmpty()) {
                logger.warn("SRG {} not found for {}, ignoring..", line[0], mcVersion.getName());
                return;
            }
            ParameterName param = methodOpt.get();
            param.setMcp(line[1]);
            parameterNameRepo.save(param);
        });
        logger.info("Done.");
    }

    private static void parseCSV(InputStream is, Consumer<String[]> processor) throws IOException {
        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(is)).withSkipLines(1).build()) {
            StreamSupport.stream(reader.spliterator(), true).forEach(processor);
        }
    }

    public static class ImportReport {

        public int importedClasses;
        public int importedFields;
        public int importedFieldChanges;
        public int importedMethods;
        public int importedMethodChanges;
        public int importedConstructors;
        public int importedMethodParameters;
        public int importedMethodParameterChanges;

        public byte[] log;
    }
}
