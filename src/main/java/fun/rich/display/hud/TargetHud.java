package fun.rich.display.hud;
import fun.rich.utils.interactions.interact.PlayerInteractionHelper;
import fun.rich.utils.math.time.StopWatch;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import fun.rich.utils.client.managers.api.draggable.AbstractDraggable;
import fun.rich.features.impl.combat.Aura;
import fun.rich.features.impl.render.Hud;
import fun.rich.common.animation.Animation;
import fun.rich.common.animation.Direction;
import fun.rich.common.animation.implement.Decelerate;
import fun.rich.utils.display.font.FontRenderer;
import fun.rich.utils.display.font.Fonts;
import fun.rich.utils.display.shape.ShapeProperties;
import fun.rich.Rich;
import fun.rich.utils.display.color.ColorAssist;
import fun.rich.utils.interactions.item.ItemTask;
import fun.rich.utils.math.calc.Calculate;
import fun.rich.utils.display.geometry.Render2D;
import fun.rich.utils.display.scissor.ScissorAssist;
import fun.rich.utils.client.packet.network.Network;
import java.awt.*;

public class TargetHud extends AbstractDraggable {
    private final Animation animation = new Decelerate().setMs(650).setValue(1);
    private final Animation faceAlphaAnimation = new Decelerate().setMs(125).setValue(1);
    private final StopWatch stopWatch = new StopWatch();
    private final StopWatch distanceUpdateTimer = new StopWatch();
    private LivingEntity lastTarget;
    private Item lastItem = Items.AIR;
    private float health;
    private float absorption;
    private float displayedDistance;

    public TargetHud() {
        super("Target Hud", 10, 80, 150, 60, true);
    }

    @Override
    public boolean visible() {
        return scaleAnimation.isDirection(Direction.FORWARDS);
    }

    @Override
    public void tick() {
        LivingEntity auraTarget = Aura.getInstance().getTarget();
        if (auraTarget != null) {
            lastTarget = auraTarget;
            startAnimation();
            faceAlphaAnimation.setDirection(Direction.FORWARDS);
        } else if (PlayerInteractionHelper.isChat(mc.currentScreen)) {
            lastTarget = mc.player;
            startAnimation();
            faceAlphaAnimation.setDirection(Direction.FORWARDS);
        } else if (stopWatch.finished(500)) {
            stopAnimation();
            faceAlphaAnimation.setDirection(Direction.BACKWARDS);
        }
    }

    @Override
    public void drawDraggable(DrawContext context) {
        if (Hud.getInstance().interfaceSettings.isSelected("Target Hud") && Hud.getInstance().state) {
            if (lastTarget != null) {
                MatrixStack matrix = context.getMatrices();
                drawMain(context, matrix);
                drawArmor(context, matrix);
                drawFace(context);
            }
        }
    }

    private void drawMain(DrawContext context, MatrixStack matrix) {
        FontRenderer font = Fonts.getSize(18, Fonts.Type.REGULAR);
        FontRenderer distancefont = Fonts.getSize(12, Fonts.Type.SEMI);
        float hp = PlayerInteractionHelper.getHealth(lastTarget);
        String stringHp = (lastTarget.isInvisible() && !Network.isSpookyTime() && !Network.isCopyTime()) ? "??" : PlayerInteractionHelper.getHealthString(hp);
        health = MathHelper.clamp(Calculate.interpolateSmooth(1, health, hp / lastTarget.getMaxHealth() * 360), 0, 360);
        float absorptionAmount = lastTarget.getAbsorptionAmount();
        absorption = MathHelper.clamp(Calculate.interpolateSmooth(1, absorption, absorptionAmount / 20.0F * 360), 0, 360);
        float actualDistance = mc.player.distanceTo(lastTarget);
        float roundedDistance = Math.round(actualDistance * 10) / 10.0f;
        if (distanceUpdateTimer.finished(10)) {
            displayedDistance = MathHelper.clamp(Calculate.interpolateSmooth(0.5f, displayedDistance, roundedDistance), 0, 100);
            distanceUpdateTimer.reset();
        }
        String distanceText = String.format("%.1f", displayedDistance);

        setWidth(150);
        setHeight(60);

        // Main background
        blur.render(ShapeProperties.create(matrix, getX(), getY(), getWidth(), 40)
                .round(15).quality(12)
                .color(new Color(0, 0, 0, 150).getRGB())
                .build());

        rectangle.render(ShapeProperties.create(matrix, getX(), getY(), getWidth(), 40)
                .round(15)
                .thickness(1.5f)
                .outlineColor(new Color(138, 43, 226, 255).getRGB())
                .color(new Color(15, 15, 15, 180).getRGB())
                .build());

        // Name and distance
        font.drawString(matrix, lastTarget.getName().getString(), getX() + 45, getY() + 10f, ColorAssist.getText());
        distancefont.drawString(matrix, "Distance: " + distanceText, getX() + 45, getY() + 22f, new Color(225, 225, 255, 255).getRGB());

        // Health Circle
        float arcSize = 28;
        float arcX = getX() + getWidth() - arcSize - 8;
        float arcY = getY() + 6;

        arc.render(ShapeProperties.create(matrix, arcX, arcY, arcSize, arcSize).round(0.26F).thickness(0.30f).end(361)
                .color(new Color(255, 255, 255, 25).getRGB()).build());
        arc.render(ShapeProperties.create(matrix, arcX, arcY, arcSize, arcSize).round(0.26F).thickness(0.30f).end(health)
                .color(new Color(255, 127, 80, 255).getRGB(), new Color(255, 127, 80, 255).getRGB(), new Color(255, 127, 80, 255).getRGB(), new Color(255, 127, 80, 255).getRGB()).build());

        if (absorption > 0 && !Network.isFunTime()) {
            arc.render(ShapeProperties.create(matrix, arcX, arcY, arcSize, arcSize).round(0.26F).thickness(0.30f)
                    .end(absorption)
                    .color(new Color(255, 215, 0, 255).getRGB()).build());
        }

        Fonts.getSize(12, Fonts.Type.BOLD).drawCenteredString(matrix, stringHp, arcX + arcSize / 2f, arcY + arcSize / 2f + 1, new Color(255, 255, 255, 225).getRGB());
    }

