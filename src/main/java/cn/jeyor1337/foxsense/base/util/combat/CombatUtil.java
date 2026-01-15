package cn.jeyor1337.foxsense.base.util.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class CombatUtil {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static boolean isShieldFacingAway(PlayerEntity target) {
        if (mc.player == null)
            return false;

        Vec3d targetPos = target.getEyePos();
        Vec3d playerPos = mc.player.getEyePos();
        Vec3d directionToPlayer = playerPos.subtract(targetPos).normalize();
        Vec3d targetLookVec = target.getRotationVector();

        double dotProduct = targetLookVec.dotProduct(directionToPlayer);

        return dotProduct < 0.0;
    }
}
