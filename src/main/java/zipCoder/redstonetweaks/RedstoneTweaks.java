/*
 * @file ModRedstonePen.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 */
package zipCoder.redstonetweaks;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import org.slf4j.Logger;
import wile.redstonepen.ModContent;
import wile.redstonepen.blocks.circuitComponents.ControlBox;
import wile.redstonepen.blocks.RedstoneTrack;
import wile.redstonepen.libmc.Networking;
import wile.redstonepen.libmc.Overlay;
import wile.redstonepen.libmc.Registries;

import static zipCoder.redstonetweaks.RedstoneTweaks.MODID;


@Mod(MODID)
public class RedstoneTweaks {

    public static final String MODID = "redstonepen";
    public static final Logger MOD_LOGGER = com.mojang.logging.LogUtils.getLogger();

    public RedstoneTweaks() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        Registries.init();
        ModContent.init();
        bus.addListener(LiveCycleEvents::onSetup);
        bus.addListener(LiveCycleEvents::onRegister);
        // Register the item to a creative tab
        bus.addListener(this::addCreative);
    }
    // -------------------------------------------------------------------------------------------------------------------
    // Events
    // -------------------------------------------------------------------------------------------------------------------

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            Registries.getRegisteredItems().forEach(
                    it -> {
                        if (!(it instanceof BlockItem bit) || (bit.getBlock() != ModContent.references.TRACK_BLOCK))
                            event.accept(it);
                    });
        }
    }

    private static class LiveCycleEvents {
        private static void onSetup(final FMLCommonSetupEvent event) {
            Networking.init(MODID);
        }

        private static void onRegister(RegisterEvent event) {
            final String registry_name = Registries.instantiate(event.getForgeRegistry());
            if (!registry_name.isEmpty()) ModContent.initReferences(registry_name);
        }

    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        @SuppressWarnings({"unchecked"})
        public static void onClientSetup(final FMLClientSetupEvent event) {
            Networking.OverlayTextMessage.setHandler(Overlay.TextOverlayGui::show);
            Overlay.TextOverlayGui.on_config(0.75, 0x00ffaa00, 0x55333333, 0x55333333, 0x55444444);
            BlockEntityRenderers.register((BlockEntityType<RedstoneTrack.TrackBlockEntity>) Registries.getBlockEntityTypeOfBlock("track"), wile.redstonepen.client.ModRenderers.TrackTer::new);
            onRegisterMenuScreens(event);
        }

        @SuppressWarnings({"unchecked"})
        public static void onRegisterMenuScreens(final FMLClientSetupEvent event) {
            MenuScreens.register((MenuType<ControlBox.ControlBoxUiContainer>) Registries.getMenuTypeOfBlock("control_box"), ControlBox.ControlBoxGui::new);
        }

        @SubscribeEvent
        public static void onRegisterModels(final ModelEvent.RegisterAdditional event) {
            wile.redstonepen.client.ModRenderers.TrackTer.registerModels().forEach(event::register);
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientGameEvents {
        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        public static void onRenderGui(net.minecraftforge.client.event.RenderGuiOverlayEvent.Post event) {
            Overlay.TextOverlayGui.INSTANCE.onRenderGui(event.getGuiGraphics());
        }

        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        public static void onRenderWorldOverlay(RenderLevelStageEvent event) {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS) {
                Overlay.TextOverlayGui.INSTANCE.onRenderWorldOverlay(event.getPoseStack(), event.getPartialTick());
            }
        }
    }

}
