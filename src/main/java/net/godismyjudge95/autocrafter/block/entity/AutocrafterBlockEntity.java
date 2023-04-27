package net.godismyjudge95.autocrafter.block.entity;

import java.util.Optional;
import java.util.stream.IntStream;

import net.godismyjudge95.autocrafter.AutocrafterData;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.*;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import net.godismyjudge95.autocrafter.AutocrafterMod;
import net.godismyjudge95.autocrafter.block.custom.AutocrafterBlock;
import net.godismyjudge95.autocrafter.screen.AutocrafterScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class AutocrafterBlockEntity extends BlockEntity
        implements NamedScreenHandlerFactory, ImplementedInventory, SidedInventory {

    public static final int INVENTORY_SIZE = 28;
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(AutocrafterBlockEntity.INVENTORY_SIZE, ItemStack.EMPTY);

    private int progress = 0;
    protected final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        public int get(int index) {
            return switch (index) {
                case 0 -> AutocrafterBlockEntity.this.progress;
                case 1 -> AutocrafterData.maxProgress;
                default -> 0;
            };
        }

        public void set(int index, int value) {
            if (index == 0) {
                AutocrafterBlockEntity.this.progress = value;
            }
        }

        public int size() {
            return 2;
        }
    };

    public AutocrafterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AUTOCRAFTER, pos, state);
    }

    /**
     * ----------------------------- START {@link ImplementedInventory} ----------------------------
     */
    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    /*
     * ----------------------------- END {@link ImplementedInventory} ----------------------------
     */

    /**
     * ----------------------------- START {@link NamedScreenHandlerFactory} ----------------------------
     */
    @Override
    public Text getDisplayName() {
//        return Text.translatable(getCachedState().getBlock().getTranslationKey());
        return Text.empty();
    }
    /*
     * ----------------------------- END {@link NamedScreenHandlerFactory} ----------------------------
     */

    /**
     * ----------------------------- START {@link ScreenHandlerFactory} ----------------------------
     */
    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new AutocrafterScreenHandler(syncId, inv, this.inventory, propertyDelegate, ScreenHandlerContext.create(world, pos));
    }
    /*
     * ----------------------------- END {@link ScreenHandlerFactory} ----------------------------
     */

    /**
     * ----------------------------- START {@link BlockEntity} ----------------------------
     */
    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
        nbt.putInt("autocrafter.progress", progress);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        Inventories.readNbt(nbt, inventory);
        super.readNbt(nbt);
        progress = nbt.getInt("autocrafter.progress");
    }
    /*
     * ----------------------------- END {@link BlockEntity} ----------------------------
     */


    /**
     * ----------------------------- START Custom Recipe Crafter ----------------------------
     */
    private void resetProgress() {
        progress = 0;
    }

    public static void tick(World world, BlockPos pos, BlockState state, AutocrafterBlockEntity entity) {
        if (world.isClient()) {
            return;
        }

        boolean hasRedstonePower = world.isReceivingRedstonePower(pos) || world.isReceivingRedstonePower(pos.up());

        if (getRecipe(entity) != null) {
            if (hasRedstonePower) {
                entity.progress++;
                markDirty(world, pos, state);

                if (entity.progress >= AutocrafterData.maxProgress) {
                    craftItem(entity);
                    entity.resetProgress();
                }
            }
        } else {
            entity.resetProgress();
            markDirty(world, pos, state);
        }
    }

    private static void craftItem(AutocrafterBlockEntity entity) {
        Pair<CraftingRecipe, ItemStack> recipeResult = getRecipe(entity);
        if (recipeResult == null) {
            return;
        }

        DefaultedList<Integer> supplyIndicesToDecrement = DefaultedList.of();
        for (int recipeIndex: AutocrafterData.recipeInvRange) {
            Item recipeIngredientItem = entity.getStack(recipeIndex).getItem();

            boolean foundIngredientItem = false;
            for (int supplyIndex : AutocrafterData.supplyInvRange) {
                Item supplyItem = entity.getStack(supplyIndex).getItem();

                if (recipeIngredientItem == supplyItem) {
                    foundIngredientItem = true;
                    supplyIndicesToDecrement.add(supplyIndex);
                    break;
                }
            }

            if (!foundIngredientItem) {
                AutocrafterMod.LOGGER.info(AutocrafterMod.MOD_ID + " - Could not craft item, not enough supplies.");
                return;
            }
        }

        // Only decrement supply inventory if all the ingredients are found
        DefaultedList<Item> byproductItems = DefaultedList.of();
        for (Integer integer : supplyIndicesToDecrement) {
            ItemStack supplyStack = entity.getStack(integer);
            Item supplyItem = supplyStack.getItem();
            entity.setStack(integer,
                    new ItemStack(supplyItem, supplyStack.getCount() - 1));

            if (supplyItem.hasRecipeRemainder()) {
                byproductItems.add(supplyItem.getRecipeRemainder());
            }
        }

        for (Item item : byproductItems) {
            boolean found = false;

            for (int i : AutocrafterData.byproductInvRange) {
                ItemStack itemStack = entity.getStack(i);
                if (itemStack.getItem() == item && itemStack.getCount() < itemStack.getMaxCount()) {
                    entity.setStack(i, new ItemStack(item, itemStack.getCount() + 1));
                    found = true;
                    break;
                }
            }

            if (!found) {
                for (int i : AutocrafterData.byproductInvRange) {
                    if (entity.getStack(i).isEmpty()) {
                        entity.setStack(i, new ItemStack(item, 1));
                        break;
                    }
                }
            }
        }

        entity.setStack(AutocrafterData.outputInvIndex, new ItemStack(recipeResult.getRight().getItem(), recipeResult.getRight().getCount() + 1));
    }

    private static Pair<CraftingRecipe, ItemStack> getRecipe(AutocrafterBlockEntity entity) {
        if (entity.world == null) {
            return null;
        }

        CraftingInventory recipeInventory = new CraftingInventory(new ScreenHandler(null, -1) {
            @Override
            public ItemStack quickMove(PlayerEntity player, int slot) {
                return null;
            }

            @Override
            public boolean canUse(PlayerEntity player) {
                return false;
            }
        }, 3, 3);
        int k = 0;
        for (int recipeIndex : AutocrafterData.recipeInvRange) {
            recipeInventory.setStack(k, entity.getStack(recipeIndex));
            k++;
        }
        Optional<CraftingRecipe> recipe =  entity.world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, recipeInventory, entity.world);
        return recipe.map(craftingRecipe -> new Pair<>(craftingRecipe, craftingRecipe.craft(recipeInventory))).orElse(null);

    }
    /*
     * ----------------------------- END Custom Recipe Crafter ----------------------------
     */

    /**
     * --------------------------- START {@link SidedInventory} ---------------------------
     */
    @Override
    public int[] getAvailableSlots(Direction direction) {
        if (world == null) {
            return new int[]{};
        }

        Direction facingDir = world.getBlockState(pos).get(AutocrafterBlock.FACING);

        int[] supplySlotIds = IntStream.rangeClosed(AutocrafterData.supplyInvRange.minInclusive(), AutocrafterData.supplyInvRange.maxInclusive()).toArray();
        int[] outputSlotIds = new int[]{AutocrafterData.outputInvIndex};
        int[] byproductSlotIds = IntStream.rangeClosed(AutocrafterData.byproductInvRange.minInclusive(), AutocrafterData.byproductInvRange.maxInclusive()).toArray();

        // Front face is always the dropper
        if (direction == facingDir) {
            return inventory.get(AutocrafterData.outputInvIndex).getCount() > 1 ? outputSlotIds : new int[]{};
        }

        // Dropper is pointing vertical and the side is the opposite of the dropper
        if (direction == facingDir.getOpposite() && (facingDir == Direction.UP || facingDir == Direction.DOWN)) {
            return byproductSlotIds;
        }

        // Dropper is horizontal and the side is down
        if (direction == Direction.DOWN) {
            return byproductSlotIds;
        }

        // Dropper is horizontal and the side is not up or down
        return supplySlotIds;
    }

    @Override
    public boolean canInsert(int slot, ItemStack itemStack, Direction direction) {
        if (world == null) {
            return false;
        }

        Direction facingDir = world.getBlockState(pos).get(AutocrafterBlock.FACING);

        // Dropper pointing vertical - don't allow inserting items into the supply inventory from the top/bottom sides which are either the dropper or byproduct outputs
        if (facingDir == Direction.UP || facingDir == Direction.DOWN) {
            return direction != Direction.UP && direction != Direction.DOWN;
        }

        // Dropper pointing horizontal - don't allow inserting items into supply inventory from the front (dropper) or bottom (byproduct) sides
        return direction != facingDir && direction != Direction.DOWN;
    }

    @Override
    public boolean canExtract(int slot, ItemStack itemStack, Direction direction) {
        if (world == null) {
            return false;
        }

        Direction facingDir = world.getBlockState(pos).get(AutocrafterBlock.FACING);

        // Dropper pointing vertical - only allow retrieving items from byproduct inventory on the opposite side of the dropper
        if (facingDir == Direction.UP || facingDir == Direction.DOWN) {
            return direction == Direction.UP || direction == Direction.DOWN;
        }

        // Dropper pointing horizontal - only allow retrieving items from byproduct inventory on the bottom
        return direction == Direction.DOWN;
    }
    /*
     * --------------------------- END {@link SidedInventory} ---------------------------
     */
}
