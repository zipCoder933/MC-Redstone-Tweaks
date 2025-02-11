/*
 * @file Auxiliaries.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * General commonly used functionality.
 */
package wile.redstonepen.libmc;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static zipCoder.redstonetweaks.RedstoneTweaks.MODID;


public class Utils {

  public interface IExperimentalFeature {}

  public static boolean isDevelopmentMode()
  { return SharedConstants.IS_RUNNING_IN_IDE; }

  @OnlyIn(Dist.CLIENT)
  @SuppressWarnings("all")
  public static boolean isShiftDown()
  {
    return (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) ||
            InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT));
  }

  @OnlyIn(Dist.CLIENT)
  @SuppressWarnings("all")
  public static boolean isCtrlDown()
  {
    return (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL) ||
            InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_RIGHT_CONTROL));
  }

  @OnlyIn(Dist.CLIENT)
  public static Optional<String> getClipboard()
  { return Optional.of(net.minecraft.client.gui.font.TextFieldHelper.getClipboardContents(Minecraft.getInstance())); }

  @OnlyIn(Dist.CLIENT)
  public static boolean setClipboard(String text)
  { net.minecraft.client.gui.font.TextFieldHelper.setClipboardContents(Minecraft.getInstance(), text); return true; }



  // -------------------------------------------------------------------------------------------------------------------
  // Localization, text formatting
  // -------------------------------------------------------------------------------------------------------------------

  /**
   * Text localization wrapper, implicitly prepends `MODID` to the
   * translation keys. Forces formatting argument, nullable if no special formatting shall be applied..
   */
  public static MutableComponent localizable(String modtrkey, Object... args)
  { return Component.translatable((modtrkey.startsWith("block.") || (modtrkey.startsWith("item."))) ? (modtrkey) : (MODID+"."+modtrkey), args); }


  public static Component localizable(String modtrkey)
  { return localizable(modtrkey, new Object[]{}); }

  @OnlyIn(Dist.CLIENT)
  public static String localize(String translationKey, Object... args)
  {
    Component tr = Component.translatable(translationKey, args);
    tr.getStyle().applyFormat(ChatFormatting.RESET);
    return tr.getString();
  }

  /**
   * Returns true if a given key is translated for the current language.
   */
  @OnlyIn(Dist.CLIENT)
  public static boolean hasTranslation(String key)
  { return net.minecraft.client.resources.language.I18n.exists(key); }

  @OnlyIn(Dist.CLIENT)
  public static List<Component> wrapText(Component text, int max_width_percent)
  {
    int max_width = ((Minecraft.getInstance().getWindow().getGuiScaledWidth())-10) * max_width_percent/100;
    return Minecraft.getInstance().font.getSplitter().splitLines(text, max_width, Style.EMPTY).stream().map(ft->Component.literal(ft.getString())).collect(Collectors.toList());
  }

  public static boolean isEmpty(Component component)
  { return component.getSiblings().isEmpty() && component.getString().isEmpty(); }

  public static final class Tooltip
  {
    @OnlyIn(Dist.CLIENT)
    public static boolean extendedTipCondition()
    { return isShiftDown(); }

    @OnlyIn(Dist.CLIENT)
    public static boolean helpCondition()
    { return isShiftDown() && isCtrlDown(); }

    /**
     * Adds an extended tooltip or help tooltip depending on the key states of CTRL and SHIFT.
     * Returns true if the localisable help/tip was added, false if not (either not CTL/SHIFT or
     * no translation found).
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean addInformation(@Nullable String advancedTooltipTranslationKey, @Nullable String helpTranslationKey, List<Component> tooltip, TooltipFlag flag, boolean addAdvancedTooltipHints)
    {
      // Note: intentionally not using keybinding here, this must be `control` or `shift`.
      final boolean help_available = (helpTranslationKey != null) && Utils.hasTranslation(helpTranslationKey + ".help");
      final boolean tip_available = (advancedTooltipTranslationKey != null) && Utils.hasTranslation(helpTranslationKey + ".tip");
      if((!help_available) && (!tip_available)) return false;
      String tip_text = "";
      if(helpCondition()) {
        if(help_available) tip_text = localize(helpTranslationKey + ".help");
      } else if(extendedTipCondition()) {
        if(tip_available) tip_text = localize(advancedTooltipTranslationKey + ".tip");
      } else if(addAdvancedTooltipHints) {
        if(tip_available) tip_text += localize(MODID + ".tooltip.hint.extended") + (help_available ? " " : "");
        if(help_available) tip_text += localize(MODID + ".tooltip.hint.help");
      }
      if(tip_text.isEmpty()) return false;
      String[] tip_list = tip_text.split("\\r?\\n");
      for(String tip:tip_list) {
        tooltip.add(Component.literal(tip.replaceAll("\\s+$","").replaceAll("^\\s+", "")).withStyle(ChatFormatting.GRAY));
      }
      return true;
    }

    /**
     * Adds an extended tooltip or help tooltip for a given stack depending on the key states of CTRL and SHIFT.
     * Format in the lang file is (e.g. for items): "item.MODID.REGISTRYNAME.tip" and "item.MODID.REGISTRYNAME.help".
     * Return value see method pattern above.
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean addInformation(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag flag, boolean addAdvancedTooltipHints)
    { return addInformation(stack.getDescriptionId(), stack.getDescriptionId(), tooltip, flag, addAdvancedTooltipHints); }

    @OnlyIn(Dist.CLIENT)
    public static boolean addInformation(String translation_key, List<Component> tooltip)
    {
      if(!Utils.hasTranslation(translation_key)) return false;
      tooltip.add(Component.literal(localize(translation_key).replaceAll("\\s+$","").replaceAll("^\\s+", "")).withStyle(ChatFormatting.GRAY));
      return true;
    }

  }

  @SuppressWarnings("unused")
  public static void playerChatMessage(final Player player, final String message)
  { player.displayClientMessage(Component.translatable(message.trim()), true); }

  public static @Nullable Component unserializeTextComponent(String serialized)
  { return Component.Serializer.fromJson(serialized); }

  public static String serializeTextComponent(Component tc)
  { return (tc==null) ? ("") : (Component.Serializer.toJson(tc)); }

  // -------------------------------------------------------------------------------------------------------------------
  // Tag Handling
  // -------------------------------------------------------------------------------------------------------------------

  public static ResourceLocation getResourceLocation(Item item)
  { return ForgeRegistries.ITEMS.getKey(item); }

  public static ResourceLocation getResourceLocation(Block block)
  { return ForgeRegistries.BLOCKS.getKey(block); }

  // -------------------------------------------------------------------------------------------------------------------
  // Item NBT data
  // -------------------------------------------------------------------------------------------------------------------

  /**
   * Equivalent to getDisplayName(), returns null if no custom name is set.
   */
  public static @Nullable Component getItemLabel(ItemStack stack)
  {
    CompoundTag nbt = stack.getTagElement("display");
    if(nbt != null && nbt.contains("Name", 8)) {
      try {
        Component tc = unserializeTextComponent(nbt.getString("Name"));
        if(tc != null) return tc;
        nbt.remove("Name");
      } catch(Exception e) {
        nbt.remove("Name");
      }
    }
    return null;
  }

  // -------------------------------------------------------------------------------------------------------------------
  // Block handling
  // -------------------------------------------------------------------------------------------------------------------

  public static boolean isWaterLogged(BlockState state)
  { return state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED); }

  public static AABB getPixeledAABB(double x0, double y0, double z0, double x1, double y1, double z1)
  { return new AABB(x0/16.0, y0/16.0, z0/16.0, x1/16.0, y1/16.0, z1/16.0); }

  public static AABB getRotatedAABB(AABB bb, Direction new_facing)
  { return getRotatedAABB(bb, new_facing, false); }

  public static AABB[] getRotatedAABB(AABB[] bb, Direction new_facing)
  { return getRotatedAABB(bb, new_facing, false); }

  public static AABB getRotatedAABB(AABB bb, Direction new_facing, boolean horizontal_rotation)
  {
    if(!horizontal_rotation) {
      switch(new_facing.get3DDataValue()) {
        case 0: return new AABB(1-bb.maxX,   bb.minZ,   bb.minY, 1-bb.minX,   bb.maxZ,   bb.maxY); // D
        case 1: return new AABB(1-bb.maxX, 1-bb.maxZ, 1-bb.maxY, 1-bb.minX, 1-bb.minZ, 1-bb.minY); // U
        case 2: return new AABB(  bb.minX,   bb.minY,   bb.minZ,   bb.maxX,   bb.maxY,   bb.maxZ); // N --> bb
        case 3: return new AABB(1-bb.maxX,   bb.minY, 1-bb.maxZ, 1-bb.minX,   bb.maxY, 1-bb.minZ); // S
        case 4: return new AABB(  bb.minZ,   bb.minY, 1-bb.maxX,   bb.maxZ,   bb.maxY, 1-bb.minX); // W
        case 5: return new AABB(1-bb.maxZ,   bb.minY,   bb.minX, 1-bb.minZ,   bb.maxY,   bb.maxX); // E
      }
    } else {
      switch(new_facing.get3DDataValue()) {
        case 0: return new AABB(  bb.minX, bb.minY,   bb.minZ,   bb.maxX, bb.maxY,   bb.maxZ); // D --> bb
        case 1: return new AABB(  bb.minX, bb.minY,   bb.minZ,   bb.maxX, bb.maxY,   bb.maxZ); // U --> bb
        case 2: return new AABB(  bb.minX, bb.minY,   bb.minZ,   bb.maxX, bb.maxY,   bb.maxZ); // N --> bb
        case 3: return new AABB(1-bb.maxX, bb.minY, 1-bb.maxZ, 1-bb.minX, bb.maxY, 1-bb.minZ); // S
        case 4: return new AABB(  bb.minZ, bb.minY, 1-bb.maxX,   bb.maxZ, bb.maxY, 1-bb.minX); // W
        case 5: return new AABB(1-bb.maxZ, bb.minY,   bb.minX, 1-bb.minZ, bb.maxY,   bb.maxX); // E
      }
    }
    return bb;
  }

  public static AABB[] getRotatedAABB(AABB[] bbs, Direction new_facing, boolean horizontal_rotation)
  {
    final AABB[] transformed = new AABB[bbs.length];
    for(int i=0; i<bbs.length; ++i) transformed[i] = getRotatedAABB(bbs[i], new_facing, horizontal_rotation);
    return transformed;
  }

  public static AABB getYRotatedAABB(AABB bb, int clockwise_90deg_steps)
  {
    final Direction[] direction_map = new Direction[]{Direction.NORTH,Direction.EAST,Direction.SOUTH,Direction.WEST};
    return getRotatedAABB(bb, direction_map[(clockwise_90deg_steps+4096) & 0x03], true);
  }

  public static AABB[] getYRotatedAABB(AABB[] bbs, int clockwise_90deg_steps)
  {
    final AABB[] transformed = new AABB[bbs.length];
    for(int i=0; i<bbs.length; ++i) transformed[i] = getYRotatedAABB(bbs[i], clockwise_90deg_steps);
    return transformed;
  }

  public static AABB getMirroredAABB(AABB bb, Direction.Axis axis)
  {
    return switch (axis) {
      case X -> new AABB(1 - bb.maxX, bb.minY, bb.minZ, 1 - bb.minX, bb.maxY, bb.maxZ);
      case Y -> new AABB(bb.minX, 1 - bb.maxY, bb.minZ, bb.maxX, 1 - bb.minY, bb.maxZ);
      case Z -> new AABB(bb.minX, bb.minY, 1 - bb.maxZ, bb.maxX, bb.maxY, 1 - bb.minZ);
    };
  }

  public static AABB[] getMirroredAABB(AABB[] bbs, Direction.Axis axis)
  {
    final AABB[] transformed = new AABB[bbs.length];
    for(int i=0; i<bbs.length; ++i) transformed[i] = getMirroredAABB(bbs[i], axis);
    return transformed;
  }

  public static VoxelShape getUnionShape(AABB ... aabbs)
  {
    VoxelShape shape = Shapes.empty();
    for(AABB aabb: aabbs) shape = Shapes.joinUnoptimized(shape, Shapes.create(aabb), BooleanOp.OR);
    return shape;
  }

  public static VoxelShape getUnionShape(AABB[] ... aabb_list)
  {
    VoxelShape shape = Shapes.empty();
    for(AABB[] aabbs:aabb_list) {
      for(AABB aabb: aabbs) shape = Shapes.joinUnoptimized(shape, Shapes.create(aabb), BooleanOp.OR);
    }
    return shape;
  }

  public static AABB[] getMappedAABB(AABB[] bbs, Function<AABB,AABB> mapper) {
    final AABB[] transformed = new AABB[bbs.length];
    for(int i=0; i<bbs.length; ++i) transformed[i] = mapper.apply(bbs[i]);
    return transformed;
  }
}
