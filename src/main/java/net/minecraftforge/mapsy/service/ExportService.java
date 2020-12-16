package net.minecraftforge.mapsy.service;

import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import net.minecraftforge.mapsy.dao.MinecraftVersion;
import net.minecraftforge.mapsy.repository.mapping.FieldNameRepo;
import net.minecraftforge.mapsy.repository.mapping.MethodNameRepo;
import net.minecraftforge.mapsy.repository.mapping.ParameterNameRepo;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by covers1624 on 15/12/20.
 */
@Service
public class ExportService {

    private final FieldNameRepo fieldRepo;
    private final MethodNameRepo methodRepo;
    private final ParameterNameRepo parameterRepo;

    public ExportService(FieldNameRepo fieldRepo, MethodNameRepo methodRepo, ParameterNameRepo parameterRepo) {
        this.fieldRepo = fieldRepo;
        this.methodRepo = methodRepo;
        this.parameterRepo = parameterRepo;
    }

    public byte[] exportZip(MinecraftVersion version) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (ZipOutputStream zOut = new ZipOutputStream(bout)) {
            zOut.putNextEntry(new ZipEntry("methods.csv"));
            exportMethods(new CSVWriter(new OutputStreamWriter(zOut)), version);
            zOut.putNextEntry(new ZipEntry("fields.csv"));
            exportFields(new CSVWriter(new OutputStreamWriter(zOut)), version);
            zOut.putNextEntry(new ZipEntry("params.csv"));
            exportParameters(new CSVWriter(new OutputStreamWriter(zOut)), version);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create zip export.");
        }
        return bout.toByteArray();
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

    public void exportParameters(ICSVWriter writer, MinecraftVersion version) {
        String[] line = new String[4];
        writer.writeNext(new String[] { "param", "name", "side" });
        parameterRepo.getAllByMinecraftVersion(version).forEach(e -> {
            if (e.getMcp() != null) {
                line[0] = e.getSrg();
                line[1] = e.getMcp();
                line[2] = String.valueOf(e.getSide().ordinal());
                writer.writeNext(line);
            }
        });
    }
}
