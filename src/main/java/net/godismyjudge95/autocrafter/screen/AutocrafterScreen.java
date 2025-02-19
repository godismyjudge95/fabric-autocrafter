package net.godismyjudge95.autocrafter.screen;

import com.mojang.blaze3d.systems.RenderSystem;

import net.godismyjudge95.autocrafter.AutocrafterData;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class AutocrafterScreen extends HandledScreen<AutocrafterScreenHandler> implements RecipeBookProvider {
    private static final Identifier TEXTURE = new Identifier("autocrafter", "textures/gui/container/autocrafter.png");
    private static final Identifier RECIPE_BUTTON_TEXTURE = new Identifier("textures/gui/recipe_button.png");
    private final RecipeBookWidget recipeBook = new RecipeBookWidget();
    private boolean narrow;

    public AutocrafterScreen(AutocrafterScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        backgroundHeight = 221;
        backgroundWidth = 176;
    }

    @Override
    protected void init() {
        super.init();
        this.narrow = this.width < 379;
        this.recipeBook.initialize(this.width, this.height, this.client, this.narrow,
                this.handler);
        this.x = this.recipeBook.findLeftEdge(this.width, this.backgroundWidth);
        this.addDrawableChild(new TexturedButtonWidget(this.x + 5, this.height / 2 - 49, 20, 18, 0, 0, 19,
                RECIPE_BUTTON_TEXTURE, button -> {
                    this.recipeBook.toggleOpen();
                    this.x = this.recipeBook.findLeftEdge(this.width, this.backgroundWidth);
                    button.setPos(this.x + 5, this.height / 2 - 49);
                }));
        this.addSelectableChild(this.recipeBook);
        this.setInitialFocus(this.recipeBook);
    }

    @Override
    public void handledScreenTick() {
        super.handledScreenTick();
        this.recipeBook.update();
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = this.x;
        int y = (this.height - this.backgroundHeight) / 2;
        drawTexture(matrices, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        renderProgressArrow(matrices, x, y);
    }

    private void renderProgressArrow(MatrixStack matrices, int x, int y) {
        if (handler.isCrafting()) {
            drawTexture(
                    matrices,
                    x + AutocrafterData.arrowCoord.x(),
                    y + AutocrafterData.arrowCoord.y(),
                    AutocrafterData.arrowActiveCoord.x(),
                    AutocrafterData.arrowActiveCoord.y(),
                    handler.getScaledProgress(),
                    AutocrafterData.arrowSize.y());

        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        if (this.recipeBook.isOpen() && this.narrow) {
            this.drawBackground(matrices, delta, mouseX, mouseY);
            this.recipeBook.render(matrices, mouseX, mouseY, delta);
        } else {
            this.recipeBook.render(matrices, mouseX, mouseY, delta);
            super.render(matrices, mouseX, mouseY, delta);
            this.recipeBook.drawGhostSlots(matrices, this.x, this.y, true, delta);
        }

        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
        this.recipeBook.drawTooltip(matrices, this.x, this.y, mouseX, mouseY);
    }

    //

    @Override
    protected boolean isPointWithinBounds(int x, int y, int width, int height, double pointX, double pointY) {
        return (!this.narrow || !this.recipeBook.isOpen())
                && super.isPointWithinBounds(x, y, width, height, pointX, pointY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.recipeBook.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(this.recipeBook);
            return true;
        }

        if (this.narrow && this.recipeBook.isOpen()) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        boolean bl = mouseX < (double) left || mouseY < (double) top || mouseX >= (double) (left + this.backgroundWidth)
                || mouseY >= (double) (top + this.backgroundHeight);
        return this.recipeBook.isClickOutsideBounds(mouseX, mouseY, this.x, this.y, this.backgroundWidth,
                this.backgroundHeight, button) && bl;
    }

    @Override
    protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
        super.onMouseClick(slot, slotId, button, actionType);

        this.recipeBook.slotClicked(slot);
    }

    @Override
    public void refreshRecipeBook() {
        this.recipeBook.refresh();
    }

    @Override
    public RecipeBookWidget getRecipeBookWidget() {
        return this.recipeBook;
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        int color = 0x404040;
        float yOffset = textRenderer.fontHeight;


        textRenderer.draw(matrices,
                Text.translatable("container.autocrafter.supplies"),
                AutocrafterData.supplySlotsCoord.x() - 1,
                AutocrafterData.supplySlotsCoord.y() - 2 - yOffset,
                color);

        Text title = Text.of("AUTOCRAFTER");
        textRenderer.draw(matrices,
                title,
                AutocrafterData.supplySlotsCoord.x() - 1 + (AutocrafterData.slotSize * 9) - textRenderer.getWidth(title),
                AutocrafterData.supplySlotsCoord.y() - 2 - yOffset,
                color);

        Text craftingText = Text.translatable("container.autocrafter.crafting");
        float craftingBoxXOffset = AutocrafterData.recipeSlotsCoord.x() - 1;
        float craftingBoxWidth = AutocrafterData.slotSize * 3;
        textRenderer.draw(
                matrices,
                craftingText,
                craftingBoxXOffset + ((craftingBoxWidth - textRenderer.getWidth(craftingText)) / 2),
                AutocrafterData.recipeSlotsCoord.y() - 2 - yOffset,
                color);

        textRenderer.draw(
                matrices,
                Text.translatable("container.autocrafter.byproducts"),
                AutocrafterData.byproductSlotsCoord.x() - 1,
                AutocrafterData.byproductSlotsCoord.y() - 2 - yOffset,
                color);
    }
}
