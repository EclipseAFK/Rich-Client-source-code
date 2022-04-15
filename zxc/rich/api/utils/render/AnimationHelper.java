package zxc.rich.api.utils.render;

import net.minecraft.client.Minecraft;
import zxc.rich.api.utils.Helper;

public class AnimationHelper implements Helper {

    public static float animation(float animation, float target, float speedTarget) {
        float dif = (target - animation) / Math.max((float) Minecraft.getDebugFPS(), 5) * 15;

        if (dif > 0) {
            dif = Math.max(speedTarget, dif);
            dif = Math.min(target - animation, dif);
        } else if (dif < 0) {
            dif = Math.min(-speedTarget, dif);
            dif = Math.max(target - animation, dif);
        }
        return animation + dif;
    }

    public static float calculateCompensation(float target, float current, long delta, double speed) {
        float diff = current - target;
        if (delta < 1) {
            delta = 1;
        }
        if (delta > 1000) {
            delta = 16;
        }
        double dif = (Math.max(speed * delta / (1000 / 60F), 0.5));
        if (diff > speed) {
            current -= dif;
            if (current < target) {
                current = target;
            }
        } else if (diff < -speed) {
            current += dif;
            if (current > target) {
                current = target;
            }
        } else {
            current = target;
        }
        return current;
    }
}
