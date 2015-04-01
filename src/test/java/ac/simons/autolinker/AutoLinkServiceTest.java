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

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Michael J. Simons
 */
public class AutoLinkServiceTest {

    static List<Node> makeAutoLinker(final TextNode textNode, final String pattern) {
	final String text = textNode.getWholeText();
	final String baseUri = textNode.baseUri();
	final List<Node> nodes = new ArrayList<>();
	final Matcher matcher = Pattern.compile(pattern).matcher(text);
	int start = 0;
	while (matcher.find()) {
	    final String textBefore = text.substring(start, matcher.start());
	    if (!textBefore.isEmpty()) {
		nodes.add(new TextNode(textBefore, baseUri));
	    }
	    nodes.add(new TextNode("THELINK!", baseUri));
	    start = matcher.end();
	}
	final String textAfter = text.substring(start);
	if (!textAfter.isEmpty()) {
	    nodes.add(new TextNode(textAfter, baseUri));
	}

	return nodes;

    }

    static List<Node> autoLink1(final TextNode textNode) {
	return makeAutoLinker(textNode, "(linkme)");
    }

    static List<Node> autoLink2(final TextNode textNode) {
	return makeAutoLinker(textNode, "(meToo)");
    }

    @Test
    public void applyAutoLinkerShouldWork() {
	final AutoLinkService autoLinkService = new AutoLinkService(new ArrayList<>());

	Document document;
	Document processedDocument;
	Element newElement;
	String in, expected;

	final AutoLinker autolinker = AutoLinkServiceTest::autoLink1;

	document = Jsoup.parseBodyFragment("<p>das ist linkme ein test</p>");
	newElement = autoLinkService.applyAutoLinker(autolinker, document);
	processedDocument = new Document("");
	processedDocument.appendChild(newElement);
	Assert.assertEquals("<p>das ist THELINK! ein test</p>", processedDocument.body().html().trim());

	document = Jsoup.parseBodyFragment("<p></p>");
	newElement = autoLinkService.applyAutoLinker(autolinker, document);
	processedDocument = new Document("");
	processedDocument.appendChild(newElement);
	Assert.assertEquals("<p></p>", processedDocument.body().html().trim());

	document = Jsoup.parseBodyFragment("<p>Einfach nur so ein Text</p>");
	newElement = autoLinkService.applyAutoLinker(autolinker, document);
	processedDocument = new Document("");
	processedDocument.appendChild(newElement);
	Assert.assertEquals("<p>Einfach nur so ein Text</p>", processedDocument.body().html().trim());

	document = Jsoup.parseBodyFragment("<p>Einfach nur so ein Text mit einem <a href=\"http://heise.de\">linkme</a>.</p>");
	newElement = autoLinkService.applyAutoLinker(autolinker, document);
	processedDocument = new Document("");
	processedDocument.appendChild(newElement);
	Assert.assertEquals("<p>Einfach nur so ein Text mit einem <a href=\"http://heise.de\">linkme</a>.</p>", processedDocument.body().html().trim());

	in = "<p>Einfach </p><form><input type=\"text\" /></form><p> nur so ein Text</p>";
	expected = in;
	document = Jsoup.parseBodyFragment(in);
	newElement = autoLinkService.applyAutoLinker(autolinker, document);
	processedDocument = new Document("");
	processedDocument.appendChild(newElement);
	processedDocument.outputSettings().prettyPrint(false).syntax(Syntax.xml);
	Assert.assertEquals(expected, processedDocument.body().html().trim());

	in = "<p>das <!-- das ist ein kommentar --> ist ein linkme.</p>";
	expected = "<p>das <!-- das ist ein kommentar --> ist ein THELINK!.</p>";
	document = Jsoup.parseBodyFragment(in);
	newElement = autoLinkService.applyAutoLinker(autolinker, document);
	processedDocument = new Document("");
	processedDocument.appendChild(newElement);
	processedDocument.outputSettings().prettyPrint(false).syntax(Syntax.xml);
	Assert.assertEquals(expected, processedDocument.body().html().trim());

	in = "<p>Einfach </p><form><input type=\"text\" /><div><p>das <!-- das ist ein kommentar --> ist ein linkme.</p></div></form><p> nur so ein Text</p>";
	expected = "<p>Einfach </p><form><input type=\"text\" /><div><p>das <!-- das ist ein kommentar --> ist ein THELINK!.</p></div></form><p> nur so ein Text</p>";
	document = Jsoup.parseBodyFragment(in);
	newElement = autoLinkService.applyAutoLinker(autolinker, document);
	processedDocument = new Document("");
	processedDocument.appendChild(newElement);
	processedDocument.outputSettings().prettyPrint(false).syntax(Syntax.xml);
	Assert.assertEquals(expected, processedDocument.body().html().trim());
    }

    @Test
    public void addLinksShouldWork() {
	final AutoLinkService autoLinkService = new AutoLinkService(Arrays.asList(AutoLinkServiceTest::autoLink1, AutoLinkServiceTest::autoLink2));

	String in, expected;

	in = "Einfach nur so ein Text";
	expected = in;
	Assert.assertEquals(expected, autoLinkService.addLinks(in, Optional.empty()));

	in = "Einfach nur linkme so ein Text und meToo.";
	expected = "Einfach nur THELINK! so ein Text und THELINK!.";
	Assert.assertEquals(expected, autoLinkService.addLinks(in, Optional.empty()));

	Assert.assertNull(autoLinkService.addLinks(null, Optional.empty()));
	Assert.assertEquals("", autoLinkService.addLinks("", Optional.empty()));
	Assert.assertEquals("	", autoLinkService.addLinks("	", Optional.empty()));
	Assert.assertEquals(" ", autoLinkService.addLinks(" ", Optional.empty()));
    }

    @Test
    public void oldJavaAutolinkerTest() throws IOException {
	Properties texts = new Properties();
	texts.load(new InputStreamReader(AutoLinkServiceTest.class.getResourceAsStream("/ac/simons/autolinker/testdata.properties"), StandardCharsets.UTF_8));

	final Set<String> keys = new TreeSet<>();

	for (Object _prop : texts.keySet()) {
	    String property = (String) _prop;
	    keys.add(property.substring(0, property.indexOf(".")));
	}

	final AutoLinkService autoLinkService = new AutoLinkService(Arrays.asList(
		new EmailAddressAutoLinker(true, true),
		new TwitterUserAutoLinker(),
		new UrlAutoLinker(30)		
	));
			
	int cnt = keys.stream().map((key) -> {
	    final String in = String.format("%s.in", key.trim());
	    final String out = String.format("%s.out", key.trim());
	    String result = autoLinkService.addLinks(texts.getProperty(in), Optional.empty());	    
	    Assert.assertEquals("Test " + key + " fails", texts.getProperty(out), result);
	    return key;
	}).map((_item) -> 1).reduce(0, Integer::sum);
	Assert.assertTrue(cnt > 0);
    }
}
