package net.godismyjudge95.autocrafter.block.custom;

import net.godismyjudge95.autocrafter.AutocrafterData;
import net.godismyjudge95.autocrafter.inventory.AutocrafterInventory;
import net.minecraft.block.*;
import net.minecraft.recipe.*;
import org.jetbrains.annotations.Nullable;

import net.godismyjudge95.autocrafter.block.entity.AutocrafterBlockEntity;
import net.godismyjudge95.autocrafter.block.entity.ModBlockEntities;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPointerImpl;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.Optional;

public class AutocrafterBlock extends BlockWithEntity {
    public static final DirectionProperty FACING = FacingBlock.FACING;
    public static final BooleanProperty TRIGGERED = Properties.TRIGGERED;
    private static final int SCHEDULED_TICK_DELAY = 4;

    public AutocrafterBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING,
                Direction.NORTH).with(TRIGGERED, false));
    }

    /**
     * ----------------------------- START {@link Block} ----------------------------
     */
    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite());
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, TRIGGERED);
    }
    /*
     * ----------------------------- END {@link Block} ----------------------------
     */

    /**
     * ----------------------------- START {@link AbstractBlock} ----------------------------
     */
    @SuppressWarnings("deprecation")
    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }
    /*
     * ----------------------------- END {@link AbstractBlock} ----------------------------
     */

    
    /**
     * ----------------------------- START {@link BlockWithEntity} ----------------------------
     */
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    // Drop all items on destroy
    @SuppressWarnings("deprecation")
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos,
                                BlockState newState, boolean moved) {
        if (state.isOf(newState.getBlock())) {
            return;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (blockEntity instanceof AutocrafterBlockEntity) {
            ItemScatterer.spawn(world, pos, (Inventory) blockEntity);
            world.updateComparators(pos, this);
        }
    }

    // Open screen on right click action
    @SuppressWarnings("deprecation")
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                              PlayerEntity player, Hand hand,
                              BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof AutocrafterBlockEntity) {
            player.openHandledScreen((AutocrafterBlockEntity) blockEntity);
            player.incrementStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
        }

        return ActionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AutocrafterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
                                                                  BlockEntityType<T> type) {
        return checkType(type, ModBlockEntities.AUTOCRAFTER, AutocrafterBlockEntity::tick);
    }
    /*
     * ------------------------------ END {@link BlockWithEntity} -----------------------------
     */

    /**
     * ---------------------------- START {@link DropperBlock} ----------------------------
     */
    public void dispense(BlockState state, ServerWorld world, BlockPos pos) {
        ItemStack reducedStack;
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof AutocrafterBlockEntity autocrafterBlockEntity)) {
            return;
        }

        BlockPointerImpl blockPointerImpl = new BlockPointerImpl(world, pos);

        ItemStack outputStack = autocrafterBlockEntity.getStack(AutocrafterData.outputInvIndex);
        if (outputStack.isEmpty() || outputStack.getCount() == 1) {
            world.setBlockState(pos, state.with(TRIGGERED, false), Block.NO_REDRAW);
            return;
        }
        
        Direction direction = world.getBlockState(pos).get(FACING);
        Inventory facingHopperInventory = HopperBlockEntity.getInventoryAt(world, pos.offset(direction));
        
        if (facingHopperInventory == null) {
            reducedStack = new ItemDispenserBehavior().dispense(blockPointerImpl, outputStack);

        } else {
            reducedStack = HopperBlockEntity.transfer(autocrafterBlockEntity, facingHopperInventory, outputStack.copy().split(1),
                    direction.getOpposite());
            
            if (reducedStack.isEmpty()) {
                reducedStack = outputStack.copy();
                reducedStack.decrement(1);
            } else {
                reducedStack = outputStack.copy();
            }
        }
        
        autocrafterBlockEntity.setStack(0, reducedStack);

        world.updateComparators(pos, this);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (!world.isReceivingRedstonePower(pos) && !world.isReceivingRedstonePower(pos.up())) {
            return;
        }

        world.setBlockState(pos, state.with(TRIGGERED, true), Block.NO_REDRAW);
        world.scheduleBlockTick(pos, this, SCHEDULED_TICK_DELAY);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos,
                              Random random) {
        dispense(state, world, pos);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        if (world.getServer() == null) {
            return 0;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (!(blockEntity instanceof AutocrafterBlockEntity) || ((AutocrafterBlockEntity) blockEntity).size() == 0) {
            return 0;
        }

        AutocrafterInventory recipeInventory = new AutocrafterInventory(3, 3);
        int k = 0;
        for (int i : AutocrafterData.recipeInvRange) {
            recipeInventory.setStack(k, ((AutocrafterBlockEntity) blockEntity).getStack(i));
            k++;
        }

        Optional<CraftingRecipe> recipe = world.getServer().getRecipeManager().getFirstMatch(RecipeType.CRAFTING, recipeInventory, world);

        if (recipe.isEmpty()) {
            if (((AutocrafterBlockEntity) blockEntity).getStack(AutocrafterData.outputInvIndex).getCount() > 1) {
                return 1;
            }

            return 0;
        }

        RecipeMatcher recipeMatcher = new RecipeMatcher();
        for (int i : AutocrafterData.supplyInvRange) {
            recipeMatcher.addInput(((AutocrafterBlockEntity) blockEntity).getStack(i));
        }

        double count = recipeMatcher.countCrafts(recipe.get(), null);
        int redstonePower = (int) Math.ceil(count / 2);

        return Math.min(redstonePower, 15);
    }
    /*
     * ---------------------------- END {@link DropperBlock} ----------------------------
     */
}
