package fun.rich.display.screens.clickgui.components.implement.module.other;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;
import fun.rich.display.screens.clickgui.MenuScreen;
import fun.rich.display.screens.clickgui.components.AbstractComponent;
import fun.rich.utils.display.font.Fonts;
import fun.rich.utils.display.shape.ShapeProperties;
import fun.rich.utils.math.calc.Calculate;
import java.awt.Color;

@Getter
@Setter
public class SearchComponent extends AbstractComponent {
    private String text = "";
    public static boolean typing;

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();
        width = 60;
        height = 15;

        blur.render(ShapeProperties.create(matrix, x, y, width, height).round(5).quality(12)
                .color(new Color(0, 0, 0, 150).getRGB())
                .build());

        rectangle.render(ShapeProperties.create(matrix, x, y, width, height).round(5)
                .thickness(1.5f)
                .outlineColor(new Color(138, 43, 226, 255).getRGB())
                .color(new Color(15, 15, 15, 180).getRGB())
                .build());

        Fonts.getSize(13, Fonts.Type.ICONS).drawString(matrix, "L", x + 5, y + 6, new Color(225, 225, 255, 200).getRGB());

        String display = text.isEmpty() && !typing ? "Search..." : text;
        Fonts.getSize(12, Fonts.Type.REGULAR).drawString(matrix, display, x + 18, y + 6.5f, new Color(225, 225, 255, 150).getRGB());

        if (typing && System.currentTimeMillis() % 1000 < 500) {
            float tw = Fonts.getSize(12, Fonts.Type.REGULAR).getStringWidth(text);
            rectangle.render(ShapeProperties.create(matrix, x + 18 + tw, y + 4, 0.5f, 7).color(new Color(225, 225, 255, 200).getRGB()).build());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Calculate.isHovered(mouseX, mouseY, x, y, width, height) && button == 0) {
            typing = !typing;
            return true;
        }
        typing = false;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (typing) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !text.isEmpty()) {
                text = text.substring(0, text.length() - 1);
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE) {
                typing = false;
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (typing && text.length() < 12) {
            text += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }
}
