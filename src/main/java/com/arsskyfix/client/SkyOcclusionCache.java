package com.arsskyfix.client;

import com.hollingsworth.arsnouveau.common.block.tile.SkyBlockTile;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.Map;
import java.util.WeakHashMap;

public final class SkyOcclusionCache {

    // a state change must persist this many frames before it takes effect (kills edge flicker)
    private static final int DEBOUNCE = 4;
    // re-run the ray only after the camera has moved this far, or this long, since the last check
    private static final double MOVE_SQ = 0.25 * 0.25;
    private static final long MAX_AGE_NANOS = 250_000_000L;

    private static final Map<SkyBlockTile, State> CACHE = new WeakHashMap<>();

    private SkyOcclusionCache() {
    }

    public static boolean isOccluded(SkyBlockTile tile, Level level) {
        State s = CACHE.computeIfAbsent(tile, t -> new State());
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        long now = System.nanoTime();

        // occlusion only depends on camera position, so a still camera (even while looking around)
        // reuses the cached result and never re-casts the ray
        double dx = cam.x - s.camX, dy = cam.y - s.camY, dz = cam.z - s.camZ;
        if (!s.init || dx * dx + dy * dy + dz * dz > MOVE_SQ || now - s.nanos > MAX_AGE_NANOS) {
            s.raw = raycastOccluded(level, tile.getBlockPos(), cam);
            s.camX = cam.x;
            s.camY = cam.y;
            s.camZ = cam.z;
            s.nanos = now;
            if (!s.init) {
                s.committed = s.raw;
                s.init = true;
            }
        }

        if (s.raw == s.committed) {
            s.pending = 0;
        } else if (++s.pending >= DEBOUNCE) {
            s.committed = s.raw;
            s.pending = 0;
        }
        return s.committed;
    }

    private static boolean raycastOccluded(Level level, BlockPos target, Vec3 from) {
        Vec3 to = Vec3.atCenterOf(target);
        Block self = level.getBlockState(target).getBlock();
        Vec3 origin = from;
        for (int i = 0; i < 64; i++) {
            BlockHitResult hit = level.clip(new ClipContext(
                origin, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
            BlockPos hp = hit.getBlockPos();
            if (hit.getType() == HitResult.Type.MISS || hp.equals(target)) {
                return false;
            }
            // real terrain between camera and block -> occluded. skip other sky blocks and blocks
            // directly touching this one (a block placed on/against it never causes an x-ray, since
            // that face is already neighbour-skipped, but its center ray would falsely read occluded).
            boolean adjacent = Math.abs(hp.getX() - target.getX()) <= 1
                && Math.abs(hp.getY() - target.getY()) <= 1
                && Math.abs(hp.getZ() - target.getZ()) <= 1;
            if (!adjacent && !level.getBlockState(hp).is(self)) {
                return true;
            }
            Vec3 dir = to.subtract(origin);
            double len = dir.length();
            if (len < 1.0e-4) {
                return false;
            }
            origin = hit.getLocation().add(dir.scale(0.02 / len));
        }
        return false;
    }

    private static final class State {
        boolean init;
        boolean committed;
        boolean raw;
        int pending;
        double camX, camY, camZ;
        long nanos;
    }
}
