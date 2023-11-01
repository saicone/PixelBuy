package com.saicone.pixelbuy.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Strings {

    public static boolean isNumber(@NotNull String s) {
        if (s.isBlank()) {
            return false;
        }
        boolean decimal = false;
        for (char c : (s.charAt(0) == '-' ? s.substring(1) : s).toCharArray()) {
            if (!Character.isDigit(c)) {
                if (!decimal && c == '.') {
                    decimal = true;
                    continue;
                }
                return false;
            }
        }
        return true;
    }

    @NotNull
    public static String replaceArgs(@NotNull String s, @Nullable Object... args) {
        if (args.length < 1 || s.isBlank()) {
            return s.replace("{#}", "0").replace("{*}", "[]").replace("{-}", "");
        }
        final char[] chars = s.toCharArray();
        final StringBuilder builder = new StringBuilder(s.length());
        String all = null;
        for (int i = 0; i < chars.length; i++) {
            final int mark = i;
            if (chars[i] == '{') {
                int num = 0;
                while (i + 1 < chars.length) {
                    if (Character.isDigit(chars[i + 1])) {
                        i++;
                        num *= 10;
                        num += chars[i] - '0';
                        continue;
                    }
                    if (i == mark) {
                        final char c = chars[i + 1];
                        if (c == '#') {
                            i++;
                            num = -1;
                        } else if (c == '*') {
                            i++;
                            num = -2;
                        } else if (c == '-') {
                            i++;
                            num = -3;
                        }
                    }
                    break;
                }
                if (i != mark && i + 1 < chars.length && chars[i + 1] == '}') {
                    i++;
                    if (num == -1) {
                        builder.append(args.length);
                    } else if (num == -2) {
                        builder.append(Arrays.toString(args));
                    } else if (num == -3) {
                        if (all == null) {
                            all = Arrays.stream(args).map(String::valueOf).collect(Collectors.joining(" "));
                        }
                        builder.append(all);
                    } else if (num < args.length) { // Avoid IndexOutOfBoundsException
                        builder.append(args[num]);
                    } else {
                        builder.append('{').append(num).append('}');
                    }
                } else {
                    i = mark;
                }
            }
            if (mark == i) {
                builder.append(chars[i]);
            }
        }
        return builder.toString();
    }

    @NotNull
    public static String replaceBracketPlaceholder(@NotNull String s, @NotNull Predicate<String> predicate, @NotNull BiFunction<String, String, Object> function) {
        return replacePlaceholder(s, '{', '}', predicate, function);
    }

    @NotNull
    public static String replacePlaceholder(@NotNull String s, @NotNull Predicate<String> predicate, @NotNull BiFunction<String, String, Object> function) {
        return replacePlaceholder(s, '%', '%', predicate, function);
    }

    @NotNull
    public static String replacePlaceholder(@NotNull String s, char start, char end, @NotNull Predicate<String> predicate, @NotNull BiFunction<String, String, Object> function) {
        if (s.isBlank() || !s.contains("" + start) || s.length() < 4) {
            return s;
        }

        final char[] chars = s.toCharArray();
        final StringBuilder builder = new StringBuilder(s.length());

        int mark = 0;
        for (int i = 0; i < chars.length; i++) {
            final char c = chars[i];

            builder.append(c);
            if (c != start || i + 1 >= chars.length) {
                mark++;
                continue;
            }

            // Faster than PlaceholderAPI ;)
            final int mark1 = i + 1;
            while (++i < chars.length) {
                final char c1 = chars[i];
                if (c1 == '_') {
                    if (i > mark1 && i + 2 < chars.length) {
                        final String id = s.substring(mark1, i);
                        if (predicate.test(id)) {
                            final int mark2 = i + 1;
                            while (++i < chars.length) {
                                final char c2 = chars[i];
                                if (c2 == end) {
                                    builder.replace(mark, i, String.valueOf(function.apply(id, s.substring(mark2, i))));
                                    break;
                                } else {
                                    builder.append(c2);
                                }
                            }
                            break;
                        }
                    }
                    builder.append(c1);
                    break;
                } else {
                    builder.append(c1);
                    if (i + 1 < chars.length && chars[i + 1] == start) {
                        break;
                    }
                }
            }

            mark = builder.length();
        }

        return builder.toString();
    }
}
