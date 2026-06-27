package fun.rich.display.hud;

import antidaunleak.api.UserProfile;
import com.google.common.base.Suppliers;
import fun.rich.utils.client.managers.api.draggable.AbstractDraggable;
import fun.rich.utils.display.atlasfont.msdf.MsdfFont;
import fun.rich.utils.display.font.Fonts;
import fun.rich.utils.display.shape.ShapeProperties;
import fun.rich.utils.display.systemrender.builders.Builder;
import fun.rich.Rich;
import fun.rich.utils.display.color.ColorAssist;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import java.awt.Color;
import java.util.function.Supplier;

public class Watermark extends AbstractDraggable {
    private int fpsCount = 0;
    private static final Supplier<MsdfFont> ICONS_FONT = Suppliers.memoize(() -> MsdfFont.builder().atlas("icons").data("icons").build());
    private static final Supplier<MsdfFont> ICONS_FONT_1 = Suppliers.memoize(() -> MsdfFont.builder().atlas("clienticon1").data("clienticon1").build());
    private static final Supplier<MsdfFont> BOLD_FONT = Suppliers.memoize(() -> MsdfFont.builder().atlas("medium").data("medium").build());
    private static final Supplier<MsdfFont> ICONS = Suppliers.memoize(() -> MsdfFont.builder().atlas("medium").data("medium").build());

    public Watermark() {
        super("Watermark", 10, 10, 92, 16, true);
    }

    @Override
    public void tick() {
        fpsCount = mc.getCurrentFps();
    }

    @Override
    public void drawDraggable(DrawContext e) {
        MatrixStack matrix = e.getMatrices();
        Matrix4f matrix4f = matrix.peek().getPositionMatrix();
        String offset = "";
        String name = Rich.getInstance().getClientInfoProvider().clientName() + offset;
        String icon = "A ";
        String point = " • ";
        String username = UserProfile.getInstance().profile("username");
        String fps = String.valueOf(fpsCount);
        String serverIp = mc.getCurrentServerEntry() != null ? mc.getCurrentServerEntry().address : "Singleplayer";
        
        String title = "neverlose";
        String serverIpText = mc.getCurrentServerEntry() != null ? mc.getCurrentServerEntry().address : "Singleplayer";
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("HH:mm:ss");
        String timeText = formatter.format(new java.util.Date());
        String userText = username != null ? username : "username";
        String fpsText = fpsCount + " fps";

        float iconWidth = Fonts.getSize(17, Fonts.Type.ICONSTYPENEW).getStringWidth("w") + 2;
        float titleWidth = Fonts.getSize(13, Fonts.Type.DEFAULT).getStringWidth(title);
        float serverIpWidth = Fonts.getSize(13, Fonts.Type.DEFAULT).getStringWidth(serverIpText);
        float timeWidth = Fonts.getSize(13, Fonts.Type.DEFAULT).getStringWidth(timeText);
        float userWidth = Fonts.getSize(13, Fonts.Type.DEFAULT).getStringWidth(userText);
        float fpsWidth = Fonts.getSize(13, Fonts.Type.DEFAULT).getStringWidth(fpsText);
        
        float spacing = 8f;
        float totalWidth = iconWidth + titleWidth + serverIpWidth + timeWidth + userWidth + fpsWidth + (spacing * 5) + 20;

        setWidth((int) totalWidth);

        blur.render(ShapeProperties.create(matrix, getX(), getY(), getWidth(), getHeight() + 4)
                .round(10f).quality(12)
                .color(new Color(0, 0, 0, 150).getRGB())
                .build());

        rectangle.render(ShapeProperties.create(matrix, getX(), getY(), getWidth(), getHeight() + 4)
                .round(10f)
                .thickness(1.5f)
                .outlineColor(new Color(138, 43, 226, 255).getRGB())
                .color(
                        new Color(15, 15, 15, 180).getRGB(),
                        new Color(15, 15, 15, 180).getRGB(),
                        new Color(15, 15, 15, 180).getRGB(),
                        new Color(15, 15, 15, 180).getRGB())
                .build());

        float currentX = getX() + 10f;
        
        Fonts.getSize(17, Fonts.Type.ICONSTYPENEW).drawString(matrix, "w", currentX, getY() + 9f, new Color(225, 225, 255, 255).getRGB());
        currentX += iconWidth;

        Fonts.getSize(13, Fonts.Type.DEFAULT).drawString(matrix, title, currentX, getY() + 9f, new Color(255, 255, 255, 255).getRGB());
        currentX += titleWidth + spacing;
        
        Fonts.getSize(13, Fonts.Type.DEFAULT).drawString(matrix, serverIpText, currentX, getY() + 9f, new Color(255, 255, 255, 255).getRGB());
        currentX += serverIpWidth + spacing;
        
        Fonts.getSize(13, Fonts.Type.DEFAULT).drawString(matrix, timeText, currentX, getY() + 9f, new Color(255, 255, 255, 255).getRGB());
        currentX += timeWidth + spacing;
        
        Fonts.getSize(13, Fonts.Type.DEFAULT).drawString(matrix, userText, currentX, getY() + 9f, new Color(255, 255, 255, 255).getRGB());
        currentX += userWidth + spacing;
        
        Fonts.getSize(13, Fonts.Type.DEFAULT).drawString(matrix, fpsText, currentX, getY() + 9f, new Color(255, 255, 255, 255).getRGB());
    }
}