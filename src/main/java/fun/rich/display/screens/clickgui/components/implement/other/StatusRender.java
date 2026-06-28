package fun.rich.display.screens.clickgui.components.implement.other;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import fun.rich.common.animation.Animation;
import fun.rich.common.animation.implement.Decelerate;
import fun.rich.utils.display.shape.ShapeProperties;
import fun.rich.display.screens.clickgui.components.AbstractComponent;
import fun.rich.utils.math.calc.Calculate;
import java.awt.Color;

import static fun.rich.common.animation.Direction.BACKWARDS;
import static fun.rich.common.animation.Direction.FORWARDS;

@Setter
@Accessors(chain = true)
public class StatusRender extends AbstractComponent {
    private boolean state;
    private Runnable runnable;
    private float alphaMultiplier = 1.0f;
    private final Animation alphaAnimation = new Decelerate().setMs(400).setValue(100);
    private final Animation stencilAnimation = new Decelerate().setMs(200).setValue(8);
    private final Animation sliderAnimation = new Decelerate().setMs(225).setValue(8);

    public StatusRender() {
        alphaAnimation.setDirection(state ? FORWARDS : BACKWARDS);
        stencilAnimation.setDirection(state ? FORWARDS : BACKWARDS);
        sliderAnimation.setDirection(state ? FORWARDS : BACKWARDS);
        alphaAnimation.reset();
        stencilAnimation.reset();
        sliderAnimation.reset();
    }

    @Override
    public StatusRender position(float x, float y) {
        this.x = x - 8;
        this.y = y;
        return this;
    }

    public StatusRender setAlphaMultiplier(float alphaMultiplier) {
        this.alphaMultiplier = alphaMultiplier;
        return this;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();
        alphaAnimation.setDirection(state ? FORWARDS : BACKWARDS);
        stencilAnimation.setDirection(state ? FORWARDS : BACKWARDS);
        sliderAnimation.setDirection(state ? FORWARDS : BACKWARDS);
        int stateColor = new Color(138, 43, 226, 255).getRGB();
        int opacity = (int) (alphaAnimation.getOutput().intValue() * alphaMultiplier);
        float sliderX = x + sliderAnimation.getOutput().floatValue();

        int baseAlpha = (int) (255 * alphaMultiplier);
        int bgAlpha = (int) (40 * alphaMultiplier);

        rectangle.render(ShapeProperties.create(matrix, x, y, 16, 8)
                .round(4).thickness(0.5f).softness(1)
                .outlineColor(new Color(138, 43, 226, 100).getRGB())
                .color(new Color(25, 25, 25, bgAlpha).getRGB())
                .build());
        rectangle.render(ShapeProperties.create(matrix, x, y, 16, 8)
                .round(4).thickness(0).softness(1)
                .color(Calculate.applyOpacity(stateColor, opacity))
                .build());
        rectangle.render(ShapeProperties.create(matrix, sliderX - 0.5f, y - 0.5f, 9, 9)
                .round(4.5f).thickness(1.0f).softness(1)
                .outlineColor(new Color(138, 43, 226, baseAlpha).getRGB())
                .color(new Color(255, 255, 255, baseAlpha).getRGB())
                .build());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Calculate.isHovered(mouseX, mouseY, x, y, 16, 8) && button == 0) {
            state = !state;
            runnable.run();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}