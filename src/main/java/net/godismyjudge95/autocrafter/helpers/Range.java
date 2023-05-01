package net.godismyjudge95.autocrafter.helpers;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

@SuppressWarnings("unused")
public record Range(int minInclusive, int maxInclusive)  implements Iterable<Integer> {
    public boolean contains(int value) {
        return value >= minInclusive && value <= maxInclusive;
    }

    @Override
    public @NotNull Iterator<Integer> iterator() {
        return new Iterator<>() {
            private int current = minInclusive;

            @Override
            public boolean hasNext() {
                return current <= maxInclusive;
            }

            @Override
            public Integer next() {
                if (hasNext()) {
                    return current++;
                } else {
                    throw new NoSuchElementException("Range reached the end");
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Can't remove values from a Range");
            }
        };
    }
}