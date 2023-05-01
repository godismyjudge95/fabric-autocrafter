package net.godismyjudge95.autocrafter.screen;

import net.godismyjudge95.autocrafter.helpers.Range;
import net.godismyjudge95.autocrafter.inventory.AutocrafterInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.RecipeUnlocker;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.util.collection.DefaultedList;

public class AutocrafterResultSlot extends CraftingResultSlot {
    private final PlayerEntity player;
    private int amount;
    private final ScreenHandler handler;
    private final Range supplyInvRange;

    public AutocrafterResultSlot(ScreenHandler handler, PlayerEntity player, Inventory inventory, Range supplyInvRange, int slotIndex, int x, int y) {
        super(player, new CraftingInventory(handler, 1, 1), inventory, slotIndex, x, y);
        this.player = player;
        this.handler = handler;
        this.supplyInvRange = supplyInvRange;
    }

    @Override
    public ItemStack takeStack(int amount) {
        if (getStack().getCount() > 1) {
            this.amount += Math.min(amount, this.getStack().getCount());
        }

        return this.inventory.removeStack(getIndex(), amount - 1);
    }

    @Override
    protected void onCrafted(ItemStack stack) {
        if (this.amount > 0) {
            stack.onCraft(this.player.world, this.player, this.amount);
        }

        if (this.inventory instanceof RecipeUnlocker) {
            ((RecipeUnlocker)this.inventory).unlockLastRecipe(this.player);
        }

        this.amount = 0;
    }

    @Override
    public void onTakeItem(PlayerEntity player, ItemStack stack) {
        this.onCrafted(stack);

        AutocrafterInventory craftingInventory = new AutocrafterInventory(handler, 3, 3);
        int k = 0;
        for (int i : supplyInvRange) {
            craftingInventory.setStack(k, inventory.getStack(i));
            k++;
        }

        DefaultedList<ItemStack> defaultedList = player.world.getRecipeManager().getRemainingStacks(RecipeType.CRAFTING, craftingInventory, player.world);

        for(int i = 0; i < defaultedList.size(); ++i) {
            ItemStack itemStack = craftingInventory.getStack(i);
            ItemStack itemStack2 = defaultedList.get(i);
            if (!itemStack.isEmpty()) {
                craftingInventory.removeStack(i, 1);
                itemStack = craftingInventory.getStack(i);
            }

            if (!itemStack2.isEmpty()) {
                if (itemStack.isEmpty()) {
                    craftingInventory.setStack(i, itemStack2);
                } else if (ItemStack.areItemsEqual(itemStack, itemStack2) && ItemStack.areNbtEqual(itemStack, itemStack2)) {
                    itemStack2.increment(itemStack.getCount());
                    craftingInventory.setStack(i, itemStack2);
                } else if (!this.player.getInventory().insertStack(itemStack2)) {
                    this.player.dropItem(itemStack2, false);
                }
            }
        }

        k = 0;
        for (int i : supplyInvRange) {
            inventory.setStack(i, craftingInventory.getStack(k));
            k++;
        }
    }
}
