package fun.rich.features.impl.misc;

import fun.rich.events.chat.ChatEvent;
import fun.rich.events.render.DrawEvent;
import fun.rich.events.render.WorldLoadEvent;
import fun.rich.events.render.WorldRenderEvent;
import fun.rich.features.module.Module;
import fun.rich.features.module.ModuleCategory;
import fun.rich.features.module.setting.implement.BooleanSetting;
import fun.rich.features.module.setting.implement.ColorSetting;
import fun.rich.utils.client.managers.event.EventHandler;
import fun.rich.utils.display.color.ColorAssist;
import fun.rich.utils.display.font.FontRenderer;
import fun.rich.utils.display.font.Fonts;
import fun.rich.utils.display.geometry.Render2D;
import fun.rich.utils.display.geometry.Render3D;
import fun.rich.utils.math.projection.Projection;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector4d;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventWaypoint extends Module {

    private final ColorSetting colorSetting = new ColorSetting("Цвет", "Цвет вейпоинта", new Color(138, 43, 226).getRGB());
    private final BooleanSetting autoRemove = new BooleanSetting("Авто-удаление", "Удалять метку при приближении", true);

    private final List<Waypoint> waypoints = new ArrayList<>();

    // Improved regex to handle various coordinate formats, including FunTime-specific ones
    // Matches: "100 200 100", "X: 100, Y: 200, Z: 100", "X: 100, Z: 100", etc.
    private final Pattern xyzPattern = Pattern.compile("(?:[XYZxyz][: ]+)?(-?\\d+)[, ]+(?:[Yy][: ]+)?(-?\\d+)[, ]+(?:[Zz][: ]+)?(-?\\d+)");
    private final Pattern xzPattern = Pattern.compile("(?:[Xx][: ]+)?(-?\\d+)[, ]+(?:[Zz][: ]+)?(-?\\d+)");

    public EventWaypoint() {
        super("EventWaypoint", "Event Waypoint", ModuleCategory.MISC);
        setup(colorSetting, autoRemove);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
        waypoints.clear();
    }

    @EventHandler
    public void onChat(ChatEvent e) {
        String msg = ColorAssist.removeFormatting(e.getMessage());
        if (msg == null) return;

        String name = "Event";
        if (msg.toLowerCase().contains("мистический") || msg.toLowerCase().contains("мистик")) name = "Mystic";
        else if (msg.toLowerCase().contains("маяк")) name = "Beacon";
        else if (msg.toLowerCase().contains("сокровище")) name = "Treasure";
        else if (msg.toLowerCase().contains("аирдроп")) name = "Airdrop";

        boolean found = false;
        Matcher xyzMatcher = xyzPattern.matcher(msg);
        while (xyzMatcher.find()) {
            try {
                double x = Double.parseDouble(xyzMatcher.group(1));
                double y = Double.parseDouble(xyzMatcher.group(2));
                double z = Double.parseDouble(xyzMatcher.group(3));

                if (Math.abs(x) > 10 || Math.abs(z) > 10) {
                    addWaypoint(name, new Vec3d(x, y, z));
                    found = true;
                }
            } catch (NumberFormatException ignored) {}
        }

        if (!found) {
            Matcher xzMatcher = xzPattern.matcher(msg);
            while (xzMatcher.find()) {
                try {
                    double x = Double.parseDouble(xzMatcher.group(1));
                    double z = Double.parseDouble(xzMatcher.group(2));

                    if (Math.abs(x) > 10 || Math.abs(z) > 10) {
                        // For XZ only, we guess Y or set to common event height
                        addWaypoint(name, new Vec3d(x, 100, z));
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    private void addWaypoint(String name, Vec3d pos) {
        // Avoid duplicates
        for (Waypoint wp : waypoints) {
            if (wp.pos.distanceTo(pos) < 2) return;
        }
        waypoints.add(new Waypoint(name, pos));
    }

    @EventHandler
    public void onRender3D(WorldRenderEvent e) {
        if (mc.player == null) return;

        waypoints.removeIf(wp -> {
            if (autoRemove.isValue() && mc.player.getPos().distanceTo(wp.pos) < 8) return true;
            return false;
        });

        for (Waypoint wp : waypoints) {
            int color = colorSetting.getColor();
            // Draw a more "premium" looking waypoint
            Render3D.drawBox(new Box(wp.pos.subtract(0.5, 0, 0.5), wp.pos.add(0.5, 255, 0.5)), ColorAssist.multAlpha(color, 0.2f), 1, false, true, false);
            Render3D.drawBox(new Box(wp.pos.subtract(0.5, 0, 0.5), wp.pos.add(0.5, 2, 0.5)), color, 2, true, true, false);
            Render3D.drawLine(wp.pos, wp.pos.add(0, 255, 0), color, 1, false);
        }
    }

    @EventHandler
    public void onDraw(DrawEvent e) {
        DrawContext context = e.getDrawContext();
        FontRenderer font = Fonts.getSize(14, Fonts.Type.BOLD);

        for (Waypoint wp : waypoints) {
            Vector4d vec = Projection.getVector4D(wp.pos.add(0, 2, 0));
            if (Projection.cantSee(vec)) continue;

            float x = (float) Projection.centerX(vec);
            float y = (float) vec.y;

            String text = String.format("%s (%.1fm)", wp.name, mc.player.getPos().distanceTo(wp.pos));
            float width = font.getStringWidth(text);

            Render2D.drawQuad(context.getMatrices(), x - width / 2 - 4, y - 12, width + 8, 14, ColorAssist.getRect(0.8f));
            font.drawCenteredText(context.getMatrices(), text, x, y - 10, -1);

            // Draw a small indicator below the text
            Render2D.drawQuad(context.getMatrices(), x - 1, y + 2, 2, 2, colorSetting.getColor());
        }
    }

    private record Waypoint(String name, Vec3d pos) {}
}
