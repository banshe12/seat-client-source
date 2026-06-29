package fun.rich.display.screens.clickgui.components.implement.module.category;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import fun.rich.features.module.Module;
import fun.rich.features.module.ModuleCategory;
import fun.rich.common.animation.Animation;
import fun.rich.common.animation.Direction;
import fun.rich.common.animation.implement.Decelerate;
import fun.rich.utils.display.font.Fonts;
import fun.rich.utils.display.shape.ShapeProperties;
import fun.rich.utils.display.color.ColorAssist;
import fun.rich.utils.math.calc.Calculate;
import fun.rich.utils.display.scissor.ScissorAssist;
import fun.rich.Rich;
import fun.rich.display.screens.clickgui.MenuScreen;
import fun.rich.display.screens.clickgui.components.AbstractComponent;
import fun.rich.display.screens.clickgui.components.implement.module.ModuleComponent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CategoryComponent extends AbstractComponent {
    private final List<ModuleComponent> moduleComponents = new ArrayList<>();
    private static final Set<ModuleComponent> globalModuleComponents = new HashSet<>();
    private final ModuleCategory category;

    private final Animation alphaAnimation = new Decelerate().setMs(300).setValue(1);
    private float scroll = 0;
    private float smoothedScroll = 0;


    private void initialize() {
        List<Module> modules = Rich.getInstance()
                .getModuleRepository()
                .modules();

        for (Module module : modules) {
            ModuleComponent newComponent = new ModuleComponent(module);

            if (globalModuleComponents.add(newComponent)) {
                moduleComponents.add(newComponent);
            }
        }
    }

    public CategoryComponent(ModuleCategory category) {
        this.category = category;
        initialize();
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MenuScreen menuScreen = MenuScreen.INSTANCE;
        globalModuleComponents.clear();
        Matrix4f positionMatrix = context.getMatrices().peek().getPositionMatrix();
        ScissorAssist scissorManager = Rich.getInstance().getScissorManager();

        drawCategoryTab(context, context.getMatrices());

        int[] offsets = calculateOffsets();
        int columnWidth = 137;
        int column = 0;
        int maxScroll = 0;
        float offsetX = 84, offsetY = 29;
        scissorManager.push(positionMatrix, menuScreen.x + offsetX, menuScreen.y + offsetY, menuScreen.width - offsetX, menuScreen.height - offsetY - 1);
        for (int i = moduleComponents.size() - 1; i >= 0; i--) {
            ModuleComponent component = moduleComponents.get(i);

            if (shouldRenderComponent(component)) {
                int componentHeight = component.getComponentHeight() + 9;

                component.x = menuScreen.x + 95 + (column * (columnWidth + 10));
                component.y = (float) (menuScreen.y + 39 + offsets[column] - componentHeight + smoothedScroll);
                component.width = columnWidth;

                if (component.y > menuScreen.y - componentHeight && menuScreen.y + menuScreen.height + 5 > component.y) {
                    component.render(context, mouseX, mouseY, delta);
                }

                offsets[column] -= componentHeight;
                maxScroll = Math.max(maxScroll, offsets[column]);

                column = (column + 1) % 2;
            }
        }
        scissorManager.pop();
        int clamped = MathHelper.clamp(maxScroll - (menuScreen.height / 2 - 80), 0, maxScroll);
        scroll = MathHelper.clamp(scroll, -clamped, 0);
        smoothedScroll = Calculate.interpolateSmooth(2, smoothedScroll, scroll);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        MenuScreen menuScreen = MenuScreen.INSTANCE;
        if (Calculate.isHovered(mouseX, mouseY, x, y, width, height) && button == 0) {
            MenuScreen.INSTANCE.setCategory(category);
            // Закрываем cosmetics при смене категории
            MenuScreen.INSTANCE.setCosmeticsOpen(false);
        }

        float offsetX = 84, offsetY = 29;
        if (Calculate.isHovered(mouseX, mouseY, menuScreen.x + offsetX, menuScreen.y + offsetY, menuScreen.width - offsetX, menuScreen.height - offsetY)) {
            boolean isAnyComponentHovered = moduleComponents.stream()
                    .anyMatch(moduleComponent -> moduleComponent.isHover(mouseX, mouseY));

            if (isAnyComponentHovered) {
                moduleComponents.forEach(moduleComponent -> {
                    if (shouldRenderComponent(moduleComponent) && moduleComponent.isHover(mouseX, mouseY)) {
                        moduleComponent.mouseClicked(mouseX, mouseY, button);
                    }
                });
                return super.mouseClicked(mouseX, mouseY, button);
            }

        }
        return super.mouseClicked(mouseX, mouseY, button);
    }


    @Override
    public boolean isHover(double mouseX, double mouseY) {
        moduleComponents.forEach(moduleComponent -> moduleComponent.isHover(mouseX, mouseY));

        for (ModuleComponent moduleComponent : moduleComponents) {
            if (moduleComponent.isHover(mouseX, mouseY)) {
                return true;
            }
        }
        return super.isHover(mouseX, mouseY);
    }


    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        moduleComponents.forEach(moduleComponent -> moduleComponent.mouseReleased(mouseX, mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        MenuScreen menuScreen = MenuScreen.INSTANCE;
        float offsetX = 84, offsetY = 29;
        boolean overPanel = Calculate.isHovered(mouseX, mouseY, menuScreen.x + offsetX, menuScreen.y + offsetY, menuScreen.width - offsetX, menuScreen.height - offsetY);
        // When search is active, allow scrolling from anywhere in the GUI
        boolean searchActive = !menuScreen.getSearchText().isEmpty();
        boolean overGui = Calculate.isHovered(mouseX, mouseY, menuScreen.x, menuScreen.y, menuScreen.width, menuScreen.height);

        if (overPanel || (searchActive && overGui)) {
            scroll += (float) (amount * 20);
        }

        moduleComponents.forEach(moduleComponent -> {
            if (shouldRenderComponent(moduleComponent)) {
                moduleComponent.mouseScrolled(mouseX, mouseY, amount);
            }
        });
        return super.mouseScrolled(mouseX, mouseY, amount);
    }


    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        moduleComponents.forEach(moduleComponent -> {
            if (shouldRenderComponent(moduleComponent)) {
                moduleComponent.keyPressed(keyCode, scanCode, modifiers);
            }
        });
        return super.keyPressed(keyCode, scanCode, modifiers);
    }


    @Override
    public boolean charTyped(char chr, int modifiers) {
        moduleComponents.forEach(moduleComponent -> {
            if (shouldRenderComponent(moduleComponent)) {
                moduleComponent.charTyped(chr, modifiers);
            }
        });
        return super.charTyped(chr, modifiers);
    }


    private void drawCategoryTab(DrawContext context, MatrixStack matrix) {
        boolean selected = MenuScreen.INSTANCE.getCategory().equals(category) && !MenuScreen.INSTANCE.isCosmeticsOpen();
        alphaAnimation.setDirection(selected ? Direction.FORWARDS : Direction.BACKWARDS);

        float anim = alphaAnimation.getOutput().floatValue();
        int selectColor = selected ? ColorAssist.getClientColor() : ColorAssist.getText();

        if (anim != 0) {
            int outlineColor = selected ? ColorAssist.getClientColor() : ColorAssist.getClientColor(anim);
            int bgColor = selected ? ColorAssist.applyOpacity(ColorAssist.getClientColor(), 40)
                    : ColorAssist.getGuiRectColor2(anim / 2);
            rectangle.render(ShapeProperties.create(matrix, x, y, width, height).round(2.25F).thickness(2)
                    .outlineColor(outlineColor).color(bgColor).build());
        }

        String texture = "textures/gui/categories/" + category.getTextureName() + ".png";
        image.setTexture(texture).render(ShapeProperties.create(matrix, x + 7, y + 4.5F, 8, 8).color(selectColor).build());

        Fonts.getSize(14, Fonts.Type.BOLD).drawString(context.getMatrices(), category.getReadableName(), (int) (x + 22), y + 7, selectColor);
    }


    private int[] calculateOffsets() {
        int[] offsets = new int[2];
        int column = 0;
        for (int i = moduleComponents.size() - 1; i >= 0; i--) {
            ModuleComponent component = moduleComponents.get(i);
            if (shouldRenderComponent(component)) {
                int componentHeight = component.getComponentHeight() + 9;
                offsets[column] += componentHeight;
                column = (column + 1) % 2;
            }
        }
        return offsets;
    }

    private boolean shouldRenderComponent(ModuleComponent component) {
        MenuScreen menuScreen = MenuScreen.INSTANCE;
        ModuleCategory moduleCategory = component.getModule().getCategory();
        String text = menuScreen.getSearchText().toLowerCase();
        String moduleName = component.getModule().getVisibleName().toLowerCase();
        return (text.equalsIgnoreCase("") ? moduleCategory.equals(menuScreen.getCategory()) : moduleName.contains(text));
    }
}
