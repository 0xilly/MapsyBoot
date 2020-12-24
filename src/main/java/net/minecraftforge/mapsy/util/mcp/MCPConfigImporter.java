package net.minecraftforge.mapsy.util.mcp;

import com.google.common.base.Objects;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import net.minecraftforge.mapsy.dao.*;
import net.minecraftforge.mapsy.service.VersionCacheService;
import net.minecraftforge.mapsy.util.MappingSide;
import net.minecraftforge.srgutils.IMappingFile;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by covers1624 on 24/12/20.
 */
public class MCPConfigImporter {

    private static final Logger logger = LogManager.getLogger();
    private static final Pattern srgIdRegex = Pattern.compile("_(\\d*)_");

    private final MCPConfig config;
    private final MinecraftVersion mcVersion;
    private final VersionCacheService cacheService;
    private final Map<String, ClassName> existingClasses;
    private final Map<String, FieldName> existingFields;
    private final Map<FieldName, List<FieldChange>> existingFieldChanges;
    private final Map<String, MethodName> existingMethods;
    private final Map<MethodName, List<MethodChange>> existingMethodChanges;
    private final Map<String, ParameterName> existingParameters;
    private final Map<ParameterName, List<ParameterChange>> existingParameterChanges;

    private final Map<String, ClassName> classMap = new HashMap<>();
    private final Map<String, FieldName> fieldMap = new HashMap<>();
    private final Map<FieldName, List<FieldChange>> fieldChangeMap = new HashMap<>();
    private final Map<String, MethodName> methodMap = new HashMap<>();
    private final Map<MethodName, List<MethodChange>> methodChangeMap = new HashMap<>();
    private final Map<String, MethodName> constructorMap = new HashMap<>();
    private final Map<String, ParameterName> methodParameterMap = new HashMap<>();
    private final Map<ParameterName, List<ParameterChange>> parameterChangeMap = new HashMap<>();

    private Set<String> validClasses;

    private IMappingFile joinedTSrg;
    private IMappingFile joinedRev;
    private Map<String, String> constructorIds;

    private IMappingFile clientMojang;
    private IMappingFile serverMojang;

    private IMappingFile srgToCMojang;
    private IMappingFile srgToSMojang;

    public MCPConfigImporter(
            MCPConfig config,
            MinecraftVersion mcVersion,
            VersionCacheService cacheService, Map<String, ClassName> existingClasses,
            Map<String, FieldName> existingFields,
            Map<FieldName, List<FieldChange>> existingFieldChanges,
            Map<String, MethodName> existingMethods,
            Map<MethodName, List<MethodChange>> existingMethodChanges,
            Map<String, ParameterName> existingParameters,
            Map<ParameterName, List<ParameterChange>> existingParameterChanges
    ) {
        this.config = config;
        this.mcVersion = mcVersion;
        this.cacheService = cacheService;
        this.existingClasses = existingClasses;
        this.existingFields = existingFields;
        this.existingFieldChanges = existingFieldChanges;
        this.existingMethods = existingMethods;
        this.existingMethodChanges = existingMethodChanges;
        this.existingParameters = existingParameters;
        this.existingParameterChanges = existingParameterChanges;
    }

