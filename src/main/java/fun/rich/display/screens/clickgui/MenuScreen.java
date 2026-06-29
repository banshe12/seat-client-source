package fun.rich.display.screens.clickgui;

import fun.rich.utils.display.color.ColorAssist;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import fun.rich.features.module.ModuleCategory;
import fun.rich.common.animation.Animation;
import fun.rich.common.animation.Direction;
import fun.rich.common.animation.implement.Decelerate;
import fun.rich.utils.display.shape.ShapeProperties;
import fun.rich.utils.client.sound.SoundManager;
import fun.rich.utils.display.interfaces.QuickImports;
import fun.rich.utils.math.calc.Calculate;
import fun.rich.utils.client.chat.StringHelper;
import fun.rich.utils.display.font.Fonts;
import fun.rich.commands.defaults.BindCommand;
import fun.rich.display.screens.clickgui.components.AbstractComponent;
import fun.rich.display.screens.clickgui.components.implement.other.*;
import fun.rich.display.screens.clickgui.components.implement.settings.TextComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static fun.rich.common.animation.Direction.BACKWARDS;
import static fun.rich.common.animation.Direction.FORWARDS;

@Setter
@Getter
public class MenuScreen extends Screen implements QuickImports {
    public static MenuScreen INSTANCE = new MenuScreen();
    private final List<AbstractComponent> components = new ArrayList<>();
    private final BackgroundComponent backgroundComponent = new BackgroundComponent();
    private final UserComponent userComponent = new UserComponent();
    private final SearchComponent searchComponent = new SearchComponent();
    private final CategoryContainerComponent categoryContainerComponent = new CategoryContainerComponent();
    // private final kronex.fun.display.screens.clickgui.components.implement.other.CosmeticsButton cosmeticsButton = new kronex.fun.display.screens.clickgui.components.implement.other.CosmeticsButton();
    // private final ThemeComponent themeComponent = new ThemeComponent();
    public final Animation animation = new Decelerate().setMs(200).setValue(1);
    public ModuleCategory category = ModuleCategory.COMBAT;
    public int x, y, width, height;
    private String hoveredModuleDesc = null;

    // Cosmetics panel
    @Getter private boolean cosmeticsOpen = false;
    // @Getter private final CosmeticsPanel cosmeticsPanel = new CosmeticsPanel();

    public void setCosmeticsOpen(boolean open) {
        this.cosmeticsOpen = open;
    }

    public void setHoveredModuleDesc(String desc) {
        this.hoveredModuleDesc = desc;
    }

    public String getSearchText() {
        if (searchComponent == null) {
            return "";
        }
        try {
            Object value = searchComponent.getClass().getMethod("getText").invoke(searchComponent);
            return value instanceof String string ? string : "";
        } catch (Throwable ignored) {
            return "";
        }
    }

    public void initialize() {
        animation.setDirection(FORWARDS);
        categoryContainerComponent.initializeCategoryComponents();
        components.addAll(Arrays.asList(backgroundComponent, userComponent, searchComponent, categoryContainerComponent));
    }

    public MenuScreen() {
        super(Text.of("MenuScreen"));
        initialize();
    }

    @Override
    public void tick() {
        close();
        components.forEach(AbstractComponent::tick);
        super.tick();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        x = window.getScaledWidth() / 2 - 200;
        y = window.getScaledHeight() / 2 - 125;
        width = 400;
        height = 250;

        rectangle.render(ShapeProperties.create(context.getMatrices(), 0, 0, window.getScaledWidth(), window.getScaledHeight())
                .color(ColorAssist.applyOpacity(0xFF000000, 100 * getScaleAnimation())).build());

        backgroundComponent.position(x, y).size(width, height);
        userComponent.position(x, y + height);

        // Кнопка Cosmetics над профилем
        // cosmeticsButton.x = x + 6;
        // cosmeticsButton.y = y + height - 55;
        // cosmeticsButton.width = 73;
        // cosmeticsButton.height = 17;

        searchComponent.position(x + 300, y + 6);
        categoryContainerComponent.position(x, y);

        Calculate.scale(context.getMatrices(), x + (float) width / 2, y + (float) height / 2, getScaleAnimation(), () -> {
            // Рендерим фон, юзера, категории всегда
            backgroundComponent.render(context, mouseX, mouseY, delta);
            userComponent.render(context, mouseX, mouseY, delta);
            categoryContainerComponent.render(context, mouseX, mouseY, delta);
            // Поиск только когда cosmetics закрыта
            if (!cosmeticsOpen) {
                searchComponent.render(context, mouseX, mouseY, delta);
            }
            // cosmeticsButton.render(context, mouseX, mouseY, delta);
            // Рендерим cosmetics панель вместо модулей если открыта
            if (cosmeticsOpen) {
                // cosmeticsPanel.render(context, mouseX, mouseY, delta);
            } else if (category == ModuleCategory.THEMES) {
                // themeComponent.position(x + 94, y + 38).size(width - 100, height - 48);
                // themeComponent.render(context, mouseX, mouseY, delta);
            }
            drawClickGuiBind(context);
            windowManager.render(context, mouseX, mouseY, delta);
        });
        super.render(context, mouseX, mouseY, delta);
    }

