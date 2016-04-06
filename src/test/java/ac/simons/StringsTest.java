/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ac.simons;

import ac.simons.autolinker.Strings;
import java.util.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * @author Michael J. Simons, 2015-04-01
 */
public class StringsTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void truncateShouldWork1() {
	expectedException.expect(IllegalArgumentException.class);
	expectedException.expectMessage("Cannot truncate string to length < 0");

	Strings.truncate("abc", -1);
    }

    @Test
    public void truncateShouldWork2() {
	expectedException.expect(IllegalArgumentException.class);
	expectedException.expectMessage("Cannot truncate string to length < 0");

	Strings.truncate("abc", 1, Optional.of("..."));
    }

    @Test
    public void truncateShouldWork3() {
	String in;
	String expected;

	in = "1";
	expected = "";
	assertEquals(expected, Strings.truncate(in, 0, Optional.empty()));

	in = "abc";
	expected = "…";
	assertEquals(expected, Strings.truncate(in, 1, Optional.of("…")));

	// Copied from
	// https://raw.githubusercontent.com/google/guava/master/guava-tests/test/com/google/common/base/AsciiTest.java
	assertEquals("foobar", Strings.truncate("foobar", 10, Optional.of("...")));
	assertEquals("fo...", Strings.truncate("foobar", 5, Optional.of("...")));
	assertEquals("foobar", Strings.truncate("foobar", 6, Optional.of("...")));
	assertEquals("...", Strings.truncate("foobar", 3, Optional.of("...")));
	assertEquals("foobar", Strings.truncate("foobar", 10, Optional.of("…")));
	assertEquals("foo…", Strings.truncate("foobar", 4, Optional.of("…")));
	assertEquals("fo--", Strings.truncate("foobar", 4, Optional.of("--")));
	assertEquals("foobar", Strings.truncate("foobar", 6, Optional.of("…")));
	assertEquals("foob…", Strings.truncate("foobar", 5, Optional.of("…")));
	assertEquals("foo", Strings.truncate("foobar", 3, Optional.of("")));
	assertEquals("", Strings.truncate("", 5, Optional.of("")));
	assertEquals("", Strings.truncate("", 5, Optional.of("...")));
	assertEquals("", Strings.truncate("", 0, Optional.of("")));

	// Naive would split the surrogate pair
	assertEquals("12👍…", Strings.truncate("12👍45", 4, Optional.of("…")));
    }
}