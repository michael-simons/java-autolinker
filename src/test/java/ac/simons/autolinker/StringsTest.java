/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ac.simons.autolinker;

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

	Strings.truncate("abc", 1, "...");
    }

    @Test
    public void truncateShouldWork3() {
	String in;
	String expected;

	in = "1";
	expected = "";
	assertEquals(expected, Strings.truncate(in, 0, null));

	in = "abc";
	expected = "…";
	assertEquals(expected, Strings.truncate(in, 1, "…"));

	// Copied from
	// https://raw.githubusercontent.com/google/guava/master/guava-tests/test/com/google/common/base/AsciiTest.java
	assertEquals("foobar", Strings.truncate("foobar", 10, "..."));
	assertEquals("fo...", Strings.truncate("foobar", 5, "..."));
	assertEquals("foobar", Strings.truncate("foobar", 6, "..."));
	assertEquals("...", Strings.truncate("foobar", 3, "..."));
	assertEquals("foobar", Strings.truncate("foobar", 10, "…"));
	assertEquals("foo…", Strings.truncate("foobar", 4, "…"));
	assertEquals("fo--", Strings.truncate("foobar", 4, "--"));
	assertEquals("foobar", Strings.truncate("foobar", 6, "…"));
	assertEquals("foob…", Strings.truncate("foobar", 5, "…"));
	assertEquals("foo", Strings.truncate("foobar", 3, ""));
	assertEquals("", Strings.truncate("", 5, ""));
	assertEquals("", Strings.truncate("", 5, "..."));
	assertEquals("", Strings.truncate("", 0, ""));

	// Naive would split the surrogate pair
	assertEquals("12👍…", Strings.truncate("12👍45", 4, "…"));
    }
}