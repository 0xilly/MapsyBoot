package net.minecraftforge.mapsy.service;

import com.google.common.base.Objects;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import net.minecraftforge.mapsy.dao.*;
import net.minecraftforge.mapsy.repository.mapping.*;
import net.minecraftforge.mapsy.util.MappingSide;
import net.minecraftforge.mapsy.util.Utils;
import net.minecraftforge.srgutils.IMappingFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

/**
 * Created by covers1624 on 18/12/20.
 */
@Service
public class ImportService {

    private static final Logger logger = LogManager.getLogger();
    private static final Pattern srgIdRegex = Pattern.compile("_(\\d*)_");

    private final ClassNameRepo classNameRepo;
    private final FieldNameRepo fieldNameRepo;
    private final MethodNameRepo methodNameRepo;
    private final ParameterNameRepo parameterNameRepo;
    private final MinecraftVersionRepo versionRepo;
    private final VersionCacheService cacheService;

    public ImportService(ClassNameRepo classNameRepo, FieldNameRepo fieldNameRepo, MethodNameRepo methodNameRepo, ParameterNameRepo parameterNameRepo, MinecraftVersionRepo versionRepo, VersionCacheService cacheService) {
        this.classNameRepo = classNameRepo;
        this.fieldNameRepo = fieldNameRepo;
        this.methodNameRepo = methodNameRepo;
        this.parameterNameRepo = parameterNameRepo;
        this.versionRepo = versionRepo;
        this.cacheService = cacheService;
    }

