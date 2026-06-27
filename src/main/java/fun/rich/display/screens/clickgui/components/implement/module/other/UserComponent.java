package fun.rich.display.screens.clickgui.components.implement.module.other;

import antidaunleak.api.UserProfile;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import fun.rich.display.screens.clickgui.components.AbstractComponent;
import fun.rich.utils.display.font.Fonts;
import fun.rich.utils.display.shape.ShapeProperties;
import fun.rich.utils.display.geometry.Render2D;
import java.awt.Color;

public class UserComponent extends AbstractComponent {
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();
        String username = UserProfile.getInstance().profile("username");
        String uid = "UID: " + UserProfile.getInstance().profile("uid");

        blur.render(ShapeProperties.create(matrix, x + 10, y + 10, 80, 20).round(5).quality(12)
                .color(new Color(0, 0, 0, 150).getRGB())
                .build());

        rectangle.render(ShapeProperties.create(matrix, x + 10, y + 10, 80, 20).round(5)
                .thickness(1.5f)
                .outlineColor(new Color(138, 43, 226, 255).getRGB())
                .color(new Color(15, 15, 15, 180).getRGB())
                .build());

        Fonts.getSize(13, Fonts.Type.BOLD).drawString(matrix, username, x + 35, y + 15, -1);
        Fonts.getSize(11, Fonts.Type.REGULAR).drawString(matrix, uid, x + 35, y + 23, new Color(200, 200, 200).getRGB());
    }
}
