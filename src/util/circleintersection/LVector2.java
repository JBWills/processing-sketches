package util.circleintersection;

import static java.lang.Math.*;

import java.io.Serializable;

public final class LVector2 implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final LVector2 NULL = new LVector2(0, 0);
	public static final LVector2 X = new LVector2(1, 0);
	public static final LVector2 Y = new LVector2(0, 1);

	public final double x;
	public final double y;

	public LVector2(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public LVector2 add(LVector2 a) {
		return new LVector2(x + a.x, y + a.y);
	}

	public LVector2 sub(LVector2 a) {
		return new LVector2(x - a.x, y - a.y);
	}

	public LVector2 neg() {
		return new LVector2(-x, -y);
	}

	public LVector2 scale(double a) {
		return new LVector2(a * x, a * y);
	}

	public double dot(LVector2 a) {
		return x * a.x + y * a.y;
	}

	public double modSquared() {
		return dot(this);
	}

	public double mod() {
		return sqrt(modSquared());
	}

	public LVector2 normalize() {
		return scale(1 / mod());
	}

	public LVector2 rotPlus90() {
		return new LVector2(-y, x);
	}

	public LVector2 rotMinus90() {
		return new LVector2(y, -x);
	}

	public double angle() {
		return atan2(y, x);
	}

	public static LVector2 fromAngle(double ang) {
		return new LVector2(cos(ang), sin(ang));
	}

	public static LVector2 fromPolar(double ang, double mod) {
		return new LVector2(mod * cos(ang), mod * sin(ang));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LVector2 other = (LVector2) obj;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + x + ", " + y + ")";
	}

}
