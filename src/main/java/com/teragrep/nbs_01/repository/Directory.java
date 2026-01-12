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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

// Represents a single Directory that can contain Filesystem objects.
public final class Directory implements FilesystemEntity {

    private static final Logger LOGGER = LoggerFactory.getLogger(Directory.class);
    private final List<FilesystemEntity> children;
    private final String name;

    public Directory(String name, List<FilesystemEntity> children) {
        this.name = name;
        this.children = children;
    }

    //public Directory copy() throws IOException {
    //    List<FilesystemEntity> copiedChildren = new ArrayList<>();
    //    for (FilesystemEntity child : children) {
    //        FilesystemEntity copy = child.copy();
    //        copiedChildren.add(copy);
    //    }
    //    return new Directory(name, copiedChildren);
    //}

    public String name() {
        return name;
    }

    public List<FilesystemEntity> children() {
        return children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Directory directory = (Directory) o;
        return Objects.equals(children, directory.children) && Objects.equals(name, directory.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(children, name);
    }
}
