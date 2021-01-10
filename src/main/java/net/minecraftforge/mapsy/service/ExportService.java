package net.minecraftforge.mapsy.service;

import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import net.minecraftforge.mapsy.dao.FieldName;
import net.minecraftforge.mapsy.dao.MethodName;
import net.minecraftforge.mapsy.dao.MinecraftVersion;
import net.minecraftforge.mapsy.dao.ParameterName;
import net.minecraftforge.mapsy.repository.mapping.FieldNameRepo;
import net.minecraftforge.mapsy.repository.mapping.MethodNameRepo;
import net.minecraftforge.mapsy.repository.mapping.ParameterNameRepo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by covers1624 on 15/12/20.
 */
@Service
public class ExportService {

    private static final Logger logger = LogManager.getLogger();

    private final FieldNameRepo fieldRepo;
    private final MethodNameRepo methodRepo;
    private final ParameterNameRepo parameterRepo;

    public ExportService(FieldNameRepo fieldRepo, MethodNameRepo methodRepo, ParameterNameRepo parameterRepo) {
        this.fieldRepo = fieldRepo;
        this.methodRepo = methodRepo;
        this.parameterRepo = parameterRepo;
    }

    @Transactional (readOnly = true)
    public byte[] exportZip(MinecraftVersion version) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (ZipOutputStream zOut = new ZipOutputStream(bout)) {

            logger.info("Exporting methods..");
            zOut.putNextEntry(new ZipEntry("methods.csv"));
            exportMethods(new CSVWriter(new OutputStreamWriter(zOut)), version);

            logger.info("Exporting fields..");
            zOut.putNextEntry(new ZipEntry("fields.csv"));
            exportFields(new CSVWriter(new OutputStreamWriter(zOut)), version);

            logger.info("Exporting params..");
            zOut.putNextEntry(new ZipEntry("params.csv"));
            exportParameters(new CSVWriter(new OutputStreamWriter(zOut)), version);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create zip export.");
        }
        return bout.toByteArray();
    }

    @Transactional (readOnly = true)
    public void exportMethods(ICSVWriter writer, MinecraftVersion version) {
        String[] line = new String[4];
        writer.writeNext(new String[] { "searge", "name", "side", "desc" }, false);
        try (Stream<MethodName> stream = methodRepo.findAllByMinecraftVersion(version)) {
            stream.forEach(e -> {
                if (e.getMcp() != null) {
                    line[0] = e.getSrg();
                    line[1] = e.getMcp();
                    line[2] = String.valueOf(e.getSide().ordinal());
                    line[3] = e.getDescription();
                    if (line[3] == null) {
                        line[3] = "";
                    }
                    writer.writeNext(line, false);
                }
            });
        }
        writer.flushQuietly();
    }

    @Transactional (readOnly = true)
    public void exportFields(ICSVWriter writer, MinecraftVersion version) {
        String[] line = new String[4];
        writer.writeNext(new String[] { "searge", "name", "side", "desc" }, false);
        try (Stream<FieldName> stream = fieldRepo.findAllByMinecraftVersion(version)) {
            stream.forEach(e -> {
                if (e.getMcp() != null) {
                    line[0] = e.getSrg();
                    line[1] = e.getMcp();
                    line[2] = String.valueOf(e.getSide().ordinal());
                    line[3] = e.getDescription();
                    if (line[3] == null) {
                        line[3] = "";
                    }
                    writer.writeNext(line, false);
                }
            });
        }
        writer.flushQuietly();
    }

    @Transactional (readOnly = true)
    public void exportParameters(ICSVWriter writer, MinecraftVersion version) {
        String[] line = new String[4];
        writer.writeNext(new String[] { "param", "name", "side" }, false);
        try (Stream<ParameterName> stream = parameterRepo.findAllByMinecraftVersion(version)) {
            stream.forEach(e -> {
                if (e.getMcp() != null) {
                    line[0] = e.getSrg();
                    line[1] = e.getMcp();
                    line[2] = String.valueOf(e.getOwner().getSide().ordinal());
                    writer.writeNext(line, false);
                }
            });
        }
        writer.flushQuietly();
    }
}
