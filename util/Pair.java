package util;

import org.apache.commons.lang3.tuple.ImmutablePair;

public class Pair {
    public Integer left;
    public Integer right;

    public Pair(Integer left, Integer right) {
        this.left = left;
        this.right = right;
    }

    public Pair(Pair pair) {
        this.left = pair.left;
        this.right = pair.right;
    }

    public Pair(ImmutablePair<Integer, Integer> pair) {
        this.left = pair.left;
        this.right = pair.right;
    }

    public Integer getLeft() {
        return left;
    }

    public Integer getRight() {
        return right;
    }

    public Pair getReversal() {
        return new Pair(right, left);
    }

    @Override
    public boolean equals(Object obj) {
        Pair pair = (Pair) obj;
        return pair.left.equals(this.left) && pair.right.equals(this.right);
    }

    @Override
    public int hashCode() {
        return (53 + left) * 53 + right;
    }
}
