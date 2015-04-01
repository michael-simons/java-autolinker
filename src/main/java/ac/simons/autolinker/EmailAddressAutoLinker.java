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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Tag;

/**
 * Turns valid ASCII email adresses into anchor text. The label can be
 * obsfuscated, the email address can be encoded to hex.
 *
 * @author Michael J. Simons, 2014-12-27
 */
public class EmailAddressAutoLinker implements AutoLinker {

    /**
     * Regex according to http://www.w3.org/Protocols/rfc822/, Originally
     * written by Cal Henderson
     * (http://iamcal.com/publish/articles/php/parsing_email/), Translated to
     * Ruby by Tim Fletcher, with changes suggested by Dan Kubb. Translated to
     * Java by Michael J. Simons
     */
    private final static String VALID_EMAIL_ADDRESS_REGEX = "(?:[^\\x00-\\x20\\x22\\x28\\x29\\x2c\\x2e\\x3a-\\x3c\\x3e\\x40\\x5b-\\x5d\\x7f-\\xff]+|\\x22(?:[^\\x0d\\x22\\x5c\\x80-\\xff]|\\x5c[\\x00-\\x7f])*\\x22)(?:\\x2e(?:[^\\x00-\\x20\\x22\\x28\\x29\\x2c\\x2e\\x3a-\\x3c\\x3e\\x40\\x5b-\\x5d\\x7f-\\xff]+|\\x22(?:[^\\x0d\\x22\\x5c\\x80-\\xff]|\\x5c[\\x00-\\x7f])*\\x22))*\\x40(?:[^\\x00-\\x20\\x22\\x28\\x29\\x2c\\x2e\\x3a-\\x3c\\x3e\\x40\\x5b-\\x5d\\x7f-\\xff]+|\\x5b(?:[^\\x0d\\x5b-\\x5d\\x80-\\xff]|\\x5c[\\x00-\\x7f])*\\x5d)(?:\\x2e(?:[^\\x00-\\x20\\x22\\x28\\x29\\x2c\\x2e\\x3a-\\x3c\\x3e\\x40\\x5b-\\x5d\\x7f-\\xff]+|\\x5b(?:[^\\x0d\\x5b-\\x5d\\x80-\\xff]|\\x5c[\\x00-\\x7f])*\\x5d))*";
    public final static Pattern VALID_EMAIL_ADDRESS = Pattern.compile(String.format("\\A%s\\z", VALID_EMAIL_ADDRESS_REGEX));
    public final static Pattern VALID_EMAIL_ADRESS_ML = Pattern.compile(String.format("(?m)(?<![^\\s])%s", VALID_EMAIL_ADDRESS_REGEX));
    public final static Pattern AT_SIGNS = Pattern.compile("[@\uFF20\\x40]");

    /**
     * A flag if the addresses in the mailto: protocoll should be hex-encoded
     */
    private final boolean hexEncodeEmailAddress;
    /**
     * A flag if the labels should be obfuscated
     */
    private final boolean obfuscateEmailAddress;

    /**
     * Instantiates a new email address autolinker.
     *
     * @param hexEncodeEmailAddress Should mailto: Addresses be hex-encoded?
     * @param obfuscateEmailAddress Should labels be obfuscated?
     */
    public EmailAddressAutoLinker(
	    boolean hexEncodeEmailAddress,
	    boolean obfuscateEmailAddress
    ) {
	this.hexEncodeEmailAddress = hexEncodeEmailAddress;
	this.obfuscateEmailAddress = obfuscateEmailAddress;
    }

    @Override
    public List<Node> createLinks(final TextNode textNode) {

	final List<Node> rv = new ArrayList<>();

	int start = 0;

	final String nodeText = textNode.getWholeText();
	final String baseUri = textNode.baseUri();

	final Matcher matcher = VALID_EMAIL_ADRESS_ML.matcher(nodeText);

	while (matcher.find()) {
	    final String emailAddress = matcher.group();

	    if (!(new String(emailAddress.getBytes(), StandardCharsets.US_ASCII)).equals(emailAddress)) {
		continue;
	    }

	    final String textBefore = nodeText.substring(start, matcher.start());
	    if (!textBefore.isEmpty()) {
		rv.add(new TextNode(textBefore, baseUri));
	    }
	    final Element newAnchor = new Element(Tag.valueOf("a"), baseUri);
	    newAnchor.attr("href", String.format("%s%s", "mailto:", hexEncodeEmailAddress ? hexEncodeEmailAddress(emailAddress) : emailAddress));
	    newAnchor.appendChild(new TextNode(obfuscateEmailAddress ? obfuscateEmailAddress(emailAddress) : emailAddress, baseUri));
	    rv.add(newAnchor);
	    start = matcher.end();
	}
	// Add a new textnode for everything after
	final String textAfter = nodeText.substring(start);
	if (!textAfter.isEmpty()) {
	    rv.add(new TextNode(textAfter, baseUri));
	}
	return rv;
    }

    /**
     * Obfuscates an email address. @ will be replaced throught " [AT] " and .
     * through " [DOT] ". The email address is lowercased before processing.
     *
     * @param emailAddress The email address to obfuscate
     * @return An obfuscated email address
     */
    public String obfuscateEmailAddress(final String emailAddress) {
	return AT_SIGNS.matcher(emailAddress.toLowerCase()).replaceAll(" [AT] ").replaceAll("\\.", " [DOT] ");
    }

    /**
     * Hex encodes an email addess, leaving the '@' intact. Browsers are able to
     * decode this and maybe it's stops spammers from using emails like that.
     * The email address is lowercased before processing.
     *
     * @param emailAddress The email address that should be encoded to
     * hexadecimal
     * @return An hexadecimal encoded email adresse
     */
    public String hexEncodeEmailAddress(String emailAddress) {
	final String emailAddressLc = emailAddress.toLowerCase();
	final StringBuilder rv = new StringBuilder();
	for (int i = 0; i < emailAddressLc.length(); ++i) {
	    char c = emailAddressLc.charAt(i);
	    rv.append(c == '@' ? c : String.format("%%%x", (int) c));
	}
	return rv.toString();
    }

}
