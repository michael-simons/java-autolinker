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
 *
 * @author Michael J. Simons
 */
public class TwitterUserAutoLinkerTest {

    @Test
    public void createLinksShouldWork() {
	final TwitterUserAutoLinker autoLinker = new TwitterUserAutoLinker();

	List<Node> result;
	Element a;

	result = autoLinker.createLinks(new TextNode("das ist ein test ohne urls"));
	Assert.assertTrue(result.size() == 1);
	Assert.assertTrue(result.get(0) instanceof TextNode);
	Assert.assertEquals("das ist ein test ohne urls", ((TextNode) result.get(0)).getWholeText());

	result = autoLinker.createLinks(new TextNode("das ist eine url ohne @rotnroll666 ohne protocoll"));
	Assert.assertTrue(result.size() == 3);
	Assert.assertTrue(result.get(0) instanceof TextNode);
	Assert.assertEquals("das ist eine url ohne ", ((TextNode) result.get(0)).getWholeText());
	Assert.assertTrue(result.get(1) instanceof Element);
	a = (Element) result.get(1);
	assertTwitterUrl(a);
	Assert.assertTrue(result.get(2) instanceof TextNode);
	Assert.assertEquals(" ohne protocoll", ((TextNode) result.get(2)).getWholeText());

	result = autoLinker.createLinks(new TextNode("@rotnroll666 ohne protocoll am anfang"));
	Assert.assertTrue(result.size() == 2);
	Assert.assertTrue(result.get(0) instanceof Element);
	a = (Element) result.get(0);
	assertTwitterUrl(a);
	Assert.assertTrue(result.get(1) instanceof TextNode);
	Assert.assertEquals(" ohne protocoll am anfang", ((TextNode) result.get(1)).getWholeText());

	result = autoLinker.createLinks(new TextNode("ohne protocoll am ende @rotnroll666"));
	Assert.assertTrue(result.size() == 2);
	Assert.assertTrue(result.get(1) instanceof Element);
	a = (Element) result.get(1);
	assertTwitterUrl(a);
	Assert.assertTrue(result.get(0) instanceof TextNode);
	Assert.assertEquals("ohne protocoll am ende ", ((TextNode) result.get(0)).getWholeText());

	result = autoLinker.createLinks(new TextNode("   @rotnroll666 ohne protocoll am anfang"));
	Assert.assertTrue(result.size() == 3);
	Assert.assertTrue(result.get(0) instanceof TextNode);
	Assert.assertEquals("   ", ((TextNode) result.get(0)).getWholeText());
	Assert.assertTrue(result.get(1) instanceof Element);
	a = (Element) result.get(1);
	assertTwitterUrl(a);
	Assert.assertTrue(result.get(2) instanceof TextNode);
	Assert.assertEquals(" ohne protocoll am anfang", ((TextNode) result.get(2)).getWholeText());

	result = autoLinker.createLinks(new TextNode("ohne protocoll am ende @rotnroll666  "));
	Assert.assertTrue(result.size() == 3);
	Assert.assertTrue(result.get(1) instanceof Element);
	a = (Element) result.get(1);
	assertTwitterUrl(a);
	Assert.assertTrue(result.get(0) instanceof TextNode);
	Assert.assertEquals("ohne protocoll am ende ", ((TextNode) result.get(0)).getWholeText());
	Assert.assertTrue(result.get(2) instanceof TextNode);
	Assert.assertEquals("  ", ((TextNode) result.get(2)).getWholeText());

    }

    private void assertTwitterUrl(Element a) {
	Assert.assertEquals("a", a.tagName());
	Assert.assertEquals("https://twitter.com/rotnroll666", a.attr("href"));
	Assert.assertEquals("@rotnroll666", ((TextNode) a.childNode(0)).getWholeText());

    }

}
