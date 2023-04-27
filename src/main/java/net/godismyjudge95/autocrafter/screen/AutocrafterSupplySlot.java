package net.godismyjudge95.autocrafter.screen;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class AutocrafterSupplySlot extends Slot {
    public AutocrafterSupplySlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return getStack().getCount() == 0;
    }
}
