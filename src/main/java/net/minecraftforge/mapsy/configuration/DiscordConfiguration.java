package net.minecraftforge.mapsy.configuration;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class DiscordConfiguration {

    @Value("${discord.token}")
    private String token;

    private DiscordApi discord;

    @PostConstruct
    public void initBot() {
        discord = new DiscordApiBuilder().setToken(token).login().join();
    }

    public DiscordApi getApi() {
        return discord;
    }
}
