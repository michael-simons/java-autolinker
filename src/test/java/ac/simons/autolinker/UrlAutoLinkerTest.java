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
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Michael J. Simons, 2014-12-27
 */
public class UrlAutoLinkerTest {

    @Test
    public void createLinksShouldWork() {
	final UrlAutoLinker autoLinker = new UrlAutoLinker(30);

	List<Node> result;
	Element a;

	result = autoLinker.createLinks(new TextNode("das ist ein test ohne urls"));
	Assert.assertTrue(result.size() == 1);
	Assert.assertTrue(result.get(0) instanceof TextNode);
	Assert.assertEquals("das ist ein test ohne urls", ((TextNode) result.get(0)).getWholeText());

	result = autoLinker.createLinks(new TextNode("das ist eine url ohne twitter.com ohne protocoll"));
	Assert.assertTrue(result.size() == 3);
	Assert.assertTrue(result.get(0) instanceof TextNode);
	Assert.assertEquals("das ist eine url ohne ", ((TextNode) result.get(0)).getWholeText());
	Assert.assertTrue(result.get(1) instanceof Element);
	a = (Element) result.get(1);
	Assert.assertEquals("a", a.tagName());
	Assert.assertEquals("http://twitter.com", a.attr("href"));
	Assert.assertEquals("http://twitter.com", a.attr("title"));
	Assert.assertEquals("twitter.com", ((TextNode) a.childNode(0)).getWholeText());
	Assert.assertTrue(result.get(2) instanceof TextNode);
	Assert.assertEquals(" ohne protocoll", ((TextNode) result.get(2)).getWholeText());

	result = autoLinker.createLinks(new TextNode("twitter.com ohne protocoll am anfang"));
	Assert.assertTrue(result.size() == 2);
	Assert.assertTrue(result.get(0) instanceof Element);
	a = (Element) result.get(0);
	Assert.assertEquals("a", a.tagName());
	Assert.assertEquals("http://twitter.com", a.attr("href"));
	Assert.assertEquals("http://twitter.com", a.attr("title"));
	Assert.assertEquals("twitter.com", ((TextNode) a.childNode(0)).getWholeText());
	Assert.assertTrue(result.get(1) instanceof TextNode);
	Assert.assertEquals(" ohne protocoll am anfang", ((TextNode) result.get(1)).getWholeText());

	result = autoLinker.createLinks(new TextNode("twitter.com ohne protocoll am anfang mit am ende https://dailyfratze.de?foo=bar"));
	Assert.assertTrue(result.size() == 3);
	Assert.assertTrue(result.get(0) instanceof Element);
	a = (Element) result.get(0);
	Assert.assertEquals("a", a.tagName());
	Assert.assertEquals("http://twitter.com", a.attr("href"));
	Assert.assertEquals("http://twitter.com", a.attr("title"));
	Assert.assertEquals("twitter.com", ((TextNode) a.childNode(0)).getWholeText());
	Assert.assertTrue(result.get(1) instanceof TextNode);
	Assert.assertEquals(" ohne protocoll am anfang mit am ende ", ((TextNode) result.get(1)).getWholeText());
	Assert.assertTrue(result.get(2) instanceof Element);
	a = (Element) result.get(2);
	Assert.assertEquals("a", a.tagName());
	Assert.assertEquals("https://dailyfratze.de?foo=bar", a.attr("href"));
	Assert.assertEquals("https://dailyfratze.de?foo=bar", a.attr("title"));
	Assert.assertEquals("dailyfratze.de?foo=bar", ((TextNode) a.childNode(0)).getWholeText());
	
	
	Assert.assertTrue(result.get(2) instanceof Element);
	a = (Element) result.get(2);
	Assert.assertEquals("a", a.tagName());
	Assert.assertEquals("https://dailyfratze.de?foo=bar", a.attr("href"));
	Assert.assertEquals("https://dailyfratze.de?foo=bar", a.attr("title"));
	Assert.assertEquals("dailyfratze.de?foo=bar", ((TextNode) a.childNode(0)).getWholeText());

	result = autoLinker.createLinks(new TextNode("das ist eine url ohne https://dailyfratze.de/app/tags/CoStarring/Anton#taggedPictures ohne protocoll"));
	Assert.assertTrue(result.get(1) instanceof Element);
	a = (Element) result.get(1);
	Assert.assertEquals("a", a.tagName());
	Assert.assertEquals("https://dailyfratze.de/app/tags/CoStarring/Anton#taggedPictures", a.attr("href"));
	Assert.assertEquals("https://dailyfratze.de/app/tags/CoStarring/Anton#taggedPictures", a.attr("title"));
	Assert.assertEquals("dailyfratze.de/app/tags/CoSta…", ((TextNode) a.childNode(0)).getWholeText());
	
	result = autoLinker.createLinks(new TextNode("  twitter.com ohne protocoll am anfang mit am ende https://dailyfratze.de?foo=bar  "));
	Assert.assertTrue(result.size() == 5);
	Assert.assertTrue(result.get(0) instanceof TextNode);
	Assert.assertEquals("  ", ((TextNode) result.get(0)).getWholeText());
	Assert.assertTrue(result.get(1) instanceof Element);
	a = (Element) result.get(1);
	Assert.assertEquals("a", a.tagName());
	Assert.assertEquals("http://twitter.com", a.attr("href"));
	Assert.assertEquals("http://twitter.com", a.attr("title"));
	Assert.assertEquals("twitter.com", ((TextNode) a.childNode(0)).getWholeText());
	Assert.assertTrue(result.get(2) instanceof TextNode);
	Assert.assertEquals(" ohne protocoll am anfang mit am ende ", ((TextNode) result.get(2)).getWholeText());
	Assert.assertTrue(result.get(3) instanceof Element);
	a = (Element) result.get(3);
	Assert.assertEquals("a", a.tagName());
	Assert.assertEquals("https://dailyfratze.de?foo=bar", a.attr("href"));
	Assert.assertEquals("https://dailyfratze.de?foo=bar", a.attr("title"));
	Assert.assertEquals("dailyfratze.de?foo=bar", ((TextNode) a.childNode(0)).getWholeText());
	Assert.assertTrue(result.get(4) instanceof TextNode);
	Assert.assertEquals("  ", ((TextNode) result.get(4)).getWholeText());

    }
}
