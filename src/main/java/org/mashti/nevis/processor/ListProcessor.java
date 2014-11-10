/**
 * This file is part of nevis.
 *
 * nevis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * nevis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with nevis.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mashti.nevis.processor;

import org.apache.commons.lang.StringUtils;
import org.mashti.nevis.Parser;
import org.mashti.nevis.element.List;
import org.mashti.nevis.element.ListItem;
import org.mashti.nevis.element.Node;
import org.mashti.nevis.element.Processor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class ListProcessor extends Processor {

    public ListProcessor() {

        //        super(Pattern.compile("(\\A|\\n{2,})(([ ]{0,3}?([-+*]|\\d+\\.)[ ]+(.*?))+)", Pattern.DOTALL));
        super(Pattern.compile("^" +
                "(\\A|\\n{2,})" +
                "(" +
                "(" +
                "[ ]{0," + 3 + "}" +
                "((?:[-+*]|\\d+[.]))" + // $3 is first list item marker
                "[ ]+" +
                ")" +
                "(?s:.+?)" +
                "(" +
                "\\z" + // End of input is OK
                "|" +
                "\\n{2,}" +
                "(?=\\S)" + // If not end of input, then a new para
                "(?![ ]*" +
                "(?:[-+*]|\\d+[.])" +
                "[ ]+" +
                ")" + // negative look ahead for another list marker
                ")" +
                ")", Pattern.MULTILINE));
    }

    @Override
    public void process(Node parent, final Matcher matcher, Parser parser) {

        final String first_marker = matcher.group(3);
        String items = matcher.group(2);
        items = Pattern.compile("\\n{2,}").matcher(items).replaceAll("\n\n\n");
        final List list = new List(parent, !StringUtils.containsAny(first_marker, new char[] {'-', '+', '*'}));
       
        if(items.contains("Item 1, graf one.")){
            System.out.println();
        }
       
        processItems(list, items, parser);
        parent.addChild(list);
    }

    private void processItems(Node parent, String items, Parser parser) {

        items = Pattern.compile("\\n{2,}\\z").matcher(items).replaceAll("\n");

        Pattern p = Pattern.compile("(\\n*)?" +
                "^([ \\t]*)([-+*]|\\d+[.])[ ]+" +
                "((?s:.+?)(\\n{1,2}|\\z))" +
                "(?=\\n*(\\z|\\2([-+\\*]|\\d+[.])[ \\t]+))", Pattern.MULTILINE);

        final Matcher matcher = p.matcher(items);

        while (matcher.find()) {
            final ListItem list_item = new ListItem(parent);
            String leadingLine = matcher.group(1);
            String split_item = matcher.group(4);
            split_item = Pattern.compile("^[ ]{4,}", Pattern.MULTILINE).matcher(split_item).replaceAll("");

            if (!(leadingLine == null || leadingLine.isEmpty()) || split_item.contains("\n\n")) {
                parser.parseBlock(list_item, split_item);
            }
            else {
                parser.parseInline(list_item, split_item.trim());
            }
            parent.addChild(list_item);
        }
    }
}
