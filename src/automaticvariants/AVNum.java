/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants;

/**
 *
 * @author Justin Swanson
 */
abstract public class AVNum {

    float num;

    public static AVNum factory(String in) {
	AVNum out;
	switch (in.charAt(0)) {
	    case '%':
		out = new AVNumPct();
		break;
	    case '-':
	    case '+':
		out = new AVNumAdd();
		break;
	    default:
		out = new AVNumSet();
	}
	out.set(in);
	return out;
    }

    void set(String s) {
	num = Float.valueOf(s.substring(1));
    }

    float value() {
	return num;
    }

    abstract float value(float val);

    abstract AVNum merge(AVNum n);

    static String merge(String s1, String s2) {
	AVNum n1 = factory(s1);
	AVNum n2 = factory(s2);
	AVNum out = n1.merge(n2);
	return out.toString();
    }

    static class AVNumSet extends AVNum {

	@Override
	public String toString() {
	    return "=" + num;
	}

	@Override
	AVNum merge(AVNum n) {
	    AVNumSet out = new AVNumSet();
	    if (n.getClass() == AVNumSet.class) {
		out.num = (num + n.num) / 2;
	    } else if (n.getClass() == AVNumAdd.class) {
		out.num = num + n.num;
	    } else {
		out.num = (int) (num * n.value());
	    }
	    return out;
	}

	@Override
	float value(float val) {
	    return value();
	}

	@Override
	void set(String s) {
	    if (s.charAt(0) == '=') {
		super.set(s);
	    } else {
		num = Float.valueOf(s);
	    }
	}
    }

    static class AVNumAdd extends AVNum {

	@Override
	public String toString() {
	    if (num < 0) {
		return "-" + Math.abs(num);
	    }
	    return "+" + num;
	}

	@Override
	void set(String s) {
	    super.set(s);
	    if (s.charAt(0) == '-') {
		num *= -1;
	    }
	}

	@Override
	AVNum merge(AVNum n) {
	    if (n.getClass() == AVNumSet.class) {
		return n.merge(this);
	    }
	    AVNumAdd out = new AVNumAdd();
	    if (n.getClass() == AVNumAdd.class) {
		out.num = (int) (value() + n.value());
	    } else {
		out.num = (int) (value() * n.value());
	    }
	    return out;
	}

	@Override
	float value(float val) {
	    return val + value();
	}
    }

    static class AVNumPct extends AVNum {

	@Override
	public String toString() {
	    return "%" + num;
	}

	@Override
	float value() {
	    return num / 100;
	}

	@Override
	AVNum merge(AVNum n) {
	    if (n.getClass() == AVNumPct.class) {
		AVNumPct out = new AVNumPct();
		out.num = (int) (value() * n.value() * 100);
	    }
	    return n.merge(this);
	}

	@Override
	float value(float val) {
	    return val *= value();
	}
    }

    boolean modified() {
	return !getClass().equals(AVNumPct.class)
		|| value() != 100.0;
    }
}