    public MinecraftVersion importMCPConfig(InputStream mcpConfig, MinecraftVersion forkFrom) throws IOException {
        Map<String, byte[]> files = Utils.loadZip(mcpConfig);
        MCPConfig config = Utils.gson.fromJson(new InputStreamReader(new ByteArrayInputStream(files.get("config.json"))), MCPConfig.class);
        logger.info("Importing MCP Config for minecraft {}, Forking from minecraft: {}", config.version, forkFrom != null ? forkFrom.getName() : null);

        logger.info("Parsing inputs..");
        IMappingFile joinedTSrg = IMappingFile.load(new ByteArrayInputStream(files.get(config.getData("mappings"))));
        IMappingFile joinedRev = joinedTSrg.reverse();
        IMappingFile clientMappings = load(cacheService.getClientMappings(config.version));
        IMappingFile serverMappings = load(cacheService.getServerMappings(config.version));

        Map<String, String> constructorIds = parseConstructors(new ByteArrayInputStream(files.get(config.getData("constructors"))));

        Optional<MinecraftVersion> mcVersionOpt = versionRepo.findByName(config.version);

        MinecraftVersion mcVersion;
        if (mcVersionOpt.isEmpty()) {
            mcVersion = new MinecraftVersion(config.version);
            versionRepo.save(mcVersion);
        } else {
            //TODO, we should probably fork off to a new revision of this version or abort.
            mcVersion = mcVersionOpt.get();
            logger.warn("Minecraft version {} already exists! continuing anyway..", config.version);
        }

        Map<String, ClassName> classMap = new HashMap<>();
        Map<String, FieldName> fieldMap = new HashMap<>();
        Map<String, MethodName> methodMap = new HashMap<>();
        Map<String, MethodName> constructorMap = new HashMap<>();
        Map<String, ParameterName> methodParameterMap = new HashMap<>();

        logger.info("Processing TSRG mappings.");
        //First pass, generate all *Name's from the joined.tsrg
        for (IMappingFile.IClass clazz : joinedTSrg.getClasses()) {
            ClassName cName = new ClassName();

            cName.setMinecraftVersion(mcVersion);
            cName.setObf(clazz.getOriginal());
            cName.setSrg(clazz.getMapped());

            classMap.put(cName.getSrg(), cName);

            for (IMappingFile.IField field : clazz.getFields()) {
                if (!field.getMapped().startsWith("field_")) { continue; }
                FieldName fName = new FieldName();

                fName.setMinecraftVersion(mcVersion);
                fName.setOwner(cName);
                fName.setObf(field.getOriginal());
                fName.setSrg(field.getMapped());
                fieldMap.put(fName.getSrg(), fName);
            }

            for (IMappingFile.IMethod method : clazz.getMethods()) {
                if (!method.getMapped().startsWith("func_")) { continue; }
                String ident = method.getMapped();
                MethodName mName = methodMap.get(ident);
                if (mName == null) {
                    mName = new MethodName();
                    mName.setMinecraftVersion(mcVersion);
                    mName.setObf(method.getOriginal());
                    mName.setObfDescriptor(method.getDescriptor());
                    mName.setSrg(method.getMapped());
                    mName.setSrgDescriptor(method.getMappedDescriptor());
                    methodMap.put(ident, mName);
                }
                mName.addOwner(cName);

                //Build all method parameters. Simple syntax p_srgid_index
                Matcher srgIdMatcher = srgIdRegex.matcher(method.getMapped());
                if (!srgIdMatcher.find()) {
                    logger.warn("SRG name {} does not match regex.", method.getMapped());
                    continue;//Wat
                }
                String srgId = srgIdMatcher.group(1);
                Type[] parameters = Type.getArgumentTypes(method.getMappedDescriptor());
                int idx = 0;
                for (Type parameter : parameters) {
                    ParameterName pName = new ParameterName();
                    pName.setMinecraftVersion(mcVersion);
                    pName.setOwner(mName);
                    pName.setSrg(String.format("p_%s_%s_", srgId, idx));
                    methodParameterMap.put(pName.getSrg(), pName);
                    boolean wide = parameter.getSort() == Type.DOUBLE || parameter.getSort() == Type.LONG;
                    idx += wide ? 2 : 1;//Account for wide vars
                }
            }
        }

        logger.info("Building SRG -> Mojang");
        //Build our SRG -> Official mappings.
        IMappingFile clientLookup = clientMappings.chain(joinedTSrg).reverse();
        IMappingFile serverLookup = serverMappings.chain(joinedTSrg).reverse();

        logger.info("Processing Mojang Client mappings.");
        //First Mojang loop, assign all mojang names, and set client side flags.
        for (IMappingFile.IClass clazz : clientLookup.getClasses()) {
            ClassName cName = classMap.get(clazz.getOriginal());
            if (cName == null) { continue; }
            cName.setMojang(clazz.getMapped());
            cName.setSide(MappingSide.CLIENT);

            for (IMappingFile.IField field : clazz.getFields()) {
                FieldName fName = fieldMap.get(field.getOriginal());
                if (fName == null) { continue; }

                fName.setMojang(field.getMapped());
                fName.setSide(MappingSide.CLIENT);
            }

            for (IMappingFile.IMethod method : clazz.getMethods()) {
                //Generate constructors
                if (method.getOriginal().equals("<init>")) {
                    String srgId = constructorIds.get(clazz.getOriginal() + method.getDescriptor());
                    //If we can't find an srg id for the constructor, and it has parameters, its probably a bug.
                    if (srgId == null && !Objects.equal(method.getDescriptor(), "()V")) {
                        logger.warn("Missing SRG id for constructor: {}.{}{}", clazz.getOriginal(), method.getOriginal(), method.getDescriptor());
                        continue;
                    }
                    MethodName ctorName = new MethodName();
                    ctorName.setMinecraftVersion(mcVersion);
                    ctorName.setObf("<init>");
                    ctorName.setObfDescriptor(joinedRev.remapDescriptor(method.getDescriptor()));
                    ctorName.setSrg(srgId != null ? srgId : "");
                    ctorName.setSrgDescriptor(method.getDescriptor());
                    ctorName.setMojang("<init>");
                    ctorName.setMojangDescriptor(method.getMappedDescriptor());
                    ctorName.setSide(MappingSide.CLIENT);
                    ctorName.addOwner(cName);
                    constructorMap.put(cName.getSrg() + method.getDescriptor(), ctorName);
                    continue;
                }

                MethodName mName = methodMap.get(method.getOriginal());
                if (mName == null) { continue; }

                mName.setMojang(method.getMapped());
                mName.setMojangDescriptor(method.getMappedDescriptor());
                mName.setSide(MappingSide.CLIENT);
            }
        }

        logger.info("Processing Mojang Server mappings.");
        //Second Mojang loop, assign all mojang names, and set server side flags, joining the flags where possible.
        for (IMappingFile.IClass clazz : serverLookup.getClasses()) {
            ClassName cName = classMap.get(clazz.getOriginal());
            if (cName == null) { continue; }
            if (cName.getSide() == MappingSide.CLIENT) {
                cName.setSide(MappingSide.BOTH);
                if (!cName.getMojang().equals(clazz.getMapped())) {
                    logger.warn("Mojang class name differs from client to server: Client: {}, Server: {}", cName.getMojang(), clazz.getMapped());
                }
                continue;
            }
            cName.setMojang(clazz.getMapped());
            cName.setSide(MappingSide.SERVER);

            for (IMappingFile.IField field : clazz.getFields()) {
                FieldName fName = fieldMap.get(field.getOriginal());
                if (fName == null) { continue; }
                if (fName.getSide() == MappingSide.CLIENT) {
                    fName.setSide(MappingSide.BOTH);
                    if (!fName.getMojang().equals(field.getMapped())) {
                        String c = clazz.getMapped() + "." + fName.getMojang();
                        String s = clazz.getMapped() + "." + field.getMapped();
                        logger.warn("Mojang field name differs from client to server: Client: {}, Server: {}", c, s);
                    }
                    continue;
                }
                fName.setMojang(field.getMapped());
                fName.setSide(MappingSide.SERVER);
            }

            for (IMappingFile.IMethod method : clazz.getMethods()) {
                //Again, generate constructors.
                if (method.getOriginal().equals("<init>")) {
                    //If it already exists, just mark it as existing on the server.
                    MethodName ctorName = constructorMap.get(cName.getSrg() + method.getDescriptor());
                    if (ctorName != null) {
                        ctorName.setSide(MappingSide.BOTH);
                        continue;
                    }

                    String srgId = constructorIds.get(clazz.getOriginal() + method.getDescriptor());
                    if (srgId == null && !Objects.equal(method.getDescriptor(), "()V")) {
                        logger.warn("Missing SRG id for constructor: {}.{}{}", clazz.getOriginal(), method.getOriginal(), method.getDescriptor());
                        continue;
                    }
                    ctorName = new MethodName();
                    ctorName.setMinecraftVersion(mcVersion);
                    ctorName.setObf("<init>");
                    ctorName.setObfDescriptor(joinedRev.remapDescriptor(method.getDescriptor()));
                    ctorName.setSrg(srgId != null ? srgId : "");
                    ctorName.setSrgDescriptor(method.getDescriptor());
                    ctorName.setMojang("<init>");
                    ctorName.setMojangDescriptor(method.getMappedDescriptor());
                    ctorName.setSide(MappingSide.SERVER);
                    ctorName.addOwner(cName);
                    constructorMap.put(cName.getSrg() + method.getDescriptor(), ctorName);
                    continue;
                }

                MethodName mName = methodMap.get(method.getOriginal());
                if (mName == null) { continue; }
                if (mName.getSide() == MappingSide.CLIENT) {
                    mName.setSide(MappingSide.BOTH);
                    if (!mName.getMojang().equals(method.getMapped())) {
                        String c = clazz.getMapped() + "." + mName.getMojang();
                        String s = clazz.getMapped() + "." + method.getMapped();
                        logger.warn("Mojang method name differs from client to server: Client: {}, Server: {}", c, s);
                    }
                    continue;
                }
                mName.setMojang(method.getMapped());
                mName.setMojangDescriptor(method.getMappedDescriptor());
                mName.setSide(MappingSide.SERVER);
            }
        }

        logger.info("Generating constructor parameters.");
        //Generate constructor parameters.
        for (MethodName ctorName : constructorMap.values()) {
            String srgId = ctorName.getSrg();
            Type[] parameters = Type.getArgumentTypes(ctorName.getSrgDescriptor());
            int idx = 0;
            for (Type parameter : parameters) {
                ParameterName pName = new ParameterName();
                pName.setMinecraftVersion(mcVersion);
                pName.setOwner(ctorName);
                pName.setSrg(String.format("p_i%s_%s_", srgId, idx));
                methodParameterMap.put(pName.getSrg(), pName);
                boolean wide = parameter.getSort() == Type.DOUBLE || parameter.getSort() == Type.LONG;
                idx += wide ? 2 : 1;//Account for wide vars
            }
        }

        logger.info("Inserting {} classes.", classMap.size());
        classNameRepo.saveAll(classMap.values());

        logger.info("Inserting {} fields.", fieldMap.size());
        fieldNameRepo.saveAll(fieldMap.values());

        logger.info("Inserting {} methods.", methodMap.size());
        methodNameRepo.saveAll(methodMap.values());

        logger.info("Inserting {} constructors.", constructorMap.size());
        methodNameRepo.saveAll(constructorMap.values());

        logger.info("Inserting {} parameters.", methodParameterMap.size());
        parameterNameRepo.saveAll(methodParameterMap.values());

        logger.info("Done.");
        return mcVersion;
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

    private static Map<String, String> parseConstructors(InputStream is) throws IOException {
        Map<String, String> constructors = new HashMap<>();
        CSVParser parser = new CSVParserBuilder().withSeparator(' ').build();
        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(is)).withCSVParser(parser).build()) {
            for (String[] line : reader) {
                if (line.length > 3) {
                    logger.warn("Malformed constructors line: {}", String.join(" ", line));
                    continue;
                }
                constructors.put(line[1] + line[2], line[0]);
            }
        }

        return constructors;
    }

    private static void parseCSV(InputStream is, Consumer<String[]> processor) throws IOException {
        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(is)).withSkipLines(1).build()) {
            StreamSupport.stream(reader.spliterator(), true).forEach(processor);
        }
    }

    private static IMappingFile load(Path path) throws IOException {
        try (InputStream is = Files.newInputStream(path)) {
            return IMappingFile.load(is);
        }
    }

    private static class MCPConfig {

        public String version;
        private Map<String, Object> data;

        @SuppressWarnings ("unchecked")
        public String getData(String... path) {
            if (data == null) { return null; }
            Map<String, Object> level = data;
            for (String part : path) {
                if (!level.containsKey(part)) { return null; }
                Object val = level.get(part);
                if (val instanceof String) { return (String) val; }
                if (val instanceof Map) { level = (Map<String, Object>) val; }
            }
            return null;
        }
    }
}
