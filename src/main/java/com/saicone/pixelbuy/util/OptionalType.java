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

import com.google.gson.Gson;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

/**
 * Object container with automatic conversion to required
 * type when value is called.
 *
 * @author Rubenicos
 */
@SuppressWarnings("unchecked")
public class OptionalType extends IterableType<Object> {

    /**
     * Static object representation of empty OptionalType.
     */
    public static final OptionalType EMPTY = new OptionalType(null);
    /**
     * {@link Gson} public instance with default configuration.
     */
    public static final Gson GSON = new Gson();
    private static final Map<Class<?>, Function<OptionalType, Object>> TYPES = new HashMap<>();

    static {
        TYPES.put(Object.class, OptionalType::asObject);
        TYPES.put(String.class, OptionalType::asString);
        TYPES.put(Character.class, OptionalType::asChar);
        TYPES.put(char.class, type -> type.asChar('\0'));
        TYPES.put(Boolean.class, OptionalType::asBoolean);
        TYPES.put(boolean.class, type -> type.asBoolean(Boolean.FALSE));
        TYPES.put(Byte.class, OptionalType::asByte);
        TYPES.put(byte.class, type -> type.asByte(Byte.MIN_VALUE));
        TYPES.put(Short.class, OptionalType::asShort);
        TYPES.put(short.class, type -> type.asShort(Short.MIN_VALUE));
        TYPES.put(Integer.class, OptionalType::asInt);
        TYPES.put(int.class, type -> type.asInt(Integer.MIN_VALUE));
        TYPES.put(Float.class, OptionalType::asFloat);
        TYPES.put(float.class, type -> type.asFloat(Float.MIN_VALUE));
        TYPES.put(Long.class, OptionalType::asLong);
        TYPES.put(long.class, type -> type.asLong(Long.MIN_VALUE));
        TYPES.put(Double.class, OptionalType::asDouble);
        TYPES.put(double.class, type -> type.asDouble(Double.MIN_VALUE));
        TYPES.put(UUID.class, OptionalType::asUuid);
    }

    private Object value;

    /**
     * Get current OptionalType from object value.
     *
     * @param value Saved value, can be null.
     * @return      An OptionalType with object value.
     */
    @NotNull
    public static OptionalType of(@Nullable Object value) {
        return value != null ? new OptionalType(value) : EMPTY;
    }

