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

/**
 * @author Michael J. Simons
 */
public abstract class StringUtils {
	/**
	 * "…" is used as the ellipsis, otherwise
	 * @see #truncate(String, int, String)
	 * @param in
	 * @param length
	 * @return
	 */
	public static String truncate(final String in, final int length) {
		return truncate(in, length, "…");
	}
	
	/**
	 * Truncates the String <code>in</code> to a maximum length of
	 * <code>lenght</code> including the ellipsis.
	 * @param in
	 * @param length
	 * @param ellipsis
	 * @return
	 */
	public static String truncate(final String in, final int length, final String ellipsis) {
		StringBuffer sb = new StringBuffer(in);
		if(in.length() > length) {
			sb.delete(length - nvl(ellipsis, "").length(), in.length());
			sb.append(ellipsis);
		}
		return sb.toString();
	}
	
	/**
	 * Hex encodes an email addess, leaving the '@' intact.
	 * @param input
	 * @return
	 */
	public static String hexEncodeEmailAddress(String input)  {
		final StringBuffer rv = new StringBuffer();
		for(int i=0; i<input.length(); ++i) {
			char c = input.charAt(i);			
			rv.append(c == '@' ? c : String.format("%%%x", (int)c));
		}
		return rv.toString();	    	    
	}
	
	/**
	 * Obfuscates an email address. @ will be replaced throught " [AT] " and . through " [DOT] " 
	 * @param emailAddress
	 * @return
	 */
	public static String obfuscateEmailAddress(final String emailAddress) {
		return LinkableEmailaddresses.atSigns.matcher(emailAddress).replaceAll(" [AT] ").replaceAll("\\.", " [DOT] ");
	}
	
	/**
	 * @param totest
	 * @return If totest is either null or has a length of 0
	 */
	public static boolean isBlank(final String totest) {
		return totest == null || totest.length() == 0;
	}
	
	/**
	 * @param value
	 * @param defaultValue
	 * @return <code>defaultValue</code> if <code>value</code> is blank, value otherwise
	 */
	public static String nvl(String value, String defaultValue) {
		return isBlank(value) ? defaultValue : value;
	}
}