/*
 * Copyright 2014-2018 michael-simons.eu.
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
import java.util.regex.Matcher;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Tag;

/**
 * Turns @mentions into anchor elements.
 * 
 * @author Michael J. Simons, 2014-12-27
 */
public class TwitterUserAutoLinker implements AutoLinker {

    @Override
    public List<Node> createLinks(final TextNode textNode) {

	final List<Node> rv = new ArrayList<>();

	int start = 0;

	final String nodeText = textNode.getWholeText();
	final String baseUri = textNode.baseUri();
	final Matcher matcher = Regex.VALID_MENTION_OR_LIST.matcher(nodeText);

	while (matcher.find()) {
	    // Add a new textnode for everything before the url
	    final String textBefore = String.format("%s%s", nodeText.substring(start, matcher.start()), matcher.group(1));
	    if (!textBefore.isEmpty()) {
		rv.add(new TextNode(textBefore));
	    }
	    final Element newAnchor = new Element(Tag.valueOf("a"), baseUri);
	    newAnchor.attr("href", String.format("https://twitter.com/%s", matcher.group(Regex.VALID_MENTION_OR_LIST_GROUP_USERNAME)));
	    newAnchor.appendChild(new TextNode(String.format("@%s", matcher.group(Regex.VALID_MENTION_OR_LIST_GROUP_USERNAME))));
	    rv.add(newAnchor);
	    start = matcher.end();
	}

	// Add a new textnode for everything after
	final String textAfter = nodeText.substring(start);
	if (!textAfter.isEmpty()) {
	    rv.add(new TextNode(textAfter));
	}
	return rv;
    }
}
