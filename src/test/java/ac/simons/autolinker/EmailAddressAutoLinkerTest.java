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
 * @author Michael J. Simons, 2014-12-28
 */
public class EmailAddressAutoLinkerTest {

    @Test
    public void createLinksShouldWork1() {
	final EmailAddressAutoLinker autoLinker = new EmailAddressAutoLinker(true, true);

	List<Node> result;
	Element a;

	result = autoLinker.createLinks(new TextNode("das ist ein test “@rotnroll666 ohne urls"));
	Assert.assertTrue(result.size() == 1);
	Assert.assertTrue(result.get(0) instanceof TextNode);
	Assert.assertEquals("das ist ein test “@rotnroll666 ohne urls", ((TextNode) result.get(0)).getWholeText());
	
	result = autoLinker.createLinks(new TextNode("  michael.simons@test.com ohne protocoll am ende michael@test.com  "));
	Assert.assertTrue(result.size() == 5);
		
	Assert.assertTrue(result.get(0) instanceof TextNode);
	Assert.assertEquals("  ", ((TextNode) result.get(0)).getWholeText());
	
	Assert.assertTrue(result.get(1) instanceof Element);
	a = (Element) result.get(1);
	Assert.assertEquals("a", a.tagName());
	Assert.assertEquals("mailto:%6d%69%63%68%61%65%6c%2e%73%69%6d%6f%6e%73@%74%65%73%74%2e%63%6f%6d", a.attr("href"));
	Assert.assertEquals("michael [DOT] simons [AT] test [DOT] com", ((TextNode) a.childNode(0)).getWholeText());
		
	Assert.assertTrue(result.get(2) instanceof TextNode);
	Assert.assertEquals(" ohne protocoll am ende ", ((TextNode) result.get(2)).getWholeText());
	
	Assert.assertTrue(result.get(3) instanceof Element);
	a = (Element) result.get(3);
	Assert.assertEquals("a", a.tagName());
	Assert.assertEquals("mailto:%6d%69%63%68%61%65%6c@%74%65%73%74%2e%63%6f%6d", a.attr("href"));
	Assert.assertEquals("michael [AT] test [DOT] com", ((TextNode) a.childNode(0)).getWholeText());
			
	Assert.assertTrue(result.get(4) instanceof TextNode);
	Assert.assertEquals("  ", ((TextNode) result.get(4)).getWholeText());
    }

    @Test
    public void createLinksShouldWork2() {
	final EmailAddressAutoLinker autoLinker = new EmailAddressAutoLinker(false, false);

	List<Node> result;
	Element a;

	result = autoLinker.createLinks(new TextNode("das ist ein test ohne urls"));
	Assert.assertTrue(result.size() == 1);
	Assert.assertTrue(result.get(0) instanceof TextNode);
	Assert.assertEquals("das ist ein test ohne urls", ((TextNode) result.get(0)).getWholeText());
	
	result = autoLinker.createLinks(new TextNode("michael.simons@test.com ohne protocoll am ende michael@test.com"));
	Assert.assertTrue(result.size() == 3);
			
	Assert.assertTrue(result.get(0) instanceof Element);
	a = (Element) result.get(0);
	Assert.assertEquals("a", a.tagName());
	Assert.assertEquals("mailto:michael.simons@test.com", a.attr("href"));
	Assert.assertEquals("michael.simons@test.com", ((TextNode) a.childNode(0)).getWholeText());
		
	Assert.assertTrue(result.get(1) instanceof TextNode);
	Assert.assertEquals(" ohne protocoll am ende ", ((TextNode) result.get(1)).getWholeText());
	
	Assert.assertTrue(result.get(2) instanceof Element);
	a = (Element) result.get(2);
	Assert.assertEquals("a", a.tagName());
	Assert.assertEquals("mailto:michael@test.com", a.attr("href"));
	Assert.assertEquals("michael@test.com", ((TextNode) a.childNode(0)).getWholeText());			
    }
        
    @Test
    public void obfuscateEmailAddressShouldWork() {
	final EmailAddressAutoLinker autoLinker = new EmailAddressAutoLinker(true, true);
	Assert.assertEquals("michael [AT] test [DOT] com", autoLinker.obfuscateEmailAddress("michael@test.com"));
	Assert.assertEquals("michael [DOT] simons [AT] test [DOT] com", autoLinker.obfuscateEmailAddress("michael.simons@test.com"));
	Assert.assertEquals("michael [DOT] simons [AT] test [DOT] com", autoLinker.obfuscateEmailAddress("Michael.Simons@teSt.com"));
    }

    @Test
    public void hexEncodeEmailAddressShouldWork() {
	final EmailAddressAutoLinker autoLinker = new EmailAddressAutoLinker(true, true);
	Assert.assertEquals("%6d%69%63%68%61%65%6c@%74%65%73%74%2e%63%6f%6d", autoLinker.hexEncodeEmailAddress("michael@test.com"));
	Assert.assertEquals("%6d%69%63%68%61%65%6c%2e%73%69%6d%6f%6e%73@%74%65%73%74%2e%63%6f%6d", autoLinker.hexEncodeEmailAddress("michael.simons@test.com"));
	Assert.assertEquals("%6d%69%63%68%61%65%6c%2e%73%69%6d%6f%6e%73@%74%65%73%74%2e%63%6f%6d", autoLinker.hexEncodeEmailAddress("Michael.Simons@teSt.com"));
    }
}
