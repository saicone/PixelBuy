package com.saicone.pixelbuy.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ConfigTag {

    private static final Set<Character> NUMBER_SUFFIX = Set.of('b', 's', 'L', 'f', 'd');

    @Nullable
    @Contract("!null -> !null")
    @SuppressWarnings("unchecked")
    public static Object toConfigValue(@Nullable Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Map) { // 10
            for (var entry : ((Map<Object, Object>) value).entrySet()) {
                entry.setValue(toConfigValue(entry.getValue()));
            }
            return value;
        } else if (value instanceof List) { // 9
            final List<Object> list = new ArrayList<>();
            for (Object o : (List<Object>) value) {
                list.add(toConfigValue(o));
            }
            return list;
        } else if (value instanceof Byte) { // 1
            return value + "b";
        } else if (value instanceof Short) { // 2
            return value + "s";
        } else if (value instanceof Long) { // 4
            return value + "L";
        } else if (value instanceof Float) { // 5
            return value + "f";
        } else if (value instanceof Double) { // 6
            return value + "d";
        } else if (value instanceof Integer || value instanceof String) { // 3, 8
            return value;
        } else { // 7, 11, 12
            String suffix;
            if (value instanceof byte[]) {
                suffix = "B";
            } else if (value instanceof long[]) {
                suffix = "L";
            } else if (value instanceof int[]) {
                suffix = "I";
            } else {
                return value;
            }

            final StringJoiner joiner = new StringJoiner(", ", '[' + suffix + "; ", "]");
            if (suffix.equals("I")) {
                suffix = "";
            }

            for (Object o : OptionalType.of(value)) {
                joiner.add(o + suffix);
            }
            return joiner.toString();
        }
    }
    @Nullable
    @Contract("!null -> !null")
    @SuppressWarnings("unchecked")
    public static Object fromConfigValue(@Nullable Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Map) {
            for (var entry : ((Map<Object, Object>) value).entrySet()) {
                entry.setValue(fromConfigValue(entry.getValue()));
            }
            return value;
        } else if (value instanceof List) {
            final List<Object> list = new ArrayList<>();
            for (Object o : (List<Object>) value) {
                list.add(fromConfigValue(o));
            }
            return list;
        } else if (value instanceof String) {
            final String s = (String) value;
            if (s.length() < 2) {
                return value;
            }
            if (s.startsWith("[") && s.endsWith("]")) {
                if (s.startsWith("[B;")) {
                    final List<Byte> list = new ArrayList<>();
                    for (String s1 : s.substring(4).split(",")) {
                        if (!s1.endsWith("B")) {
                            list.clear();
                            return value;
                        }
                        list.add(Byte.parseByte(s1.trim().substring(0, s1.length() - 1)));
                    }
                    final byte[] array = new byte[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        array[i] = list.get(i);
                    }
                    return array;
                } else if (s.startsWith("[L;")) {
                    final List<Long> list = new ArrayList<>();
                    for (String s1 : s.substring(4).split(",")) {
                        if (!s1.endsWith("L")) {
                            list.clear();
                            return value;
                        }
                        list.add(Long.parseLong(s1.trim().substring(0, s1.length() - 1)));
                    }
                    final long[] array = new long[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        array[i] = list.get(i);
                    }
                    return array;
                } else if (s.startsWith("[I;")) {
                    final List<Integer> list = new ArrayList<>();
                    for (String s1 : s.substring(4).split(",")) {
                        list.add(Integer.parseInt(s1.trim()));
                    }
                    final int[] array = new int[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        array[i] = list.get(i);
                    }
                    return array;
                } else {
                    return value;
                }
            }

            final char suffix = s.charAt(s.length() - 1);
            if (NUMBER_SUFFIX.contains(suffix)) {
                final String s1 = s.substring(0, s.length() - 1);
                if (Strings.isNumber(s1)) {
                    switch (suffix) {
                        case 'b':
                            return Byte.parseByte(s1);
                        case 's':
                            return Short.parseShort(s1);
                        case 'L':
                            return Long.parseLong(s1);
                        case 'f':
                            return Float.parseFloat(s1);
                        case 'd':
                            return Double.parseDouble(s1);
                    }
                }
            }
        }
        return value;
    }
}
