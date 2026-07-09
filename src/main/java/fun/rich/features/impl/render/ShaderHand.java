package fun.rich.features.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import fun.rich.events.render.ItemRendererEvent;
import fun.rich.features.module.Module;
import fun.rich.features.module.ModuleCategory;
import fun.rich.features.module.setting.implement.BooleanSetting;
import fun.rich.features.module.setting.implement.ColorSetting;
import fun.rich.features.module.setting.implement.SliderSettings;
import fun.rich.utils.client.managers.event.EventHandler;
import fun.rich.utils.display.color.ColorAssist;

import java.awt.*;

public class ShaderHand extends Module {

    private final ColorSetting colorSetting = new ColorSetting("Цвет", "Цвет руки").value(new Color(138, 43, 226).getRGB());
    private final SliderSettings alphaSetting = new SliderSettings("Прозрачность", "Настройка прозрачности руки").setValue(150f).range(0f, 255f);
    private final BooleanSetting rainbowSetting = new BooleanSetting("Переливание", "Рука будет переливаться").setValue(false);

    public ShaderHand() {
        super("Shader Hand", "Shader Hand", ModuleCategory.RENDER);
        setup(colorSetting, alphaSetting, rainbowSetting);
    }

    @EventHandler
    public void onItemRender(ItemRendererEvent e) {
        int color = rainbowSetting.isValue() ? ColorAssist.getRainbow(5, 0) : colorSetting.getColor();
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        float a = alphaSetting.getValue() / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(r, g, b, a);
        // NOTE: Hand resetting is handled by Minecraft's HeldItemRenderer after each hand is rendered,
        // but we ensure clean state for transparency.
    }

    @Override
    public void deactivate() {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
