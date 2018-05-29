package net.md_5.bungee.discovery;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.api.config.ServerInfo;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KubernetesDiscoveryProvider implements DiscoveryProvider {

    private DiscoveryListener listener;
    private Logger logger;
    private boolean done;

    public KubernetesDiscoveryProvider(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void start(final DiscoveryListener listener) {
        this.listener = listener;

        DefaultKubernetesClient client = new DefaultKubernetesClient();

        client.services().inAnyNamespace()
                .watch(new Watcher<Service>() {
                    @Override
                    public void eventReceived(Action action, Service resource) {
                        Map<String, String> annotations = resource.getMetadata().getAnnotations();
                        if (annotations != null)
                        {
                            String key = annotations.get("org.spigotmc.bungee/name");

                            if (key != null)
                            {
                                ServiceSpec spec = resource.getSpec();
                                String clusterIP = spec.getClusterIP();
                                int port = 25565;
                                List<ServicePort> servicePorts = spec.getPorts();
                                for (ServicePort servicePort : servicePorts) {
                                    Integer containerPort = servicePort.getPort();
                                    if (containerPort != null && containerPort == 25565)
                                    {
                                        if (servicePort.getTargetPort().getIntVal() != null)
                                        {
                                            port = servicePort.getTargetPort().getIntVal();
                                        }
                                    }
                                }

                                String motd = "Welcome";
                                if (annotations.get("org.spigotmc.bungee/motd") != null)
                                {
                                    motd = annotations.get("org.spigotmc.bungee/motd");
                                }
                                boolean restricted = false;
                                if (annotations.get("org.spigotmc.bungee/restricted") != null)
                                {
                                    restricted = Boolean.parseBoolean(annotations.get("org.spigotmc.bungee/restricted"));
                                }

                                final String forcedHost = annotations.get("org.spigotmc.bungee/forcedHost");

                                ServerInfo serverInfo = new BungeeServerInfo(key, new InetSocketAddress(clusterIP, port), motd, restricted);

                                logger.log(Level.FINE, "Discovered service action={0} key={1}, info={2}",
                                        new Object[]{action, key, serverInfo});

                                switch (action)
                                {
                                    case ADDED:
                                    case MODIFIED:
                                        listener.addOrModify(key, serverInfo, forcedHost);
                                        break;

                                    case DELETED:
                                        listener.remove(key, serverInfo, forcedHost);
                                }
                            }
                        }

                    }

                    @Override
                    public void onClose(KubernetesClientException cause) {
                        logger.info("Closing kubernetes service discovery");
                    }
                });
    }

    public void stop() {
        done = true;
    }
}
