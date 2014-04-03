package org.grobid.core.utilities;

public class Pair<A, B> {

    public final A a;
    public final B b;

    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb
                .append("('")
                .append(a)
                .append("'; '")
                .append(b)
                .append("')");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Pair)) {
            return false;
        }
        Pair<?, ?> that = (Pair<?, ?>) o;
        return ((this.a == null) ? that.a == null : this.a.equals(that.a)) &&
                ((this.b == null) ? that.b == null : this.b.equals(that.b));
    }

    @Override
    public int hashCode() {
        return 7 * (a != null ? a.hashCode() : 11) + 13 * (b != null ? b.hashCode() : 3);
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

}
