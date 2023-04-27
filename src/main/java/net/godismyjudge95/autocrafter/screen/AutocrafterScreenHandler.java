package net.godismyjudge95.autocrafter.screen;

import java.util.Optional;

import net.godismyjudge95.autocrafter.AutocrafterData;
import net.godismyjudge95.autocrafter.helpers.Range;
import net.godismyjudge95.autocrafter.block.entity.AutocrafterBlockEntity;
import net.godismyjudge95.autocrafter.inventory.AutocrafterInventory;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AutocrafterScreenHandler extends AbstractRecipeScreenHandler<CraftingInventory> {
    protected final AutocrafterInventory inventory;
    protected final PlayerEntity player;
    private final PropertyDelegate propertyDelegate;
    private final ScreenHandlerContext context;

    // Client
    public AutocrafterScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, DefaultedList.ofSize(AutocrafterBlockEntity.INVENTORY_SIZE, ItemStack.EMPTY),
                new ArrayPropertyDelegate(2), ScreenHandlerContext.EMPTY);
    }

    // Server
    public AutocrafterScreenHandler(int syncId, PlayerInventory playerInventory, DefaultedList<ItemStack> inventory,
                                    PropertyDelegate delegate, ScreenHandlerContext context) {
        super(ModScreenHandlers.AUTOCRAFTER_SCREEN_HANDLER, syncId);

        AutocrafterInventory autocrafterInventory = new AutocrafterInventory(inventory, 3, 3);
        checkSize(autocrafterInventory, AutocrafterBlockEntity.INVENTORY_SIZE);
        this.player = playerInventory.player;
        this.context = context;
        this.propertyDelegate = delegate;
        this.inventory = autocrafterInventory;
        this.inventory.onOpen(player);
        this.inventory.setHandler(this);

        addScreenSlots();
        addProperties(propertyDelegate);
    }

    private void addScreenSlots() {
        // Slot 0
        addSlot(new AutocrafterResultSlot(this, player, inventory, AutocrafterData.supplyInvRange, AutocrafterData.outputSlotIndex,
                AutocrafterData.resultSlotCoord.x(), AutocrafterData.resultSlotCoord.y()));

        addRecipeInventorySlots(); // Slots 1-9
        addPlayerInventorySlots(); // Slots 10-36
        addPlayerHotbarSlots(); // Slots 37-45
        addSupplyInventorySlots(); // Slots 46-54
        addByproductInventorySlots(); // Slots 55-63
    }

    private void addRecipeInventorySlots() {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 3; ++col) {
                addSlot(new Slot(inventory, AutocrafterData.recipeInvRange.minInclusive() + col + row * 3, AutocrafterData.recipeSlotsCoord.x() + col * AutocrafterData.slotSize,
                        AutocrafterData.recipeSlotsCoord.y() + row * AutocrafterData.slotSize));
            }
        }
    }

    private void addSupplyInventorySlots() {
        for (int col = 0; col < 9; ++col) {
            addSlot(new AutocrafterSupplySlot(inventory, AutocrafterData.supplyInvRange.minInclusive() + col,
                    AutocrafterData.supplySlotsCoord.x() + col * AutocrafterData.slotSize, AutocrafterData.supplySlotsCoord.y()));
        }
    }

    private void addByproductInventorySlots() {
        for (int col = 0; col < 9; ++col) {
            addSlot(new AutocrafterByproductSlot(inventory, AutocrafterData.byproductInvRange.minInclusive() + col,
                    AutocrafterData.byproductSlotsCoord.x() + col * AutocrafterData.slotSize, AutocrafterData.byproductSlotsCoord.y()));
        }
    }

    private void addPlayerHotbarSlots() {
        for (int col = 0; col < 9; ++col) {
            addSlot(new Slot(player.getInventory(), col, AutocrafterData.playerHotbarSlotsCoord.x() + col * AutocrafterData.slotSize,
                    AutocrafterData.playerHotbarSlotsCoord.y()));
        }
    }

    private void addPlayerInventorySlots() {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                addSlot(new Slot(player.getInventory(), col + row * 9 + 9,
                        AutocrafterData.playerInventorySlotsCoord.x() + col * AutocrafterData.slotSize,
                        AutocrafterData.playerInventorySlotsCoord.y() + row * AutocrafterData.slotSize));
            }
        }
    }

    public boolean isCrafting() {
        return propertyDelegate.get(0) > 0;
    }

    public int getScaledProgress() {
        int progress = propertyDelegate.get(0);
        int maxProgress = propertyDelegate.get(1);

        return maxProgress != 0 && progress != 0 ? progress * AutocrafterData.arrowSize.x() / maxProgress : 0;
    }

    protected boolean insertItem(ItemStack stack, Range range, boolean fromLast) {
        return insertItem(stack, range.minInclusive(), range.maxInclusive(), fromLast);
    }

    /**
     * @see ScreenHandler ----------------------------------------------------------------------------------------------
     */
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    // Shift + player inventory slotId
    public ItemStack quickMove(PlayerEntity player, int slotId) {
        Slot slot = getSlot(slotId);

        if (!slot.hasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack currentItemStack = slot.getStack();
        ItemStack resultItemStack = currentItemStack.copy();

        if (slotId == AutocrafterData.outputInvIndex) {
            return ItemStack.EMPTY;
        } else if (AutocrafterData.playerInventorySlotsRange.contains(slotId) || AutocrafterData.playerHotbarSlotsRange.contains(slotId)) {
            boolean insertedIntoSupplySlot = insertItem(currentItemStack, AutocrafterData.supplySlotsRange, false);

            if (!insertedIntoSupplySlot) {
                if (AutocrafterData.playerInventorySlotsRange.contains(slotId)) {
                    boolean insertedIntoPlayerHotbarSlot = insertItem(currentItemStack, AutocrafterData.playerHotbarSlotsRange, false);

                    if (!insertedIntoPlayerHotbarSlot) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    boolean insertedIntoPlayerInventorySlot = insertItem(currentItemStack, AutocrafterData.playerInventorySlotsRange, false);

                    if (!insertedIntoPlayerInventorySlot) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        } else {
            boolean insertedIntoPlayerInventoryOrHotbarSlot = insertItem(currentItemStack, AutocrafterData.playerInventorySlotsRange.minInclusive(), AutocrafterData.playerHotbarSlotsRange.maxInclusive(), false);

            if (!insertedIntoPlayerInventoryOrHotbarSlot) {
                return ItemStack.EMPTY;
            }
        }

        if (currentItemStack.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }

        if (currentItemStack.getCount() == resultItemStack.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTakeItem(player, currentItemStack);

        return resultItemStack;
    }
    /*
     * END ScreenHandler -----------------------------------------------------------------------------------------------
     */

    /**
     * @see CraftingScreenHandler --------------------------------------------------------------------------------------
     */
    public static void updateResult(AutocrafterScreenHandler handler, World world, BlockPos pos, PlayerEntity player, AutocrafterInventory inventory) {
        if (world.isClient || world.getServer() == null) {
            return;
        }

        // Update comparators
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof AutocrafterBlockEntity) {
            world.updateComparators(pos, blockEntity.getCachedState().getBlock());
        }

        CraftingResultInventory resultInventory = new CraftingResultInventory();
        resultInventory.setStack(0, inventory.getStack(AutocrafterData.outputInvIndex));

        AutocrafterInventory recipeInventory = new AutocrafterInventory(3, 3);
        int k = 0;
        for (int i : AutocrafterData.recipeInvRange) {
            recipeInventory.setStack(k, inventory.getStack(i));
            k++;
        }

        AutocrafterInventory supplyInventory = new AutocrafterInventory(3, 3);
        supplyInventory.setHandler(handler);
        k = 0;
        for (int i : AutocrafterData.supplyInvRange) {
            supplyInventory.setStackWithoutEvents(k, inventory.getStack(i));
            k++;
        }

        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) player;
        ItemStack newOutputStack = ItemStack.EMPTY;
        Optional<CraftingRecipe> recipe = world.getServer().getRecipeManager().getFirstMatch(RecipeType.CRAFTING, recipeInventory, world);

        if (recipe.isPresent()) {
            CraftingRecipe craftingRecipe = recipe.get();
            ItemStack craftedItemStack = craftingRecipe.craft(handler.inventory);

            if (resultInventory.shouldCraftRecipe(world, serverPlayerEntity, craftingRecipe) && craftedItemStack.isItemEnabled(world.getEnabledFeatures())) {
                newOutputStack = craftedItemStack;
            }
        }

        inventory.setStackWithoutEvents(AutocrafterData.outputInvIndex, newOutputStack);
        handler.setPreviousTrackedSlot(AutocrafterData.outputInvIndex, newOutputStack);

        serverPlayerEntity.networkHandler
                .sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, handler.nextRevision(), AutocrafterData.outputInvIndex, newOutputStack));
    }

    public void onContentChanged(Inventory inventory) {
        context.run((world, pos) -> AutocrafterScreenHandler.updateResult(this, world, pos, player, (AutocrafterInventory) inventory));
    }
    /*
     * END CraftingScreenHandler
     */

    /**
     * @see AbstractRecipeScreenHandler --------------------------------------------------------------------------------
     */
    public void fillInputSlots(boolean craftAll, Recipe<?> recipe, ServerPlayerEntity player) {
        super.fillInputSlots(false, recipe, player);
    }

    public void populateRecipeFinder(RecipeMatcher finder) {
        this.inventory.provideRecipeInputs(finder);
    }

    public void clearCraftingSlots() {
        for (int i : AutocrafterData.recipeInvRange) {
            this.inventory.setStack(i, ItemStack.EMPTY);
        }

        this.inventory.setStack(AutocrafterData.outputInvIndex, ItemStack.EMPTY);
    }

    public boolean matches(Recipe<? super CraftingInventory> recipe) {
        return recipe.matches(this.inventory, this.player.world);
    }

    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        boolean isResultSlot = AutocrafterData.outputInvIndex == slot.getIndex();
        boolean isRecipeSlot = AutocrafterData.recipeInvRange.contains(slot.getIndex());
        boolean isByproductSlot = AutocrafterData.byproductInvRange.contains(slot.getIndex());

        if (isRecipeSlot) {
            return this.inventory.getStack(slot.id).getCount() == 0 && super.canInsertIntoSlot(stack, slot);
        }

        return !isResultSlot && !isByproductSlot && super.canInsertIntoSlot(stack, slot);
    }

    public int getCraftingResultSlotIndex() {
        return AutocrafterData.outputInvIndex;
    }

    public int getCraftingWidth() {
        return this.inventory.getWidth();
    }

    public int getCraftingHeight() {
        return this.inventory.getHeight();
    }

    public int getCraftingSlotCount() {
        return 10;
    }

    public RecipeBookCategory getCategory() {
        return RecipeBookCategory.CRAFTING;
    }

    public boolean canInsertIntoSlot(int index) {
        if (AutocrafterData.recipeInvRange.contains(index)) {
            return getSlot(index).getStack().getCount() < 1;
        }

        return !AutocrafterData.byproductInvRange.contains(index) && index != AutocrafterData.outputInvIndex;
    }
    /*
     * END AbstractRecipeScreenHandler ---------------------------------------------------------------------------------
     */
}
