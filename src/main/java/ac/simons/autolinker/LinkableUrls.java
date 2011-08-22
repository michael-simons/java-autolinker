/**
 * Created by Michael Simons, michael-simons.eu
 * and released under The BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 *
 * Copyright (c) 2010, Michael Simons
 * All rights reserved.
 *
 * Redistribution  and  use  in  source   and  binary  forms,  with  or   without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source   code must retain   the above copyright   notice,
 *   this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary  form must reproduce  the above copyright  notice,
 *   this list of conditions  and the following  disclaimer in the  documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name  of  michael-simons.eu   nor the names  of its contributors
 *   may be used  to endorse   or promote  products derived  from  this  software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS  PROVIDED BY THE  COPYRIGHT HOLDERS AND  CONTRIBUTORS "AS IS"
 * AND ANY  EXPRESS OR  IMPLIED WARRANTIES,  INCLUDING, BUT  NOT LIMITED  TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL  THE COPYRIGHT HOLDER OR CONTRIBUTORS  BE LIABLE
 * FOR ANY  DIRECT, INDIRECT,  INCIDENTAL, SPECIAL,  EXEMPLARY, OR  CONSEQUENTIAL
 * DAMAGES (INCLUDING,  BUT NOT  LIMITED TO,  PROCUREMENT OF  SUBSTITUTE GOODS OR
 * SERVICES; LOSS  OF USE,  DATA, OR  PROFITS; OR  BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT  LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE  USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ac.simons.autolinker;

import static ac.simons.utils.StringUtils.isBlank;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Tag;

import ac.simons.utils.StringUtils;

/**
 * @author Michael J. Simons
 */
public class LinkableUrls implements Linkable {
	// The following regex are based on https://github.com/mzsanford/twitter-text-java/blob/master/src/com/twitter/Regex.java
	// and are licensed under the Apache License 2.0:
	// http://www.apache.org/licenses/LICENSE-2.0
	private static final String urlValidPreceedingChars = "(?:[^\\-/\"':!=A-Z0-9_@＠]+|^|\\:)";		
	private static final String urlValidDomain = "(?:[^\\p{Punct}\\s][\\.-](?=[^\\p{Punct}\\s])|[^\\p{Punct}\\s]){1,}\\.[a-z]{2,}(?::[0-9]+)?";
	private static final String urlValidGeneralPathChars = "[a-z0-9!\\*';:=\\+\\$/%#\\[\\]\\-_,~]";
	/** 
	 * Allow URL paths to contain balanced parens
	 * 1. Used in Wikipedia URLs like /Primer_(film)
	 * 2. Used in IIS sessions like /S(dfd346)/
	 */
	private static final String urlBalanceParens = "(?:\\(" + urlValidGeneralPathChars + "+\\))";		
	private static final String urlValidUrlPathChars = 
		"(?:" +
		urlBalanceParens +
	    "|@" + urlValidGeneralPathChars + "+/" +
	    "|[\\.,]?" + urlValidGeneralPathChars + "+" +
	    ")";

	/** 
	 * 
	 * Valid end-of-path chracters (so /foo. does not gobble the period).
	 * 2. Allow =&# for empty URL parameters and other URL-join artifacts
	 */
	private static final String urlValidUrlPathEndingChars = "(?:[a-z0-9=_#/]+|"+urlBalanceParens+")";
	private static final String urlValidUrlQueryChars = "[a-z0-9!\\*'\\(\\);:&=\\+\\$/%#\\[\\]\\-_\\.,~]";
	private static final String urlValidUrlQueryEndingChars = "[a-z0-9_&=#/]";
	private static final String validUrlPatternString =		
		"(?ims)" +
		"(" +                                                         //  $1 total match
	      "(" + urlValidPreceedingChars + ")" +                       //  $2 Preceeding chracter	     
	      "(" +                                                       //  $3 URL
	        "(https?://)" +                                           //  $4 Protocol
	        "(" + urlValidDomain + ")" +                              //  $5 Domain(s) and optional port number
	        "(/" +
	          "(?:" +
	            urlValidUrlPathChars + "+" +
	              urlValidUrlPathEndingChars + "|" +                  //     1+ path chars and a valid last char
	            urlValidUrlPathChars + "+" +
	              urlValidUrlPathEndingChars + "?|" +                 //     Optional last char to handle /@foo/ case
	            urlValidUrlPathEndingChars +                          //     Just a # case
	          ")?" +
	        ")?" +                                                    //  $6 URL Path and anchor
	        "(\\?" + urlValidUrlQueryChars + "*" +                    //  $7 Query String
	                urlValidUrlQueryEndingChars + ")?" +
	        "|" + 
	        "(www\\." + urlValidDomain + ")" +
	      ")" +
	    ")"
	  ;
	public static final Pattern validUrl = Pattern.compile(validUrlPatternString);
	
	private int maxLabelLength = 30;
	
	@Override
	public  boolean linkTo(final List<Node> changedNodes, final TextNode node, final String baseUri) {
		boolean changed = false;
		int start = 0;
		final String nodeText = node.getWholeText();
		final Matcher matcher = validUrl.matcher(nodeText);
		while(matcher.find()) {				
			// Add a new textnode for everything before the url
			final String textBefore = String.format("%s%s", nodeText.substring(start, matcher.start()), matcher.group(2));
			if(!isBlank(textBefore))
				changedNodes.add(new TextNode(textBefore, baseUri));
			final String protocol = matcher.group(4) == null || matcher.group(4).length() == 0 ? "http://" : "";
			final Element newAnchor = new Element(Tag.valueOf("a"), baseUri);					
			final String hrefAndTitle = String.format("%s%s", protocol, matcher.group(3));
			newAnchor.attr("href",  hrefAndTitle);
			newAnchor.attr("title", hrefAndTitle);			
			newAnchor.appendChild(new TextNode(StringUtils.truncate(matcher.group(3).replaceFirst(Pattern.quote(StringUtils.nvl(matcher.group(4), protocol)), ""), maxLabelLength), baseUri));
			changedNodes.add(newAnchor);
			start = matcher.end();
			changed = true;
		}
		// Add a new textnode for everything after
		final String textAfter = nodeText.substring(start);
		if(!isBlank(textAfter))
			changedNodes.add(new TextNode(textAfter, baseUri));
		return changed;
	}

	public int getMaxLabelLength() {
		return maxLabelLength;
	}

	public void setMaxLabelLength(int maxLabelLength) {
		this.maxLabelLength = maxLabelLength;
	}	
}