/*
 * Notebook server for Teragrep Backend (nbs_01)
 * Copyright (C) 2025 Suomen Kanuuna Oy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 * Additional permission under GNU Affero General Public License version 3
 * section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with other code, such other code is not for that reason alone subject to any
 * of the requirements of the GNU Affero GPL version 3 as long as this Program
 * is the same Program as licensed from Suomen Kanuuna Oy without any additional
 * modifications.
 *
 * Supplemented terms under GNU Affero General Public License version 3
 * section 7
 *
 * Origin of the software must be attributed to Suomen Kanuuna Oy. Any modified
 * versions must be marked as "Modified version of" The Program.
 *
 * Names of the licensors and authors may not be used for publicity purposes.
 *
 * No rights are granted for use of trade names, trademarks, or service marks
 * which are in The Program if any.
 *
 * Licensee must indemnify licensors and authors for any liability that these
 * contractual assumptions impose on licensors and authors.
 *
 * To the extent this program is licensed as part of the Commercial versions of
 * Teragrep, the applicable Commercial License may apply to this file if you as
 * a licensee so wish it.
 */
package com.teragrep.nbs_01.repository;

import java.util.Objects;
import java.util.UUID;

// Represents a Paragraph that can be added to a Notebook.
public final class Paragraph {

    private final String id;
    private final String title;
    private final Script script;

    public Paragraph() {
        this.id = "";
        this.title = "";
        this.script = new Script();
    }

    public Paragraph(final String id, final String title, final Script script) {
        this.id = id;
        this.title = title;
        this.script = script;
    }

    public String title() {
        return title;
    }

    public String id() {
        return id;
    }

    public Script script() {
        return script;
    }

    public Paragraph copy() {
        final String copyId = UUID.randomUUID().toString();
        return copy(copyId);
    }

    public Paragraph copy(final String copyId) {
        final Paragraph copy = new Paragraph(copyId, title, new Script(script().text()));
        return copy;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Paragraph paragraph = (Paragraph) o;
        return Objects.equals(id, paragraph.id) && Objects.equals(title, paragraph.title)
                && Objects.equals(script, paragraph.script);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, script);
    }
}
