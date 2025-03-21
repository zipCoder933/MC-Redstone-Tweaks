/*
 * @file Guis.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Gui Wrappers and Widgets.
 */
package wile.redstonepen.libmc;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import wile.redstonepen.client.TooltipDisplay;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static zipCoder.redstonetweaks.RedstoneTweaks.MODID;

@SuppressWarnings("deprecation")
public class Guis
{
  // -------------------------------------------------------------------------------------------------------------------
  // Gui base
  // -------------------------------------------------------------------------------------------------------------------

  @OnlyIn(Dist.CLIENT)
  public static abstract class ContainerGui<T extends AbstractContainerMenu> extends AbstractContainerScreen<T>
  {
    protected final ResourceLocation background_image_;
    protected final Player player_;
    protected final BackgroundImage gui_background_;
    protected final TooltipDisplay tooltip_ = new TooltipDisplay();

    public ContainerGui(T menu, Inventory player_inv, Component title, String background_image, int width, int height)
    {
      super(menu, player_inv, title);
      this.background_image_ = new ResourceLocation(MODID, background_image);
      this.player_ = player_inv.player;
      this.imageWidth = width;
      this.imageHeight = height;
      gui_background_ = new BackgroundImage(background_image_, width, height, Coord2d.ORIGIN);
    }

    public ContainerGui(T menu, Inventory player_inv, Component title, String background_image)
    {
      super(menu, player_inv, title);
      this.background_image_ = new ResourceLocation(MODID, background_image);
      this.player_ = player_inv.player;
      gui_background_ = new BackgroundImage(background_image_, imageWidth, imageHeight, Coord2d.ORIGIN);
    }

    @Override
    public void init()
    {
      super.init();
      gui_background_.init(this, Coord2d.ORIGIN).show();
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTicks)
    {
      renderBackground(gg);
      super.render(gg, mouseX, mouseY, partialTicks);
      if(!tooltip_.render(gg, this, mouseX, mouseY)) renderTooltip(gg, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics gg, int x, int y)
    {}

    @Override
    protected final void renderBg(GuiGraphics gg, float partialTicks, int mouseX, int mouseY)
    {
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.enableDepthTest();
      gui_background_.draw(gg, this);
      renderBgWidgets(gg, partialTicks, mouseX, mouseY);
      RenderSystem.disableBlend();
    }

    public final ResourceLocation getBackgroundImage()
    { return background_image_; }

    protected void renderBgWidgets(GuiGraphics gg, float partialTicks, int mouseX, int mouseY)
    {}

    protected void renderItemTemplate(GuiGraphics gg, ItemStack stack, int x, int y)
    {
      gg.renderItem(stack, x, y);
    }
  }

  // -------------------------------------------------------------------------------------------------------------------
  // Gui elements
  // -------------------------------------------------------------------------------------------------------------------

  @OnlyIn(Dist.CLIENT)
  public static class Coord2d
  {
    public static final Coord2d ORIGIN = new Coord2d(0,0);
    public final int x, y;
    public Coord2d(int x, int y) { this.x=x; this.y=y; }
    public static Coord2d of(int x, int y) { return new Coord2d(x,y); }
    public String toString() { return "["+x+","+y+"]"; }
  }

  @OnlyIn(Dist.CLIENT)
  public static class UiWidget extends net.minecraft.client.gui.components.AbstractWidget
  {
    protected static final Component EMPTY_TEXT = Component.literal("");
    protected static final Function<UiWidget,Component> NO_TOOLTIP = (uiw)->EMPTY_TEXT;

    private final Minecraft mc_;
    private Function<UiWidget,Component> tooltip_ = NO_TOOLTIP;
    private Screen parent_;

    public UiWidget(int x, int y, int width, int height, Component title)
    { super(x, y, width, height, title); mc_ = Minecraft.getInstance(); }

    public UiWidget init(Screen parent)
    {
      this.parent_ = parent;
      setX(getX() + ((parent instanceof AbstractContainerScreen<?>) ? ((AbstractContainerScreen<?>)parent).getGuiLeft() : 0));
      setY(getY() + ((parent instanceof AbstractContainerScreen<?>) ? ((AbstractContainerScreen<?>)parent).getGuiTop() : 0));
      return this;
    }

