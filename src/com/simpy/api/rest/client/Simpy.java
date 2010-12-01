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

import com.simpy.api.rest.client.beans.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Simpy is a class for accessing the <a href="http://www.simpy.com/doc/api/rest">Simpy REST API</a>.
 *
 * @author David Czarnecki
 * @version $Id: Simpy.java,v 1.21 2007/04/20 14:41:28 czarneckid Exp $
 * @since 1.0
 */
public class Simpy {

    private Log logger = LogFactory.getLog(Simpy.class);

    private HttpClient httpClient;
    private DocumentBuilder documentBuilder;
    private int httpResult;
    private String username;
    private String password;


    /**
     * Create an object to interact with Simpy
     *
     * @param username Username
     * @param password Password
     */
    public Simpy(String username, String password) {
        this.username = username;
        this.password = password;

        CookiePolicy.registerCookieSpec(CookiePolicy.BROWSER_COMPATIBILITY, CookiePolicy.getCookieSpec(CookiePolicy.BROWSER_COMPATIBILITY).getClass());

        httpClient = new HttpClient();
        HttpClientParams httpClientParams = new HttpClientParams();
        DefaultHttpMethodRetryHandler defaultHttpMethodRetryHandler = new DefaultHttpMethodRetryHandler(0, false);
        httpClientParams.setParameter(SimpyConstants.USER_AGENT_HEADER, SimpyConstants.USER_AGENT_VALUE);
        httpClientParams.setParameter(HttpClientParams.RETRY_HANDLER, defaultHttpMethodRetryHandler);
        httpClient.setParams(httpClientParams);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setValidating(false);
        documentBuilderFactory.setIgnoringElementContentWhitespace(true);
        documentBuilderFactory.setIgnoringComments(true);
        documentBuilderFactory.setCoalescing(true);
        documentBuilderFactory.setNamespaceAware(false);
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            logger.error(e);
        }
    }


    /**
     * Return the HTTP status code of the last operation
     *
     * @return HTTP status code
     */
    public int getHttpResult() {
        return httpResult;
    }


    /**
     * Encode the username and password for BASIC authentication
     *
     * @return Basic + Base64 encoded(username + ':' + password)
     */
    private String encodeForAuthorization() {
        String information = username + ":" + password;
        return SimpyConstants.BASIC + new String(Base64.encodeBase64(information.getBytes()));
    }


    /**
     * Return a list of {@link Tag} objects
     *
     * @return List of {@link Tag} objects
     */
    public List getTags() {
        List tags = new ArrayList();
        StringBuffer result = new StringBuffer();

        GetMethod get = new GetMethod(SimpyConstants.API_GET_TAGS);
        get.addRequestHeader(SimpyConstants.AUTHORIZATION_HEADER, encodeForAuthorization());
        get.setDoAuthentication(true);
        get.setFollowRedirects(true);

        try {
            httpResult = httpClient.executeMethod(get);
            logger.debug("Result: " + httpResult);
            if (get.getResponseBodyAsStream() != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(get.getResponseBodyAsStream(), SimpyConstants.UTF8));
                String input;
                while ((input = bufferedReader.readLine()) != null) {
                    result.append(input).append(SimpyConstants.LINE_SEPARATOR);
                }

                result = new StringBuffer(result.toString().replaceAll("<!DOCTYPE tags SYSTEM \"GetTags.dtd\">", ""));

                Document document = documentBuilder.parse(new InputSource(new StringReader(result.toString())));
                NodeList tagItems = document.getElementsByTagName(SimpyConstants.TAG_TAG);
                if (tagItems != null && tagItems.getLength() > 0) {
                    for (int i = 0; i < tagItems.getLength(); i++) {
                        Node tagItem = tagItems.item(i);
                        String count = tagItem.getAttributes().getNamedItem(SimpyConstants.COUNT_ATTRIBUTE).getNodeValue();
                        String tag = tagItem.getAttributes().getNamedItem(SimpyConstants.NAME_ATTRIBUTE).getNodeValue();

                        tags.add(new Tag(tag, Integer.parseInt(count)));
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e);
        } catch (SAXException e) {
            logger.error(e);
        } finally {
            get.releaseConnection();
        }

        return tags;
    }


    /**
     * Depending on your input parameters, this call returns all links that were
     * added on or between given dates, or links matching a given query.
     *
     * @param q          A query string that forces the API call to return only
     *                   the matching links.
     * @param date       This parameter should not be used in combination
     *                   with the afterDate and beforeDate parameters. It limits the links
     *                   returned to links added on the given date.
     * @param afterDate  This parameter should be used in combination with the
     *                   beforeDate parameter. It limits the links returned to links added after
     *                   the given date, excluding the date specified.
     * @param beforeDate This parameter should be used in combination with the
     *                   afterDate parameter. It limits the links returned to links added before
     *                   the given date, excluding the date specified.
     * @return Returns all links, links added on or between given
     *         dates, or links matching a given query.
     */
    public List getAllLinks(String q, String date, String afterDate, String beforeDate) {
        return getLinks(q, date, afterDate, beforeDate, Integer.MAX_VALUE);
    }


    /**
     * Same as {@link #getAllLinks(String, String, String, String) getAllLinks()}
     * except that it returns the ten most relevant links (depending on the given
     * parameters).
     *
     * @see #getAllLinks(String, String, String, String)
     */
    public List getLinks(String q, String date, String afterDate, String beforeDate) {
        return getLinks(q, date, afterDate, beforeDate, 10);
    }

    /**
     * This API call removes the given tag.
     *
     * @param tag Specifies the tag to remove.
     * @return Status reponse indicating either success or failure.
     */
    public int removeTag(String tag) {
        int operationStatus = 0;
        StringBuffer result = new StringBuffer();

        GetMethod get = new GetMethod(SimpyConstants.API_REMOVE_TAG);
        get.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        get.addRequestHeader(SimpyConstants.AUTHORIZATION_HEADER, encodeForAuthorization());
        get.setDoAuthentication(true);
        get.setFollowRedirects(true);

        List queryParameters = new ArrayList();

        queryParameters.add(new NameValuePair(SimpyConstants.TAG, tag));

        if (queryParameters.size() > 0) {
            get.setQueryString((NameValuePair[]) queryParameters.toArray(new NameValuePair[queryParameters.size()]));
        }

        try {
            httpResult = httpClient.executeMethod(get);
            logger.debug("Result: " + httpResult);
            if (get.getResponseBodyAsStream() != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(get.getResponseBodyAsStream(), SimpyConstants.UTF8));
                String input;
                while ((input = bufferedReader.readLine()) != null) {
                    result.append(input).append(SimpyConstants.LINE_SEPARATOR);
                }

                result = new StringBuffer(result.toString().replaceAll("<!DOCTYPE status SYSTEM \"Status.dtd\">", ""));
                result = new StringBuffer(result.toString().replaceAll("<!DOCTYPE error SYSTEM \"Error.dtd\">", ""));

                Document document = documentBuilder.parse(new InputSource(new StringReader(result.toString())));
                NodeList codeItems = document.getElementsByTagName(SimpyConstants.CODE_TAG);
                if (codeItems != null && codeItems.getLength() > 0) {
                    for (int i = 0; i < codeItems.getLength(); i++) {
                        Node codeItem = codeItems.item(i);
                        operationStatus = Integer.parseInt(getNodeValue(codeItem));
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e);
        } catch (SAXException e) {
            logger.error(e);
        } finally {
            get.releaseConnection();
        }


        return operationStatus;
    }

    /**
     * This API call renames the given tag.
     *
     * @param fromTag Specifies the tag to rename.
     * @param toTag   Specifies the new tag name.
     * @return Status reponse indicating either success or failure.
     */
    public int renameTag(String fromTag, String toTag) {
        int operationStatus = 0;
        StringBuffer result = new StringBuffer();

        GetMethod get = new GetMethod(SimpyConstants.API_RENAME_TAG);
        get.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        get.addRequestHeader(SimpyConstants.AUTHORIZATION_HEADER, encodeForAuthorization());
        get.setDoAuthentication(true);
        get.setFollowRedirects(true);

        List queryParameters = new ArrayList();

        queryParameters.add(new NameValuePair(SimpyConstants.FROM_TAG, fromTag));
        queryParameters.add(new NameValuePair(SimpyConstants.TO_TAG, toTag));

        if (queryParameters.size() > 0) {
            get.setQueryString((NameValuePair[]) queryParameters.toArray(new NameValuePair[queryParameters.size()]));
        }

        try {
            httpResult = httpClient.executeMethod(get);
            logger.debug("Result: " + httpResult);
            if (get.getResponseBodyAsStream() != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(get.getResponseBodyAsStream(), SimpyConstants.UTF8));
                String input;
                while ((input = bufferedReader.readLine()) != null) {
                    result.append(input).append(SimpyConstants.LINE_SEPARATOR);
                }

                result = new StringBuffer(result.toString().replaceAll("<!DOCTYPE status SYSTEM \"Status.dtd\">", ""));
                result = new StringBuffer(result.toString().replaceAll("<!DOCTYPE error SYSTEM \"Error.dtd\">", ""));

                Document document = documentBuilder.parse(new InputSource(new StringReader(result.toString())));
                NodeList codeItems = document.getElementsByTagName(SimpyConstants.CODE_TAG);
                if (codeItems != null && codeItems.getLength() > 0) {
                    for (int i = 0; i < codeItems.getLength(); i++) {
                        Node codeItem = codeItems.item(i);
                        operationStatus = Integer.parseInt(getNodeValue(codeItem));
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e);
        } catch (SAXException e) {
            logger.error(e);
        } finally {
            get.releaseConnection();
        }


        return operationStatus;
    }

    /**
     * This API call merges two tags into a new tag.
     *
     * @param fromTag1 Specifies the first tag to merge.
     * @param fromTag2 Specifies the second tag to merge.
     * @param toTag    Specifies the tag to merge the two tags into.
     * @return Status reponse indicating either success or failure.
     */
    public int mergeTags(String fromTag1, String fromTag2, String toTag) {
        int operationStatus = 0;
        StringBuffer result = new StringBuffer();

        GetMethod get = new GetMethod(SimpyConstants.API_MERGE_TAGS);
        get.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        get.addRequestHeader(SimpyConstants.AUTHORIZATION_HEADER, encodeForAuthorization());
        get.setDoAuthentication(true);
        get.setFollowRedirects(true);

        List queryParameters = new ArrayList();

        queryParameters.add(new NameValuePair(SimpyConstants.FROM_TAG1, fromTag1));
        queryParameters.add(new NameValuePair(SimpyConstants.FROM_TAG2, fromTag2));
        queryParameters.add(new NameValuePair(SimpyConstants.FROM_TAG2, fromTag2));
        queryParameters.add(new NameValuePair(SimpyConstants.TO_TAG, toTag));

        if (queryParameters.size() > 0) {
            get.setQueryString((NameValuePair[]) queryParameters.toArray(new NameValuePair[queryParameters.size()]));
        }

        try {
            httpResult = httpClient.executeMethod(get);
            logger.debug("Result: " + httpResult);
            if (get.getResponseBodyAsStream() != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(get.getResponseBodyAsStream(), SimpyConstants.UTF8));
                String input;
                while ((input = bufferedReader.readLine()) != null) {
                    result.append(input).append(SimpyConstants.LINE_SEPARATOR);
                }

                result = new StringBuffer(result.toString().replaceAll("<!DOCTYPE status SYSTEM \"Status.dtd\">", ""));
                result = new StringBuffer(result.toString().replaceAll("<!DOCTYPE error SYSTEM \"Error.dtd\">", ""));

                Document document = documentBuilder.parse(new InputSource(new StringReader(result.toString())));
                NodeList codeItems = document.getElementsByTagName(SimpyConstants.CODE_TAG);
                if (codeItems != null && codeItems.getLength() > 0) {
                    for (int i = 0; i < codeItems.getLength(); i++) {
                        Node codeItem = codeItems.item(i);
                        operationStatus = Integer.parseInt(getNodeValue(codeItem));
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e);
        } catch (SAXException e) {
            logger.error(e);
        } finally {
            get.releaseConnection();
        }


        return operationStatus;
    }

    /**
     * This API call splits a single tag into two separate tags.
     *
     * @param tag    Specifies the tag to split.
     * @param toTag1 Specifies the first tag to split into.
     * @param toTag2 Specifies the second tag to split into.
     * @return Status reponse indicating either success or failure.
     */
    public int splitTag(String tag, String toTag1, String toTag2) {
        int operationStatus = 0;
        StringBuffer result = new StringBuffer();

        GetMethod get = new GetMethod(SimpyConstants.API_SPLIT_TAG);
        get.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        get.addRequestHeader(SimpyConstants.AUTHORIZATION_HEADER, encodeForAuthorization());
        get.setDoAuthentication(true);
        get.setFollowRedirects(true);

        List queryParameters = new ArrayList();

        queryParameters.add(new NameValuePair(SimpyConstants.TAG, tag));
        queryParameters.add(new NameValuePair(SimpyConstants.TO_TAG1, toTag1));
        queryParameters.add(new NameValuePair(SimpyConstants.TO_TAG2, toTag2));

        if (queryParameters.size() > 0) {
            get.setQueryString((NameValuePair[]) queryParameters.toArray(new NameValuePair[queryParameters.size()]));
        }

        try {
            httpResult = httpClient.executeMethod(get);
            logger.debug("Result: " + httpResult);
            if (get.getResponseBodyAsStream() != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(get.getResponseBodyAsStream(), SimpyConstants.UTF8));
                String input;
                while ((input = bufferedReader.readLine()) != null) {
                    result.append(input).append(SimpyConstants.LINE_SEPARATOR);
                }

                result = new StringBuffer(result.toString().replaceAll("<!DOCTYPE status SYSTEM \"Status.dtd\">", ""));
                result = new StringBuffer(result.toString().replaceAll("<!DOCTYPE error SYSTEM \"Error.dtd\">", ""));

                Document document = documentBuilder.parse(new InputSource(new StringReader(result.toString())));
                NodeList codeItems = document.getElementsByTagName(SimpyConstants.CODE_TAG);
                if (codeItems != null && codeItems.getLength() > 0) {
                    for (int i = 0; i < codeItems.getLength(); i++) {
                        Node codeItem = codeItems.item(i);
                        operationStatus = Integer.parseInt(getNodeValue(codeItem));
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e);
        } catch (SAXException e) {
            logger.error(e);
        } finally {
            get.releaseConnection();
        }


        return operationStatus;
    }

    /**
     * Depending on your input parameters, this methdod returns links
     * added on or between given dates, or links matching a given query. The
     * number of links to be returned can be freely configured.
     *
     * @param limit number of links to return
     * @return Returns
     */
    public List getLinks(String q, String date, String afterDate, String beforeDate, int limit) {
        List links = new ArrayList();
        StringBuffer result = new StringBuffer();

        GetMethod get = new GetMethod(SimpyConstants.API_GET_LINKS);
        get.addRequestHeader(SimpyConstants.AUTHORIZATION_HEADER, encodeForAuthorization());
        get.setDoAuthentication(true);
        get.setFollowRedirects(true);

        List queryParameters = new ArrayList();
        queryParameters.add(new NameValuePair(SimpyConstants.LIMIT, String.valueOf(limit)));

        if (!SimpyUtils.checkNullOrBlank(q)) {
            queryParameters.add(new NameValuePair(SimpyConstants.Q, q));
        }

        if (!SimpyUtils.checkNullOrBlank(date)) {
            queryParameters.add(new NameValuePair(SimpyConstants.DATE, date));
        } else {
            if (!SimpyUtils.checkNullOrBlank(afterDate)) {
                queryParameters.add(new NameValuePair(SimpyConstants.AFTER_DATE, afterDate));
            }

            if (!SimpyUtils.checkNullOrBlank(beforeDate)) {
                queryParameters.add(new NameValuePair(SimpyConstants.BEFORE_DATE, beforeDate));
            }
        }

        get.setQueryString((NameValuePair[]) queryParameters.toArray(new NameValuePair[0]));

        try {
            httpResult = httpClient.executeMethod(get);
            logger.debug("Result: " + httpResult);
            if (get.getResponseBodyAsStream() != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(get.getResponseBodyAsStream(), SimpyConstants.UTF8));
                String input;
                while ((input = bufferedReader.readLine()) != null) {
                    result.append(input).append(SimpyConstants.LINE_SEPARATOR);
                }

                result = new StringBuffer(result.toString().replaceAll("<!DOCTYPE links SYSTEM \"GetLinks.dtd\">", ""));

                Document document = documentBuilder.parse(new InputSource(new StringReader(result.toString())));
                NodeList linkItems = document.getElementsByTagName(SimpyConstants.LINK_TAG);
                if (linkItems != null && linkItems.getLength() > 0) {
                    for (int i = 0; i < linkItems.getLength(); i++) {
                        Node linkItem = linkItems.item(i);
                        String accessType = linkItem.getAttributes().getNamedItem(SimpyConstants.ACCESS_TYPE_ATTRIBUTE).getNodeValue();
                        String url = null;
                        String modDate = null;
                        String addDate = null;
                        String title = null;
                        String nickname = null;
                        String note = null;
                        List tags = null;

                        NodeList children = linkItem.getChildNodes();
                        if (children != null && children.getLength() > 0) {
                            for (int j = 0; j < children.getLength(); j++) {
                                Node child = children.item(j);
                                if (SimpyConstants.URL_TAG.equals(child.getNodeName())) {
                                    url = getNodeValue(child);
                                } else if (SimpyConstants.MOD_DATE_TAG.equals(child.getNodeName())) {
                                    modDate = getNodeValue(child);
                                } else if (SimpyConstants.ADD_DATE_TAG.equals(child.getNodeName())) {
                                    addDate = getNodeValue(child);
                                } else if (SimpyConstants.TITLE_TAG.equals(child.getNodeName())) {
                                    title = getNodeValue(child);
                                } else if (SimpyConstants.NICKNAME_TAG.equals(child.getNodeName())) {
                                    nickname = getNodeValue(child);
                                } else if (SimpyConstants.NOTE.equals(child.getNodeName())) {
                                    note = getNodeValue(child);
                                } else if (SimpyConstants.TAGS_TAG.equals(child.getNodeName())) {
                                    tags = getChildrenForNode(child);
                                }
                            }
                        }

                        links.add(new Link(accessType, url, modDate, addDate, title, nickname, note, tags));
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e);
        } catch (SAXException e) {
            logger.error(e);
        } finally {
            get.releaseConnection();
        }

        return links;
    }


    /**
     * Returns the list of your Topics, their meta-data, including the number of new links added each Topic since your last login.
     *
     * @return Returns the list of your Topics, their meta-data, including the number of new links added each Topic since your last login.
     */
    public List getTopics() {
        List topics = new ArrayList();
        StringBuffer result = new StringBuffer();

        GetMethod get = new GetMethod(SimpyConstants.API_GET_TOPICS);
        get.addRequestHeader(SimpyConstants.AUTHORIZATION_HEADER, encodeForAuthorization());
        get.setDoAuthentication(true);
        get.setFollowRedirects(true);

        try {
            httpResult = httpClient.executeMethod(get);
            logger.debug("Result: " + httpResult);
            if (get.getResponseBodyAsStream() != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(get.getResponseBodyAsStream(), SimpyConstants.UTF8));
                String input;
                while ((input = bufferedReader.readLine()) != null) {
                    result.append(input).append(SimpyConstants.LINE_SEPARATOR);
                }

                result = new StringBuffer(result.toString().replaceAll("<!DOCTYPE topics SYSTEM \"GetTopics.dtd\">", ""));

                Document document = documentBuilder.parse(new InputSource(new StringReader(result.toString())));
                NodeList topicItems = document.getElementsByTagName(SimpyConstants.TOPIC_TAG);
                if (topicItems != null && topicItems.getLength() > 0) {
                    for (int i = 0; i < topicItems.getLength(); i++) {
                        Node topicItem = topicItems.item(i);
                        Topic topic = new Topic();

                        topic.setId(Integer.parseInt(topicItem.getAttributes().getNamedItem(SimpyConstants.ID_ATTRIBUTE).getNodeValue()));
                        topic.setName(topicItem.getAttributes().getNamedItem(SimpyConstants.NAME_ATTRIBUTE).getNodeValue());
                        topic.setDescription(topicItem.getAttributes().getNamedItem(SimpyConstants.DESCRIPTION_ATTRIBUTE).getNodeValue());
                        topic.setAddDate(topicItem.getAttributes().getNamedItem(SimpyConstants.ADD_DATE_ATTRIBUTE).getNodeValue());
                        topic.setNewLinks(Integer.parseInt(topicItem.getAttributes().getNamedItem(SimpyConstants.NEW_LINKS_ATTRIBUTE).getNodeValue()));

                        List users = new ArrayList();
                        NodeList children = topicItem.getChildNodes();
                        if (children != null && children.getLength() > 0) {
                            for (int j = 0; j < children.getLength(); j++) {
                                Node child = children.item(j);
                                if (SimpyConstants.USER_TAG.equals(child.getNodeName())) {
                                    users.add(new User(child.getAttributes().getNamedItem(SimpyConstants.USERNAME_ATTRIBUTE).getNodeValue()));
                                } else if (SimpyConstants.FILTER_TAG.equals(child.getNodeName())) {
                                    topic.setFilter(new Filter(child.getAttributes().getNamedItem(SimpyConstants.NAME_ATTRIBUTE).getNodeValue(), child.getAttributes().getNamedItem(SimpyConstants.QUERY_ATTRIBUTE).getNodeValue()));
                                }
                            }
                        }

                        topic.setUsers(users);
                        topics.add(topic);
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e);
        } catch (SAXException e) {
            logger.error(e);
        } finally {
            get.releaseConnection();
        }

        return topics;
    }


    /**
     * Returns the meta-data for a given Topic.
     *
     * @param topicId The ID of a Topic you want to retrieve. The ID must belong to your Topic.
     * @return Returns the meta-data for a given Topic.
     */
    public Topic getTopic(int topicId) {
        Topic topic = null;
        StringBuffer result = new StringBuffer();

        GetMethod get = new GetMethod(SimpyConstants.API_GET_TOPIC);
        get.addRequestHeader(SimpyConstants.AUTHORIZATION_HEADER, encodeForAuthorization());
        get.setDoAuthentication(true);
        get.setFollowRedirects(true);
        get.setQueryString(new NameValuePair[]{new NameValuePair(SimpyConstants.TOPIC_ID, Integer.toString(topicId))});

        try {
            httpResult = httpClient.executeMethod(get);
            logger.debug("Result: " + httpResult);
            if (get.getResponseBodyAsStream() != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(get.getResponseBodyAsStream(), SimpyConstants.UTF8));
                String input;
                while ((input = bufferedReader.readLine()) != null) {
                    result.append(input).append(SimpyConstants.LINE_SEPARATOR);
                }

                result = new StringBuffer(result.toString().replaceAll("<!DOCTYPE topic SYSTEM \"GetTopic.dtd\">", ""));

                Document document = documentBuilder.parse(new InputSource(new StringReader(result.toString())));
                NodeList topicItems = document.getElementsByTagName(SimpyConstants.TOPIC_TAG);
                if (topicItems != null && topicItems.getLength() > 0) {
                    for (int i = 0; i < topicItems.getLength(); i++) {
                        Node topicItem = topicItems.item(i);
                        topic = new Topic();

                        topic.setId(Integer.parseInt(topicItem.getAttributes().getNamedItem(SimpyConstants.ID_ATTRIBUTE).getNodeValue()));
                        topic.setName(topicItem.getAttributes().getNamedItem(SimpyConstants.NAME_ATTRIBUTE).getNodeValue());
                        topic.setDescription(topicItem.getAttributes().getNamedItem(SimpyConstants.DESCRIPTION_ATTRIBUTE).getNodeValue());
                        topic.setAddDate(topicItem.getAttributes().getNamedItem(SimpyConstants.ADD_DATE_ATTRIBUTE).getNodeValue());
                        topic.setNewLinks(Integer.parseInt(topicItem.getAttributes().getNamedItem(SimpyConstants.NEW_LINKS_ATTRIBUTE).getNodeValue()));
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e);
        } catch (SAXException e) {
            logger.error(e);
        } finally {
            get.releaseConnection();
        }

        return topic;
    }


    /**
     * Returns all your Notes in the reverse chronological order by add date
     * (i.e. the most recently added Note first) or by relevance, if a
     * search String is specified.
     *
     * @param q a search String that is used in the API
     * @return a List of Note objects
     */
    public List getAllNotes(String q) {
        return getNotes(q, Integer.MAX_VALUE);
    }


    /**
     * Fetches the ten most recently addded Notes from the simpy, or the ten
     * most relevant Notes, according to the specified search String. The
     * search String is only applied to the API call, if it's not <code>null</code>
     * and not an empty String.<br/>
     * Use {@link #getAllNotes(String) getAllNotes()} if you want to fetch all
     * Notes from the API.
     *
     * @param q a search String that is used in the API
     * @return a List of Note objects
     * @see #getAllNotes(String)
     */
    public List getNotes(String q) {
        return getNotes(q, 10);
    }


    /**
     * Returns all your Notes in the reverse chronological order by add date
     * (i.e. the most recently added Note first) or by rank, if you use this
     * in the search mode.
     *
     * @param q     A query string that forces the API call to return only the matching Notes.
     * @param limit the number of Notes to return.
     * @return Returns all your Notes in the reverse chronological order by
     *         add date (i.e. the most recently added Note first) or by rank,
     *         if you use this in the search mode.
     */
    public List getNotes(String q, int limit) {
        List notes = new ArrayList();
        StringBuffer result = new StringBuffer();

        GetMethod get = new GetMethod(SimpyConstants.API_GET_NOTES);
        get.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        get.addRequestHeader(SimpyConstants.AUTHORIZATION_HEADER, encodeForAuthorization());
        get.setDoAuthentication(true);
        get.setFollowRedirects(true);

        List queryParameters = new ArrayList();
        queryParameters.add(new NameValuePair(SimpyConstants.LIMIT, String.valueOf(limit)));

        if (!SimpyUtils.checkNullOrBlank(q)) {
            queryParameters.add(new NameValuePair(SimpyConstants.Q, q));
        }

        get.setQueryString((NameValuePair[]) queryParameters.toArray(new NameValuePair[0]));

        try {
            httpResult = httpClient.executeMethod(get);
            logger.debug("Result: " + httpResult);
            if (get.getResponseBodyAsStream() != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(get.getResponseBodyAsStream(), SimpyConstants.UTF8));
                String input;
                while ((input = bufferedReader.readLine()) != null) {
                    result.append(input).append(SimpyConstants.LINE_SEPARATOR);
                }

                result = new StringBuffer(result.toString().replaceAll("<!DOCTYPE notes SYSTEM \"GetNotes.dtd\">", ""));

                Document document = documentBuilder.parse(new InputSource(new StringReader(result.toString())));
                NodeList noteItems = document.getElementsByTagName(SimpyConstants.NOTE_TAG);
                if (noteItems != null && noteItems.getLength() > 0) {
                    for (int i = 0; i < noteItems.getLength(); i++) {
                        Node noteItem = noteItems.item(i);

                        NodeList children = noteItem.getChildNodes();
                        if (children != null && children.getLength() > 1) {
                            Note note = new Note();

                            note.setAccessType(noteItem.getAttributes().getNamedItem(SimpyConstants.ACCESS_TYPE_ATTRIBUTE).getNodeValue());
                            List tags;

                            for (int j = 0; j < children.getLength(); j++) {
                                Node child = children.item(j);
                                if (SimpyConstants.URI_TAG.equals(child.getNodeName())) {
                                    note.setUri(getNodeValue(child));
                                } else if (SimpyConstants.MOD_DATE_TAG.equals(child.getNodeName())) {
                                    note.setModDate(getNodeValue(child));
                                } else if (SimpyConstants.ADD_DATE_TAG.equals(child.getNodeName())) {
                                    note.setAddDate(getNodeValue(child));
                                } else if (SimpyConstants.TITLE_TAG.equals(child.getNodeName())) {
                                    note.setTitle(getNodeValue(child));
                                } else if (SimpyConstants.DESCRIPTION_TAG.equals(child.getNodeName())) {
                                    note.setDescription(getNodeValue(child));
                                } else if (SimpyConstants.TAGS_TAG.equals(child.getNodeName())) {
                                    tags = getChildrenForNode(child);
                                    note.setTags(tags);
                                } else if (SimpyConstants.ID_TAG.equals(child.getNodeName())) {
                                    note.setId(getNodeValue(child));
                                }
                            }

                            notes.add(note);
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e);
        } catch (SAXException e) {
            logger.error(e);
        } finally {
            get.releaseConnection();
        }

        return notes;
    }


    /**
     * Saves the given link and returns a status reponse indicating either success or failure.
     *
     * @param title      The title of the page to save.
     * @param href       The URL of the page to save. It must start with 'http://'.
     * @param accessType Use 1 to make the link public and 0 to make it private.
     * @param tags       Comma-separated list of tags.
     * @param nickname   An alternative, custom title.
     * @param note       A free-text note to go with the link.
     * @return Status reponse indicating either success or failure.
     * @throws IllegalArgumentException If either <code>title</code> or <code>href</code> is <code>null</code> as they are required parameters
     */
    public int saveLink(String title, String href, int accessType, String tags, String nickname, String note) {
        if (title == null) {
            throw new IllegalArgumentException(SimpyConstants.TITLE + " is a required parameter");
        }

        if (href == null) {
            throw new IllegalArgumentException(SimpyConstants.HREF + " is a required parameter");
        }

        int operationStatus = 0;
        StringBuffer result = new StringBuffer();

        GetMethod get = new GetMethod(SimpyConstants.API_SAVE_LINK);
        get.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        get.addRequestHeader(SimpyConstants.AUTHORIZATION_HEADER, encodeForAuthorization());
        get.setDoAuthentication(true);
        get.setFollowRedirects(true);

        List queryParameters = new ArrayList();

        queryParameters.add(new NameValuePair(SimpyConstants.TITLE, title));
        queryParameters.add(new NameValuePair(SimpyConstants.HREF, href));
        queryParameters.add(new NameValuePair(SimpyConstants.ACCESS_TYPE, Integer.toString(accessType)));

        if (!SimpyUtils.checkNullOrBlank(tags)) {
            queryParameters.add(new NameValuePair(SimpyConstants.TAGS, tags));
        }

        if (!SimpyUtils.checkNullOrBlank(nickname)) {
            queryParameters.add(new NameValuePair(SimpyConstants.URL_NICKNAME, nickname));
        }

        if (!SimpyUtils.checkNullOrBlank(note)) {
            queryParameters.add(new NameValuePair(SimpyConstants.NOTE, note));
        }

        if (queryParameters.size() > 0) {
            get.setQueryString((NameValuePair[]) queryParameters.toArray(new NameValuePair[queryParameters.size()]));
        }

        try {
            httpResult = httpClient.executeMethod(get);
            logger.debug("Result: " + httpResult);
            if (get.getResponseBodyAsStream() != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(get.getResponseBodyAsStream(), SimpyConstants.UTF8));
                String input;
                while ((input = bufferedReader.readLine()) != null) {
                    result.append(input).append(SimpyConstants.LINE_SEPARATOR);
                }

                result = new StringBuffer(result.toString().replaceAll("<!DOCTYPE status SYSTEM \"Status.dtd\">", ""));
                result = new StringBuffer(result.toString().replaceAll("<!DOCTYPE error SYSTEM \"Error.dtd\">", ""));

                Document document = documentBuilder.parse(new InputSource(new StringReader(result.toString())));
                NodeList codeItems = document.getElementsByTagName(SimpyConstants.CODE_TAG);
                if (codeItems != null && codeItems.getLength() > 0) {
                    for (int i = 0; i < codeItems.getLength(); i++) {
                        Node codeItem = codeItems.item(i);
                        operationStatus = Integer.parseInt(getNodeValue(codeItem));
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e);
        } catch (SAXException e) {
            logger.error(e);
        } finally {
            get.releaseConnection();
        }


        return operationStatus;
    }

    /**
     * Deletes the given link and returns a status reponse indicating either success or failure.
     *
     * @param href The URL of the bookmark to delete. It must start with 'http://'.
     * @return Status reponse indicating either success or failure.
     * @throws IllegalArgumentException If <code>href</code> is <code>null</code> as they are required parameters
     */
    public int deleteLink(String href) {
        if (href == null) {
            throw new IllegalArgumentException(SimpyConstants.HREF + " is a required parameter");
        }

        int operationStatus = 0;
        StringBuffer result = new StringBuffer();

        GetMethod get = new GetMethod(SimpyConstants.API_SAVE_LINK);
        get.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        get.addRequestHeader(SimpyConstants.AUTHORIZATION_HEADER, encodeForAuthorization());
        get.setDoAuthentication(true);
        get.setFollowRedirects(true);

        List queryParameters = new ArrayList();

        queryParameters.add(new NameValuePair(SimpyConstants.HREF, href));

        if (queryParameters.size() > 0) {
            get.setQueryString((NameValuePair[]) queryParameters.toArray(new NameValuePair[queryParameters.size()]));
        }

        try {
            httpResult = httpClient.executeMethod(get);
            logger.debug("Result: " + httpResult);
            if (get.getResponseBodyAsStream() != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(get.getResponseBodyAsStream(), SimpyConstants.UTF8));
                String input;
                while ((input = bufferedReader.readLine()) != null) {
                    result.append(input).append(SimpyConstants.LINE_SEPARATOR);
                }

                result = new StringBuffer(result.toString().replaceAll("<!DOCTYPE status SYSTEM \"Status.dtd\">", ""));
                result = new StringBuffer(result.toString().replaceAll("<!DOCTYPE error SYSTEM \"Error.dtd\">", ""));

                Document document = documentBuilder.parse(new InputSource(new StringReader(result.toString())));
                NodeList codeItems = document.getElementsByTagName(SimpyConstants.CODE_TAG);
                if (codeItems != null && codeItems.getLength() > 0) {
                    for (int i = 0; i < codeItems.getLength(); i++) {
                        Node codeItem = codeItems.item(i);
                        operationStatus = Integer.parseInt(getNodeValue(codeItem));
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e);
        } catch (SAXException e) {
            logger.error(e);
        } finally {
            get.releaseConnection();
        }


        return operationStatus;
    }

    /**
     * Deletes the given Note and returns a status reponse indicating either success or failure.
     *
     * @param noteId       The ID of the Note to delete.
     * @return Status reponse indicating either success or failure.
     * @throws IllegalArgumentException If <code>title</code> is <code>null</code> as it is a required parameter
     */
    public int deleteNote(String noteId) {
        if (noteId == null) {
            throw new IllegalArgumentException(SimpyConstants.NOTE_ID + " is a required parameter");
        }

        int operationStatus = 0;
        StringBuffer result = new StringBuffer();

        GetMethod get = new GetMethod(SimpyConstants.API_DELETE_NOTE);
        get.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        get.addRequestHeader(SimpyConstants.AUTHORIZATION_HEADER, encodeForAuthorization());
        get.setDoAuthentication(true);
        get.setFollowRedirects(true);

        List queryParameters = new ArrayList();

        queryParameters.add(new NameValuePair(SimpyConstants.NOTE_ID, noteId));

        if (queryParameters.size() > 0) {
            get.setQueryString((NameValuePair[]) queryParameters.toArray(new NameValuePair[queryParameters.size()]));
        }

        try {
            httpResult = httpClient.executeMethod(get);
            logger.debug("Result: " + httpResult);
            if (get.getResponseBodyAsStream() != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(get.getResponseBodyAsStream(), SimpyConstants.UTF8));
                String input;
                while ((input = bufferedReader.readLine()) != null) {
                    result.append(input).append(SimpyConstants.LINE_SEPARATOR);
                }

                result = new StringBuffer(result.toString().replaceAll("<!DOCTYPE status SYSTEM \"Status.dtd\">", ""));
                result = new StringBuffer(result.toString().replaceAll("<!DOCTYPE error SYSTEM \"Error.dtd\">", ""));

                Document document = documentBuilder.parse(new InputSource(new StringReader(result.toString())));
                NodeList codeItems = document.getElementsByTagName(SimpyConstants.CODE_TAG);
                if (codeItems != null && codeItems.getLength() > 0) {
                    for (int i = 0; i < codeItems.getLength(); i++) {
                        Node codeItem = codeItems.item(i);
                        operationStatus = Integer.parseInt(getNodeValue(codeItem));
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e);
        } catch (SAXException e) {
            logger.error(e);
        } finally {
            get.releaseConnection();
        }


        return operationStatus;
    }    

    /**
     * Saves a new Note and returns a status reponse indicating either success or failure.
     *
     * @param title       The title of the Note to save.
     * @param tags        Comma-separated list of tags.
     * @param description A free-text note.
     * @return Status reponse indicating either success or failure.
     * @throws IllegalArgumentException If <code>title</code> is <code>null</code> as it is a required parameter
     */
    public int saveNote(String title, String tags, String description) {
        if (title == null) {
            throw new IllegalArgumentException(SimpyConstants.TITLE + " is a required parameter");
        }

        int operationStatus = 0;
        StringBuffer result = new StringBuffer();

        GetMethod get = new GetMethod(SimpyConstants.API_SAVE_NOTE);
        get.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        get.addRequestHeader(SimpyConstants.AUTHORIZATION_HEADER, encodeForAuthorization());
        get.setDoAuthentication(true);
        get.setFollowRedirects(true);

        List queryParameters = new ArrayList();

        queryParameters.add(new NameValuePair(SimpyConstants.TITLE, title));

        if (!SimpyUtils.checkNullOrBlank(tags)) {
            queryParameters.add(new NameValuePair(SimpyConstants.TAGS, tags));
        }

        if (!SimpyUtils.checkNullOrBlank(description)) {
            queryParameters.add(new NameValuePair(SimpyConstants.DESCRIPTION_ATTRIBUTE, description));
        }

        if (queryParameters.size() > 0) {
            get.setQueryString((NameValuePair[]) queryParameters.toArray(new NameValuePair[queryParameters.size()]));
        }

        try {
            httpResult = httpClient.executeMethod(get);
            logger.debug("Result: " + httpResult);
            if (get.getResponseBodyAsStream() != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(get.getResponseBodyAsStream(), SimpyConstants.UTF8));
                String input;
                while ((input = bufferedReader.readLine()) != null) {
                    result.append(input).append(SimpyConstants.LINE_SEPARATOR);
                }

                result = new StringBuffer(result.toString().replaceAll("<!DOCTYPE status SYSTEM \"Status.dtd\">", ""));
                result = new StringBuffer(result.toString().replaceAll("<!DOCTYPE error SYSTEM \"Error.dtd\">", ""));

                Document document = documentBuilder.parse(new InputSource(new StringReader(result.toString())));
                NodeList codeItems = document.getElementsByTagName(SimpyConstants.CODE_TAG);
                if (codeItems != null && codeItems.getLength() > 0) {
                    for (int i = 0; i < codeItems.getLength(); i++) {
                        Node codeItem = codeItems.item(i);
                        operationStatus = Integer.parseInt(getNodeValue(codeItem));
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e);
        } catch (SAXException e) {
            logger.error(e);
        } finally {
            get.releaseConnection();
        }


        return operationStatus;
    }

    /**
     * This API call returns the list of your Watchlists, their meta-data, including the number of new links added to each Watchlist since your last login.
     *
     * @return List of {@link Watchlist} items
     */
    public List getWatchlists() {
        List watchlists = new ArrayList();
        StringBuffer result = new StringBuffer();

        GetMethod get = new GetMethod(SimpyConstants.API_GET_WATCHLISTS);
        get.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        get.addRequestHeader(SimpyConstants.AUTHORIZATION_HEADER, encodeForAuthorization());
        get.setDoAuthentication(true);
        get.setFollowRedirects(true);

        try {
            httpResult = httpClient.executeMethod(get);
            logger.debug("Result: " + httpResult);
            if (get.getResponseBodyAsStream() != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(get.getResponseBodyAsStream(), SimpyConstants.UTF8));
                String input;
                while ((input = bufferedReader.readLine()) != null) {
                    result.append(input).append(SimpyConstants.LINE_SEPARATOR);
                }

                Document document = documentBuilder.parse(new InputSource(new StringReader(result.toString())));
                NodeList watchlistItems = document.getElementsByTagName(SimpyConstants.WATCHLIST_TAG);
                if (watchlistItems != null && watchlistItems.getLength() > 0) {
                    for (int i = 0; i < watchlistItems.getLength(); i++) {
                        Watchlist watchlist = null;
                        Node watchlistItem = watchlistItems.item(i);

                        if (watchlistItem != null) {
                            watchlist = new Watchlist();

                            watchlist.setId(Integer.parseInt(watchlistItem.getAttributes().getNamedItem(SimpyConstants.ID_ATTRIBUTE).getNodeValue()));
                            watchlist.setName(watchlistItem.getAttributes().getNamedItem(SimpyConstants.NAME_ATTRIBUTE).getNodeValue());
                            watchlist.setDescription(watchlistItem.getAttributes().getNamedItem(SimpyConstants.DESCRIPTION_ATTRIBUTE).getNodeValue());
                            watchlist.setAddDate(watchlistItem.getAttributes().getNamedItem(SimpyConstants.ADD_DATE_ATTRIBUTE).getNodeValue());
                            watchlist.setNewLinks(Integer.parseInt(watchlistItem.getAttributes().getNamedItem(SimpyConstants.NEW_LINKS_ATTRIBUTE).getNodeValue()));

                            NodeList watchlistChildren = watchlistItem.getChildNodes();
                            if (watchlistChildren != null && watchlistChildren.getLength() > 1) {
                                List users = new ArrayList();
                                List filters = new ArrayList();

                                for (int j = 0; j < watchlistChildren.getLength(); j++) {
                                    Node child = watchlistChildren.item(j);

                                    if (SimpyConstants.FILTER_TAG.equals(child.getNodeName())) {
                                        Filter filter = new Filter();
                                        filter.setName(child.getAttributes().getNamedItem(SimpyConstants.NAME_ATTRIBUTE).getNodeValue());
                                        filter.setQuery(child.getAttributes().getNamedItem(SimpyConstants.QUERY_ATTRIBUTE).getNodeValue());
                                        filters.add(filter);
                                    } else if (SimpyConstants.USER_TAG.equals(child.getNodeName())) {
                                        User user = new User();
                                        user.setUsername(child.getAttributes().getNamedItem(SimpyConstants.USERNAME_ATTRIBUTE).getNodeValue());
                                        users.add(user);
                                    }
                                }

                                watchlist.setUsers(users);
                                watchlist.setFilters(filters);
                            }

                            watchlists.add(watchlist);
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e);
        } catch (SAXException e) {
            logger.error(e);
        } finally {
            get.releaseConnection();
        }

        return watchlists;
    }

    /**
     * Retrieve a {@link Watchlist} for the given watchlist id
     *
     * @param watchlistId Watchlist to retrieve
     * @return {@link Watchlist} for the given watchlist id or <code>null</code> if the watchlist could not be loaded
     * @since 1.2
     */
    public Watchlist getWatchlist(int watchlistId) {
        Watchlist watchlist = null;
        StringBuffer result = new StringBuffer();

        GetMethod get = new GetMethod(SimpyConstants.API_GET_WATCHLIST);
        get.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        get.addRequestHeader(SimpyConstants.AUTHORIZATION_HEADER, encodeForAuthorization());
        get.setDoAuthentication(true);
        get.setFollowRedirects(true);

        List queryParameters = new ArrayList();
        queryParameters.add(new NameValuePair(SimpyConstants.WATCHLIST_ID, String.valueOf(watchlistId)));

        get.setQueryString((NameValuePair[]) queryParameters.toArray(new NameValuePair[0]));

        try {
            httpResult = httpClient.executeMethod(get);
            logger.debug("Result: " + httpResult);
            if (get.getResponseBodyAsStream() != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(get.getResponseBodyAsStream(), SimpyConstants.UTF8));
                String input;
                while ((input = bufferedReader.readLine()) != null) {
                    result.append(input).append(SimpyConstants.LINE_SEPARATOR);
                }

                Document document = documentBuilder.parse(new InputSource(new StringReader(result.toString())));
                NodeList watchlistItems = document.getElementsByTagName(SimpyConstants.WATCHLIST_TAG);
                if (watchlistItems != null && watchlistItems.getLength() > 0) {
                    for (int i = 0; i < watchlistItems.getLength(); i++) {
                        Node watchlistItem = watchlistItems.item(i);

                        if (watchlistItem != null) {
                            watchlist = new Watchlist();

                            watchlist.setId(Integer.parseInt(watchlistItem.getAttributes().getNamedItem(SimpyConstants.ID_ATTRIBUTE).getNodeValue()));
                            watchlist.setName(watchlistItem.getAttributes().getNamedItem(SimpyConstants.NAME_ATTRIBUTE).getNodeValue());
                            watchlist.setDescription(watchlistItem.getAttributes().getNamedItem(SimpyConstants.DESCRIPTION_ATTRIBUTE).getNodeValue());
                            watchlist.setAddDate(watchlistItem.getAttributes().getNamedItem(SimpyConstants.ADD_DATE_ATTRIBUTE).getNodeValue());
                            watchlist.setNewLinks(Integer.parseInt(watchlistItem.getAttributes().getNamedItem(SimpyConstants.NEW_LINKS_ATTRIBUTE).getNodeValue()));

                            NodeList watchlistChildren = watchlistItem.getChildNodes();
                            if (watchlistChildren != null && watchlistChildren.getLength() > 1) {
                                List users = new ArrayList();
                                List filters = new ArrayList();

                                for (int j = 0; j < watchlistChildren.getLength(); j++) {
                                    Node child = watchlistChildren.item(j);

                                    if (SimpyConstants.FILTER_TAG.equals(child.getNodeName())) {
                                        Filter filter = new Filter();
                                        filter.setName(child.getAttributes().getNamedItem(SimpyConstants.NAME_ATTRIBUTE).getNodeValue());
                                        filter.setQuery(child.getAttributes().getNamedItem(SimpyConstants.QUERY_ATTRIBUTE).getNodeValue());
                                        filters.add(filter);
                                    } else if (SimpyConstants.USER_TAG.equals(child.getNodeName())) {
                                        User user = new User();
                                        user.setUsername(child.getAttributes().getNamedItem(SimpyConstants.USERNAME_ATTRIBUTE).getNodeValue());
                                        users.add(user);
                                    }
                                }

                                watchlist.setUsers(users);
                                watchlist.setFilters(filters);
                            }
                        }

                    }
                }
            }
        } catch (IOException e) {
            logger.error(e);
        } catch (SAXException e) {
            logger.error(e);
        } finally {
            get.releaseConnection();
        }

        return watchlist;
    }

    // ============ private Helper methods ============

    /**
     * Retrieve the text for a node
     *
     * @param node Node
     * @return Text for a node or <code>null</code> if none available
     */
    private String getNodeValue(Node node) {
        String result = null;
        NodeList children = node.getChildNodes();
        if (children != null && children.getLength() > 0) {
            result = children.item(0).getNodeValue();
        }

        return result;
    }


    /**
     * Retrieve the text for the child nodes of a given node
     *
     * @param node Node
     * @return List with text for the child nodes
     */
    private List getChildrenForNode(Node node) {
        List result = new ArrayList();

        NodeList children = node.getChildNodes();
        if (children != null && children.getLength() > 0) {
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i).getChildNodes() != null && children.item(i).getChildNodes().getLength() > 0) {
                    result.add(getNodeValue(children.item(i)));
                }
            }
        }

        return result;
    }

}
