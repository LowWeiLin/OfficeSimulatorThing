package com.officelife.common;

import java.util.Objects;

public class Pair<T, K> {
    public final T first;
    public final K second;

    public Pair(T first, K second) {
        this.first = first;
        this.second = second;
    }

    public Pair(Pair<T, K> copy) {
        this(copy.first, copy.second);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (getClass() != o.getClass())
            return false;

        Pair<T, K> other = (Pair<T, K>) o;
        return Objects.equals(other.first, this.first)
                && Objects.equals(other.second, this.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return String.format("Pair of (%s, %s)", this.first, this.second);
    }
}