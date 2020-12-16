package net.minecraftforge.mapsy.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
@ConfigurationProperties (prefix = "discord")
public class DiscordConfiguration {

    private String token;

    private Character cmdOp;

    private Set<Long> forcedAdmins;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Character getCmdOp() {
        return cmdOp;
    }

    public void setCmdOp(Character cmdOp) {
        this.cmdOp = cmdOp;
    }

    public Set<Long> getForcedAdmins() {
        return forcedAdmins;
    }

    public void setForcedAdmins(Set<Long> forcedAdmins) {
        this.forcedAdmins = forcedAdmins;
    }
}
