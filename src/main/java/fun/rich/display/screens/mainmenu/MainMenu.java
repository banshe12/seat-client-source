package fun.rich.display.screens.mainmenu;

import fun.rich.common.animation.Direction;
import fun.rich.display.screens.mainmenu.altscreen.AltScreen;
import fun.rich.utils.display.color.ColorAssist;
import fun.rich.utils.display.font.Fonts;
import fun.rich.utils.display.geometry.Render2D;
import fun.rich.utils.display.interfaces.QuickImports;
import fun.rich.utils.display.shape.ShapeProperties;
import fun.rich.common.animation.implement.Decelerate;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainMenu extends Screen implements QuickImports {
    public static MainMenu INSTANCE = new MainMenu();
    public int width, height;

    private static final int PARTICLE_COUNT = 80;
    private final List<Particle> particles = new ArrayList<>();
    private boolean particlesInitialized = false;
    private int lastWindowWidth = 0;
    private int lastWindowHeight = 0;

    private enum View { MAIN_MENU, ALT_SCREEN }
    private View currentView = View.MAIN_MENU;
    private AltScreen altScreen;

    private float[] buttonHoverProgress = new float[5];
    private long lastRenderTime = 0L;
    private boolean initialized = false;

    private final Color TEXT_COLOR_BRIGHT = new Color(255, 255, 255);
    private final Color FOOTER_COLOR = new Color(150, 150, 150);

    private static class Particle {
        float x, y, vx, vy, size, alpha;
        boolean isDead = false;

        Particle(float x, float y) {
            this.x = x;
            this.y = y;
            Random rand = new Random();
            this.vx = (rand.nextFloat() - 0.5f) * 0.15f;
            this.vy = (rand.nextFloat() - 0.5f) * 0.15f;
            this.size = 0.5f + rand.nextFloat() * 1.0f;
            this.alpha = 0.1f + rand.nextFloat() * 0.25f;
        }

        void update(float delta, int width, int height) {
            x += vx * delta;
            y += vy * delta;
            if (x < -10 || x > width + 10 || y < -10 || y > height + 10) isDead = true;
        }
    }

    public MainMenu() {
        super(Text.of("MainMenu"));
        for (int i = 0; i < 5; i++) buttonHoverProgress[i] = 0f;
    }

    @Override
    protected void init() {
        initialized = false;
        particlesInitialized = false;
    }

    @Override
    public void tick() {
        super.tick();
        if (altScreen != null && currentView == View.ALT_SCREEN) {
            altScreen.tick();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        mc.options.getGuiScale().setValue(2);
        width = window.getScaledWidth();
        height = window.getScaledHeight();

        long currentTime = System.currentTimeMillis();
        if (!initialized) {
            lastRenderTime = currentTime;
            initialized = true;
        }
        float deltaTime = Math.min((currentTime - lastRenderTime) / 1000f, 0.05f) * 60f;
        lastRenderTime = currentTime;

        if (lastWindowWidth != width || lastWindowHeight != height) {
            particles.clear();
            particlesInitialized = false;
            lastWindowWidth = width;
            lastWindowHeight = height;
        }

        for (Particle p : particles) p.update(deltaTime, width, height);
        particles.removeIf(p -> p.isDead);
        Random rand = new Random();
        while (particles.size() < PARTICLE_COUNT) {
            particles.add(new Particle(rand.nextFloat() * width, rand.nextFloat() * height));
        }

        updateButtonAnimations(deltaTime, mouseX, mouseY);

        // 1. Отрисовка фона
        image.setTexture("textures/mainmenu/background.png").render(
                ShapeProperties.create(context.getMatrices(), 0, 0, width, height).color(-1).build()
        );

        // 2. Партиклы
        for (Particle p : particles) {
            int alpha = (int) (p.alpha * 255);
            rectangle.render(ShapeProperties.create(context.getMatrices(), p.x, p.y, p.size * 2, p.size * 2)
                    .color(new Color(255, 255, 255, alpha).getRGB()).build());
        }

        // 3. Выбор экрана
        if (currentView == View.MAIN_MENU) {
            renderCentralMenu(context, mouseX, mouseY);
            renderFooter(context);
        } else {
            renderAltScreenWrapper(context, mouseX, mouseY, delta);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderCentralMenu(DrawContext context, int mouseX, int mouseY) {
        float mw = 200, mh = 260;
        float mx = (width - mw) / 2f;
        float my = (height - mh) / 2f;

        // Эффект стекла
        rectangle.render(ShapeProperties.create(context.getMatrices(), mx, my, mw, mh).round(12)
                .color(new Color(15, 15, 18, 140).getRGB()).build());

        float logoSize = 46;
        float logoX = mx + (mw - logoSize) / 2f;
        float logoY = my + 15;
        image.setTexture("textures/mainmenu/logo.png").render(
                ShapeProperties.create(context.getMatrices(), logoX, logoY, logoSize, logoSize).color(-1).build()
        );

        float bw = mw - 30;
        float bh = 25;
        float bx = mx + 15;
        float by = my + 75;
        float space = 6;

        drawButton(context, bx, by, bw, bh, "Одиночная игра", 0);
        drawButton(context, bx, by + (bh + space), bw, bh, "Мультиплеер", 1);
        drawButton(context, bx, by + (bh + space) * 2, bw, bh, "Настройки", 2);
        drawButton(context, bx, by + (bh + space) * 3, bw, bh, "Аккаунты", 3);
        drawButton(context, bx, by + (bh + space) * 4, bw, bh, "Выход", 4);
    }

    private void renderAltScreenWrapper(DrawContext context, int mouseX, int mouseY, float delta) {
        float centerX = width / 2f - 80;
        float centerY = height / 2f - 105;

        if (altScreen == null) {
            altScreen = new AltScreen(centerX, centerY);
        } else {
            altScreen.updatePosition(centerX, centerY);
        }

        // ИСПРАВЛЕНИЕ: Цвета подогнаны под эффект стекла из renderCentralMenu
        Color bgColor = new Color(15, 15, 18, 140); // Тот самый цвет прозрачного центрального блока
        Color buttonColor = new Color(30, 30, 35, 80); // Цвет для внутренних плашек/аккаунтов
        Color outlineColor = new Color(200, 200, 200, 40); // Цвет тонкой обводки
        Color gradientColor = new Color(15, 15, 18, 140);
        Color textColor = new Color(255, 255, 255, 255);

        altScreen.render(context, buttonColor, outlineColor, gradientColor, textColor, bgColor);
    }

    private void renderFooter(DrawContext context) {
        String t1 = "Войдя в свою учетную запись, вы соглашаетесь со всеми нашими политиками, ";
        String t2 = "включая нашу ";
        String t3 = "Политику конфиденциальности";
        String t4 = " и ";
        String t5 = "Условия использования";

        Fonts.getSize(11, Fonts.Type.DEFAULT).drawCenteredString(context.getMatrices(), t1, width / 2f, height - 25, FOOTER_COLOR.getRGB());

        float w2 = Fonts.getSize(11, Fonts.Type.DEFAULT).getStringWidth(t2);
        float w3 = Fonts.getSize(11, Fonts.Type.DEFAULT).getStringWidth(t3);
        float w4 = Fonts.getSize(11, Fonts.Type.DEFAULT).getStringWidth(t4);
        float w5 = Fonts.getSize(11, Fonts.Type.DEFAULT).getStringWidth(t5);

        float startX = (width - (w2 + w3 + w4 + w5)) / 2f;
        float y = height - 15;

        Fonts.getSize(11, Fonts.Type.DEFAULT).drawString(context.getMatrices(), t2, startX, y, FOOTER_COLOR.getRGB());
        Fonts.getSize(11, Fonts.Type.DEFAULT).drawString(context.getMatrices(), t3, startX + w2, y, TEXT_COLOR_BRIGHT.getRGB());
        Fonts.getSize(11, Fonts.Type.DEFAULT).drawString(context.getMatrices(), t4, startX + w2 + w3, y, FOOTER_COLOR.getRGB());
        Fonts.getSize(11, Fonts.Type.DEFAULT).drawString(context.getMatrices(), t5, startX + w2 + w3 + w4, y, TEXT_COLOR_BRIGHT.getRGB());
    }

    private void drawButton(DrawContext ctx, float x, float y, float w, float h, String text, int index) {
        float hover = buttonHoverProgress[index];

        int alphaBg = (int) (30 + (hover * 40));
        rectangle.render(ShapeProperties.create(ctx.getMatrices(), x, y, w, h).round(6)
                .color(new Color(30, 30, 35, alphaBg).getRGB()).build());

        int alphaBorder = (int) (40 + (hover * 80));
        rectangle.render(ShapeProperties.create(ctx.getMatrices(), x, y, w, h).thickness(1f).round(6)
                .outlineColor(new Color(200, 200, 200, alphaBorder).getRGB())
                .color(0).build());

        int textAlpha = (int) (180 + (hover * 75));
        Fonts.getSize(13, Fonts.Type.DEFAULT).drawCenteredString(ctx.getMatrices(), text, x + w / 2f, y + h / 2f - 3.5f, new Color(255, 255, 255, textAlpha).getRGB());
    }

    private void updateButtonAnimations(float deltaTime, float mouseX, float mouseY) {
        if (currentView == View.ALT_SCREEN) {
            for (int i = 0; i < 5; i++) buttonHoverProgress[i] = lerp(buttonHoverProgress[i], 0f, 0.2f * deltaTime);
            return;
        }

        float mw = 200, mh = 260;
        float mx = (width - mw) / 2f;
        float my = (height - mh) / 2f;
        float bw = mw - 30;
        float bh = 25;
        float bx = mx + 15;
        float by = my + 75;
        float space = 6;
        float lerpSpeed = 0.2f * deltaTime;

        for (int i = 0; i < 5; i++) {
            float y = by + i * (bh + space);
            boolean hovered = isIn(mouseX, mouseY, bx, y, bw, bh);
            buttonHoverProgress[i] = lerp(buttonHoverProgress[i], hovered ? 1f : 0f, lerpSpeed);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (currentView == View.ALT_SCREEN && altScreen != null) {
            return altScreen.mouseClicked(mx, my, btn);
        }

        if (currentView == View.MAIN_MENU && btn == 0) {
            float mw = 200, mh = 260;
            float mx_ = (width - mw) / 2f;
            float my_ = (height - mh) / 2f;
            float bw = mw - 30;
            float bh = 25;
            float bx = mx_ + 15;
            float by = my_ + 75;
            float space = 6;

            if (isIn(mx, my, bx, by, bw, bh)) { mc.setScreen(new SelectWorldScreen(this)); return true; }
            if (isIn(mx, my, bx, by + (bh + space), bw, bh)) { mc.setScreen(new MultiplayerScreen(this)); return true; }
            if (isIn(mx, my, bx, by + (bh + space) * 2, bw, bh)) { mc.setScreen(new OptionsScreen(this, mc.options)); return true; }
            if (isIn(mx, my, bx, by + (bh + space) * 3, bw, bh)) { currentView = View.ALT_SCREEN; return true; }
            if (isIn(mx, my, bx, by + (bh + space) * 4, bw, bh)) { mc.stop(); return true; }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        if (currentView == View.ALT_SCREEN && altScreen != null) return altScreen.mouseScrolled(mx, my, v);
        return super.mouseScrolled(mx, my, h, v);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (currentView == View.ALT_SCREEN && altScreen != null) return altScreen.mouseDragged(mx, my, btn);
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        if (altScreen != null) altScreen.mouseReleased();
        return super.mouseReleased(mx, my, btn);
    }

    @Override
    public boolean charTyped(char c, int m) {
        if (currentView == View.ALT_SCREEN && altScreen != null) return altScreen.charTyped(c);
        return super.charTyped(c, m);
    }

    @Override
    public boolean keyPressed(int k, int s, int m) {
        if (k == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            if (currentView == View.ALT_SCREEN) {
                currentView = View.MAIN_MENU;
                return true;
            }
            return false;
        }
        if (currentView == View.ALT_SCREEN && altScreen != null && altScreen.keyPressed(k)) return true;
        return super.keyPressed(k, s, m);
    }

    private boolean isIn(double mx, double my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private float lerp(float start, float end, float step) {
        return start + step * (end - start);
    }
}