    public UiWidget init(Screen parent, Coord2d position)
    {
      this.parent_ = parent;
      setX(position.x + ((parent instanceof AbstractContainerScreen<?>) ? ((AbstractContainerScreen<?>)parent).getGuiLeft() : 0));
      setY(position.y + ((parent instanceof AbstractContainerScreen<?>) ? ((AbstractContainerScreen<?>)parent).getGuiTop() : 0));
      return this;
    }

    public final UiWidget tooltip(Function<UiWidget,Component> tip)
    { tooltip_ = tip; return this; }

    public final UiWidget tooltip(Component tip)
    { tooltip_ = (o)->tip; return this; }

    public final int getWidth()
    { return this.width; }

    public final int getHeight()
    { return this.height; }

    public Coord2d getMousePosition()
    {
      final Window win = mc_.getWindow();
      return Coord2d.of(
        Mth.clamp(((int)(mc_.mouseHandler.xpos() * (double)win.getGuiScaledWidth() / (double)win.getScreenWidth()))-getX(), -1, this.width+1),
        Mth.clamp(((int)(mc_.mouseHandler.ypos() * (double)win.getGuiScaledHeight() / (double)win.getScreenHeight()))-getY(), -1, this.height+1)
      );
    }

    protected final Coord2d screenCoordinates(Coord2d xy, boolean reverse)
    { return (reverse) ? (Coord2d.of(xy.x+getX(), xy.y+getY())) : (Coord2d.of(xy.x-getX(), xy.y-getY())); }

    public UiWidget show()
    { visible = true; return this; }

    public UiWidget hide()
    { visible = false; return this; }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput neo)
    {}

    @Override
    protected void renderWidget(GuiGraphics gg, int mouseX, int mouseY, float partialTicks)
    {
      if(isHovered) renderToolTip(gg, mouseX, mouseY);
    }

    public void renderToolTip(GuiGraphics gg, int mouseX, int mouseY)
    {
      if(!visible || (!active) || (tooltip_ == NO_TOOLTIP)) return;
      final Component tip = tooltip_.apply(this);
      if(tip.getString().trim().isEmpty()) return;
      gg.renderTooltip(mc_.font, List.of(tip.getVisualOrderText()), mouseX, mouseY);
    }
  }

  @OnlyIn(Dist.CLIENT)
  public static class HorizontalProgressBar extends UiWidget
  {
    private final Coord2d texture_position_base_;
    private final Coord2d texture_position_filled_;
    private final ResourceLocation atlas_;
    private double progress_max_ = 100;
    private double progress_ = 0;

    public HorizontalProgressBar(ResourceLocation atlas, int width, int height, Coord2d base_texture_xy, Coord2d filled_texture_xy)
    {
      super(0, 0, width, height, EMPTY_TEXT);
      atlas_ = atlas;
      texture_position_base_ = base_texture_xy;
      texture_position_filled_ = filled_texture_xy;
    }

    public HorizontalProgressBar setProgress(double progress)
    { progress_ = Mth.clamp(progress, 0, progress_max_); return this; }

    public double getProgress()
    { return progress_; }

    public HorizontalProgressBar setMaxProgress(double progress)
    { progress_max_ = Math.max(progress, 0); return this; }

    public double getMaxProgress()
    { return progress_max_; }

    public HorizontalProgressBar show()
    { visible = true; return this; }

    public HorizontalProgressBar hide()
    { visible = false; return this; }

    @Override
    public void playDownSound(SoundManager handler)
    {}

    @Override
    protected void renderWidget(GuiGraphics gg, int mouseX, int mouseY, float partialTicks)
    {
      RenderSystem.setShaderTexture(0, atlas_);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.enableDepthTest();
      gg.blit(atlas_, getX(), getY(), texture_position_base_.x, texture_position_base_.y, width, height);
      if((progress_max_ > 0) && (progress_ > 0)) {
        int w = Mth.clamp((int)Math.round((progress_ * width) / progress_max_), 0, width);
        gg.blit(atlas_, getX(), getY(), texture_position_filled_.x, texture_position_filled_.y, w, height);
      }
      if(isHovered) renderToolTip(gg, mouseX, mouseY);
    }
  }

  @OnlyIn(Dist.CLIENT)
  public static class BackgroundImage extends UiWidget
  {
    private final ResourceLocation atlas_;
    private final Coord2d atlas_position_;
    public boolean visible;

    public BackgroundImage(ResourceLocation atlas, int width, int height, Coord2d atlas_position)
    {
      super(0, 0, width, height, EMPTY_TEXT);
      atlas_ = atlas;
      atlas_position_ = atlas_position;
      this.width = width;
      this.height = height;
      visible = true;
    }

