/*
 * This file is part of mcode, licensed under the MIT License
 *
 * Copyright (c) Rubenicos
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.saicone.pixelbuy.util;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Abstract class to allow any object to be targeted on the enhanced for statement.
 *
 * @author Rubenicos
 *
 * @param <T> the iterable object type.
 */
public abstract class IterableType<T> implements Iterable<T> {

    /**
     * Get the object that can be iterated.
     *
     * @return an iterable object.
     */
    @Nullable
    protected abstract T getIterable();

    /**
     * Set the object that can be iterated.
     *
     * @param object an iterable object.
     */
    protected abstract void setIterable(@Nullable T object);

    /**
     * Check if the current object can be iterated using for statement.<br>
     * This condition can be applied to any {@link Iterable} type or array.
     *
     * @return true if the object can be iterated.
     */
    public boolean isIterable() {
        return getIterable() != null && (getIterable() instanceof Iterable || getIterable().getClass().isArray());
    }

    /**
     * Same has {@link #isIterable()} but with inverted result.
     *
     * @return true if the object can't be iterated.
     */
    public boolean isNotIterable() {
        return !isIterable();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<T> iterator() {
        Objects.requireNonNull(getIterable(), "Cannot iterate over empty object");
        if (getIterable() instanceof Iterable) {
            return ((Iterable<T>) getIterable()).iterator();
        } else if (getIterable() instanceof Object[]) {
            return new ObjectArrayIterator();
        } else if (getIterable().getClass().isArray()) {
            return new PrimitiveArrayIterator();
        } else {
            return new SingleObjectIterator();
        }
    }

    /**
     * Iterator for Object array types.
     */
    private class ObjectArrayIterator extends ArrayIterator {
        @Override
        public int size() {
            return ((Object[]) getIterable()).length;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T get(int index) {
            return (T) ((Object[]) getIterable())[index];
        }
    }

    /**
     * Iterator for primitive array types
     */
    private class PrimitiveArrayIterator extends ArrayIterator {
        @Override
        public int size() {
            return Array.getLength(getIterable());
        }

        @Override
        @SuppressWarnings("unchecked")
        public T get(int index) {
            return (T) Array.get(getIterable(), index);
        }
    }

    /**
     * Abstract iterator for arrays.
     */
    private abstract class ArrayIterator implements Iterator<T> {
        int currentIndex;
        int lastIndex = -1;

        public abstract int size();

        public abstract T get(int index);

        @Override
        public boolean hasNext() {
            return currentIndex != size();
        }

        @Override
        public T next() {
            int i = currentIndex;
            if (i >= size()) {
                throw new NoSuchElementException();
            }
            currentIndex = i + 1;
            return get(lastIndex = i);
        }

        @Override
        public void remove() {
            if (lastIndex < 0) {
                throw new IllegalStateException();
            }

            remove(lastIndex);
            currentIndex = lastIndex;
            lastIndex = -1;
        }

        @SuppressWarnings("unchecked")
        public void remove(int index) {
            final int size = size();
            if (size == 0 || index >= size) {
                throw new ConcurrentModificationException();
            }
            Object newArray = Array.newInstance(getIterable().getClass().getComponentType(), size - 1);
            for (int i = 0; i < size; i++) {
                if (i != index) {
                    Array.set(newArray, i, get(i));
                }
            }
            setIterable((T) newArray);
        }
    }

    /**
     * Iterator for single object instance.
     */
    private class SingleObjectIterator implements Iterator<T> {
        private boolean consumed = false;

        @Override
        public boolean hasNext() {
            return !consumed && getIterable() != null;
        }

        @Override
        public T next() {
            if (consumed || getIterable() == null) {
                throw new NoSuchElementException();
            }
            consumed = true;
            return getIterable();
        }

        @Override
        public void remove() {
            if (consumed || getIterable() == null) {
                setIterable(null);
                consumed = false;
            } else {
                throw new IllegalStateException();
            }
        }
    }
}
