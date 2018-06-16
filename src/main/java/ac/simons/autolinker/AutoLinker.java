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

import java.util.List;

import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

/**
 * Defines something that can be detected inside a bunch of text node and turned
 * into an html anchor tag. The interface is similar to interface "Linkable"
 * from my 2010er project
 * <a href="https://github.com/michael-simons/java-autolinker">java-autolinker</a>.
 *
 * @author Michael J. Simons, 2012-12-27
 */
public interface AutoLinker {

	/**
	 * Gets the content of <code>node</code> and tries to find linkable content.
	 * If content is found, create new text nodes before and after the content
	 * and a new anchor element with the new link
	 * <br>
	 * If the autolinker makes no changes, just return a list containing the
	 * original node.
	 *
	 * @param textNode The text node which may contain linkable texts
	 * @return The new node list created from {@code textNode}
	 */
	List<Node> createLinks(TextNode textNode);
}
