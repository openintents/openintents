/*
 * Copyright (C) 2008  Tom Gibara
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.tomgibara.android.veecheck;

import static com.tomgibara.android.veecheck.Veecheck.XML_NAMESPACE;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Instances of this class are used to parse 'intentional' information for a
 * specified version.
 * 
 * @author Tom Gibara
 */

public class VeecheckResult extends DefaultHandler {

	/**
	 * The local name of the version element in the
	 * {@link Veecheck#XML_NAMESPACE}.
	 */

	private static final String VERSION_TAG = "version";

	/**
	 * The local name of the intent element in the
	 * {@link Veecheck#XML_NAMESPACE}.
	 */

	private static final String INTENT_TAG = "intent";

	/**
	 * A pattern used to split an attribute value into key/value pairs.
	 */

	private static final Pattern SEMI_SPLIT = Pattern.compile(";");

	/**
	 * A pattern used to parse key/value pairs.
	 */

	private static final Pattern KEY_VALUE = Pattern
			.compile("\\s*(\\S+)\\s*:(.*)");

	/**
	 * Parses a string into a map of keys to values. The format is akin to that
	 * used for the CSS style attribute in HTML (semicolon delimited, colon
	 * separated key/value pairs) but no escaping of colons or semicolons is
	 * supported.
	 * 
	 * @param str
	 *            the string to be parsed, may be null
	 * @return a map of key/value pairs, never null
	 */

	public static Map<String, String> toMap(final String str) {
		HashMap<String, String> map = new HashMap<String, String>();
		if (str != null) {
			String[] propArr = SEMI_SPLIT.split(str);
			for (String propStr : propArr) {
				Matcher kvMatcher = KEY_VALUE.matcher(propStr);
				// we ignore non matches
				if (!kvMatcher.matches())
					continue;
				String key = kvMatcher.group(1);
				if (map.containsKey(key))
					throw new IllegalArgumentException(String.format(
							"Duplicate key: %s", key));
				String value = kvMatcher.group(2).trim();
				map.put(key, value);
			}
		}
		return map;
	}

	/**
	 * The version against which version elements will be compared until a
	 * matching version is found.
	 */

	final VeecheckVersion version;

	/**
	 * Whether the intent contained within the current version element should
	 * provide the return values.
	 */
	private boolean recordNext = false;

	/**
	 * Whether the match has been determined and all subsequent processing can
	 * be skipped.
	 */

	private boolean skip = false;

	/**
	 * Whether this result object found intent information that matched the
	 * specified version.
	 */

	public boolean matched;

	/**
	 * The intent action, if any, for the specified version.
	 */

	public String action = null;

	/**
	 * The content type, if any, for the specified version.
	 */

	public String type = null;

	/**
	 * The data uri, if any, for the specified version.
	 */

	public String data = null;

	/**
	 * The extra properties, if any, for the specified version.
	 */

	public Map<String, String> extras = null;

	public VeecheckVersion latestVersion;

	public boolean greater;

	private VeecheckVersion currentVersion;

	/**
	 * Constructs a new {@link ContentHandler} that can be supplied to a SAX
	 * parser for the purpose of identifying intent information for a given
	 * application version
	 * 
	 * @param version
	 *            information about an application
	 */

	public VeecheckResult(VeecheckVersion version) {
		this.version = version;
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attrs) throws SAXException {
		// if (skip)
		// return; // nothing else to do
		if (!uri.equals(XML_NAMESPACE))
			return;
		if (recordNext) {
			if (!localName.equals(INTENT_TAG))
				return;
			action = attrs.getValue("action");
			data = attrs.getValue("data");
			type = attrs.getValue("type");
			extras = toMap(attrs.getValue("extras"));
			recordMatch(true);
		} else {
			if (!localName.equals(VERSION_TAG))
				return;
			VeecheckVersion v = new VeecheckVersion(attrs);

			// try to find current version (= first matching version)
			recordNext = version.matches(v);
			if (recordNext && currentVersion == null) {
				currentVersion = v;
			}

			// try to find update version
			if (latestVersion == null || v.greater(latestVersion)) {
				latestVersion = v;
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (skip)
			return; // nothing else to do
		if (!uri.equals(XML_NAMESPACE))
			return;
		if (localName.equals(VERSION_TAG)) {
			if (recordNext)
				recordMatch(false);
		}
	}

	private void recordMatch(boolean matched) {
		recordNext = false;
		this.matched = matched;
		skip = true;
	}

}