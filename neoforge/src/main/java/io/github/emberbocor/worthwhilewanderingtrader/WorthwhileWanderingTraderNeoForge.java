package io.github.emberbocor.worthwhilewanderingtrader;

import io.github.emberbocor.worthwhilewanderingtrader.config.ServerConfig;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

@Mod(WorthwhileWanderingTrader.MODID)
public class WorthwhileWanderingTraderNeoForge {
    public WorthwhileWanderingTraderNeoForge() {
        NeoForge.EVENT_BUS.addListener((ServerAboutToStartEvent event) -> ServerConfig.load(event.getServer()));
    }
}
