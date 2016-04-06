/*
 * Copyright 2014 michael-simons.eu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ac.simons.autolinker;

import java.util.Optional;

/**
 * @author Michael J. Simons, 2014-12-27
 */
public final class Strings {

    private Strings() {
    }

    /**
     * @see #truncate(java.lang.String, int, java.util.Optional)
     * @param in
     * @param length
     * @return
     */
    public static String truncate(final String in, final int length) {
	return truncate(in, length, Optional.of("…"));
    }

    /**
     * Truncates the string {@code in} to a string with maximal {@code length}
     * chars. If an ellipsis is given, the length of the ellipsis is taken into
     * account as well.
     * <br>
     * This method counts the number of unicode codepoints in the string and the
     * ellipsis, not the number of chars. So any text with a 4 byte utf8 char in
     * it will likely have a length greater than {@code length}.
     *
     * @param in The text to truncate
     * @param length The maximum length
     * @param ellipsis An optional ellipsis
     * @return A truncated string if {@code in} is longher than {@code length}
     */
    public static String truncate(final String in, final int length, final Optional<String> ellipsis) {
	final String _ellipsis = ellipsis.orElse("");
	final int maxLength = length - _ellipsis.codePointCount(0, _ellipsis.length());
	if (maxLength < 0) {
	    throw new IllegalArgumentException("Cannot truncate string to length < 0");
	}

	String rv = in;
	if (in.codePointCount(0, in.length()) > length) {
	    rv = new String(in.codePoints().limit(maxLength).toArray(), 0, maxLength) + _ellipsis;
	}
	return rv;
    }
}
