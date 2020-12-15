package net.minecraftforge.mapsy.service.mapping;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraftforge.mapsy.dao.ClassName;
import net.minecraftforge.mapsy.dao.FieldName;
import net.minecraftforge.mapsy.dao.MethodName;
import net.minecraftforge.mapsy.dao.MinecraftVersion;
import net.minecraftforge.mapsy.repository.mapping.ClassNameRepo;
import net.minecraftforge.mapsy.repository.mapping.FieldNameRepo;
import net.minecraftforge.mapsy.repository.mapping.MethodNameRepo;
import net.minecraftforge.mapsy.repository.mapping.MinecraftVersionRepo;
import net.minecraftforge.mapsy.service.VersionCacheService;
import net.minecraftforge.srgutils.IMappingFile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static net.minecraftforge.mapsy.util.MappingSide.*;

@Service
public class UpdateService {

    private ClassNameRepo classNameRepo;
    private MethodNameRepo methodNameRepo;
    private FieldNameRepo fieldNameRepo;
    private MinecraftVersionRepo versionRepo;
    private VersionCacheService cacheService;

    private final ThreadPoolExecutor exec = (ThreadPoolExecutor) Executors.newFixedThreadPool(5, new ThreadFactoryBuilder().setNameFormat("mapsy update service").build());

    public UpdateService(ClassNameRepo classNameRepo, MethodNameRepo methodNameRepo, FieldNameRepo fieldNameRepo, MinecraftVersionRepo versionRepo, VersionCacheService cacheService) {
        this.classNameRepo = classNameRepo;
        this.methodNameRepo = methodNameRepo;
        this.fieldNameRepo = fieldNameRepo;
        this.versionRepo = versionRepo;
        this.cacheService = cacheService;
    }

    public void updateMappings(InputStream mcpconfig) throws IOException {
        MCPConfigInfo mcpConfigInfo = getMCPConfig(mcpconfig);
        Path client = cacheService.getClientMappings(mcpConfigInfo.getVersion());
        Path server = cacheService.getServerMappings(mcpConfigInfo.getVersion());
        IMappingFile clientMappings = IMappingFile.load(client.toFile());
        IMappingFile serverMappings = IMappingFile.load(server.toFile());
        IMappingFile mcpConfigMappings = mcpConfigInfo.getTsrg();
//        insertVersion(mcpConfigInfo.getVersion());
        MappingsMeta mappingsMeta = createMappingMeta(mcpConfigInfo.getVersion(), clientMappings, serverMappings, mcpConfigMappings);
//        insertClassesIntoDB(mappingsMeta);
        insertFieldIntoDb(mappingsMeta);
//        insertMethodsIntoDb(mappingsMeta);
    }


    private void insertVersion(String version) {
        versionRepo.save(new MinecraftVersion(version));
    }


    private void insertClassesIntoDB(MappingsMeta meta) {
        var classMap = meta.getClassMap();
        Set<ClassName> classes = new HashSet<>(classMap.values());
        classNameRepo.saveAll(classes);
    }

    private void insertMethodsIntoDb(MappingsMeta meta) {
        var methodMap = meta.getMethodMap();
        methodMap.forEach(((s, method) -> {
            method.getOwners().stream().map(own -> classNameRepo.findBySrgAndMinecraftVersion(own.getSrg(), meta.getMinecraftVersion()).get()).forEach(method::addOwner);
            methodNameRepo.save(method);
        }));
    }

    private void insertFieldIntoDb(MappingsMeta meta) {
        var fieldMap = meta.getFieldMap();
        fieldMap.forEach((s, field) -> {
            var ownerClass = classNameRepo.findBySrgAndMinecraftVersion(field.getOwner().getSrg(), meta.getMinecraftVersion());
            field.setOwner(ownerClass.get());
            fieldNameRepo.save(field);
        });
    }

