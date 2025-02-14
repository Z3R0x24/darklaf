/*
 * MIT License
 *
 * Copyright (c) 2021 Jannis Weis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package com.github.weisj.darklaf.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;

import com.github.weisj.darklaf.properties.icons.IconResolver;
import com.github.weisj.darklaf.properties.parser.ParseResult;
import com.github.weisj.darklaf.properties.parser.Parser;
import com.github.weisj.darklaf.properties.parser.ParserContext;
import com.github.weisj.darklaf.util.*;

/**
 * Utility class for loading and parsing the contents of .property files.
 *
 * @author Jannis Weis
 */
public final class PropertyLoader {
    private static final Logger LOGGER = LogUtil.getLogger(PropertyLoader.class);

    private static final char REFERENCE_PREFIX = '%';

    public static Properties loadProperties(final Class<?> clazz, final String name, final String path) {
        final Properties properties = new Properties();
        String p = path + name + ".properties";
        try (InputStream stream = clazz.getResourceAsStream(p)) {
            properties.load(stream);
        } catch (IOException | NullPointerException e) {
            LOGGER.log(Level.SEVERE, "Could not load " + p + " " + e.getMessage(), e);
        }
        return properties;
    }

    public static void putProperties(final Properties properties, final Properties accumulator,
            final UIDefaults currentDefaults, final IconResolver iconResolver) {
        putProperties(properties, properties.stringPropertyNames(), accumulator, currentDefaults, iconResolver);
    }

    public static void putProperties(final Properties properties, final UIDefaults defaults,
            final IconResolver iconResolver) {
        putProperties(properties, properties.stringPropertyNames(), defaults, defaults, iconResolver);
    }

    public static void putProperties(final Map<Object, Object> properties, final Set<String> keys,
            final Map<Object, Object> accumulator, final UIDefaults currentDefaults, final IconResolver iconResolver) {
        ParserContext context = new ParserContext(accumulator, currentDefaults, iconResolver);
        for (final String key : keys) {
            final String value = properties.get(key).toString();
            ParseResult parseResult = Parser.parse(Parser.createParseResult(key, value), context);
            if (parseResult.finished) {
                Object result = parseResult.result;
                if (result != null) {
                    if (Parser.isDebugMode()) {
                        accumulator.put(parseResult.key, parseResult);
                    } else {
                        accumulator.put(parseResult.key, result);
                    }
                } else {
                    currentDefaults.remove(parseResult.key);
                }
            }
        }
    }

    public static void replaceProperties(final Map<Object, Object> properties,
            final Predicate<Map.Entry<Object, Object>> predicate,
            final Function<Map.Entry<Object, Object>, Object> mapper) {
        replacePropertyEntriesOfType(Object.class, properties, predicate, mapper);
    }

    public static <T> void replacePropertiesOfType(final Class<T> type, final Map<Object, Object> properties,
            final Function<T, T> mapper) {
        replacePropertyEntriesOfType(type, properties, e -> true, e -> mapper.apply(e.getValue()));
    }

    public static <T> void replacePropertiesOfType(final Class<T> type, final Map<Object, Object> properties,
            final Predicate<Map.Entry<Object, T>> predicate, final Function<T, T> mapper) {
        replacePropertyEntriesOfType(type, properties, predicate, e -> mapper.apply(e.getValue()));
    }

    @SuppressWarnings("unchecked")
    public static <T> void replacePropertyEntriesOfType(final Class<T> type, final Map<Object, Object> properties,
            final Predicate<Map.Entry<Object, T>> predicate, final Function<Map.Entry<Object, T>, T> mapper) {
        properties.entrySet().stream().filter(e -> type == Object.class || type.isInstance(e.getValue()))
                .filter(e -> predicate.test((Map.Entry<Object, T>) e))
                .forEach(e -> Optional.ofNullable(mapper.apply((Map.Entry<Object, T>) e)).ifPresent(e::setValue));
    }

    public static String getReferencePrefix() {
        return String.valueOf(REFERENCE_PREFIX);
    }
}
