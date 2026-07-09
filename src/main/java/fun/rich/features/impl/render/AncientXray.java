package fun.rich.features.impl.render;

import fun.rich.events.render.WorldRenderEvent;
import fun.rich.features.module.Module;
import fun.rich.features.module.ModuleCategory;
import fun.rich.features.module.setting.implement.ColorSetting;
import fun.rich.features.module.setting.implement.SliderSettings;
import fun.rich.utils.client.managers.event.EventHandler;
import fun.rich.utils.display.geometry.Render3D;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.awt.*;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AncientXray extends Module {

    private final Set<BlockPos> debrisPositions = ConcurrentHashMap.newKeySet();
    private long lastScanTime = 0;

    private final ColorSetting colorSetting = new ColorSetting("Цвет", "Цвет подсветки", new Color(166, 117, 84).getRGB());
    private final SliderSettings radiusFinder = new SliderSettings("Дистанция поиска", "Диапазон поиска")
            .setValue(16f).range(8F, 64F);

    private final SliderSettings scanDelay = new SliderSettings("Задержка сканирования", "Задержка в секундах")
            .setValue(2f).range(0.5F, 10F);

    public AncientXray() {
        super("AncientXray", "Ancient Xray", ModuleCategory.RENDER);
        setup(colorSetting, radiusFinder, scanDelay);
    }

    @Override
    public void activate() {
        scanWorld();
    }

    @Override
    public void deactivate() {
        debrisPositions.clear();
    }

    @EventHandler
    public void onRender3D(WorldRenderEvent e) {
        if (mc.world == null || mc.player == null) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastScanTime > scanDelay.getValue() * 1000L) {
            scanWorld();
            lastScanTime = currentTime;
        }

        double maxDistance = radiusFinder.getValue() * radiusFinder.getValue();
        BlockPos playerPos = mc.player.getBlockPos();

        int color = colorSetting.getColor();
        for (BlockPos pos : debrisPositions) {
            if (playerPos.getSquaredDistance(pos) > maxDistance) continue;

            if (mc.world.getBlockState(pos).isOf(Blocks.ANCIENT_DEBRIS)) {
                Render3D.drawBox(new Box(pos), color, 2.0f);
            }
        }
    }

    private void scanWorld() {
        if (mc.world == null || mc.player == null) return;

        Set<BlockPos> found = ConcurrentHashMap.newKeySet();
        BlockPos playerPos = mc.player.getBlockPos();
        int radius = radiusFinder.getInt();

        // Optimized scan: spread over time or limited radius on main thread
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    if (!mc.world.isInBuildLimit(pos)) continue;

                    if (mc.world.getBlockState(pos).isOf(Blocks.ANCIENT_DEBRIS)) {
                        if (isExposed(pos)) {
                            found.add(pos.toImmutable());
                        }
                    }
                }
            }
        }
        debrisPositions.clear();
        debrisPositions.addAll(found);
    }

    private boolean isExposed(BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (mc.world.getBlockState(pos.offset(direction)).isAir()) {
                return true;
            }
        }
        return false;
    }
}
