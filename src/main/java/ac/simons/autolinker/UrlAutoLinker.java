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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Tag;

/**
 * Turns all kinds of plain text urls into anchor elements. If no protocol is
 * given, http is assumend.
 *
 * @author Michael J. Simons, 2014-12-27
 */
public class UrlAutoLinker implements AutoLinker {

    /**
     * Maximum length of the anchor text until it gets truncated.
     */
    private final int maxLabelLength;

    /**
     * Instantiate a new URL autolinker with a given maximum label length.
     *
     * @param maxLabelLength Maximum length of the anchor text until it gets
     * truncated
     */
    public UrlAutoLinker(int maxLabelLength) {
	this.maxLabelLength = maxLabelLength;
    }

    @Override
    public List<Node> createLinks(final TextNode textNode) {

	final List<Node> rv = new ArrayList<>();

	int start = 0;
	final String nodeText = textNode.getWholeText();
	final String baseUri = textNode.baseUri();

	final Matcher matcher = Regex.VALID_URL.matcher(nodeText);
	while (matcher.find()) {
	    // Add a new textnode for everything before the url
	    final String textBefore = String.format("%s%s", nodeText.substring(start, matcher.start()), matcher.group(Regex.VALID_URL_GROUP_BEFORE));
	    if (!textBefore.isEmpty()) {
		rv.add(new TextNode(textBefore, baseUri));
	    }
	    final Optional<String> protocol = Optional.ofNullable(matcher.group(Regex.VALID_URL_GROUP_PROTOCOL));

	    final Element newAnchor = new Element(Tag.valueOf("a"), baseUri);
	    final String url = String.format("%s%s", protocol.isPresent() ? "" : "http://", matcher.group(Regex.VALID_URL_GROUP_URL));
	    newAnchor.attr("href", url);
	    newAnchor.attr("title", url);

	    newAnchor.appendChild(new TextNode(Strings.truncate(matcher.group(Regex.VALID_URL_GROUP_URL).replaceFirst(Pattern.quote(protocol.orElse("http://")), ""), maxLabelLength), baseUri));
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
}
