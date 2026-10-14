package io.github.emberbocor.worthwhilewanderingtrader;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(WorthwhileWanderingTrader.MODID)
public class WorthwhileWanderingTrader {
    public static final String MODID = "worthwhilewanderingtrader";
    public static final Logger LOGGER = LogUtils.getLogger();

    public WorthwhileWanderingTrader(IEventBus modEventBus, ModContainer modContainer) {
    }
}
