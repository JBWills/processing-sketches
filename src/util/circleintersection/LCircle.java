package util.circleintersection;

import java.io.Serializable;

public final class LCircle implements Serializable {

	private static final long serialVersionUID = 1L;

	public final LVector2 c;
	public final double r;

	public LCircle(LVector2 c, double r) {
		if (!(r > 0)) throw new IllegalArgumentException("Radius must be positive");
		this.c = c;
		this.r = r;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((c == null) ? 0 : c.hashCode());
		long temp;
		temp = Double.doubleToLongBits(r);
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
		LCircle other = (LCircle) obj;
		if (c == null) {
			if (other.c != null)
				return false;
		} else if (!c.equals(other.c))
			return false;
		if (Double.doubleToLongBits(r) != Double.doubleToLongBits(other.r))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(c: " + c + ", r: " + r + ")";
	}

}
