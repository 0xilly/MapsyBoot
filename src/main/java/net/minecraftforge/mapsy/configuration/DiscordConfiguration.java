package net.minecraftforge.mapsy.configuration;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.security.auth.login.LoginException;

@Configuration
public class DiscordConfiguration {

    @Value("${discord.token}")
    private String token;

    @Value("${discord.cmdop}")
    private String cmdOp;

    private JDA jda;

    @PostConstruct
    public void initBot() {
        try {
            jda = JDABuilder.createDefault(token).build();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    public void registerListener(Object object) {
       jda.addEventListener(object);
    }

    public void start() {
        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        jda.shutdown();
    }

    public void emergencyStop() {
        jda.shutdownNow();
    }

    public void shutdown() {
        jda.shutdownNow();
        System.exit(2);
    }

    public void restart() {
        stop();
        start();
    }
    public JDA getJda() {
        return this.jda;
    }

    public String getCmdOp() {
        return cmdOp;
    }
}
