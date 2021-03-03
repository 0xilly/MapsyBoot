package net.minecraftforge.mapsy.controller;

import net.minecraftforge.mapsy.repository.mapping.FieldNameRepo;
import net.minecraftforge.mapsy.repository.mapping.MethodNameRepo;
import net.minecraftforge.mapsy.repository.mapping.MinecraftVersionRepo;
import net.minecraftforge.mapsy.service.ExportService;
import net.minecraftforge.mapsy.service.ImportService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Created by covers1624 on 18/12/20.
 */
@Controller
public class TestController {

    private final ImportService importService;
    private final ExportService exportService;
    private final MinecraftVersionRepo versionRepo;
    private final MethodNameRepo methodNameRepo;
    private final FieldNameRepo fieldNameRepo;

    public TestController(ImportService importService, ExportService exportService, MinecraftVersionRepo versionRepo, MethodNameRepo methodNameRepo, FieldNameRepo fieldNameRepo) {
        this.importService = importService;
        this.exportService = exportService;
        this.versionRepo = versionRepo;
        this.methodNameRepo = methodNameRepo;
        this.fieldNameRepo = fieldNameRepo;
    }

    @ResponseBody
    @PostMapping (value = "/import_mcp_config")
    public String uploadMapping(InputStream input) throws IOException {
        importService.importMCPConfig(input, Optional.empty());
        return "bees";
    }

    @ResponseBody
    @PostMapping (value = "/import_mcp_snapshot")
    public String uploadSnapshot(InputStream input) throws IOException {
        importService.importMCPSnapshot(input, versionRepo.findLatestRevisionOf("1.16.4").get());
        return "bees";
    }

    @ResponseBody
    @Transactional (readOnly = true)
    @RequestMapping(value = "/export_snapshot", produces = "application/zip")
    public byte[] exportSnapshot() {
        return exportService.exportZip(versionRepo.findLatestRevisionOf("1.16.4").get());
    }

}
