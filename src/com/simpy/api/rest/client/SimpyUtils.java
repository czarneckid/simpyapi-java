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

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

/**
 * Utilities for Simpy Java API
 *
 * @author David Czarnecki
 * @since 1.0
 * @version $Id: SimpyUtils.java,v 1.3 2007/01/19 02:23:54 czarneckid Exp $
 */
public class SimpyUtils {


    /**
     * UTC format
     * <p/>
     * SimpleDateFormats are not threadsafe, but we should not need more than one per
     * thread.
     */
    private static final ThreadLocal UTC_DATE_FORMAT_OBJECT = new ThreadLocal() {
        protected Object initialValue() {
            return new SimpleDateFormat(SimpyConstants.UTC_DATE_FORMAT);
        }
    };

    /**
     * Simpy date format (yyyy-MM-dd)
     * SimpleDateFormats are not threadsafe, but we should not need more than one per
     * thread.
     */
    private static final ThreadLocal SIMPY_DATE_FORMAT_OBJECT = new ThreadLocal() {
        protected Object initialValue() {
            return new SimpleDateFormat(SimpyConstants.SIMPY_DATE_FORMAT);
        }
    };

    /**
     * Return a date in UTC style
     *
     * @param date Date
     * @return Date formatted as ISO 8601
     */
    public static String getUTCDate(Date date) {
        return ((SimpleDateFormat) UTC_DATE_FORMAT_OBJECT.get()).format(date);
    }

    /**
     * Return a date in Simpy style (yyyy-MM-dd)
     *
     * @param date Date
     * @return Date formatted as yyyy-MM-dd
     */
    public static String getSimpyDate(Date date) {
        return ((SimpleDateFormat) SIMPY_DATE_FORMAT_OBJECT.get()).format(date);
    }

    /**
     * Parse a date from a UTC string
     *
     * @param time Input string of form yyyy-MM-ddTHH:mm:ssZ
     * @return Date parsed from UTC string or <code>null</code> if error in
     *         parsing
     */
    public static Date getDateFromUTCString(String time) {
        Date result = null;

        try {
            ((SimpleDateFormat) UTC_DATE_FORMAT_OBJECT.get()).setCalendar(Calendar.getInstance(TimeZone.getTimeZone("GMT")));
            result = ((SimpleDateFormat) UTC_DATE_FORMAT_OBJECT.get()).parse(time);
        } catch (ParseException e) {
        }

        return result;
    }

    /**
     * Check to see if the input is <code>null</code> or blank
     *
     * @param input Input
     * @return <code>true</code> if input is null or blank, <code>false</code> otherwise
     */
    public static boolean checkNullOrBlank(String input) {
        return (input == null || "".equals(input));
    }

    /**
     * Encode input using UTF-8
     *
     * @param input Input
     * @return Input encoded using UTF-8 or <code>null</code> if input was null
     */
    public static String encodeUTF8(String input) {
        if (input == null) {
            return null;
        }

        try {
            return URLEncoder.encode(input, SimpyConstants.UTF8);
        } catch (UnsupportedEncodingException e) {
            return input;
        }
    }

    /**
     * Encode input with only for ?, &amp;, and # characters
     *
     * @param input Input
     * @return Input encoded changing ? to %3F, &amp; to %26, and # to %23
     * @since 1.3
     */
    public static String encodeURL(String input) {
        if (input == null) {
            return null;
        }

        String encodedInput = input.replaceAll("\\?", "%3F");
        encodedInput = input.replaceAll("&", "%26");
        encodedInput = input.replaceAll("#", "%23");

        return encodedInput;
    }
}