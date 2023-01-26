package drawingbot.geom.easing;

import drawingbot.utils.Utils;

import java.util.function.Function;

public enum EnumEaseType {
    EASE_IN_LINEAR(EnumEaseCurve.LINEAR, EnumEaseTarget.IN, EasingUtils::linear),
    EASE_OUT_LINEAR(EnumEaseCurve.LINEAR, EnumEaseTarget.OUT, EasingUtils::linear),
    EASE_BOTH_LINEAR(EnumEaseCurve.LINEAR, EnumEaseTarget.BOTH, EasingUtils::linear),

    EASE_IN_SINE(EnumEaseCurve.SINE, EnumEaseTarget.IN, EasingUtils::easeInSine),
    EASE_OUT_SINE(EnumEaseCurve.SINE, EnumEaseTarget.OUT, EasingUtils::easeOutSine),
    EASE_BOTH_SINE(EnumEaseCurve.SINE, EnumEaseTarget.BOTH, EasingUtils::easeInOutSine),

    EASE_IN_QUAD(EnumEaseCurve.QUAD, EnumEaseTarget.IN, EasingUtils::easeInQuad),
    EASE_OUT_QUAD(EnumEaseCurve.QUAD, EnumEaseTarget.OUT, EasingUtils::easeOutQuad),
    EASE_BOTH_QUAD(EnumEaseCurve.QUAD, EnumEaseTarget.BOTH, EasingUtils::easeInOutQuad),

    EASE_IN_CUBIC(EnumEaseCurve.CUBIC, EnumEaseTarget.IN, EasingUtils::easeInCubic),
    EASE_OUT_CUBIC(EnumEaseCurve.CUBIC, EnumEaseTarget.OUT, EasingUtils::easeOutCubic),
    EASE_BOTH_CUBIC(EnumEaseCurve.CUBIC, EnumEaseTarget.BOTH, EasingUtils::easeInOutCubic),

    EASE_IN_QUART(EnumEaseCurve.QUART, EnumEaseTarget.IN, EasingUtils::easeInQuart),
    EASE_OUT_QUART(EnumEaseCurve.QUART, EnumEaseTarget.OUT, EasingUtils::easeOutQuart),
    EASE_BOTH_QUART(EnumEaseCurve.QUART, EnumEaseTarget.BOTH, EasingUtils::easeInOutQuart),

    EASE_IN_QUINT(EnumEaseCurve.QUINT, EnumEaseTarget.IN, EasingUtils::easeInQuint),
    EASE_OUT_QUINT(EnumEaseCurve.QUINT, EnumEaseTarget.OUT, EasingUtils::easeOutQuint),
    EASE_BOTH_QUINT(EnumEaseCurve.QUINT, EnumEaseTarget.BOTH, EasingUtils::easeInOutQuint),

    EASE_IN_EXPO(EnumEaseCurve.EXPO, EnumEaseTarget.IN, EasingUtils::easeInExpo),
    EASE_OUT_EXPO(EnumEaseCurve.EXPO, EnumEaseTarget.OUT, EasingUtils::easeOutExpo),
    EASE_BOTH_EXPO(EnumEaseCurve.EXPO, EnumEaseTarget.BOTH, EasingUtils::easeInOutExpo),

    EASE_IN_CIRC(EnumEaseCurve.CIRC, EnumEaseTarget.IN, EasingUtils::easeInCirc),
    EASE_OUT_CIRC(EnumEaseCurve.CIRC, EnumEaseTarget.OUT, EasingUtils::easeOutCirc),
    EASE_BOTH_CIRC(EnumEaseCurve.CIRC, EnumEaseTarget.BOTH, EasingUtils::easeInOutCirc),

    EASE_IN_BACK(EnumEaseCurve.BACK, EnumEaseTarget.IN, EasingUtils::easeInBack),
    EASE_OUT_BACK(EnumEaseCurve.BACK, EnumEaseTarget.OUT, EasingUtils::easeOutBack),
    EASE_BOTH_BACK(EnumEaseCurve.BACK, EnumEaseTarget.BOTH, EasingUtils::easeInOutBack),

    EASE_IN_ELASTIC(EnumEaseCurve.ELASTIC, EnumEaseTarget.IN, EasingUtils::easeInElastic),
    EASE_OUT_ELASTIC(EnumEaseCurve.ELASTIC, EnumEaseTarget.OUT, EasingUtils::easeOutElastic),
    EASE_BOTH_ELASTIC(EnumEaseCurve.ELASTIC, EnumEaseTarget.BOTH, EasingUtils::easeInOutElastic),

    EASE_IN_BOUNCE(EnumEaseCurve.BOUNCE, EnumEaseTarget.IN, EasingUtils::easeInBounce),
    EASE_OUT_BOUNCE(EnumEaseCurve.BOUNCE, EnumEaseTarget.OUT, EasingUtils::easeOutBounce),
    EASE_BOTH_BOUNCE(EnumEaseCurve.BOUNCE, EnumEaseTarget.BOTH, EasingUtils::easeInOutBounce);

    private EnumEaseCurve curve;
    private EnumEaseTarget target;
    private Function<Double, Double> easingFunction;

    EnumEaseType(EnumEaseCurve curve, EnumEaseTarget target, Function<Double, Double> easingFunction) {
        this.curve = curve;
        this.target = target;
        this.easingFunction = easingFunction;
    }

    public EnumEaseCurve getCurveType() {
        return curve;
    }

    public EnumEaseTarget getEaseTarget() {
        return target;
    }

    public double apply(double x) {
        return easingFunction != null ? easingFunction.apply(x) : x;
    }

    @Override
    public String toString() {
        return Utils.capitalize(name());
    }

}
