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
package com.teragrep.nbs_01.responses;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionResponseTest {

    private final String throwable1message = "Failed to open notebook!";
    private final String throwable2message = "Notebook at /notebooks/my_folder_2A94M5J1D/nonexistentNotebook.zpln was not found!";
    private final String throwable3message = "File at path /notebooks/my_folder_2A94M5J1D/nonexistentNotebook.zpln was not found!";
    private final String throwable4message = "No permission to access file at path /notebooks/my_folder_2A94M5J1D/nonexistentNotebook.zpln!";

    // An ExceptionResponse should combine the underlying cause messages of the given Throwable and form it into a proper response.
    @Test
    void testBodyGeneration() {
        Throwable throwable4 = new FileNotFoundException(throwable4message);
        Throwable throwable3 = new IOException(throwable3message, throwable4);
        Throwable throwable2 = new RuntimeException(throwable2message, throwable3);
        Throwable throwable1 = new Exception(throwable1message, throwable2);
        String expectedMessage = throwable1 + "\nCaused by: " + throwable2 + "\nCaused by: " + throwable3
                + "\nCaused by: " + throwable4;

        ExceptionResponse response = new ExceptionResponse(500, throwable1);
        Assertions.assertEquals(expectedMessage, response.body().getString("message"));
    }

    // An ExceptionResponse should be able to take exceptions that don't have specific messages as well.
    @Test
    void testBodyGenerationWithExceptionsWithoutMessages() {
        Throwable throwable4 = new FileNotFoundException();
        Throwable throwable3 = new IOException(throwable3message, throwable4);
        Throwable throwable2 = new RuntimeException(throwable3);
        Throwable throwable1 = new Exception(throwable1message, throwable2);
        String expectedMessage = throwable1 + "\nCaused by: " + throwable2 + "\nCaused by: " + throwable3
                + "\nCaused by: " + throwable4;

        ExceptionResponse response = new ExceptionResponse(500, throwable1);
        Assertions.assertEquals(expectedMessage, response.body().getString("message"));
    }
}
