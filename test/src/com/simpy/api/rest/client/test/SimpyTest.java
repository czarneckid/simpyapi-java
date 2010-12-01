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
package com.simpy.api.rest.client.test;

import com.simpy.api.rest.client.Simpy;
import com.simpy.api.rest.client.beans.Link;
import com.simpy.api.rest.client.beans.Tag;
import com.simpy.api.rest.client.beans.Topic;
import com.simpy.api.rest.client.beans.Watchlist;

import java.util.List;

/**
 * Testing Simpy
 *
 * @author David Czarnecki
 * @version $Id: SimpyTest.java,v 1.7 2007/01/19 02:23:54 czarneckid Exp $
 * @since 1.0
 */
public class SimpyTest {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: SimpyTest {Simpy username} {Simpy password} {topicId - this is optional} {watchlistId - this is optional}");
            System.exit(1);
        } else {
            System.out.println("Using: " + args[0] + ":" + args[1]);
            Simpy simpy = new Simpy(args[0], args[1]);

            System.out.println("\n----- Testing getTags -----\n");
            List tags = simpy.getTags();
            Tag tag;
            for (int i = 0; i < tags.size(); i++) {
                tag = (Tag) tags.get(i);
                System.out.println(tag.getTag() + ":" + tag.getCount());
            }

            Thread.sleep(1000);

            System.out.println("\n----- Testing getLinks -----\n");
            List links = simpy.getLinks(null, null, null, null);
            Link link;
            for (int i = 0; i < links.size(); i++) {
                link = (Link) links.get(i);
                System.out.println(link.toString());
            }

            Thread.sleep(1000);

            System.out.println("\n----- Testing getLinks -----\n");
            links = simpy.getLinks(null, "2004-05-10", null, null);
            for (int i = 0; i < links.size(); i++) {
                link = (Link) links.get(i);
                System.out.println(link.toString());
            }

            Thread.sleep(1000);

            System.out.println("\n----- Testing getTopics -----\n");
            List topics = simpy.getTopics();
            Topic topic;
            for (int i = 0; i < topics.size(); i++) {
                topic = (Topic) topics.get(i);
                System.out.println(topic.toString());
            }

            Thread.sleep(1000);

            if (args.length == 3) {
                System.out.println("\n----- Testing getTopic -----\n");
                topic = simpy.getTopic(Integer.parseInt(args[2]));
                System.out.println(topic.toString());
            }

            System.out.println("\n----- Testing getNotes -----\n");
            List notes = simpy.getNotes(null);
            for (int i = 0; i < notes.size(); i++) {
                System.out.println(notes.get(i).toString());
            }

            Thread.sleep(1000);

            System.out.println("\n----- Testing saveLink -----\n");
            System.out.println(simpy.saveLink("blojsom", "http://blojsom.sf.net", 0, "java blog software", null, "This is the best Java blog software. Let's try これは日本語のテキストです。読めますか？with XML-RPC"));

            Thread.sleep(1000);


            System.out.println("\n----- Testing saveNote -----\n");
            System.out.println(simpy.saveNote("Title", "she he we be", "This is a note description with Iñtërnâtiônàlizætiøn Let's try これは日本語のテキストです。読めますか？with XML-RPC"));

            Thread.sleep(1000);

            System.out.println("\n----- Testing renameTag -----\n");
            System.out.println("Renaming blog to snorg: " + simpy.renameTag("blog", "snorg"));

            Thread.sleep(1000);

            System.out.println("\n----- Testing getTags -----\n");
            tags = simpy.getTags();
            for (int i = 0; i < tags.size(); i++) {
                tag = (Tag) tags.get(i);
                System.out.println(tag.getTag() + ":" + tag.getCount());
            }

            Thread.sleep(1000);

            System.out.println("\n----- Testing mergeTags -----\n");
            System.out.println("Merging snorg + java to snava: " + simpy.mergeTags("snorg", "java", "snava"));

            Thread.sleep(1000);

            System.out.println("\n----- Testing getTags -----\n");
            tags = simpy.getTags();
            for (int i = 0; i < tags.size(); i++) {
                tag = (Tag) tags.get(i);
                System.out.println(tag.getTag() + ":" + tag.getCount());
            }

            Thread.sleep(1000);

            System.out.println("\n----- Testing splitTag -----\n");
            System.out.println("Splitting snava to blog + java: " + simpy.splitTag("snava", "blog", "java"));

            Thread.sleep(1000);

            System.out.println("\n----- Testing getTags -----\n");
            tags = simpy.getTags();
            for (int i = 0; i < tags.size(); i++) {
                tag = (Tag) tags.get(i);
                System.out.println(tag.getTag() + ":" + tag.getCount());
            }
            
            Thread.sleep(1000);

            System.out.println("\n----- Testing getWatchlists -----\n");

            List watchlists = simpy.getWatchlists();
            for (int i = 0; i < watchlists.size(); i++) {
                Watchlist watchlist1 = (Watchlist) watchlists.get(i);
                System.out.println(watchlist1.toString());
            }

            if (args.length == 4) {
                System.out.println("\n----- Testing getWatchlist -----\n");

                Thread.sleep(1000);

                Watchlist watchlist = simpy.getWatchlist(Integer.parseInt(args[3]));
                System.out.println(watchlist.toString());
            }
        }
    }
}