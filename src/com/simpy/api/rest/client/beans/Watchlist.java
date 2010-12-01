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
package com.simpy.api.rest.client.beans;

import java.util.List;

/**
 * Watchlist
 *
 * @author David Czarnecki
 * @since 1.2
 * @version $Id: Watchlist.java,v 1.4 2007/01/19 02:23:54 czarneckid Exp $
 */
public class Watchlist {

    private List users;
    private List filters;

    private int id;
    private String name;
    private String description;
    private String addDate;
    private int newLinks;

    public Watchlist() {
    }

    public Watchlist(int id, String name, String description, String addDate, int newLinks, List users, List filters) {
        this.users = users;
        this.filters = filters;
        this.id = id;
        this.name = name;
        this.description = description;
        this.addDate = addDate;
        this.newLinks = newLinks;
    }

    public List getUsers() {
        return users;
    }

    public void setUsers(List users) {
        this.users = users;
    }

    public List getFilters() {
        return filters;
    }

    public void setFilters(List filters) {
        this.filters = filters;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddDate() {
        return addDate;
    }

    public void setAddDate(String addDate) {
        this.addDate = addDate;
    }

    public int getNewLinks() {
        return newLinks;
    }

    public void setNewLinks(int newLinks) {
        this.newLinks = newLinks;
    }

    public String toString() {
        int userItems = (users != null) ? users.size() : 0;
        int filterItems = (filters != null) ? filters.size() : 0;

        return id + ":" + name + ":" + description + ":" + addDate + ":" + newLinks + ": Users(" + userItems + "): Filters (" + filterItems + ")";
    }
}
