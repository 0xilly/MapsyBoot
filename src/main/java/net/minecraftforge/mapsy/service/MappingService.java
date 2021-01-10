package net.minecraftforge.mapsy.service;

import net.minecraftforge.mapsy.dao.FieldChange;
import net.minecraftforge.mapsy.dao.MethodChange;
import net.minecraftforge.mapsy.dao.MinecraftVersion;
import net.minecraftforge.mapsy.dao.UserDAO;
import net.minecraftforge.mapsy.repository.mapping.*;
import org.springframework.stereotype.Service;

@Service
public class MappingService {

    private MinecraftVersionRepo minecraftVersionRepo;
    private FieldNameRepo fieldNameRepo;
    private FieldChangeRepo fieldChangeRepo;
    private MethodNameRepo methodNameRepo;
    private MethodChangeRepo methodChangeRepo;

    public MappingService(MinecraftVersionRepo minecraftVersion, FieldNameRepo fieldNameRepo, FieldChangeRepo fieldChangeRepo, MethodNameRepo methodNameRepo, MethodChangeRepo methodChangeRepo) {
        this.minecraftVersionRepo = minecraftVersion;
        this.fieldNameRepo = fieldNameRepo;
        this.fieldChangeRepo = fieldChangeRepo;
        this.methodNameRepo = methodNameRepo;
        this.methodChangeRepo = methodChangeRepo;
    }


    private MinecraftVersion getLatestVersion() {
        MinecraftVersion version = null;
        var versionList = minecraftVersionRepo.findAll();
        for (var v: versionList) {
            if (v.isLatest()) {
                version = v;
            }
        }
        return version;
    }

    public UpdateResult updateFieldMapping(String obfName, String targetName, String description, UserDAO user, boolean overrideMojang) {
        var field = fieldNameRepo.findBySrgAndMinecraftVersion(obfName, getLatestVersion());

        if (field.isPresent()) {
            if (field.get().isLocked()) {
                return new UpdateResult(Result.FAILURE, "This is a locked field unable to rename field");
            }

            if (!field.get().getMojang().equals(targetName)) {
                FieldChange fieldChange = new FieldChange();
                fieldChange.setField(field.get()); // set the field that changed

                if (field.get().getMcp() == null) {
                    fieldChange.setOldName(field.get().getSrg());
                } else {
                    fieldChange.setOldName(field.get().getMcp());
                }

                field.get().setMcp(targetName);
                fieldChange.setNewName(targetName);
                fieldChange.setUser(user);

                if (description != null) { // check for description
                    field.get().setDescription(description);
                }
                fieldNameRepo.save(field.get());
                fieldChangeRepo.save(fieldChange);
                return new UpdateResult(Result.SUCCESS, "Updated mappings");
            } else {
                return new UpdateResult(Result.FAILURE, "Official name conflict please use another name");
            }

        } else {
            return new UpdateResult(Result.FAILURE, "No field found in the database");
        }

    }

    public UpdateResult updateMethodMappings(String obfName, String targetName, String description, UserDAO user, boolean overrideMojang) {

        var method = methodNameRepo.findBySrgAndMinecraftVersion(obfName, getLatestVersion());

        if (method.isPresent()) {
            if (method.get().isLocked()) {
                return new UpdateResult(Result.FAILURE, "This is a locked method unable to rename field");
            }

            if (!method.get().getMojang().equals(targetName)) {
                MethodChange methodChange = new MethodChange();
                methodChange.setMethod(method.get()); // set the method that changed

                if (method.get().getMcp() == null) {
                    methodChange.setOldName(method.get().getSrg());
                } else {
                    methodChange.setOldName(method.get().getMcp());
                }

                method.get().setMcp(targetName);
                methodChange.setNewName(targetName);
                methodChange.setUser(user);

                if (description != null) { // check for description
                    method.get().setDescription(description);
                }
                methodNameRepo.save(method.get());
                methodChangeRepo.save(methodChange);
                return new UpdateResult(Result.SUCCESS, "Updated mappings");
            } else {
                return new UpdateResult(Result.FAILURE, "Official name conflict please use another name");
            }

        } else {
            return new UpdateResult(Result.FAILURE, "No field found in the database");
        }
    }

    public static class UpdateResult {
       private final Result result;
       private final String cause;

        public UpdateResult(Result result, String cause) {
            this.result = result;
            this.cause = cause;
        }

        public Result getResult() { return result; }
        public String getCause() { return cause; }
    }

    public static enum Result {
        SUCCESS,
        FAILURE
    }

}
