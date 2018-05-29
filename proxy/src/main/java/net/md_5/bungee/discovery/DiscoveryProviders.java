package net.md_5.bungee.discovery;

import net.md_5.bungee.BungeeCord;

public class DiscoveryProviders {
    private BungeeCord bungeeCord;
    private DiscoveryProvider provider;

    public DiscoveryProviders(BungeeCord bungeeCord) {

        this.bungeeCord = bungeeCord;
    }

    public void start(DiscoveryListener listener) {
        if (bungeeCord.getConfig().isUseKubernetesDiscovery()) {
            provider = new KubernetesDiscoveryProvider(bungeeCord.getLogger());
        }
        else {
            provider = new NoopDiscoveryProvider();
        }
        provider.start(listener);
    }
}
