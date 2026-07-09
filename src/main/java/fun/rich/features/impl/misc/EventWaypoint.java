package fun.rich.features.impl.misc;

import fun.rich.events.chat.ChatEvent;
import fun.rich.events.render.WorldRenderEvent;
import fun.rich.features.module.Module;
import fun.rich.features.module.ModuleCategory;
import fun.rich.utils.client.managers.event.EventHandler;
import fun.rich.utils.display.color.ColorAssist;
import fun.rich.utils.display.geometry.Render3D;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventWaypoint extends Module {

    private Vec3d eventPos = null;
    private final Pattern coordPattern = Pattern.compile("(?:[Xx]:?\\s*)?(-?\\d+)[,\\s]+(?:[Yy]:?\\s*)?(-?\\d+)[,\\s]+(?:[Zz]:?\\s*)?(-?\\d+)");

    public EventWaypoint() {
        super("EventWaypoint", "Event Waypoint", ModuleCategory.MISC);
    }

    @EventHandler
    public void onChat(ChatEvent e) {
        String message = e.getMessage();
        Matcher matcher = coordPattern.matcher(message);
        if (matcher.find()) {
            try {
                double x = Double.parseDouble(matcher.group(1));
                double y = Double.parseDouble(matcher.group(2));
                double z = Double.parseDouble(matcher.group(3));
                eventPos = new Vec3d(x, y, z);
            } catch (NumberFormatException ignored) {
            }
        }
    }

    @EventHandler
    public void onRender3D(WorldRenderEvent e) {
        if (eventPos != null) {
            int color = ColorAssist.getRainbow(5, 0);
            Render3D.drawBox(new Box(eventPos.add(-0.5, 0, -0.5), eventPos.add(0.5, 2, 0.5)), color, 2.0f);
            Render3D.drawLine(eventPos.add(0, 0, 0), eventPos.add(0, 255, 0), color, 2.0f, false);
        }
    }

    @Override
    public void deactivate() {
        eventPos = null;
    }
}
