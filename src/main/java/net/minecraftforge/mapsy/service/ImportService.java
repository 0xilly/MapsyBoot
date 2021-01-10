package net.minecraftforge.mapsy.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import net.minecraftforge.mapsy.dao.*;
import net.minecraftforge.mapsy.repository.mapping.*;
import net.minecraftforge.mapsy.util.Utils;
import net.minecraftforge.mapsy.util.mcp.MCPConfig;
import net.minecraftforge.mapsy.util.mcp.MCPConfigImporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by covers1624 on 18/12/20.
 */
@Service
public class ImportService {

    private static final Logger logger = LogManager.getLogger();

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
    public MinecraftVersion importMCPConfig(InputStream mcpConfig, Optional<MinecraftVersion> forkFrom) throws IOException {
        Map<String, byte[]> files = Utils.loadZip(mcpConfig);
        MCPConfig config = Utils.gson.fromJson(new InputStreamReader(new ByteArrayInputStream(files.get("config.json"))), MCPConfig.class);
        config.zipFiles = files;
        logger.info("Importing MCP Config for minecraft {}, Forking from minecraft: {}", config.version, forkFrom.map(MinecraftVersion::getName).orElse(null));

        Optional<MinecraftVersion> mcVersionOpt = versionRepo.findByName(config.version);

        MinecraftVersion mcVersion;
        if (mcVersionOpt.isEmpty()) {
            mcVersion = new MinecraftVersion(config.version);
        } else {
            MinecraftVersion existing = mcVersionOpt.get();
            mcVersion = new MinecraftVersion(config.version);
            mcVersion.setRevision(existing.getRevision() + 1);
            logger.warn("Minecraft version {} already exists. Revision is now: {}", config.version, mcVersion.getRevision());
            //TODO, set forkFrom to this version if its not specified?
        }
        versionRepo.save(mcVersion);

        logger.info("Loading existing mappings..");
        Map<String, ClassName> existingClasses = forkFrom
                .map(mc -> classNameRepo.findAllByMinecraftVersion(mc)
                        .collect(Collectors.toMap(ClassName::getSrg, Function.identity())))
                .orElse(Collections.emptyMap());
        Map<String, FieldName> existingFields = forkFrom
                .map(mc -> fieldNameRepo.findAllByMinecraftVersion(mc)
                        .collect(Collectors.toMap(FieldName::getSrg, Function.identity())))
                .orElse(Collections.emptyMap());
        Map<FieldName, List<FieldChange>> existingFieldChanges = forkFrom
                .map(mc -> existingFields.values().stream()
                        .collect(Collectors.toMap(e -> e, fieldChangeRepo::getAllByField)))
                .orElse(Collections.emptyMap());
        Map<String, MethodName> existingMethods = forkFrom
                .map(mc -> methodNameRepo.findAllByMinecraftVersion(mc)
                        .filter(e -> !e.isConstructor())
                        .collect(Collectors.toMap(MethodName::getSrg, Function.identity())))
                .orElse(Collections.emptyMap());
        Map<MethodName, List<MethodChange>> existingMethodChanges = forkFrom
                .map(mc -> existingMethods.values().stream()
                        .collect(Collectors.toMap(e -> e, methodChangeRepo::getAllByMethod)))
                .orElse(Collections.emptyMap());

        Map<String, ParameterName> existingParameters = forkFrom
                .map(mc -> parameterNameRepo.findAllByMinecraftVersion(mc)
                        .collect(Collectors.toMap(ParameterName::getSrg, Function.identity())))
                .orElse(Collections.emptyMap());

        Map<ParameterName, List<ParameterChange>> existingParameterChanges = forkFrom
                .map(mc -> existingParameters.values().stream()
                        .collect(Collectors.toMap(e -> e, parameterChangeRepo::getAllByParameter)))
                .orElse(Collections.emptyMap());

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

        importer.process();

        insert("classes", classNameRepo, importer.getClassMap());
        insert("fields", fieldNameRepo, importer.getFieldMap());
        insertLists("field changes", fieldChangeRepo, importer.getFieldChangeMap());
        insert("methods", methodNameRepo, importer.getMethodMap());
        insertLists("method changes", methodChangeRepo, importer.getMethodChangeMap());
        insert("constructors", methodNameRepo, importer.getConstructorMap());
        insert("parameters", parameterNameRepo, importer.getMethodParameterMap());

        logger.info("Done.");
        return mcVersion;
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
}
