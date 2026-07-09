package fun.rich.features.impl.render;

import fun.rich.events.render.ItemRendererEvent;
import fun.rich.features.module.Module;
import fun.rich.features.module.ModuleCategory;
import fun.rich.features.module.setting.implement.BooleanSetting;
import fun.rich.features.module.setting.implement.ColorSetting;
import fun.rich.features.module.setting.implement.SliderSettings;
import fun.rich.utils.client.managers.event.EventHandler;
import fun.rich.utils.display.color.ColorAssist;
import fun.rich.utils.display.shape.ShapeProperties;
import fun.rich.utils.display.shape.implement.Blur;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.ColorHelper;

import java.awt.*;

public class ShaderHand extends Module {

    private final ColorSetting colorSetting = new ColorSetting("Цвет", "Цвет руки", new Color(138, 43, 226).getRGB());
    private final SliderSettings alphaSetting = new SliderSettings("Прозрачность", "Альфа-канал руки", 0.5f).range(0f, 1f);
    public final SliderSettings blurSetting = new SliderSettings("Блюр", "Интенсивность размытия", 0f).range(0f, 10f);
    private final BooleanSetting rainbow = new BooleanSetting("Радуга", "Переливание цветов", false);
    private final SliderSettings rainbowSpeed = new SliderSettings("Скорость радуги", "Скорость переливания", 5f).range(1f, 20f).visible(rainbow::isValue);

    public ShaderHand() {
        super("ShaderHand", "Shader Hand", ModuleCategory.RENDER);
        setup(colorSetting, alphaSetting, blurSetting, rainbow, rainbowSpeed);
    }

    @EventHandler
    public void onItemRender(ItemRendererEvent e) {
        // Blur implementation would ideally be handled in a Mixin that wraps the hand rendering
        // with a shader or post-processing effect.
    }

    public int getColor() {
        int color = rainbow.isValue() ? ColorAssist.astolfo(rainbowSpeed.getInt() * 100, 0, 0.5f, 1f, alphaSetting.getValue()) : ColorAssist.multAlpha(colorSetting.getColor(), alphaSetting.getValue());
        return color;
    }
}
