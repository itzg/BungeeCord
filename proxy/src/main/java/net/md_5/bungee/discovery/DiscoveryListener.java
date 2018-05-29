package net.md_5.bungee.discovery;

import net.md_5.bungee.api.config.ServerInfo;

public interface DiscoveryListener {
    void addOrModify(String key, ServerInfo serverInfo, String forcedHost);
    void remove(String key, ServerInfo serverInfo, String forcedHost);
}
