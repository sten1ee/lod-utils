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
        public boolean hasNext() {
            return src.hasNext();
        }

        @Override
        public T next() {
            return converter.apply(src.next());
        }

        @Override
        public void remove() {
            src.remove();
        }
    }

    /*private static class IterableAdapter<S, T> implements Iterable<T> {
        private final Iterable<S> src;
        private final Function<S, T> converter;

        private IterableAdapter(Iterable<S> src, Function<S, T> converter) {
            this.src = src;
            this.converter = converter;
        }

        @Override
        public Iterator<T> iterator() {
            return new IteratorAdapter<>(src.iterator(), converter);
        }
    }*/

    public static <S, T>  Iterable<T> convert(Iterable<S> src, Function<S, T> converter) {
        /*return new IterableAdapter<S, T>(src, converter);*/
        return () -> new IteratorAdapter<>(src.iterator(), converter);
    }

    /*public static <S>  Iterable<S> convert(Iterable<S> src, Function<S, S> converter) {
        return src;
    }*/
}
