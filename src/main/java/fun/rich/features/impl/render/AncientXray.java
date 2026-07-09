package fun.rich.features.impl.render;

import fun.rich.events.player.TickEvent;
import fun.rich.events.render.WorldRenderEvent;
import fun.rich.features.module.Module;
import fun.rich.features.module.ModuleCategory;
import fun.rich.features.module.setting.implement.SliderSettings;
import fun.rich.utils.client.managers.event.EventHandler;
import fun.rich.utils.display.geometry.Render3D;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

public class AncientXray extends Module {

    private final SliderSettings radiusSetting = new SliderSettings("Радиус", "Радиус поиска обломков").setValue(16f).range(8f, 64f);
    private final List<BlockPos> debrisPositions = new ArrayList<>();
    private long lastScanTime = 0;
    private int currentX, currentY, currentZ;
    private boolean scanning = false;
    private final List<BlockPos> tempDebris = new ArrayList<>();

    public AncientXray() {
        super("AncientXray", "Ancient Xray", ModuleCategory.RENDER);
        setup(radiusSetting);
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (mc.player == null || mc.world == null) return;

        if (!scanning && System.currentTimeMillis() - lastScanTime > 1000) {
            startScan();
        }

        if (scanning) {
            continueScan();
        }
    }

    private void startScan() {
        int radius = radiusSetting.getInt();
        currentX = -radius;
        currentY = -radius;
        currentZ = -radius;
        tempDebris.clear();
        scanning = true;
    }

    private void continueScan() {
        int radius = radiusSetting.getInt();
        BlockPos playerPos = mc.player.getBlockPos();
        int count = 0;

        while (scanning && count < 5000) {
            BlockPos pos = playerPos.add(currentX, currentY, currentZ);
            if (mc.world.getBlockState(pos).getBlock() == Blocks.ANCIENT_DEBRIS) {
                if (isExposed(pos)) {
                    tempDebris.add(pos.toImmutable());
                }
            }

            count++;
            currentZ++;
            if (currentZ > radius) {
                currentZ = -radius;
                currentY++;
                if (currentY > radius) {
                    currentY = -radius;
                    currentX++;
                    if (currentX > radius) {
                        scanning = false;
                        debrisPositions.clear();
                        debrisPositions.addAll(tempDebris);
                        lastScanTime = System.currentTimeMillis();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onRender3D(WorldRenderEvent e) {
        int color = 0xFFA67554;
        for (BlockPos pos : debrisPositions) {
            Render3D.drawBox(new Box(pos), color, 2.0f);
        }
    }

    private boolean isExposed(BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (!mc.world.getBlockState(pos.offset(dir)).isOpaque()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void deactivate() {
        debrisPositions.clear();
        scanning = false;
    }
}
