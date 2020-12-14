package net.minecraftforge.mapsy.service.discord;

import net.minecraftforge.mapsy.configuration.DiscordConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class DiscordService {

    @Autowired
    private DiscordConfiguration discordConfiguration;

    @PostConstruct
    private void registerListeners() {
       var api = discordConfiguration.getApi();
       api.addListener(new CommandHandler());
    }

}
