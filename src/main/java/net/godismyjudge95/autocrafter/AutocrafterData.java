package net.godismyjudge95.autocrafter;

import net.godismyjudge95.autocrafter.helpers.Range;
import net.godismyjudge95.autocrafter.helpers.ScreenPosition;

public class AutocrafterData {
    // Crafting Interval
    public static final int maxProgress = 20;

    // Inventory Ranges
    public static final Integer outputInvIndex = 0;
    public static final Range recipeInvRange = new Range(1, 9); // Recipe 3x3 grid
    public static final Range supplyInvRange = new Range(10, 18); // Supply 1x9 grid
    public static final Range byproductInvRange = new Range(19, 27); // Byproduct 1x9 grid

    // Slot Coordinates
    public static final int slotSize = 18;
    public static final ScreenPosition supplySlotsCoord = new ScreenPosition(8, 18);
    public static final ScreenPosition recipeSlotsCoord = new ScreenPosition(30, 49);
    public static final ScreenPosition arrowCoord = new ScreenPosition(89, 68);
    public static final ScreenPosition arrowActiveCoord = new ScreenPosition(176, 0);
    public static final ScreenPosition arrowSize = new ScreenPosition(24, 17);
    public static final ScreenPosition resultSlotCoord = new ScreenPosition(124, 67);
    public static final ScreenPosition byproductSlotsCoord = new ScreenPosition(8, 116);
    public static final ScreenPosition playerInventorySlotsCoord = new ScreenPosition(8, 138);
    public static final ScreenPosition playerHotbarSlotsCoord = new ScreenPosition(8, 196);

    // Slot Indices
    public static final int outputSlotIndex = 0;
    public static final Range recipeSlotsRange = new Range(1, 9);
    public static final Range playerInventorySlotsRange = new Range(10, 36);
    public static final Range playerHotbarSlotsRange = new Range(37, 45);
    public static final Range supplySlotsRange = new Range(46, 54);
    public static final Range byproductSlotsRange = new Range(55, 63);
}
