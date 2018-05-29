package net.md_5.bungee.discovery;

public class NoopDiscoveryProvider implements DiscoveryProvider {

    @Override
    public void start(DiscoveryListener listener) {
        // also does nothing
    }
}