    public void process() throws IOException {
        logger.info("Parsing inputs..");
        logger.info("> joined.tsrg");
        joinedTSrg = IMappingFile.load(new ByteArrayInputStream(config.zipFiles.get(config.getData("mappings"))));
        joinedRev = joinedTSrg.reverse();
        validClasses = joinedTSrg.getClasses().stream().map(IMappingFile.INode::getMapped).collect(Collectors.toSet());
        logger.info("> constructors.txt");
        constructorIds = parseConstructors(new ByteArrayInputStream(config.zipFiles.get(config.getData("constructors"))));

        logger.info("> client_mappings.txt");
        clientMojang = load(cacheService.getClientMappings(config.version));
        logger.info("> server_mappings.txt");
        serverMojang = load(cacheService.getServerMappings(config.version));

        logger.info("> srg -> mojang");
        srgToCMojang = clientMojang.chain(joinedTSrg).reverse();
        srgToSMojang = serverMojang.chain(joinedTSrg).reverse();

        processTSRG();
        processMojangSide(srgToCMojang, MappingSide.CLIENT);
        processMojangSide(srgToSMojang, MappingSide.SERVER);

        logger.info("Generating constructor parameters.");
        //Generate constructor parameters.
        for (MethodName ctorName : constructorMap.values()) {
            String srgId = ctorName.getSrg();
            if (StringUtils.isEmpty(srgId)) { continue; }
            Type[] parameters = Type.getArgumentTypes(ctorName.getSrgDescriptor());
            int idx = 0;
            for (Type parameter : parameters) {
                String ident = String.format("p_i%s_%s_", srgId, idx);
                ParameterName existing = existingParameters.get(ident);
                ParameterName pName;
                if (existing != null) {
                    pName = existing.fork();
                    List<ParameterChange> pChanges = existingParameterChanges.get(existing)
                            .stream()
                            .map(e -> e.fork(pName))
                            .collect(Collectors.toList());
                    parameterChangeMap.put(pName, pChanges);
                } else {
                    pName = new ParameterName();
                }
                pName.setMinecraftVersion(mcVersion);
                pName.setOwner(ctorName);
                pName.setSrg(ident);
                methodParameterMap.put(ident, pName);
                boolean wide = parameter.getSort() == Type.DOUBLE || parameter.getSort() == Type.LONG;
                idx += wide ? 2 : 1;//Account for wide vars
            }
        }
    }

    private void processTSRG() {
        logger.info("Processing TSRG mappings.");
        //First pass, generate all *Name's from the joined.tsrg
        for (IMappingFile.IClass clazz : joinedTSrg.getClasses()) {
            ClassName cName = new ClassName();//Fork not necessary as no complex data.

            cName.setMinecraftVersion(mcVersion);
            cName.setObf(clazz.getOriginal());
            cName.setSrg(clazz.getMapped());

            classMap.put(cName.getSrg(), cName);

            for (IMappingFile.IField field : clazz.getFields()) {
                if (!field.getMapped().startsWith("field_")) { continue; }
                FieldName existing = existingFields.get(field.getMapped());
                FieldName fName;
                if (existing != null) {
                    fName = existing.fork();
                    List<FieldChange> fChanges = existingFieldChanges.get(existing)
                            .stream()
                            .map(e -> e.fork(fName))
                            .collect(Collectors.toList());
                    fieldChangeMap.put(fName, fChanges);
                } else {
                    fName = new FieldName();
                }

                fName.setMinecraftVersion(mcVersion);
                fName.setOwner(cName);
                fName.setObf(field.getOriginal());
                fName.setSrg(field.getMapped());
                fieldMap.put(fName.getSrg(), fName);
            }

            for (IMappingFile.IMethod method : clazz.getMethods()) {
                if (!method.getMapped().startsWith("func_")) { continue; }
                MethodName mName = methodMap.get(method.getMapped());
                if (mName == null) {
                    MethodName existing = existingMethods.get(method.getMapped());
                    if (existing != null) {
                        MethodName newName = existing.fork();
                        List<MethodChange> mChanges = existingMethodChanges.get(existing)
                                .stream()
                                .map(e -> e.fork(newName))
                                .collect(Collectors.toList());
                        methodChangeMap.put(newName, mChanges);
                        mName = newName;
                    } else {
                        mName = new MethodName();
                    }
                    mName.setMinecraftVersion(mcVersion);
                    mName.setObf(method.getOriginal());
                    mName.setObfDescriptor(method.getDescriptor());
                    mName.setSrg(method.getMapped());
                    mName.setSrgDescriptor(method.getMappedDescriptor());
                    methodMap.put(mName.getSrg(), mName);
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
                    String ident = String.format("p_%s_%s_", srgId, idx);
                    ParameterName existing = existingParameters.get(ident);
                    ParameterName pName;
                    if (existing != null) {
                        pName = existing.fork();
                        List<ParameterChange> pChanges = existingParameterChanges.get(existing)
                                .stream()
                                .map(e -> e.fork(pName))
                                .collect(Collectors.toList());
                        parameterChangeMap.put(pName, pChanges);
                    } else {
                        pName = new ParameterName();
                    }
                    pName.setMinecraftVersion(mcVersion);
                    pName.setOwner(mName);
                    pName.setSrg(ident);
                    methodParameterMap.put(ident, pName);
                    boolean wide = parameter.getSort() == Type.DOUBLE || parameter.getSort() == Type.LONG;
                    idx += wide ? 2 : 1;//Account for wide vars
                }
            }
        }
    }