    private void drawArmor(DrawContext context, MatrixStack matrix) {
        ItemStack[] slots = new ItemStack[] {
                lastTarget.getMainHandStack(),
                lastTarget.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD),
                lastTarget.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST),
                lastTarget.getEquippedStack(net.minecraft.entity.EquipmentSlot.LEGS),
                lastTarget.getEquippedStack(net.minecraft.entity.EquipmentSlot.FEET),
                lastTarget.getOffHandStack()
        };

        float startX = getX() + 30;
        float y = getY() + 45;
        float slotSize = 16;
        float spacing = 4;

        for (int i = 0; i < 6; i++) {
            float currentX = startX + i * (slotSize + spacing);

            blur.render(ShapeProperties.create(matrix, currentX, y, slotSize, slotSize)
                    .round(5).quality(12)
                    .color(new Color(0, 0, 0, 100).getRGB())
                    .build());

            rectangle.render(ShapeProperties.create(matrix, currentX, y, slotSize, slotSize)
                    .round(5)
                    .thickness(1.0f)
                    .outlineColor(new Color(138, 43, 226, 50).getRGB())
                    .color(new Color(15, 15, 15, 180).getRGB())
                    .build());

            if (!slots[i].isEmpty()) {
                Render2D.defaultDrawStack(context, slots[i], currentX, y, false, false, 0.8F);
            } else {
                Fonts.getSize(10, Fonts.Type.DEFAULT).drawCenteredString(matrix, "x", currentX + slotSize / 2f, y + slotSize / 2f + 1, new Color(255, 255, 255, 100).getRGB());
            }
        }
    }

    private void drawUsingItem(DrawContext context, MatrixStack matrix) {
        animation.setDirection(lastTarget.isUsingItem() ? Direction.FORWARDS : Direction.BACKWARDS);
        if (!lastTarget.getActiveItem().isEmpty() && lastTarget.getActiveItem().getCount() != 0) {
            lastItem = lastTarget.getActiveItem().getItem();
        }
        if (!animation.isFinished(Direction.BACKWARDS) && !lastItem.equals(Items.AIR)) {
            int size = 24;
            float anim = animation.getOutput().floatValue();
            float progress = (lastTarget.getItemUseTime() + tickCounter.getTickDelta(false)) / ItemTask.maxUseTick(lastItem) * 360;
            float x = getX() - (size + 5) * anim;
            float y = getY() + 4;
            ScissorAssist scissorManager = Rich.getInstance().getScissorManager();
            scissorManager.push(matrix.peek().getPositionMatrix(), getX() - 50, getY(), 50, getHeight());
            Calculate.setAlpha(anim, () -> {
                blur.render(ShapeProperties.create(matrix, x, y, size, size).quality(5)
                        .round(12).softness(1).thickness(2).outlineColor(ColorAssist.getOutline(0)).color(ColorAssist.getRect(0.7F)).build());
                arc.render(ShapeProperties.create(matrix, x, y, size, size).round(0.38F).thickness(0.30f).end(progress)
                        .color(ColorAssist.fade(0), ColorAssist.fade(200), ColorAssist.fade(0), ColorAssist.fade(200)).build());
                Render2D.defaultDrawStack(context, lastItem.getDefaultStack(), x + 3f, y + 3f, false, false, 1f);
            });
            scissorManager.pop();
        }
    }

    private void drawFace(DrawContext context) {
        EntityRenderer<? super LivingEntity, ?> baseRenderer = mc.getEntityRenderDispatcher().getRenderer(lastTarget);
        if (!(baseRenderer instanceof LivingEntityRenderer<?, ?, ?>)) {
            return;
        }
        @SuppressWarnings("unchecked")
        LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?> renderer = (LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?>) baseRenderer;
        LivingEntityRenderState state = renderer.getAndUpdateRenderState(lastTarget, tickCounter.getTickDelta(false));
        Identifier textureLocation = renderer.getTexture(state);
        float alpha = faceAlphaAnimation.getOutput().floatValue();

        float faceSize = 30;
        float faceX = getX() + 8;
        float faceY = getY() + 5;

        // Round head logic would be complex with standard drawTexture, so we use a rounded background behind it
        rectangle.render(ShapeProperties.create(context.getMatrices(), faceX - 1, faceY - 1, faceSize + 2, faceSize + 2)
                .round(8).color(new Color(138, 43, 226, 255).getRGB()).build());

        Calculate.setAlpha(alpha, () -> {
            Render2D.drawTexture(context, textureLocation, faceX, faceY, faceSize, 8, 8, 8, 64, ColorAssist.getRect(1), ColorAssist.multRed(-1, 1 + lastTarget.hurtTime / 4F));
        });
    }
}