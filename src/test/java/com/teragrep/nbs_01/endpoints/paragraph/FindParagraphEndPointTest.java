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
package com.teragrep.nbs_01.endpoints.paragraph;

import com.teragrep.nbs_01.AbstractNotebookServerTest;
import com.teragrep.nbs_01.repository.Directory;
import com.teragrep.nbs_01.requests.JsonRequest;
import com.teragrep.nbs_01.responses.JsonResponse;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FindParagraphEndPointTest extends AbstractNotebookServerTest {

    private String paragraphId = "20150210-015259_1403135953";
    private final Path notebookPath = Paths
            .get("my_folder_2A94M5J1D/my_second_folder_2A94M5J2D/my_note1_2A94M5J1Z.zpln");
    private final String expectedFileContent = "{\"id\":\"20150210-015259_1403135953\",\"title\":\"Load data into table\",\"script\":{\"text\":\"%test import org.apache.commons.io.IOUtils\\nimport java.net.URL\\nimport java.nio.charset.Charset\\n\\n// Zeppelin creates and injects sc (SparkContext) and sqlContext (HiveContext or SqlContext)\\n// So you don't need create them manually\\n\\n// load bank data\\nval bankText = sc.parallelize(\\n    IOUtils.toString(\\n        new URL(\\\"https://s3.amazonaws.com/apache-zeppelin/tutorial/bank/bank.csv\\\"),\\n        Charset.forName(\\\"utf8\\\")).split(\\\"\\\\n\\\"))\\n\\ncase class Bank(age: Integer, job: String, marital: String, education: String, balance: Integer)\\n\\nval bank = bankText.map(s => s.split(\\\";\\\")).filter(s => s(0) != \\\"\\\\\\\"age\\\\\\\"\\\").map(\\n    s => Bank(s(0).toInt, \\n            s(1).replaceAll(\\\"\\\\\\\"\\\", \\\"\\\"),\\n            s(2).replaceAll(\\\"\\\\\\\"\\\", \\\"\\\"),\\n            s(3).replaceAll(\\\"\\\\\\\"\\\", \\\"\\\"),\\n            s(5).replaceAll(\\\"\\\\\\\"\\\", \\\"\\\").toInt\\n        )\\n).toDF()\\nbank.registerTempTable(\\\"bank\\\")\"}}";

    @BeforeEach
    private void setUp() {
        copyFileRecursively(notebookResources().toFile(), notebookDirectory().toFile());
    }

    @AfterEach
    private void tearDown() {
        deleteFileRecursively(notebookDirectory().toFile());
    }

    @Test
    // Assert that a HTTP request to /notebook/{path/to/notebook}/paragraph/{paragraphId} endpoint results in a response with the expected file contents
    public void httpFindTest() {
        // Assert that the file exists.
        Assertions.assertTrue(Files.exists(Paths.get(notebookDirectory().toString(), notebookPath.toString())));

        FindParagraphEndPoint endPoint = new FindParagraphEndPoint(new Directory(notebookDirectory()));
        String body = "{\"path\":\"" + notebookPath + "\",\"paragraphId\":\"" + paragraphId + "\"}";
        JsonResponse response = endPoint.createResponse(new JsonRequest(body));
        Assertions.assertEquals(expectedFileContent, response.body().getString("message").strip().toString());
    }

    @Test
    // Assert that a HTTP request to /notebook/{path/to/notebook}/paragraph/{paragraphId} endpoint with a nonexistent Notebook results in an error
    public void httpFindParagraphFromNonExistentNotebookTest() {
        String nonExistentNotebookName = "nonexistentNotebook";
        Path nonExistentNotebookPath = Paths.get(notebookDirectory().toString(), nonExistentNotebookName);
        FindParagraphEndPoint endPoint = new FindParagraphEndPoint(new Directory(notebookDirectory()));
        String body = "{\"path\":\"" + nonExistentNotebookName + "\",\"paragraphId\":\"" + paragraphId + "\"}";
        JsonResponse response = endPoint.createResponse(new JsonRequest(body));
        Assertions
                .assertEquals(
                        "java.io.FileNotFoundException: Notebook or directory with path " + nonExistentNotebookPath
                                + " not found!",
                        response.body().getString("message").strip()
                );
    }

    @Test
    // Assert that a HTTP request to /notebook/{path/to/notebook}/paragraph/{paragraphId} endpoint with a nonexistent paragraphId results in an error
    public void httpFindNonexistentParagraph() {
        String nonExistentParagraphId = "nonExistentParagraphId";
        FindParagraphEndPoint endPoint = new FindParagraphEndPoint(new Directory(notebookDirectory()));
        String body = "{\"path\":\"" + notebookPath + "\",\"paragraphId\":\"" + nonExistentParagraphId + "\"}";
        JsonResponse response = endPoint.createResponse(new JsonRequest(body));
        Assertions
                .assertEquals(
                        "com.teragrep.nbs_01.exceptions.MalformedRequestException: Paragraph not found!",
                        response.body().getString("message").strip()
                );
    }

}
