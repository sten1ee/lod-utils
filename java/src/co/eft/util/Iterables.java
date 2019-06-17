package co.eft.util;

import java.util.Iterator;
import java.util.function.Function;

public class Iterables {

    private Iterables() {}

    private static class IteratorAdapter<S, T> implements Iterator<T> {
        private final Iterator<S>    src;
        private final Function<S, T> converter;

        IteratorAdapter(Iterator<S> src, Function<S, T> converter) {
            this.src = src;
            this.converter = converter;
        }

        @Override
        public boolean hasNext() { return src.hasNext(); }

        @Override
        public T next() { return converter.apply(src.next()); }

        @Override
        public void remove() { src.remove(); }
    }

    public static <S, T>  Iterable<T> convert(Iterable<S> src, Function<S, T> converter) {
        return () -> new IteratorAdapter<>(src.iterator(), converter);
    }
}
