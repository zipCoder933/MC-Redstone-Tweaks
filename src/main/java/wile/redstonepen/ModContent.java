/*
 * @file ModContent.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 */
package wile.redstonepen;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import wile.redstonepen.blocks.*;
import wile.redstonepen.blocks.circuitComponents.CircuitComponents;
import wile.redstonepen.blocks.circuitComponents.ControlBox;
import wile.redstonepen.blocks.circuitComponents.relay.*;
import wile.redstonepen.items.RedstonePenItem;
import wile.redstonepen.libmc.Utils;
import wile.redstonepen.libmc.Registries;
import wile.redstonepen.libmc.StandardBlocks;


public class ModContent {
    public static void init() {
        initBlocks();
        initItems();
        Registries.addRecipeSerializer("crafting_extended_shapeless", () -> wile.redstonepen.libmc.ExtendedShapelessRecipe.SERIALIZER);
    }

    public static void initBlocks() {
        /**
         * Control box and redstone track are the ONLY blocks that are tile entities
         */

        Registries.addBlock("track", () -> new RedstoneTrack.RedstoneTrackBlock(StandardBlocks.CFG_DEFAULT, BlockBehaviour.Properties.of().noCollission().instabreak().dynamicShape().randomTicks()), RedstoneTrack.TrackBlockEntity::new);

        Registries.addBlock("relay", () -> new RelayBlock(StandardBlocks.CFG_CUTOUT, BlockBehaviour.Properties.of().noCollission().instabreak(), Utils.getPixeledAABB(5, 0, 0, 11, 1, 16)), CircuitComponents.DirectedComponentBlockItem::new);
        Registries.addBlock("inverted_relay", () -> new InvertedRelayBlock(StandardBlocks.CFG_CUTOUT, BlockBehaviour.Properties.of().noCollission().instabreak(), Utils.getPixeledAABB(5, 0, 0, 11, 1, 16)), CircuitComponents.DirectedComponentBlockItem::new);
        Registries.addBlock("bistable_relay", () -> new BistableRelayBlock(StandardBlocks.CFG_CUTOUT, BlockBehaviour.Properties.of().noCollission().instabreak(), Utils.getPixeledAABB(5, 0, 0, 11, 1, 16)), CircuitComponents.DirectedComponentBlockItem::new);
        Registries.addBlock("pulse_relay", () -> new PulseRelayBlock(StandardBlocks.CFG_CUTOUT, BlockBehaviour.Properties.of().noCollission().instabreak(), Utils.getPixeledAABB(5, 0, 0, 11, 1, 16)), CircuitComponents.DirectedComponentBlockItem::new);
        Registries.addBlock("bridge_relay", () -> new BridgeRelayBlock(StandardBlocks.CFG_CUTOUT, BlockBehaviour.Properties.of().noCollission().instabreak(), Utils.getPixeledAABB(5, 0, 0, 11, 1, 16)), CircuitComponents.DirectedComponentBlockItem::new);
        Registries.addBlock("control_box", () -> new ControlBox.ControlBoxBlock(StandardBlocks.CFG_CUTOUT, BlockBehaviour.Properties.of().noCollission().instabreak(), new AABB[]{Utils.getPixeledAABB(0, 0, 0, 16, 2, 16), Utils.getPixeledAABB(3, 1, 3, 13, 3.9, 13)}), CircuitComponents.DirectedComponentBlockItem::new, ControlBox.ControlBoxBlockEntity::new, ControlBox.ControlBoxUiContainer::new);

        Registries.addBlock("basic_lever", () -> new BasicLever.BasicLeverBlock(new BasicLever.BasicLeverBlock.Config(0.8f, 0.9f), BlockBehaviour.Properties.of().noCollission().isValidSpawn((s, w, p, b) -> false).strength(0.3f).sound(SoundType.METAL).pushReaction(PushReaction.DESTROY)));

        Registries.addBlock("basic_button", () -> new BasicButton.BasicButtonBlock(new BasicButton.BasicButtonBlock.Config(0.8f, 0.9f, 20), BlockBehaviour.Properties.of().noCollission().isValidSpawn((s, w, p, b) -> false).strength(0.3f).sound(SoundType.METAL).pushReaction(PushReaction.DESTROY)));
        Registries.addBlock("basic_pulse_button", () -> new BasicButton.BasicButtonBlock(new BasicButton.BasicButtonBlock.Config(0.8f, 0.9f, 2), BlockBehaviour.Properties.of().noCollission().isValidSpawn((s, w, p, b) -> false).strength(0.3f).sound(SoundType.METAL).pushReaction(PushReaction.DESTROY)));
    }

    public static void initItems() {
        Registries.addItem("quill", () -> new RedstonePenItem((new Item.Properties()).rarity(Rarity.UNCOMMON).stacksTo(64).defaultDurability(0)));
        Registries.addItem("pen", () -> new RedstonePenItem((new Item.Properties()).rarity(Rarity.UNCOMMON).stacksTo(1).defaultDurability(256)));
    }

    public static void initReferences(String registry_name) {
        switch (registry_name) {
            case "minecraft:block" -> {
                references.TRACK_BLOCK = (RedstoneTrack.RedstoneTrackBlock) Registries.getBlock("track");
                references.BRIDGE_RELAY_BLOCK = (BridgeRelayBlock) Registries.getBlock("bridge_relay");
                references.CONTROLBOX_BLOCK = (ControlBox.ControlBoxBlock) Registries.getBlock("control_box");
            }
        }
    }

    //--------------------------------------------------------------------------------------------------------------------
    // Accessors
    //--------------------------------------------------------------------------------------------------------------------

    public static MenuType<?> getMenuTypeOfBlock(String block_name) {
        return Registries.getMenuTypeOfBlock(block_name);
    }

    public static MenuType<?> getMenuTypeOfBlock(Block block) {
        return Registries.getMenuTypeOfBlock(block);
    }

    public static BlockEntityType<?> getBlockEntityTypeOfBlock(String block_name) {
        return Registries.getBlockEntityTypeOfBlock(block_name);
    }

    public static BlockEntityType<?> getBlockEntityTypeOfBlock(Block block) {
        return Registries.getBlockEntityTypeOfBlock(block);
    }

    public static final class references {
        public static RedstoneTrack.RedstoneTrackBlock TRACK_BLOCK = null;
        public static BridgeRelayBlock BRIDGE_RELAY_BLOCK = null;
        public static ControlBox.ControlBoxBlock CONTROLBOX_BLOCK = null;
    }

}
