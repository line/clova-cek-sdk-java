/*
 * Copyright 2018 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.linecorp.clova.extension.boot.util;

import org.springframework.core.MethodParameter;

import lombok.experimental.UtilityClass;

/**
 * String utilities.
 *
 * <p>Mainly for internal use within the framework; consider
 * <a href="http://commons.apache.org/proper/commons-lang/">Apache's Commons Lang</a>,
 * <a href="https://github.com/google/guava">Google Guava</a>
 * for a more comprehensive suite of {@code String} utilities.
 */
@UtilityClass
public class StringUtils {

    /**
     * Checks if the string is whitespace, empty, or null.
     *
     * @param str string to check, may be null
     *
     * @return {@code true} if the string is null, empty, or whitespace
     */
    public static boolean isBlank(String str) {
        if (str == null) {
            return true;
        }
        String trimmed = org.springframework.util.StringUtils.trimWhitespace(str);
        return org.springframework.util.StringUtils.isEmpty(trimmed);
    }

    /**
     * Checks if the string is not empty, not null and not whitespace.
     *
     * @param str string to check, may be null
     *
     * @return {@code true} if the string is not empty and not null and not whitespace
     */
    public static boolean isNotBlank(String str) {
        if (str == null) {
            return false;
        }
        String trimmed = org.springframework.util.StringUtils.trimWhitespace(str);
        return !org.springframework.util.StringUtils.isEmpty(trimmed);
    }

    /**
     * Converts {@link MethodParameter} to user-friendly text.
     *
     * @param methodParam {@link MethodParameter} to convert, may be null
     *
     * @return MethodParameter converted to string
     */
    public static String methodParamToString(MethodParameter methodParam) {
        if (methodParam == null) {
            return null;
        }

        return "method:" + methodParam.getMethod() + ", " +
               "paramName:" + methodParam.getParameterName() + ", " +
               "paramType: " + methodParam.getParameterType();
    }

    /**
     * Checks if the string is pascal case.
     *
     * @param text text to check, may be null
     *
     * @return {@code true} if the text is pascal case, and not blank
     */
    public static boolean isPascalCase(String text) {
        return isNotBlank(text) && text.matches("[A-Z0-9][a-zA-Z0-9]*");
    }

    /**
     * Converts the pascal case to lower camel case.
     *
     * @param text text to convert, may be null
     *
     * @return converted text
     *
     * @throws IllegalArgumentException text is not pascal case
     */
    public static String pascalToCamel(String text) {
        if (isBlank(text)) {
            return text;
        }
        if (!isPascalCase(text)) {
            throw new IllegalArgumentException(String.format("text is not pascal case. [%s]", text));
        }

        char c = text.charAt(0);
        char lc = Character.toLowerCase(c);
        if (c == lc) {
            return text;
        }
        StringBuilder sb = new StringBuilder(text);
        sb.setCharAt(0, lc);
        return sb.toString();
    }

    /**
     * Checks if the string is lower snake case.
     *
     * @param text text to check, may be null
     *
     * @return {@code true} if the text is lower snake case, and not blank
     */
    public static boolean isLowerSnakeCase(String text) {
        return isNotBlank(text) && text.matches("[a-z0-9]+(_[a-z0-9]+)?");
    }

    /**
     * Converts the lower snake case to lower camel case.
     *
     * @param text text to convert, may be null
     *
     * @return converted text
     *
     * @throws IllegalArgumentException text is not lower snake case
     */
    public static String lowerSnakeToCamel(String text) {
        if (isBlank(text)) {
            return text;
        }
        if (!isLowerSnakeCase(text)) {
            throw new IllegalArgumentException(String.format("text is not lower snake case. [%s]", text));
        }

        StringBuilder sb = new StringBuilder(text);

        for (int i = 0; i < sb.length(); i++) {
            if (sb.charAt(i) == '_') {
                sb.deleteCharAt(i);
                sb.replace(i, i + 1, String.valueOf(Character.toUpperCase(sb.charAt(i))));
            }
        }

        return sb.toString();
    }

    /**
     * Checks if the string is upper snake case.
     *
     * @param text text to check, may be null
     *
     * @return {@code true} if the text is upper snake case, and not blank
     */
    public static boolean isUpperSnakeCase(String text) {
        return isNotBlank(text) && text.matches("[A-Z0-9]+(_[A-Z0-9]+)?");
    }

    /**
     * Converts the upper snake case to lower camel case.
     *
     * @param text text to convert, may be null
     *
     * @return converted text
     *
     * @throws IllegalArgumentException text is not upper snake case
     */
    public static String upperSnakeToCamel(String text) {
        if (isBlank(text)) {
            return text;
        }
        if (!isUpperSnakeCase(text)) {
            throw new IllegalArgumentException(String.format("text is not upper snake case. [%s]", text));
        }

        StringBuilder sb = new StringBuilder(text.toLowerCase());

        for (int i = 0; i < sb.length(); i++) {
            if (sb.charAt(i) == '_') {
                sb.deleteCharAt(i);
                sb.replace(i, i + 1, String.valueOf(Character.toUpperCase(sb.charAt(i))));
            }
        }

        return sb.toString();
    }

    /**
     * Checks if the string is lower kebab case.
     *
     * @param text text to check, may be null
     *
     * @return {@code true} if the text is lower kebab case, and not blank
     */
    public static boolean isLowerKebabCase(String text) {
        return isNotBlank(text) && text.matches("[a-z0-9]+(-[a-z0-9]+)?");
    }

    /**
     * Converts the lower kebab case to lower camel case.
     *
     * @param text text to convert, may be null
     *
     * @return converted text
     *
     * @throws IllegalArgumentException text is not lower kebab case
     */
    public static String lowerKebabToCamel(String text) {
        if (isBlank(text)) {
            return text;
        }
        if (!isLowerKebabCase(text)) {
            throw new IllegalArgumentException(String.format("text is not lower kebab case. [%s]", text));
        }

        StringBuilder sb = new StringBuilder(text);

        for (int i = 0; i < sb.length(); i++) {
            if (sb.charAt(i) == '-') {
                sb.deleteCharAt(i);
                sb.replace(i, i + 1, String.valueOf(Character.toUpperCase(sb.charAt(i))));
            }
        }

        return sb.toString();
    }

    /**
     * Checks if the string is upper kebab case.
     *
     * @param text text to check, may be null
     *
     * @return {@code true} if the text is upper kebab case, and not blank
     */
    public static boolean isUpperKebabCase(String text) {
        return isNotBlank(text) && text.matches("[A-Z0-9]+(-[A-Z0-9]+)?");
    }

    /**
     * Converts the upper kebab case to lower camel case.
     *
     * @param text text to convert, may be null
     *
     * @return converted text
     *
     * @throws IllegalArgumentException text is not upper kebab case
     */
    public static String upperKebabToCamel(String text) {
        if (isBlank(text)) {
            return text;
        }
        if (!isUpperKebabCase(text)) {
            throw new IllegalArgumentException(String.format("text is not upper kebab case. [%s]", text));
        }

        StringBuilder sb = new StringBuilder(text.toLowerCase());

        for (int i = 0; i < sb.length(); i++) {
            if (sb.charAt(i) == '-') {
                sb.deleteCharAt(i);
                sb.replace(i, i + 1, String.valueOf(Character.toUpperCase(sb.charAt(i))));
            }
        }

        return sb.toString();
    }

}