    public void draw(GuiGraphics gg, Screen parent)
    {
      if(!visible) return;
      RenderSystem.setShaderTexture(0, atlas_);
      gg.blit(atlas_, getX(), getY(), atlas_position_.x, atlas_position_.y, width, height);
    }
  }

  @OnlyIn(Dist.CLIENT)
  public static class CheckBox extends UiWidget
  {
    private final Coord2d texture_position_off_;
    private final Coord2d texture_position_on_;
    private final ResourceLocation atlas_;
    private boolean checked_ = false;
    private Consumer<CheckBox> on_click_ = (checkbox)->{};

    public CheckBox(ResourceLocation atlas, int width, int height, Coord2d atlas_texture_position_off, Coord2d atlas_texture_position_on)
    {
      super(0, 0, width, height, EMPTY_TEXT);
      texture_position_off_ = atlas_texture_position_off;
      texture_position_on_ = atlas_texture_position_on;
      atlas_ = atlas;
    }

    public boolean checked()
    { return checked_; }

    public CheckBox checked(boolean on)
    { checked_ = on; return this; }

    public CheckBox onclick(Consumer<CheckBox> action)
    { on_click_ = action; return this; }

    @Override
    public void onClick(double mouseX, double mouseY)
    { checked_ = !checked_; on_click_.accept(this); }

    @Override
    protected void renderWidget(GuiGraphics gg, int mouseX, int mouseY, float partialTicks)
    {
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderTexture(0, atlas_);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.enableDepthTest();
      Coord2d pos = checked_ ? texture_position_on_ : texture_position_off_;
      gg.blit(atlas_, getX(), getY(), pos.x, pos.y, width, height);
      if(isHovered) renderToolTip(gg, mouseX, mouseY);
    }
  }

  @OnlyIn(Dist.CLIENT)
  public static class ImageButton extends UiWidget
  {
    private final Coord2d texture_position_;
    private final ResourceLocation atlas_;
    private Consumer<ImageButton> on_click_ = (bt)->{};

    public ImageButton(ResourceLocation atlas, int width, int height, Coord2d atlas_texture_position)
    {
      super(0, 0, width, height, Component.empty());
      texture_position_ = atlas_texture_position;
      atlas_ = atlas;
    }

    public ImageButton onclick(Consumer<ImageButton> action)
    { on_click_ = action; return this; }

    @Override
    public void onClick(double mouseX, double mouseY)
    { on_click_.accept(this); }

    @Override
    protected void renderWidget(GuiGraphics gg, int mouseX, int mouseY, float partialTicks)
    {
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderTexture(0, atlas_);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.enableDepthTest();
      Coord2d pos = texture_position_;
      gg.blit(atlas_, getX(), getY(), pos.x, pos.y, width, height);
      if(isHovered) renderToolTip(gg, mouseX, mouseY);
    }
  }

  @OnlyIn(Dist.CLIENT)
  public static class Image extends UiWidget
  {
    private final Coord2d texture_position_;
    private final ResourceLocation atlas_;

    public Image(ResourceLocation atlas, int width, int height, Coord2d atlas_texture_position)
    {
      super(0, 0, width, height, Component.empty());
      texture_position_ = atlas_texture_position;
      atlas_ = atlas;
    }

    @Override
    public void onClick(double mouseX, double mouseY)
    {}

    @Override
    protected void renderWidget(GuiGraphics gg, int mouseX, int mouseY, float partialTicks)
    {
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderTexture(0, atlas_);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.enableDepthTest();
      Coord2d pos = texture_position_;
      gg.blit(atlas_, getX(), getY(), pos.x, pos.y, width, height);
      if(isHovered) renderToolTip(gg, mouseX, mouseY);
    }
  }

  @OnlyIn(Dist.CLIENT)
  public static class TextBox extends net.minecraft.client.gui.components.EditBox
  {
    public TextBox(int x, int y, int width, int height, Component title, Font font) { super(font, x, y, width, height, title); setBordered(false); }
    public TextBox withMaxLength(int len) { super.setMaxLength(len); return this; }
    public TextBox withBordered(boolean b) { super.setBordered(b); return this; }
    public TextBox withValue(String s) { super.setValue(s); return this; }
    public TextBox withEditable(boolean e) { super.setEditable(e); return this; }
    public TextBox withResponder(Consumer<String> r) { super.setResponder(r); return this; }
  }
}