    public void openGui() {
        animation.setDirection(Direction.FORWARDS);
        mc.setScreen(this);
        SoundManager.playSound(SoundManager.OPEN_GUI);
    }

    public float getScaleAnimation() {
        return animation.getOutput().floatValue();
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Клик по виджету бинда ClickGUI
        if (Calculate.isHovered(mouseX, mouseY, bindBx, bindBy, bindBw, bindBh)) {
            if (button == 0) {
                bindingGui = !bindingGui;
                return true;
            }
        } else if (bindingGui && button != 0) {
            // Правая/средняя кнопка мыши как бинд
            BindCommand.ClickGuiManager.setClickGuiKey(button);
            bindingGui = false;
            return true;
        } else if (bindingGui) {
            bindingGui = false;
        }

        if (!windowManager.mouseClicked(mouseX, mouseY, button)) {
            // cosmeticsButton.mouseClicked(mouseX, mouseY, button);
            if (cosmeticsOpen) {
                // cosmeticsPanel.mouseClicked(mouseX, mouseY, button);
                // Клик по категории закрывает cosmetics
                components.forEach(component -> component.mouseClicked(mouseX, mouseY, button));
            } else if (category == ModuleCategory.THEMES /* && themeComponent.mouseClicked(mouseX, mouseY, button) */) {
                return true;
            } else {
                components.forEach(component -> component.mouseClicked(mouseX, mouseY, button));
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }


    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        components.forEach(component -> component.mouseReleased(mouseX, mouseY, button));
        windowManager.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!windowManager.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            components.forEach(component -> component.mouseDragged(mouseX, mouseY, button, deltaX, deltaY));
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (cosmeticsOpen /* && cosmeticsPanel.mouseScrolled(mouseX, mouseY, vertical) */) {
            return true;
        }
        if (!windowManager.mouseScrolled(mouseX, mouseY, vertical)) {
            components.forEach(component -> component.mouseScrolled(mouseX, mouseY, vertical));
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }


    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Если ждём бинд для ClickGUI
        if (bindingGui) {
            if (keyCode == 256) { // ESC — отмена
                bindingGui = false;
            } else {
                int key = keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_DELETE ? -1 : keyCode;
                BindCommand.ClickGuiManager.setClickGuiKey(key);
                bindingGui = false;
            }
            return true;
        }

        if (keyCode == 256 && shouldCloseOnEsc()) {
            SoundManager.playSound(SoundManager.CLOSE_GUI);
            animation.setDirection(BACKWARDS);
            return true;
        }

        if (!windowManager.keyPressed(keyCode, scanCode, modifiers)) {
            components.forEach(component -> component.keyPressed(keyCode, scanCode, modifiers));
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }


    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!windowManager.charTyped(chr, modifiers)) {
            components.forEach(component -> component.charTyped(chr, modifiers));
        }
        return super.charTyped(chr, modifiers);
    }


    @Override
    public boolean shouldPause() {
        return false;
    }


    @Override
    public void close() {
        if (animation.isFinished(BACKWARDS)) {
            TextComponent.typing = false;
            super.close();
        }
    }

    // --- ClickGUI bind widget ---
    private boolean bindingGui = false;
    private float bindBx, bindBy, bindBw, bindBh;

    private void drawClickGuiBind(DrawContext context) {
        String bindName = bindingGui ? "..." : StringHelper.getBindName(BindCommand.ClickGuiManager.getClickGuiKey());

        float textW = Fonts.getSize(12, Fonts.Type.BOLD).getStringWidth(bindName);
        float totalW = textW + 14f;
        float bh = 15f;
        float bx = x + 298 - totalW - 4;
        float by = y + 6;

        bindBx = bx; bindBy = by; bindBw = totalW; bindBh = bh;

        rectangle.render(ShapeProperties.create(context.getMatrices(), bx, by, totalW, bh)
                .round(2.5F).thickness(2).softness(0.5F)
                .outlineColor(ColorAssist.getOutline())
                .color(ColorAssist.getGuiRectColor(0.5F)).build());

        float textY = by + 1f;
        Fonts.getSize(12, Fonts.Type.BOLD).drawString(context.getMatrices(), bindName,
                bx + 7, by + 5.5f, 0xFFFFFFFF);
    }
}
