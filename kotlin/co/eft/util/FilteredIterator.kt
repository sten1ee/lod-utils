package co.eft.util

fun <T> Iterator<T>.filter(pred: (T)->Boolean): Iterator<T> {
    class FilteredIterator(val original_iter: Iterator<T>) : Iterator<T> {
        var next: T? = null

        override fun hasNext(): Boolean {
            if (next != null)
                return true

            while (original_iter.hasNext()) {
                val candidate = original_iter.next()
                if (pred(candidate)) {
                    next = candidate
                    return true
                }
            }
            return false
        }

        override fun next(): T {
            val snapshot1 = next
            if (snapshot1 != null) {
                next = null
                return snapshot1
            }

            require (hasNext()) { throw NoSuchElementException() }

            val snapshot2 = next
            require (snapshot2 != null) { throw ConcurrentModificationException() }
            next = null
            return snapshot2
        }
    }

    return FilteredIterator(this)
}