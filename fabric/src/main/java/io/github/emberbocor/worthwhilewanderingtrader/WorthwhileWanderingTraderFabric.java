package io.github.emberbocor.worthwhilewanderingtrader;

import io.github.emberbocor.worthwhilewanderingtrader.config.ServerConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class WorthwhileWanderingTraderFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(ServerConfig::load);
    }
}