    /**
     * Cast any object to required type.<br>
     * Take in count this method may produce {@link ClassCastException} if you want to cast inconvertible types.
     *
     * @param object Object to cast.
     * @return       The object as required type or null;
     * @param <T>    Required type to cast the object.
     */
    @Nullable
    @SuppressWarnings("unused")
    public static <T> T cast(@Nullable Object object) {
        try {
            return (T) object;
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * Constructs an OptionalType with specified value.
     *
     * @param value Saved value, can be null.
     */
    public OptionalType(@Nullable Object value) {
        this.value = value;
    }

    /**
     * Get actual optional type value.
     *
     * @return the defined value.
     */
    @Nullable
    public Object getValue() {
        return value;
    }

    @Override
    protected @Nullable Object getIterable() {
        return value;
    }

    /**
     * Set actual optional type value.
     *
     * @param value the value to set.
     */
    protected void setValue(@Nullable Object value) {
        this.value = value;
    }

    @Override
    protected void setIterable(@Nullable Object value) {
        this.value = value;
    }

    /**
     * Check if the existence of current value.
     *
     * @return true if current value is null.
     */
    public boolean isEmpty() {
        return value == null;
    }

    /**
     * Same has {@link #isEmpty()} but with inverted result.
     *
     * @return true if current value is not null.
     */
    public boolean isNotEmpty() {
        return !isEmpty();
    }

    /**
     * Check if the current value is instance of defined class.
     *
     * @param clazz Class type.
     * @return      true if value is instance of class.
     */
    public boolean isInstance(@NotNull Class<?> clazz) {
        return clazz.isInstance(value);
    }

    /**
     * Same has {@link #isInstance(Class)} but with inverted result.
     *
     * @param clazz Class type.
     * @return      true if value is not instance of class.
     */
    public boolean isNotInstance(@NotNull Class<?> clazz) {
        return !isInstance(clazz);
    }

    /**
     * Get the first value.<br>
     * This method extract the first value if the current object is a collection or array.
     *
     * @return An OptionalType with object value.
     */
    @NotNull
    @Contract("-> this")
    public OptionalType first() {
        if (isIterable()) {
            final Iterator<Object> iterator = this.iterator();
            return iterator.hasNext() ? OptionalType.of(iterator.next()) : OptionalType.EMPTY;
        }
        return this;
    }

    /**
     * Get actual value as single object<br>
     * This method make a recursively extract from the first value if the current object is a collection or array.
     *
     * @return An OptionalType with object value.
     */
    @NotNull
    @Contract("-> this")
    public OptionalType single() {
        if (isIterable()) {
            final Iterator<Object> iterator = this.iterator();
            return iterator.hasNext() ? OptionalType.of(iterator.next()).single() : OptionalType.EMPTY;
        }
        return this;
    }

    /**
     * Get actual value converted to required type.
     *
     * @param <T> Type to cast.
     * @return    Actual value or null if cast fails.
     */
    @Nullable
    public <T> T value() {
        return cast(value);
    }

    /**
     * Get actual value converted to required type class.
     *
     * @param clazz Class to check instance of.
     * @param <T>   Type to cast.
     * @return      Actual value or null if cast fails.
     */
    @Nullable
    public <T> T value(@NotNull Class<T> clazz) {
        return isInstance(clazz) ? (T) value : null;
    }

    /**
     * Clear the current object, only affect {@link Collection} and {@link Map} types.
     */
    public void clear() {
        clear(false);
    }

    /**
     * Clear the current object, only affect {@link Collection} and {@link Map} types.
     *
     * @param deep true to clear the inner objects.
     */
    public void clear(boolean deep) {
        if (value == null) {
            return;
        }
        clear(value, deep);
        value = null;
    }

    private void clear(@NotNull Object object, boolean deep) {
        if (object instanceof Iterable) {
            if (deep) {
                for (Object o : (Iterable<?>) object) {
                    clear(o, true);
                }
            }
            if (object instanceof Collection) {
                ((Collection<?>) object).clear();
            }
        } else if (object instanceof Map) {
            if (deep) {
                for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
                    clear(entry.getValue(), true);
                    clear(entry.getKey(), true);
                }
            }
            ((Map<?, ?>) object).clear();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OptionalType objects = (OptionalType) o;

        return Objects.equals(value, objects.value);
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    /**
     * Get actual value converted to required type or default defined value.
     *
     * @param def Default value if cast fails.
     * @param <T> Type to cast.
     * @return    Actual value or default if cast fails.
     */
    @Contract("!null -> !null")
    public <T> T or(@Nullable T def) {
        T value = value();
        return value != null ? value : def;
    }

    /**
     * Get actual value converted to required type class or default defined value.
     *
     * @param clazz Class to check instance of.
     * @param def   Default value if cast fails.
     * @param <T>   Type to cast.
     * @return      Actual value or default if cast fails.
     */
    @Nullable
    @Contract("_, !null -> !null")
    public <T> T or(@NotNull Class<T> clazz, @Nullable T def) {
        return isInstance(clazz) ? (T) value : def;
    }

    /**
     * Get actual value by function.
     *
     * @param function Function to process the value.
     * @return         The function result.
     * @param <T>      The required function result type.
     */
    @Nullable
    public <T> T by(@NotNull ThrowableFunction<@Nullable Object, @Nullable T> function) {
        try {
            return function.apply(value);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Get actual value by function with defined default value.
     *
     * @param function Function to process the actual non-null value.
     * @param def      Defined default value if the actual or final value is null.
     * @return         The function result or default value if the function return null object or throws exception.
     * @param <T>      The required result type.
     */
    @Nullable
    @Contract("_, !null -> !null")
    public <T> T by(@NotNull ThrowableFunction<@Nullable Object, @Nullable T> function, @Nullable T def) {
        if (value == null) {
            return def;
        }
        final T obj;
        try {
            obj = function.apply(value);
        } catch (Throwable t) {
            return def;
        }
        return obj != null ? obj : def;
    }

    /**
     * Get actual value as type or by function with defined default value.
     *
     * @param type     The class type to match the value.
     * @param function Function to process the actual non-null value.
     * @param def      Defined default value if the actual or final value is null.
     * @return         A value as required type or default value if the function return null object or throws exception.
     * @param <T>      The required result type.
     */
    @Nullable
    @Contract("_, _, !null -> !null")
    public <T> T by(@NotNull Class<T> type, @NotNull ThrowableFunction<@Nullable Object, @Nullable T> function, @Nullable T def) {
        if (type.isInstance(value)) {
            return (T) value;
        }
        if (value instanceof Boolean && Number.class.isAssignableFrom(type)) {
            return by(object -> function.apply(Boolean.TRUE.equals(object) ? "1" : "0"), def);
        }
        return by(function, def);
    }

    /**
     * Get actual value as specific type using Gson deserializer.
     *
     * @param type The specific type.
     * @return     The value as required type or null if deserialization fails.
     * @param <T>  The required type.
     */
    public <T> T as(@NotNull Type type) {
        return GSON.fromJson(GSON.toJsonTree(value), type);
    }

    /**
     * Get actual value as required class type using Gson deserializer.
     *
     * @param type The class of type.
     * @return     The value as required type or null if deserialization fails.
     * @param <T>  The required type.
     */
    public <T> T as(@NotNull Class<T> type) {
        if (value == null) {
            return null;
        }
        if (value.getClass() == type) {
            try {
                return (T) value;
            } catch (ClassCastException ignored) { }
        }
        try {
            final Function<OptionalType, Object> function = TYPES.get(type);
            if (function != null) {
                return (T) function.apply(this);
            }
        } catch (ClassCastException ignored) { }
        return GSON.fromJson(GSON.toJsonTree(value), type);
    }

    /**
     * Get actual value as array using function.
     *
     * @param array    The array to fill with objects.
     * @param function Function to convert the value or any element inside value to required type.
     * @return         The value as required array type.
     * @param <T>      The required type.
     */
    @NotNull
    public <T> T[] asArray(@NotNull T[] array, @NotNull Function<@NotNull OptionalType, @Nullable T> function) {
        if (value == null) {
            return array;
        }
        try {
            return (T[]) value;
        } catch (ClassCastException ignored) { }

        return asList(function).toArray(array);
    }

    /**
     * Get actual value as list using function.
     *
     * @param function Function to convert the value or any element inside value to required type.
     * @return         The value as required list type.
     * @param <T>      The required type.
     */
    @NotNull
    public <T> List<T> asList(@NotNull Function<@NotNull OptionalType, @Nullable T> function) {
        return asCollection(new ArrayList<>(), function);
    }

    /**
     * Apply actual value to collection using function.
     *
     * @param collection The collection to add values.
     * @param function   Function to convert the value or any element inside value to required type.
     * @return           The provided collection.
     * @param <T>        The required type inside collection.
     * @param <C>        The required collection type.
     */
    @NotNull
    public <T, C extends Collection<T>> C asCollection(@NotNull C collection, @NotNull Function<@NotNull OptionalType, @Nullable T> function) {
        if (value == null) {
            return collection;
        }
        try {
            return (C) value;
        } catch (ClassCastException ignored) { }
        if (isIterable()) {
            forEach(object -> {
                final T result = function.apply(OptionalType.of(object));
                if (result != null) {
                    collection.add(result);
                }
            });
        } else {
            final T result = function.apply(this);
            if (result != null) {
                collection.add(result);
            }
        }
        return collection;
    }

    /**
     * Get the actual value as Object or null.
     *
     * @return An Object value or null.
     */
    @Nullable
    public Object asObject() {
        return asObject(null);
    }

    /**
     * Get the actual value as Object or default value instead.
     *
     * @param def Default Object value.
     * @return    An Object value or default value.
     */
    @Nullable
    @Contract("!null -> !null")
    public Object asObject(@Nullable Object def) {
        return value != null ? value : def;
    }

    /**
     * Get the actual value as String or null.<br>
     * This method only convert the actual value to String if it isn't null.
     *
     * @return A String value or null.
     */
    @Nullable
    public String asString() {
        return asString(null);
    }

    /**
     * Get the actual value as String if it isn't null or default value instead.
     *
     * @param def Default String value.
     * @return    A String value or default value.
     */
    @Nullable
    @Contract("!null -> !null")
    public String asString(@Nullable String def) {
        return value != null ? String.valueOf(value) : def;
    }

    /**
     * Get the actual value as Character or null if the conversion fails.
     *
     * @return A Character value or null.
     */
    @Nullable
    public Character asChar() {
        return asChar(null);
    }

    /**
     * Get the actual value as Character or default value instead if the conversion fails.
     *
     * @param def Default Character value.
     * @return    A Character value or default value.
     */
    @Nullable
    @Contract("!null -> !null")
    public Character asChar(@Nullable Character def) {
        return by(Character.class, object -> {
            final String s = String.valueOf(object);
            return s.isBlank() ? null : s.charAt(0);
        }, def);
    }

    /**
     * Get the actual value as Boolean or null if the conversion fails.
     *
     * @return A Boolean value or null.
     */
    @Nullable
    public Boolean asBoolean() {
        return asBoolean(null);
    }

    /**
     * Get the actual value as Boolean or default value instead if the conversion fails.
     *
     * @param def Default Boolean value.
     * @return    A Boolean value or default value.
     */
    @Nullable
    @Contract("!null -> !null")
    public Boolean asBoolean(@Nullable Boolean def) {
        return by(Boolean.class, object -> {
            switch (String.valueOf(object).toLowerCase()) {
                case "true":
                case "1":
                case "1.0":
                case "yes":
                case "on":
                case "y":
                    return true;
                case "false":
                case "0":
                case "0.0":
                case "no":
                case "off":
                case "n":
                    return false;
                default:
                    return null;
            }
        }, def);
    }

    /**
     * Get the actual value as Byte or null if the conversion fails.
     *
     * @return A Byte value or null.
     */
    @Nullable
    public Byte asByte() {
        return asByte(null);
    }

    /**
     * Get the actual value as Byte or default value instead if the conversion fails.
     *
     * @param def Default Byte value.
     * @return    A Byte value or default value.
     */
    @Nullable
    @Contract("!null -> !null")
    public Byte asByte(@Nullable Byte def) {
        return by(Byte.class, object -> object instanceof Number ? ((Number) object).byteValue() : Byte.parseByte(String.valueOf(object)), def);
    }

    /**
     * Get the actual value as Short or null if the conversion fails.
     *
     * @return A Short value or null.
     */
    @Nullable
    public Short asShort() {
        return asShort(null);
    }

    /**
     * Get the actual value as Short or default value instead if the conversion fails.
     *
     * @param def Default Short value.
     * @return    A Short value or default value.
     */
    @Nullable
    @Contract("!null -> !null")
    public Short asShort(@Nullable Short def) {
        return by(Short.class, object -> object instanceof Number ? ((Number) object).shortValue() : Short.parseShort(String.valueOf(object)), def);
    }

    /**
     * Get the actual value as Integer or null if the conversion fails.
     *
     * @return A Integer value or null.
     */
    @Nullable
    public Integer asInt() {
        return asInt(null);
    }

    /**
     * Get the actual value as Integer or default value instead if the conversion fails.
     *
     * @param def Default Integer value.
     * @return    A Integer value or default value.
     */
    @Nullable
    @Contract("!null -> !null")
    public Integer asInt(@Nullable Integer def) {
        return by(Integer.class, object -> object instanceof Number ? ((Number) object).intValue() : Integer.parseInt(String.valueOf(object)), def);
    }

    /**
     * Get the actual value as Float or null if the conversion fails.
     *
     * @return A Float value or null.
     */
    @Nullable
    public Float asFloat() {
        return asFloat(null);
    }

    /**
     * Get the actual value as Float or default value instead if the conversion fails.
     *
     * @param def Default Float value.
     * @return    A Float value or default value.
     */
    @Nullable
    @Contract("!null -> !null")
    public Float asFloat(@Nullable Float def) {
        return by(Float.class, object -> object instanceof Number ? ((Number) object).floatValue() : Float.parseFloat(String.valueOf(object)), def);
    }

    /**
     * Get the actual value as Long or null if the conversion fails.
     *
     * @return A Long value or null.
     */
    @Nullable
    public Long asLong() {
        return asLong(null);
    }

    /**
     * Get the actual value as Long or default value instead if the conversion fails.
     *
     * @param def Default Long value.
     * @return    A Long value or default value.
     */
    @Nullable
    @Contract("!null -> !null")
    public Long asLong(@Nullable Long def) {
        return by(Long.class, object -> object instanceof Number ? ((Number) object).longValue() : Long.parseLong(String.valueOf(object)), def);
    }

    /**
     * Get the actual value as Double or null if the conversion fails.
     *
     * @return A Double value or null.
     */
    @Nullable
    public Double asDouble() {
        return asDouble(null);
    }

    /**
     * Get the actual value as Double or default value instead if the conversion fails.
     *
     * @param def Default Double value.
     * @return    A Double value or default value.
     */
    @Nullable
    @Contract("!null -> !null")
    public Double asDouble(@Nullable Double def) {
        return by(Double.class, object -> object instanceof Number ? ((Number) object).doubleValue() : Double.parseDouble(String.valueOf(object)), def);
    }

    /**
     * Get the actual value as UUID or null if the conversion fails.
     *
     * @return An UUID value or null.
     */
    @Nullable
    public UUID asUuid() {
        return asUuid(null);
    }

    /**
     * Get the actual value as UUID or default value instead if the conversion fails.
     *
     * @param def Default UUID value.
     * @return    An UUID value or default value.
     */
    @Nullable
    @Contract("!null -> !null")
    public UUID asUuid(@Nullable UUID def) {
        return by(UUID.class, (object) -> {
            if (object instanceof int[]) {
                return getUUID((int[]) object);
            } else if (object instanceof String) {
                return UUID.fromString((String) object);
            } else {
                return null;
            }
        }, def);
    }

    /**
     * Get the actual value as Enum type Set.<br>
     * This method only works if the actual value is a bitField.
     *
     * @param enumType Enum type class.
     * @return         A Set with Enum elements or null if the class isn't an enum type
     * @param <E>      Enum required type.
     */
    @NotNull
    public <E extends Enum<E>> Set<E> asEnumSet(@NotNull Class<E> enumType) {
        final E[] constants = enumType.getEnumConstants();
        return asElementSet(ordinal -> ordinal < constants.length ? constants[ordinal] : null, constants.length);
    }

    /**
     * Get the actual value as element type Set.<br>
     * This method only works if the actual value is a bitField.
     *
     * @param element Function to convert ordinal to element type.
     * @param size    The maximum length of the enum values.
     * @return        A Set with Enum elements.
     * @param <E>     Element required type.
     */
    @NotNull
    public <E> Set<E> asElementSet(@NotNull Function<@NotNull Integer, @Nullable E> element, int size) {
        final Set<E> set = new HashSet<>();
        for (Integer ordinal : asOrdinalSet(size)) {
            E e = element.apply(ordinal);
            if (e != null) {
                set.add(e);
            }
        }
        return set;
    }

    /**
     * Get the actual value as ordinal-value Set.<br>
     * This method only works if the actual value is a bitField.
     *
     * @param size The maximum length of the ordinal values.
     * @return     A Set with ordinals.
     */
    @NotNull
    public Set<Integer> asOrdinalSet(int size) {
        final Set<Integer> ordinals = new HashSet<>();
        final Integer bitField = asInt();
        if (bitField == null) {
            return ordinals;
        }
        for (int i = 0; i < size; i++) {
            final byte bit = (byte) (1 << i);
            if ((bitField & bit) == bit) {
                ordinals.add(i);
            }
        }
        return ordinals;
    }

    private static UUID getUUID(int[] array) {
        if (array.length == 4) {
            StringBuilder builder = new StringBuilder();
            for (int i : array) {
                String hex = Integer.toHexString(i);
                builder.append(new String(new char[8 - hex.length()]).replace('\0', '0')).append(hex);
            }
            if (builder.length() == 32) {
                builder.insert(20, '-').insert(16, '-').insert(12, '-').insert(8, '-');
                return UUID.fromString(builder.toString());
            } else {
                throw new IllegalArgumentException("The final converted UUID '" + builder + "' doesn't is a 32-length string");
            }
        }
        throw new IllegalArgumentException("The provided int array isn't a 4-length array");
    }
}
