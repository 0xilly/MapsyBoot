package net.minecraftforge.mapsy.service.discord.command.admin;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.minecraftforge.mapsy.dao.MinecraftVersion;
import net.minecraftforge.mapsy.repository.mapping.MinecraftVersionRepo;
import net.minecraftforge.mapsy.service.ImportService;
import net.minecraftforge.mapsy.service.discord.DiscordService;
import net.minecraftforge.mapsy.service.discord.command.BaseCommand;
import net.minecraftforge.mapsy.service.discord.command.CommandSource;
import net.minecraftforge.mapsy.util.LoggerCapture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by covers1624 on 11/1/21.
 */
@Component
public class VersionCommands extends BaseCommand {

    private static final Logger logger = LogManager.getLogger();

    private final MinecraftVersionRepo versionRepo;
    private final ImportService importService;

    public VersionCommands(MinecraftVersionRepo versionRepo, ImportService importService) {
        this.versionRepo = versionRepo;
        this.importService = importService;
    }

    @Autowired
    public void register(DiscordService discord) {
        discord.registerCommand(literal("version")
                .then(literal("list")
                        .executes(ctx -> listVersions(ctx.getSource()))
                )
                .then(literal("latest")
                        .executes(ctx -> getLatest(ctx.getSource()))
                )
                .then(literal("promote")
                        .then(argument("version", StringArgumentType.string())
                                .executes(ctx -> promoteLatest(ctx.getSource(), ctx.getArgument("version", String.class)))
                        )
                )
                .then(literal("import")
                        .executes(ctx -> importVersion(ctx.getSource(), null))
                        .then(argument("version", StringArgumentType.string())
                                .executes(ctx -> importVersion(ctx.getSource(), ctx.getArgument("version", String.class)))
                        )
                )
                .then(literal("import-snapshot")
                        .executes(ctx -> importSnapshot(ctx.getSource(), null))
                        .then(argument("version", StringArgumentType.string())
                                .executes(ctx -> importSnapshot(ctx.getSource(), ctx.getArgument("version", String.class)))
                        )
                )
        );
    }

    protected int listVersions(CommandSource src) {
        Map<String, Integer> versionCounts = new HashMap<>();
        for (MinecraftVersion v : versionRepo.findAll()) {
            versionCounts.compute(v.getName(), (e, c) -> c == null ? 1 : c + 1);
        }

        MessageBuilder builder = new MessageBuilder();
        builder.append("Versions available: ");
        builder.appendCodeBlock(
                versionCounts.entrySet().stream()
                        .sorted(Comparator.comparing((Function<Map.Entry<String, Integer>, DefaultArtifactVersion>) e -> new DefaultArtifactVersion(e.getKey())).reversed())
                        .map(e -> e.getKey() + " | Revisions: " + e.getValue())
                        .collect(Collectors.joining("\n")), null);
        src.getChannel().sendMessage(builder.build()).complete();
        return 0;
    }

    protected int getLatest(CommandSource src) {
        Optional<MinecraftVersion> latestOpt = versionRepo.findByLatestIsTrue();
        if (latestOpt.isEmpty()) {
            src.getChannel().sendMessage("Latest not set.").complete();
            return 0;
        }
        MinecraftVersion latest = latestOpt.get();
        Optional<MinecraftVersion> headOpt = versionRepo.findLatestRevisionOf(latest.getName());

        if (headOpt.isEmpty() || latest.getRevision() == headOpt.get().getRevision()) {
            src.getChannel().sendMessageFormat("Latest: `%s` Revision `%s`", latest.getName(), latest.getRevision()).complete();
            return 0;
        }
        int diff = headOpt.get().getRevision() - latest.getRevision();
        if (diff < 0) {
            src.getChannel().sendMessage("Revision diff is negative, report this to an admin.").complete();
            return 0;
        }

        src.getChannel().sendMessageFormat("Latest: `%s-%s`, which is behind by `%s` revisions.", latest.getName(), latest.getRevision(), diff).complete();
        return 0;
    }

