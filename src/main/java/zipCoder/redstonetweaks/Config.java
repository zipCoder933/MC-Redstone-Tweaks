package zipCoder.redstonetweaks;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import static zipCoder.redstonetweaks.RedstoneTweaks.MODID;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue CIRCUIT_COMP_OVERLAY =
            BUILDER.comment("If there should be an overlay when holding circuit components")
                    .define("client.circuitCompOverlay", false);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    /**
     * Variables
     */
    public static boolean circuitCompOverlay;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        circuitCompOverlay = CIRCUIT_COMP_OVERLAY.get();
    }
}