    private void processMojangSide(IMappingFile mappings, MappingSide side) {
        logger.info("Processing Mojang {} mappings.", side);
        for (IMappingFile.IClass clazz : mappings.getClasses()) {
            ClassName cName = classMap.get(clazz.getOriginal());
            if (cName == null) { continue; }
            if (cName.getSide() == side.opposite()) {
                cName.setSide(MappingSide.BOTH);
                if (!cName.getMojang().equals(clazz.getMapped())) {
                    logger.warn("Mojang class name differs from client to server: {}: {}, {}: {}", side, cName.getMojang(), side.opposite(), clazz.getMapped());
                }
            } else {
                cName.setMojang(clazz.getMapped());
                cName.setSide(side);
            }

            for (IMappingFile.IField field : clazz.getFields()) {
                FieldName fName = fieldMap.get(field.getOriginal());
                if (fName == null) { continue; }
                if (fName.getSide() == side.opposite()) {
                    fName.setSide(MappingSide.BOTH);
                    if (!fName.getMojang().equals(field.getMapped())) {
                        String s = clazz.getMapped() + "." + fName.getMojang();
                        String o = clazz.getMapped() + "." + field.getMapped();
                        logger.warn("Mojang field name differs from client to server: {}: {}, {}: {}", side, o, side.opposite(), o);
                    }
                    continue;
                }
                fName.setMojang(field.getMapped());
                fName.setSide(side);
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
                    ctorName.setSide(side);
                    ctorName.addOwner(cName);
                    constructorMap.put(cName.getSrg() + method.getDescriptor(), ctorName);
                    continue;
                }

                MethodName mName = methodMap.get(method.getOriginal());
                if (mName == null) { continue; }
                if (mName.getSide() == side.opposite()) {
                    mName.setSide(MappingSide.BOTH);
                    if (!mName.getMojang().equals(method.getMapped())) {
                        String s = clazz.getMapped() + "." + mName.getMojang();
                        String o = clazz.getMapped() + "." + method.getMapped();
                        logger.warn("Mojang method name differs from client to server: {}: {}, {}: {}", side, s, side.opposite(), o);
                    }
                    continue;
                }
                mName.setMojang(method.getMapped());
                mName.setMojangDescriptor(method.getMappedDescriptor());
                mName.setSide(side);
            }
        }
    }

    //@formatter:off
    public Map<String, ClassName> getClassMap() { return classMap; }
    public Map<String, FieldName> getFieldMap() { return fieldMap; }
    public Map<FieldName, List<FieldChange>> getFieldChangeMap() { return fieldChangeMap; }
    public Map<String, MethodName> getMethodMap() { return methodMap; }
    public Map<MethodName, List<MethodChange>> getMethodChangeMap() { return methodChangeMap; }
    public Map<String, MethodName> getConstructorMap() { return constructorMap; }
    public Map<String, ParameterName> getMethodParameterMap() { return methodParameterMap; }
    //@formatter:on

    private static IMappingFile load(Path path) throws IOException {
        try (InputStream is = Files.newInputStream(path)) {
            return IMappingFile.load(is);
        }
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
}
