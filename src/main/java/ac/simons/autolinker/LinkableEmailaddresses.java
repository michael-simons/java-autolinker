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
public class LinkableEmailaddresses implements Linkable {
	/** 
	 * Regex according to http://www.w3.org/Protocols/rfc822/, Originally written by Cal Henderson (http://iamcal.com/publish/articles/php/parsing_email/),
	 * Translated to Ruby by Tim Fletcher, with changes suggested by Dan Kubb.
	 * Translated to Java by Michael J. Simons 
	 */
	public final static String validEmailAddress = "(?:[^\\x00-\\x20\\x22\\x28\\x29\\x2c\\x2e\\x3a-\\x3c\\x3e\\x40\\x5b-\\x5d\\x7f-\\xff]+|\\x22(?:[^\\x0d\\x22\\x5c\\x80-\\xff]|\\x5c[\\x00-\\x7f])*\\x22)(?:\\x2e(?:[^\\x00-\\x20\\x22\\x28\\x29\\x2c\\x2e\\x3a-\\x3c\\x3e\\x40\\x5b-\\x5d\\x7f-\\xff]+|\\x22(?:[^\\x0d\\x22\\x5c\\x80-\\xff]|\\x5c[\\x00-\\x7f])*\\x22))*\\x40(?:[^\\x00-\\x20\\x22\\x28\\x29\\x2c\\x2e\\x3a-\\x3c\\x3e\\x40\\x5b-\\x5d\\x7f-\\xff]+|\\x5b(?:[^\\x0d\\x5b-\\x5d\\x80-\\xff]|\\x5c[\\x00-\\x7f])*\\x5d)(?:\\x2e(?:[^\\x00-\\x20\\x22\\x28\\x29\\x2c\\x2e\\x3a-\\x3c\\x3e\\x40\\x5b-\\x5d\\x7f-\\xff]+|\\x5b(?:[^\\x0d\\x5b-\\x5d\\x80-\\xff]|\\x5c[\\x00-\\x7f])*\\x5d))*";
	public final static Pattern validEmailAddressPattern = Pattern.compile(String.format("\\A%s\\z", validEmailAddress));
	public final static Pattern validEmailAddressPatternMl = Pattern.compile(String.format("(?m)%s", validEmailAddress));
	public final static Pattern atSigns = Pattern.compile("[@\uFF20\\x40]");
	
	private boolean hexEncodeEmailAddress = true;
	private boolean obfuscateEmailAddress = true;
	
	@Override
	public boolean linkTo(List<Node> changedNodes, TextNode node, String baseUri) {
		boolean changed = false;
		
		int start = 0;
		final String nodeText = node.getWholeText();
		final Matcher matcher = validEmailAddressPatternMl.matcher(nodeText);
		
		while(matcher.find()) {			
			final String textBefore = nodeText.substring(start, matcher.start());
			if(!isBlank(textBefore))
				changedNodes.add(new TextNode(textBefore, baseUri));
							
			final String emailAddress = matcher.group();				
			final Element newAnchor = new Element(Tag.valueOf("a"), baseUri);					
			newAnchor.attr("href", String.format("%s%s", "mailto:", hexEncodeEmailAddress ? StringUtils.hexEncodeEmailAddress(emailAddress) : emailAddress));
			newAnchor.appendChild(new TextNode(obfuscateEmailAddress ? obfuscateEmailAddress(emailAddress) : emailAddress, baseUri));
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
	
	public boolean isHexEncodeEmailAddress() {
		return hexEncodeEmailAddress;
	}

	public void setHexEncodeEmailAddress(boolean hexEncodeEmailAddress) {
		this.hexEncodeEmailAddress = hexEncodeEmailAddress;
	}

	public boolean isObfuscateEmailAddress() {
		return obfuscateEmailAddress;
	}

	public void setObfuscateEmailAddress(boolean obfuscateEmailAddress) {
		this.obfuscateEmailAddress = obfuscateEmailAddress;
	}
	
	/**
	 * Obfuscates an email address. @ will be replaced throught " [AT] " and . through " [DOT] " 
	 * @param emailAddress
	 * @return
	 */
	public static String obfuscateEmailAddress(final String emailAddress) {
		return atSigns.matcher(emailAddress).replaceAll(" [AT] ").replaceAll("\\.", " [DOT] ");
	}
}