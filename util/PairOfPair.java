package util;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;

public class PairOfPair {
    private Pair pair1;
    private Pair pair2;

    public PairOfPair(ImmutablePair<Integer, Integer> pair1, ImmutablePair<Integer, Integer> pair2) {
        this.pair1 = new Pair(pair1);
        this.pair2 = new Pair(pair2);
        if (this.pair1.hashCode() > this.pair2.hashCode()) {
            Pair temp = this.pair1;
            this.pair1 = this.pair2;
            this.pair2 = temp;
        }
    }

    public PairOfPair(Pair pair1, Pair pair2) {
        if (pair1.hashCode() > pair2.hashCode()) {
            this.pair1 = new Pair(pair2);
            this.pair2 = new Pair(pair1);
        } else {
            this.pair1 = new Pair(pair1);
            this.pair2 = new Pair(pair2);
        }
    }

    public Pair getLeft() {
        return pair1;
    }

    public Pair getRight() {
        return pair2;
    }

    public PairOfPair getNegative() {
        return new PairOfPair(pair1.getReversal(), pair2.getReversal());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        PairOfPair that = (PairOfPair) o;

        return (that.getLeft().equals(this.getLeft()) && that.getRight().equals(this.getRight()));
    }

    @Override
    public int hashCode() {
        return pair1.hashCode() + pair2.hashCode();
    }

    public String toString() {
        return pair1.toString() + " " + pair2.toString();
    }
}
