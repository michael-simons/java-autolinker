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

	public AutoLinkService(final List<AutoLinker> autolinkers) {
		this.autolinkers = autolinkers;
	}

	/**
	 * Applies an autolinker to a given element (or a document, which is an
	 * element itself) and creates a new element from the nodes the autolinker
	 * returns.
	 *
	 * @param element    The element whos text nodes should be autolinked
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
					} else if (childNode instanceof TextNode) { // Only TextNodes may have possible urls
						l.addAll(autoLinker.createLinks(((TextNode) childNode)));
					} else { // Other nodes are just kept
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
	 * @param textWithLinkableStuff The text that contains possible urls
	 * @param baseUrl               Base url for creating absolute urls from relative urls
	 * @return The text with all recognizable URLs turned into links
	 * @see #addLinks(java.lang.String, java.lang.String, java.lang.Class)
	 */
	public String addLinks(final String textWithLinkableStuff, final String baseUrl) {
		return addLinks(textWithLinkableStuff, baseUrl, String.class);
	}

	/**
	 * Looks through a text with linkable stuff and applies all configured
	 * {@link AutoLinker} to this text.
	 *
	 * @param <T>                   Type of the resulting document with embedded links
	 * @param textWithLinkableStuff A list that may contain urls and such
	 * @param baseUrl               An optional base url for resolving relative urls
	 * @param targetClass           Class of the generated document
	 * @return A new text with urls turned into anchor tags.
	 */
	public <T> T addLinks(final String textWithLinkableStuff, final String baseUrl, final Class<? extends T> targetClass) {
		var optionalBaseUrl = Optional.ofNullable(baseUrl);
		T rv;
		if (String.class.isAssignableFrom(targetClass)) {
			rv = (T) textWithLinkableStuff;
		} else if (Document.class.isAssignableFrom(targetClass)) {
			rv = (T) Document.createShell(optionalBaseUrl.orElse(""));
		} else {
			throw new RuntimeException(String.format("Invalid target class: %s", targetClass.getName()));
		}

		if (!(textWithLinkableStuff == null || textWithLinkableStuff.trim().isEmpty())) {
			// Create a document
			final Document document = addLinks(Jsoup.parseBodyFragment(textWithLinkableStuff, optionalBaseUrl.orElse("")));
			// As the linkables do their work in place, use the document as return
			if (Document.class.isAssignableFrom(targetClass)) {
				rv = (T) document;
			} else {
				document
					.outputSettings()
					.prettyPrint(false)
					.escapeMode(EscapeMode.xhtml)
					.charset(StandardCharsets.UTF_8);
				rv = (T) Parser.unescapeEntities(document.body().html().trim(), true);
			}
		}
		return rv;
	}

	/**
	 * A convenience method for adding links in an existing document.
	 *
	 * @param document Existing document, will be modified
	 * @return Modified document with autolinked urls
	 * @see #addLinks(java.lang.String, java.lang.String, java.lang.Class)
	 */
	public Document addLinks(final Document document) {
		// Let each linkable process the document
		autolinkers.forEach((autoLinker) -> {
			final Element body = document.body();
			body.replaceWith(applyAutoLinker(autoLinker, body));
		});
		return document;
	}
}
