package io.github.emberbocor.worthwhilewanderingtrader;

import io.github.emberbocor.worthwhilewanderingtrader.config.ServerConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(WorthwhileWanderingTrader.MODID)
public class WorthwhileWanderingTraderForge {
    public WorthwhileWanderingTraderForge() {
        MinecraftForge.EVENT_BUS.addListener((ServerAboutToStartEvent event) -> ServerConfig.load(event.getServer()));
    }
}