    //TODO: look for alternatives there's probably a better way to do this but I want to get this doen by friday so hack now optimize later
    private MappingsMeta createMappingMeta(String version, IMappingFile mojangclient, IMappingFile mojangserver, IMappingFile joinedMappings) {
        MinecraftVersion minecraftVersion = versionRepo.findByName(version).get();
        HashMap<String, ClassName> classMap = new HashMap<>();
        HashMap<String, MethodName> methodMap = new HashMap<>();
        HashMap<String, FieldName> fieldMap = new HashMap<>();

        // Load the base info into memory
        joinedMappings.getClasses().forEach(klass-> {
            var clName = new ClassName();

            clName.setMinecraftVersion(minecraftVersion);
            clName.setObf(klass.getOriginal());
            clName.setSrg(klass.getMapped());

            classMap.put(klass.getOriginal() ,clName);

            klass.getFields().forEach(field-> {
                var feName = new FieldName();

                feName.setObf(field.getOriginal());
                feName.setSrg(field.getMapped());
                feName.setOwner(clName);
                feName.setMinecraftVersion(minecraftVersion);

                fieldMap.put(clName.getObf() + "." + field.getOriginal(), feName);
            });

            klass.getMethods().forEach(method-> {
                var meName = new MethodName();

                meName.setObf(method.getOriginal());
                meName.setSrg(method.getMapped());
                meName.addOwner(clName);
                meName.setDescription(method.getDescriptor());
                meName.setMinecraftVersion(minecraftVersion);

                methodMap.put(clName.getObf() + "." + method.getOriginal() + "." + method.getDescriptor(), meName);
            });
        });

        //NOTE original and mapped are swapped for mojang names
        // Second pass to insert client mojang info the naming
        mojangclient.getClasses().forEach(klass -> {
            var clName = classMap.get(klass.getMapped()); // Call mapped because that is the name is the original!

            if (clName != null) {
                clName.setMojang(klass.getOriginal());
                clName.setSide(CLIENT);

                klass.getFields().forEach(field -> {
                    var fieldName = fieldMap.get(clName.getObf() +"."+field.getMapped());

                    if (fieldName != null) {
                        fieldName.setMojang(field.getOriginal());
                        fieldName.setSide(CLIENT);

                    } else {
                        //TODO write an auditor for these
                    }
                });

                klass.getMethods().forEach(method -> {
                    var methodName = methodMap.get(clName.getObf() + "."+ method.getMapped() + "." + method.getDescriptor());

                    if (methodName != null) {
                        methodName.setMojang(method.getOriginal());
                        methodName.setSide(CLIENT);

                    } else {
//                        System.out.println("Mapping not found for " + method.getMapped() + " Named: " + method.getOriginal());
                    }
                });
            } else {
                if (!klass.getOriginal().contains("package-info")) {
                    //TODO write auditer
                }

//                System.out.println("Mapping not found for " + klass.getMapped() + " Named: " + klass.getOriginal());
            }
        });

        //NOTE original and mapped are swapped for mojang names
        //Third pass to insert server mojang info the naming and to check if class exists on both sides
        mojangserver.getClasses().forEach(klass -> {
            var clName = classMap.get(klass.getMapped()); // Call mapped because that is the name is the original!

            if (clName != null) {
                clName.setMojang(klass.getOriginal());

                if (clName.getSide() == CLIENT) {
                    clName.setSide(BOTH);

                } else {
                    clName.setSide(SERVER);
                }

                klass.getFields().forEach(field -> {
                    var fieldName = fieldMap.get(clName.getObf() + "." + field.getMapped());

                    if (fieldName != null) {
                        fieldName.setMojang(field.getOriginal());

                        if (fieldName.getSide() == CLIENT) {
                            fieldName.setSide(BOTH);

                        } else {
                            fieldName.setSide(SERVER);
                        }
                    } else {
                        //TODO write an auditor for these
                    }
                });

                klass.getMethods().forEach(method -> {
                    var methodName = methodMap.get(clName.getObf() + "."+ method.getMapped() + "." + method.getDescriptor());

                    if (methodName != null) {
                        methodName.setMojang(method.getOriginal());

                        if (methodName.getSide() == CLIENT) {
                            methodName.setSide(BOTH);
                        } else {
                            methodName.setSide(SERVER);
                        }
                    } else {
                        //TODO write an auditor for these
                    }
                });
            } else {
                if (!klass.getOriginal().contains("package-info")) {
                    //TODO write auditor
                }
            }
        });

        return new MappingsMeta(classMap, methodMap, fieldMap, minecraftVersion);
    }

    public MCPConfigInfo getMCPConfig(InputStream inputStream) throws IOException {
        var zin = new ZipInputStream(inputStream);
        ZipEntry entry;

        IMappingFile mappingFile = null;
        String json = null;

        while ((entry = zin.getNextEntry()) != null) {
           if (entry.getName().equals("config.json")) {
               json = CharStreams.toString(new InputStreamReader(zin, Charsets.UTF_8));
           }
           if (entry.getName().equals("config/joined.tsrg")) {
               mappingFile = IMappingFile.load(zin);
           }
           if (json != null && mappingFile != null) {
               break;
           }
        }
        var jObj = new Gson().fromJson(json, JsonObject.class);
        var version = jObj.get("version").getAsString();
        zin.close();
        return new MCPConfigInfo(mappingFile, version);

    }

    public final static class MCPConfigInfo {
        private final IMappingFile tsrg;
        private final String version;

        public MCPConfigInfo(IMappingFile tsrg, String version) {
            this.tsrg = tsrg;
            this.version = version;
        }

        //@formatter:off
        public IMappingFile getTsrg() { return tsrg; }
        public String getVersion() { return version; }
        //@formatter:on
    }

    public final static class MappingsMeta {
        private HashMap<String, ClassName> classMap;
        private HashMap<String, MethodName> methodMap;
        private HashMap<String, FieldName> fieldMap;
        private MinecraftVersion minecraftVersion;

        public MappingsMeta(HashMap<String, ClassName> classMap, HashMap<String, MethodName> methodMap, HashMap<String, FieldName> fieldMap, MinecraftVersion minecraftVersion) {
            this.classMap = classMap;
            this.methodMap = methodMap;
            this.fieldMap = fieldMap;
            this.minecraftVersion = minecraftVersion;
        }

        //@formatter:off
        public HashMap<String, ClassName> getClassMap() { return classMap; }
        public HashMap<String, MethodName> getMethodMap() { return methodMap; }
        public HashMap<String, FieldName> getFieldMap() { return fieldMap; }
        public MinecraftVersion getMinecraftVersion() { return minecraftVersion; }
        //@formatter:on
    }
}
