/**
 * Copyright (c) 2005-2007, David A. Czarnecki
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * - Neither the name of simpy-java nor the names of its contributors may
 *   be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.simpy.api.rest.client;

/**
 * Constants used in the Simpy Java API
 *
 * @author David Czarnecki
 * @since 1.0
 * @version $Id: SimpyConstants.java,v 1.15 2007/05/17 02:49:48 czarneckid Exp $
 */
public class SimpyConstants {

    public static final String USER_AGENT_HEADER = "User-Agent";
    public static final String USER_AGENT_VALUE = "Simpy-Java/1.3";
    public static final String UTF8 = "UTF-8";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BASIC = "Basic ";
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final String UTC_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final String SIMPY_DATE_FORMAT = "yyyy-MM-dd";

    // API service endpoints
    public static final String API_GET_TAGS = "http://www.simpy.com/simpy/api/rest/GetTags.do";
    public static final String API_GET_LINKS = "http://www.simpy.com/simpy/api/rest/GetLinks.do";
    public static final String API_SAVE_LINK = "http://www.simpy.com/simpy/api/rest/SaveLink.do";
    public static final String API_GET_TOPICS = "http://www.simpy.com/simpy/api/rest/GetTopics.do";
    public static final String API_GET_TOPIC = "http://www.simpy.com/simpy/api/rest/GetTopic.do";
    public static final String API_GET_NOTES = "http://www.simpy.com/simpy/api/rest/GetNotes.do";
    public static final String API_SAVE_NOTE = "http://www.simpy.com/simpy/api/rest/SaveNote.do";
    public static final String API_DELETE_NOTE = "http://www.simpy.com/simpy/api/rest/DeleteNote.do";    
    public static final String API_REMOVE_TAG = "http://www.simpy.com/simpy/api/rest/RemoveTag.do";
    public static final String API_RENAME_TAG = "http://www.simpy.com/simpy/api/rest/RenameTag.do";
    public static final String API_MERGE_TAGS = "http://www.simpy.com/simpy/api/rest/MergeTags.do";
    public static final String API_SPLIT_TAG = "http://www.simpy.com/simpy/api/rest/SplitTag.do";
    public static final String API_GET_WATCHLIST = "http://www.simpy.com/simpy/api/rest/GetWatchlist.do";
    public static final String API_GET_WATCHLISTS = "http://www.simpy.com/simpy/api/rest/GetWatchlists.do";

    // Various URL parameters for sending data to Simpy
    public static final String Q = "q";
    public static final String LIMIT = "limit";
    public static final String DATE = "date";
    public static final String AFTER_DATE = "afterDate";
    public static final String BEFORE_DATE = "beforeDate";
    public static final String TITLE = "title";
    public static final String HREF = "href";
    public static final String ACCESS_TYPE = "accessType";
    public static final String TAGS = "tags";
    public static final String URL_NICKNAME = "urlNickname";
    public static final String NOTE = "note";
    public static final String NOTE_ID = "noteId";    
    public static final String TOPIC_ID = "topicId";
    public static final String TAG = "tag";
    public static final String FROM_TAG = "fromTag";
    public static final String FROM_TAG1 = "fromTag1";
    public static final String FROM_TAG2 = "fromTag2";
    public static final String TO_TAG = "toTag";
    public static final String TO_TAG1 = "toTag1";
    public static final String TO_TAG2 = "toTag2";
    public static final String WATCHLIST_ID = "watchlistId";

    // Status codes
    public static final int STATUS_CODE_SUCCESS = 0;
    public static final int STATUS_CODE_MISSING_PARAMETER = 100;
    public static final int STATUS_CODE_NON_EXISTENT_ENTITY = 200;
    public static final int STATUS_CODE_RETRIEVAL_ERROR = 300;
    public static final int STATUS_CODE_STORAGE_ERROR = 301;
    public static final int STATUS_CODE_QUOTA_REACHED = 500;

    // Tags
    public static final String TAG_TAG = "tag";
    public static final String CODE_TAG = "code";
    public static final String MESSAGE_TAG = "message";
    public static final String LINK_TAG = "link";
    public static final String URL_TAG = "url";
    public static final String MOD_DATE_TAG = "modDate";
    public static final String ADD_DATE_TAG = "addDate";
    public static final String TITLE_TAG = "title";
    public static final String NICKNAME_TAG = "nickname";
    public static final String TAGS_TAG = "tags";
    public static final String NOTE_TAG = "note";
    public static final String DESCRIPTION_TAG = "description";
    public static final String TOPIC_TAG = "topic";
    public static final String USER_TAG = "user";
    public static final String FILTER_TAG = "filter";
    public static final String URI_TAG = "uri";
    public static final String WATCHLIST_TAG = "watchlist";
    public static final String ID_TAG = "id";

    // Attributes
    public static final String COUNT_ATTRIBUTE = "count";
    public static final String TAG_ATTRIBUTE = "tag";
    public static final String ACCESS_TYPE_ATTRIBUTE = "accessType";
    public static final String ID_ATTRIBUTE = "id";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String DESCRIPTION_ATTRIBUTE = "description";
    public static final String ADD_DATE_ATTRIBUTE = "addDate";
    public static final String NEW_LINKS_ATTRIBUTE = "newLinks";
    public static final String USERNAME_ATTRIBUTE = "username";
    public static final String QUERY_ATTRIBUTE = "query";

    // Access type
    public static final int PUBLIC_ACCESS_TYPE = 1;
    public static final int PRIVATE_ACCESS_TYPE = 0;
}
