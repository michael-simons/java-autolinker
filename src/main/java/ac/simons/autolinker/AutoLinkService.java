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
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;

/**
 * @author Michael J. Simons, 2014-12-27
 */
public class AutoLinkService {

    private final List<AutoLinker> autolinkers;
    
    public AutoLinkService(List<AutoLinker> autolinkers) {
	this.autolinkers = autolinkers;
    }

    /**
     * Applies an autolinker to a given element (or a document, which is an
     * element itself) and creates a new element from the nodes the autolinker
     * returns.
     *
     * @param element The element whos text nodes should be autolinked
     * @param autoLinker
     * @return An optional processed element. The optional will be empty if
     * {@code element} is an anchor
     */
    Element applyAutoLinker(final AutoLinker autoLinker, final Element element) {
	final List<Node> newChildNodes = element.childNodes().stream()
		.collect(ArrayList::new,
			(l, childNode) -> {
			    // Child node is itself an element with children
			    if (childNode instanceof Element) {
				final Element childElement = (Element) childNode;
				if (childElement.tagName().equals("a")) {
				    l.add(childElement);
				} else {
				    l.add(applyAutoLinker(autoLinker, childElement));
				}
			    } // Only TextNodes may have possible urls
			    else if (childNode instanceof TextNode) {
				l.addAll(autoLinker.createLinks(((TextNode) childNode)));
			    } // Other nodes are just kept
			    else {
				l.add(childNode);
			    }
			},
			ArrayList::addAll
		);
	final Element rv = new Element(element.tag(), element.baseUri(), element.attributes());
	newChildNodes.forEach(rv::appendChild);
	return rv;
    }

    /**
     * Looks through a text with linkable stuff and applies all configured
     * {@link AutoLinker} to this text.
     *
     * @param textWithLinkableStuff A list that may contain urls and such
     * @param baseUrl An optional base url for resolving relative urls
     * @return A new text with urls turned into ancher tags.
     */    
    public String addLinks(final String textWithLinkableStuff, final Optional<String> baseUrl) {
	String rv = textWithLinkableStuff;
	if (!(rv == null || rv.trim().isEmpty())) {
	    // Create a document
	    final Document document = Jsoup.parseBodyFragment(rv, baseUrl.orElse(""));
	    // Let each linkable process the document
	    autolinkers.forEach((autoLinker) -> {
		final Element body = document.body();
		body.replaceWith(applyAutoLinker(autoLinker, body));
	    });
	    // As the linkables do their work in place, use the document as return
	    document
		    .outputSettings()
		    .prettyPrint(false)
		    .escapeMode(EscapeMode.xhtml)
		    .charset(StandardCharsets.UTF_8);
	    rv = Parser.unescapeEntities(document.body().html().trim(), true);
	}
	return rv;
    }
}
