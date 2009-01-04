/*   
 * 	 Copyright (C) 2008-2009 pjv (and others, see About dialog)
 * 
 * 	 This file is part of OI About.
 *
 *   OI About is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   OI About is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with OI About.  If not, see <http://www.gnu.org/licenses/>.
 *   
 */

package org.openintents.metadata;

/**
 * Metadata definition belonging to OI About.
 * 
 * @author pjv
 *
 */
public final class AboutMetaData {
	
	/**
	 * Empty, preventing instantiation.
	 */
	private AboutMetaData() {
		//Empty, preventing instantiation.
	}

	/**
	 * Metadata key matching with AboutIntents.EXTRA_COMMENTS.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.metadata.COMMENTS"
	 * </p>
	 */
	public static final String METADATA_COMMENTS = 
		"org.openintents.metadata.COMMENTS";
	
	/**
	 * Metadata key matching with AboutIntents.EXTRA_COPYRIGHT.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.metadata.COPYRIGHT"
	 * </p>
	 */
	public static final String METADATA_COPYRIGHT = 
		"org.openintents.metadata.COPYRIGHT";
	
	/**
	 * Metadata key matching with AboutIntents.EXTRA_WEBSITE_URL.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.metadata.WEBSITE_URL"
	 * </p>
	 */
	public static final String METADATA_WEBSITE_URL = 
		"org.openintents.metadata.WEBSITE_URL";
	
	/**
	 * Metadata key matching with AboutIntents.EXTRA_WEBSITE_LABEL.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.metadata.WEBSITE_LABEL"
	 * </p>
	 */
	public static final String METADATA_WEBSITE_LABEL = 
		"org.openintents.metadata.WEBSITE_LABEL";
	
	/**
	 * Metadata key matching with AboutIntents.EXTRA_AUTHORS.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.metadata.AUTHORS"
	 * </p>
	 */
	public static final String METADATA_AUTHORS = "org.openintents.metadata.AUTHORS";
	
	/**
	 * Metadata key matching with AboutIntents.EXTRA_DOCUMENTERS.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.metadata.DOCUMENTERS"
	 * </p>
	 */
	public static final String METADATA_DOCUMENTERS = 
		"org.openintents.metadata.DOCUMENTERS";
	
	/**
	 * Metadata key matching with AboutIntents.EXTRA_TRANSLATORS.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.metadata.TRANSLATORS"
	 * </p>
	 */
	public static final String METADATA_TRANSLATORS = 
		"org.openintents.metadata.TRANSLATORS";
	
	/**
	 * Metadata key matching with AboutIntents.EXTRA_ARTISTS.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.metadata.ARTISTS"
	 * </p>
	 */
	public static final String METADATA_ARTISTS = "org.openintents.metadata.ARTISTS";
	
/*	*//**
	 * Metadata key matching with AboutIntents.EXTRA_LICENSE.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.metadata.LICENSE"
	 * </p>
	 *//*
	public static final String METADATA_LICENSE = "org.openintents.metadata.LICENSE";*/
	/**
	 * Metadata key matching with AboutIntents.EXTRA_WRAP_LICENSE.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.metadata.WRAP_LICENSE"
	 * </p>
	 */
	public static final String METADATA_WRAP_LICENSE = 
		"org.openintents.metadata.WRAP_LICENSE";

}
