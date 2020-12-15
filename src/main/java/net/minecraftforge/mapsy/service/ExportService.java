package net.minecraftforge.mapsy.service;

import com.opencsv.ICSVWriter;
import net.minecraftforge.mapsy.dao.MinecraftVersion;
import net.minecraftforge.mapsy.repository.mapping.FieldNameRepo;
import net.minecraftforge.mapsy.repository.mapping.MethodNameRepo;
import org.springframework.stereotype.Service;

/**
 * Created by covers1624 on 15/12/20.
 */
@Service
public class ExportService {

    private final FieldNameRepo fieldRepo;
    private final MethodNameRepo methodRepo;

    public ExportService(FieldNameRepo fieldRepo, MethodNameRepo methodRepo) {
        this.fieldRepo = fieldRepo;
        this.methodRepo = methodRepo;
    }

    public void exportMethods(ICSVWriter writer, MinecraftVersion version) {
        String[] line = new String[4];
        writer.writeNext(new String[] { "searge", "name", "side", "desc" });
        methodRepo.getAllByMinecraftVersion(version).forEach(e -> {
            if (e.getMcp() != null) {
                line[0] = e.getSrg();
                line[1] = e.getMcp();
                line[2] = String.valueOf(e.getSide().ordinal());
                line[3] = e.getDescription();
                if (line[3] == null) {
                    line[3] = "";
                }
                writer.writeNext(line);
            }
        });
    }

    public void exportFields(ICSVWriter writer, MinecraftVersion version) {
        String[] line = new String[4];
        writer.writeNext(new String[] { "searge", "name", "side", "desc" });
        fieldRepo.getAllByMinecraftVersion(version).forEach(e -> {
            if (e.getMcp() != null) {
                line[0] = e.getSrg();
                line[1] = e.getMcp();
                line[2] = String.valueOf(e.getSide().ordinal());
                line[3] = e.getDescription();
                if (line[3] == null) {
                    line[3] = "";
                }
                writer.writeNext(line);
            }
        });
    }
}
