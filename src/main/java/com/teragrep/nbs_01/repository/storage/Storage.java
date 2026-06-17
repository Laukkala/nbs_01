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
package com.teragrep.nbs_01.repository.storage;

import com.teragrep.nbs_01.exceptions.MalformedRequestException;
import com.teragrep.nbs_01.repository.Notebook;
import com.teragrep.nbs_01.repository.Paragraph;
import com.teragrep.nbs_01.repository.Script;
import com.teragrep.nbs_01.repository.identifiers.Identifier;
import com.teragrep.nbs_01.repository.serialization.SerializedNotebook;
import com.teragrep.nbs_01.repository.serialization.SerializedParagraph;
import com.teragrep.nbs_01.repository.serialization.SerializedScript;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.util.List;

public interface Storage {

    public abstract String readDirectory(Identifier identifier) throws IOException, MalformedRequestException;

    public abstract List<Identifier> listFiles(Identifier identifier)
            throws MalformedRequestException, FileNotFoundException, IOException;

    public abstract void writeFile(Identifier identifier, String content) throws MalformedRequestException, IOException;

    public abstract void writeDirectory(Identifier identifier) throws FileAlreadyExistsException, IOException;

    public abstract void deleteFile(Identifier identifier)
            throws NoSuchFileException, MalformedRequestException, IOException;

    public abstract void deleteDirectory(Identifier identifier)
            throws NoSuchFileException, MalformedRequestException, IOException;

    public abstract void copyDirectory(Identifier source, Identifier destination)
            throws FileNotFoundException, FileAlreadyExistsException, IOException, MalformedRequestException;

    public abstract Notebook deserializeNotebook(Identifier identifier) throws IOException;

    public abstract SerializedNotebook serializeNotebook(Notebook notebook);

    public abstract SerializedParagraph serializeParagraph(Paragraph paragraph);

    public abstract SerializedScript serializeScript(Script script);

    public abstract boolean exists(Identifier identifierZ);
}
