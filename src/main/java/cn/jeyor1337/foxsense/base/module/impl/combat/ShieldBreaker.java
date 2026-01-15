package cn.jeyor1337.foxsense.base.module.impl.combat;

import com.cubk.event.annotations.EventTarget;

import cn.jeyor1337.foxsense.base.event.EventAttack;
import cn.jeyor1337.foxsense.base.event.EventTick;
import cn.jeyor1337.foxsense.base.module.Module;
import cn.jeyor1337.foxsense.base.module.ModuleType;
import cn.jeyor1337.foxsense.base.util.combat.CombatUtil;
import cn.jeyor1337.foxsense.base.util.item.ItemUtils;
import cn.jeyor1337.foxsense.base.util.timer.TimerUtil;
import cn.jeyor1337.foxsense.base.value.BooleanValue;
import cn.jeyor1337.foxsense.base.value.ModeValue;
import cn.jeyor1337.foxsense.base.value.NumberValue;
import cn.jeyor1337.foxsense.mixin.MinecraftClientAccessor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;

public class ShieldBreaker extends Module {
    public static boolean breakingShield = false;

    private final NumberValue cps = new NumberValue("CPS", 20, 1, 20, 1);
    private final NumberValue reactionDelay = new NumberValue("Reaction Delay", 0, 0, 250, 5);
    private final NumberValue swapDelay = new NumberValue("Swap Delay", 50, 0, 500, 10);
    private final NumberValue attackDelay = new NumberValue("Attack Delay", 50, 0, 500, 10);
    private final NumberValue swapBackDelay = new NumberValue("Swap Back Delay", 100, 0, 500, 10);

    private final ModeValue mode = new ModeValue("Mode", new String[] { "Auto", "OnHit" }, "Auto");
    private final BooleanValue revertSlot = new BooleanValue("Revert Slot", true);
    private final BooleanValue rayTraceCheck = new BooleanValue("Check Facing", true);
    private final BooleanValue disableIfUsingItem = new BooleanValue("Disable if using item", true);

    private final TimerUtil cpsTimer = new TimerUtil();
    private final TimerUtil reactionTimer = new TimerUtil();
    private final TimerUtil swapTimer = new TimerUtil();
    private final TimerUtil attackTimer = new TimerUtil();
    private final TimerUtil swapBackTimer = new TimerUtil();

    private int savedSlot = -1;
    private boolean lastAttackPressed = false;

    public ShieldBreaker() {
        super("Shield Breaker", "Automatically breaks the opponent's shield", ModuleType.COMBAT);
        addValues(mode, cps, reactionDelay, swapDelay, attackDelay, swapBackDelay,
                revertSlot, rayTraceCheck, disableIfUsingItem);
    }

    private boolean canRun() {
        if (isNull() || mc.currentScreen != null)
            return false;
        if (!ItemUtils.hasWeapon(AxeItem.class))
            return false;
        if (mc.player.isUsingItem() && disableIfUsingItem.isEnabled())
            return false;
        return cpsTimer.hasElapsedTime((long) (1000.0 / cps.getValue().doubleValue()));
    }

    private PlayerEntity getTargetPlayer() {
        if (!(mc.crosshairTarget instanceof EntityHitResult entityHit))
            return null;
        if (!(entityHit.getEntity() instanceof PlayerEntity target))
            return null;
        return target;
    }

    @EventTarget
    private void onAttackEvent(EventAttack event) {
        if (isNull())
            return;
        if (mode.getValue().equals("OnHit")) {
            lastAttackPressed = true;
        }
    }

    @EventTarget
    private void onTickEvent(EventTick event) {
        if (!canRun())
            return;

        PlayerEntity target = getTargetPlayer();
        if (target == null)
            return;

        boolean isOnHitMode = mode.getValue().equals("OnHit");
        boolean isBlocking = target.isBlocking() && target.isHolding(Items.SHIELD);
        boolean canBreak = !rayTraceCheck.isEnabled() || !CombatUtil.isShieldFacingAway(target);
        boolean shouldActivate = !isOnHitMode || lastAttackPressed;

        if (isBlocking && canBreak && shouldActivate) {

            if (!(mc.player.getMainHandStack().getItem() instanceof AxeItem)) {
                if (reactionTimer.hasElapsedTime(reactionDelay.getValue().longValue())
                        && swapTimer.hasElapsedTime(swapDelay.getValue().longValue())) {
                    breakingShield = true;
                    if (savedSlot == -1)
                        savedSlot = ((cn.jeyor1337.foxsense.mixin.PlayerInventoryAccessor) mc.player.getInventory())
                                .getSelectedSlot();
                    ItemUtils.swapToWeapon(AxeItem.class);
                    attackTimer.reset();
                }
            }

            if (mc.player.getMainHandStack().getItem() instanceof AxeItem) {
                if (attackTimer.hasElapsedTime(attackDelay.getValue().longValue()) || savedSlot == -1) {
                    ((MinecraftClientAccessor) mc).invokeDoAttack();
                    cpsTimer.reset();
                    attackTimer.reset();
                    swapBackTimer.reset();
                    breakingShield = false;
                    lastAttackPressed = false;
                }
            }

        } else {
            if (!reactionTimer.hasElapsedTime(reactionDelay.getValue().longValue() / 2))
                reactionTimer.reset();

            if (savedSlot != -1 && swapBackTimer.hasElapsedTime(swapBackDelay.getValue().longValue())) {
                if (revertSlot.isEnabled())
                    ((cn.jeyor1337.foxsense.mixin.PlayerInventoryAccessor) mc.player.getInventory())
                            .setSelectedSlot(savedSlot);
                savedSlot = -1;
            }
        }
    }

    @Override
    protected void onDisable() {
        if (savedSlot != -1 && revertSlot.isEnabled()) {
            ((cn.jeyor1337.foxsense.mixin.PlayerInventoryAccessor) mc.player.getInventory()).setSelectedSlot(savedSlot);
        }
        savedSlot = -1;
        breakingShield = false;
        lastAttackPressed = false;
        super.onDisable();
    }
}
