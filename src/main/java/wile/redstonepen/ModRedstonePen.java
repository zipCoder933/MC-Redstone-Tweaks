/*
 * @file ModRedstonePen.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 */
package wile.redstonepen;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import wile.redstonepen.blocks.ControlBox;
import wile.redstonepen.blocks.RedstoneTrack;
import wile.redstonepen.libmc.Networking;
import wile.redstonepen.libmc.Overlay;
import wile.redstonepen.libmc.Registries;

import static wile.redstonepen.ModRedstonePen.MODID;


@Mod(MODID)
public class ModRedstonePen {

    public static final String MODID = "redstonepen";
    public static final String MODNAME = "Redstone Pen";
    public static final Logger MOD_LOGGER = com.mojang.logging.LogUtils.getLogger();

    public ModRedstonePen() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        Registries.init();
        ModContent.init();
        bus.addListener(LiveCycleEvents::onSetup);
        bus.addListener(LiveCycleEvents::onRegister);
        CREATIVE_MODE_TABS.register(bus);
    }

    // -------------------------------------------------------------------------------------------------------------------
    // Createive Mode Tab
    // -------------------------------------------------------------------------------------------------------------------

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, MODID);
    public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register(
            "tab_" + MODID, () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.tabredstonepen"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> new ItemStack(Registries.getItem("pen")))
                    .displayItems((parameters, output) -> Registries.getRegisteredItems().forEach(
                            it -> {
                                if (!(it instanceof BlockItem bit) || (bit.getBlock() != ModContent.references.TRACK_BLOCK))
                                    output.accept(it);
                            })
                    ).build()
    );

    // -------------------------------------------------------------------------------------------------------------------
    // Events
    // -------------------------------------------------------------------------------------------------------------------

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