    protected int promoteLatest(CommandSource src, String vStr) {
        Optional<MinecraftVersion> versionOpt = versionRepo.findByVersionRevisionPair(parseVersionRevisionPair(vStr));
        if (versionOpt.isEmpty()) {
            src.getChannel().sendMessage("Version not found.").complete();
            return 0;
        }
        MinecraftVersion version = versionOpt.get();

        Optional<MinecraftVersion> latestOpt = versionRepo.findByLatestIsTrue();
        MinecraftVersion latest = latestOpt.orElse(null);

        if (latest != null && version.getRevision() == latest.getRevision()) {
            src.getChannel().sendMessage("Latest is already at the specified version.").complete();
            return 0;
        }

        String latestStr = "none";
        if (latest != null) {
            latest.setLatest(false);
            versionRepo.save(latest);
            latestStr = String.format("`%s-%s`", latest.getName(), latest.getRevision());
        }
        version.setLatest(true);
        versionRepo.save(version);
        src.getChannel().sendMessageFormat("Promoted `%s-%s` to latest from %s", version.getName(), version.getRevision(), latestStr).complete();

        return 0;
    }

    protected int importVersion(CommandSource src, String from) {
        List<Message.Attachment> attachments = src.getAttachments();
        if (attachments.isEmpty()) {
            src.getChannel().sendMessage("Expected attachments.").complete();
            return -1;
        }
        if (attachments.size() != 1) {
            src.getChannel().sendMessage("Expected one attachment. Got: " + attachments.size()).complete();
            return -1;
        }
        Message.Attachment zipAttachment = attachments.get(0);

        Optional<MinecraftVersion> formFrom = versionRepo.findByVersionRevisionPair(parseVersionRevisionPair(from));

        zipAttachment.retrieveInputStream()
                .thenAcceptAsync(is -> {
                    String str = formFrom.map(e -> String.format("`%s-%s`", e.getName(), e.getRevision())).orElse("none");
                    src.getChannel().sendMessage("Importing MCPConfig zip. Forking from version: " + str).complete();
                    ByteArrayOutputStream bOut = new ByteArrayOutputStream();
                    ImportService.ImportReport report;
                    MessageBuilder builder = new MessageBuilder();
                    try (LoggerCapture capture = new LoggerCapture(bOut)) {
                        capture.attachRoot();
                        report = importService.importMCPConfig(is, formFrom);
                        String fork = report.forkedFrom == null ? "null" : String.format("`%s-%s`", report.forkedFrom.getName(), report.forkedFrom.getRevision());
                        builder.appendFormat("Imported, `%s-%s` forked from %s", report.version.getName(), report.version.getRevision(), fork);
                    }
                    if (report.failure) {
                        builder.append("Import may have failed, see log for details.");
                    }
                    src.getChannel()
                            .sendMessage(builder.build())
                            .addFile(bOut.toByteArray(), "import.log")
                            .complete();
                }, ImportService.EXECUTOR);
        return 0;
    }

    protected int importSnapshot(CommandSource src, String from) {
        List<Message.Attachment> attachments = src.getAttachments();
        if (attachments.isEmpty()) {
            src.getChannel().sendMessage("Expected attachments.").complete();
            return -1;
        }
        if (attachments.size() != 1) {
            src.getChannel().sendMessage("Expected one attachment. Got: " + attachments.size()).complete();
            return -1;
        }
        Message.Attachment zipAttachment = attachments.get(0);

        Optional<MinecraftVersion> versionOpt = versionRepo.findByVersionRevisionPair(parseVersionRevisionPair(from));
        if (versionOpt.isEmpty()) {
            src.getChannel().sendMessage("Version not found.").complete();
            return -1;
        }
        MinecraftVersion version = versionOpt.get();

        zipAttachment.retrieveInputStream()
                .thenAcceptAsync(is -> {
                    src.getChannel().sendMessageFormat("Importing MCPSnapshot zip into: `%s-%s`", version.getName(), version.getRevision()).complete();
                    ByteArrayOutputStream bOut = new ByteArrayOutputStream();
                    try (LoggerCapture capture = new LoggerCapture(bOut)) {
                        capture.attachRoot();
                        importService.importMCPSnapshot(is, version);
                    }
                    src.getChannel()
                            .sendMessage("Imported.")
                            .addFile(bOut.toByteArray(), "import.log")
                            .complete();
                }, ImportService.EXECUTOR);
        return 0;
    }

}
