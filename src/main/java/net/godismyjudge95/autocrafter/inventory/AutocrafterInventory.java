package net.godismyjudge95.autocrafter.inventory;

import net.godismyjudge95.autocrafter.AutocrafterData;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;

public class AutocrafterInventory extends CraftingInventory {
    private final DefaultedList<ItemStack> stacks;
    private final int width;
    private final int height;
    private ScreenHandler handler = null;

    public AutocrafterInventory(ScreenHandler handler, int craftingWidth, int craftingHeight) {
        super(handler, craftingWidth, craftingHeight);
        this.stacks = DefaultedList.ofSize(craftingWidth * craftingHeight, ItemStack.EMPTY);
        this.handler = handler;
        this.width = craftingWidth;
        this.height = craftingHeight;
    }

    public AutocrafterInventory(DefaultedList<ItemStack> inventory, int craftingWidth, int craftingHeight) {
        super(null, craftingWidth, craftingHeight);
        this.stacks = inventory;
        this.width = craftingWidth;
        this.height = craftingHeight;
    }

    public AutocrafterInventory(int craftingWidth, int craftingHeight) {
        super(null, craftingWidth, craftingHeight);
        this.stacks = DefaultedList.ofSize(craftingWidth * craftingHeight, ItemStack.EMPTY);
        this.width = craftingWidth;
        this.height = craftingHeight;
    }

    public void setHandler(ScreenHandler handler) {
        this.handler = handler;
    }

    @Override
    public int size() {
        return this.stacks.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemStack : this.stacks) {
            if (itemStack.isEmpty()) continue;
            return false;
        }

        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot >= this.size()) {
            return ItemStack.EMPTY;
        }

        return this.stacks.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack itemStack = removeStackWithoutEvents(slot, amount);
        markDirty();
        return itemStack;
    }

    public ItemStack removeStackWithoutEvents(int slot, int amount) {
        return Inventories.splitStack(this.stacks, slot, amount);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        setStackWithoutEvents(slot, stack);
        markDirty();
    }

    public void setStackWithoutEvents(int slot, ItemStack stack) {
        stacks.set(slot, stack);
    }

    @Override
    public void markDirty() {
        if (handler != null) {
            handler.onContentChanged(this);
        }
    }

    @Override
    public void clear() {
        stacks.clear();
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public void provideRecipeInputs(RecipeMatcher finder) {
        for (int i : AutocrafterData.recipeInvRange) {
            finder.addUnenchantedInput(this.getStack(i));
        }
    }
}
