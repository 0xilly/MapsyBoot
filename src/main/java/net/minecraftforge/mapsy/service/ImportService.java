package net.minecraftforge.mapsy.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import net.minecraftforge.mapsy.dao.ClassName;
import net.minecraftforge.mapsy.dao.FieldName;
import net.minecraftforge.mapsy.dao.MethodName;
import net.minecraftforge.mapsy.dao.MinecraftVersion;
import net.minecraftforge.mapsy.repository.mapping.ClassNameRepo;
import net.minecraftforge.mapsy.repository.mapping.FieldNameRepo;
import net.minecraftforge.mapsy.repository.mapping.MethodNameRepo;
import net.minecraftforge.mapsy.repository.mapping.MinecraftVersionRepo;
import net.minecraftforge.mapsy.util.MappingSide;
import net.minecraftforge.mapsy.util.Utils;
import net.minecraftforge.srgutils.IMappingFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import java.util.stream.StreamSupport;

/**
 * Created by covers1624 on 18/12/20.
 */
@Service
public class ImportService {

    private static final Logger logger = LogManager.getLogger();

    private final ClassNameRepo classNameRepo;
    private final MethodNameRepo methodNameRepo;
    private final FieldNameRepo fieldNameRepo;
    private final MinecraftVersionRepo versionRepo;
    private final VersionCacheService cacheService;

    public ImportService(ClassNameRepo classNameRepo, MethodNameRepo methodNameRepo, FieldNameRepo fieldNameRepo, MinecraftVersionRepo versionRepo, VersionCacheService cacheService) {
        this.classNameRepo = classNameRepo;
        this.methodNameRepo = methodNameRepo;
        this.fieldNameRepo = fieldNameRepo;
        this.versionRepo = versionRepo;
        this.cacheService = cacheService;
    }

    public MinecraftVersion importMCPConfig(InputStream mcpConfig, MinecraftVersion forkFrom) throws IOException {
        Map<String, byte[]> files = Utils.loadZip(mcpConfig);
        MCPConfig config = Utils.gson.fromJson(new InputStreamReader(new ByteArrayInputStream(files.get("config.json"))), MCPConfig.class);

        IMappingFile joinedTSrg = IMappingFile.load(new ByteArrayInputStream(files.get(config.getData("mappings"))));
        IMappingFile clientMappings = load(cacheService.getClientMappings(config.version));
        IMappingFile serverMappings = load(cacheService.getServerMappings(config.version));

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
                    mName.setObfDesciptor(method.getDescriptor());
                    mName.setSrg(method.getMapped());
                    mName.setDescriptor(method.getMappedDescriptor());
                    methodMap.put(ident, mName);
                }
                mName.addOwner(cName);
            }
        }

        //Build our SRC -> Official mappings.
        IMappingFile clientLookup = clientMappings.chain(joinedTSrg).reverse();
        IMappingFile serverLookup = serverMappings.chain(joinedTSrg).reverse();

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
                MethodName mName = methodMap.get(method.getOriginal());
                if (mName == null) { continue; }

                mName.setMojang(method.getMapped());
                mName.setSide(MappingSide.CLIENT);
            }
        }

        //Second Mojang loop, assign all mojang names, and set server side flags, joining the flags where possible.
        for (IMappingFile.IClass clazz : serverLookup.getClasses()) {
            ClassName cName = classMap.get(clazz.getOriginal());
            if (cName == null) { continue; }
            if (cName.getSide() == MappingSide.CLIENT) {
                //TODO, check names match.
                cName.setSide(MappingSide.BOTH);
                continue;
            }
            cName.setMojang(clazz.getMapped());
            cName.setSide(MappingSide.SERVER);

            for (IMappingFile.IField field : clazz.getFields()) {
                FieldName fName = fieldMap.get(field.getOriginal());
                if (fName == null) { continue; }
                if (fName.getSide() == MappingSide.CLIENT) {
                    //TODO, check names match.
                    fName.setSide(MappingSide.BOTH);
                    continue;
                }
                fName.setMojang(field.getMapped());
                fName.setSide(MappingSide.SERVER);
            }

            for (IMappingFile.IMethod method : clazz.getMethods()) {
                MethodName mName = methodMap.get(method.getOriginal());
                if (mName == null) { continue; }
                if (mName.getSide() == MappingSide.CLIENT) {
                    //TODO, check names match.
                    mName.setSide(MappingSide.BOTH);
                    continue;
                }
                mName.setMojang(method.getMapped());
                mName.setSide(MappingSide.SERVER);
            }
        }

        logger.info("Inserting {} classes.", classMap.size());
        classNameRepo.saveAll(classMap.values());

        logger.info("Inserting {} fields.", fieldMap.size());
        fieldNameRepo.saveAll(fieldMap.values());

        logger.info("Inserting {} methods.", methodMap.size());
        methodNameRepo.saveAll(methodMap.values());

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
        logger.info("Done.");
    }

    private void parseCSV(InputStream is, Consumer<String[]> processor) throws IOException {
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
