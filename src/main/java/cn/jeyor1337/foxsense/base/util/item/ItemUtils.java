package cn.jeyor1337.foxsense.base.util.item;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ItemUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static boolean isSwordItem(Item item) {
        return item == Items.WOODEN_SWORD ||
                item == Items.STONE_SWORD ||
                item == Items.IRON_SWORD ||
                item == Items.GOLDEN_SWORD ||
                item == Items.DIAMOND_SWORD ||
                item == Items.NETHERITE_SWORD;
    }

    public static boolean isShieldItem(Item item) {
        return item == Items.SHIELD;
    }

    public static boolean isChestplateItem(Item item) {
        return item == Items.LEATHER_CHESTPLATE ||
                item == Items.CHAINMAIL_CHESTPLATE ||
                item == Items.IRON_CHESTPLATE ||
                item == Items.GOLDEN_CHESTPLATE ||
                item == Items.DIAMOND_CHESTPLATE ||
                item == Items.NETHERITE_CHESTPLATE;
    }

    public static int findChestplateSlot() {
        if (mc.player == null)
            return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && isChestplateItem(stack.getItem())) {
                return i;
            }
        }
        return -1;
    }

    public static void swapToChestplate() {
        int slot = findChestplateSlot();
        if (slot != -1 && mc.player != null) {
            ((cn.jeyor1337.foxsense.mixin.PlayerInventoryAccessor) mc.player.getInventory()).setSelectedSlot(slot);
        }
    }

    public static boolean hasItem(Item item) {
        if (mc.player == null)
            return false;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                return true;
            }
        }
        return false;
    }

    public static int findSlot(Item item) {
        if (mc.player == null)
            return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    public static void swapToSlot(Item item) {
        int slot = findSlot(item);
        if (slot != -1 && mc.player != null) {
            ((cn.jeyor1337.foxsense.mixin.PlayerInventoryAccessor) mc.player.getInventory()).setSelectedSlot(slot);
        }
    }

    public static boolean hasWeapon(Class<? extends Item> itemClass) {
        if (mc.player == null)
            return false;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && itemClass.isInstance(stack.getItem())) {
                return true;
            }
        }
        return false;
    }

    public static int findWeaponSlot(Class<? extends Item> itemClass) {
        if (mc.player == null)
            return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && itemClass.isInstance(stack.getItem())) {
                return i;
            }
        }
        return -1;
    }

    public static void swapToWeapon(Class<? extends Item> itemClass) {
        int slot = findWeaponSlot(itemClass);
        if (slot != -1 && mc.player != null) {
            ((cn.jeyor1337.foxsense.mixin.PlayerInventoryAccessor) mc.player.getInventory()).setSelectedSlot(slot);
        }
    }
}
