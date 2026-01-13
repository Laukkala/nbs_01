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
package com.teragrep.nbs_01.protocols.http;

import com.teragrep.nbs_01.protocols.http.path.HTTPBasicRequestPath;
import com.teragrep.nbs_01.protocols.http.path.HTTPRequestPath;
import com.teragrep.nbs_01.protocols.http.path.StubPath;
import com.teragrep.nbs_01.protocols.http.body.Body;
import com.teragrep.nbs_01.protocols.http.body.StringBody;
import com.teragrep.nbs_01.protocols.http.body.StubBody;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class BasicHTTPRequestTest {

    @Test
    public void headersTest() {
        final HTTPRequestPath requestPath = new HTTPBasicRequestPath(Paths.get("target", "testLocation"));
        final Header locationHeader = new BasicHeader("Location", requestPath.toString());
        final Header contentTypeHeader = new BasicHeader("Content-Type", "application/json");
        final List<Header> headers = new ArrayList<>();
        headers.add(locationHeader);
        headers.add(contentTypeHeader);
        final BasicHTTPRequest testRequest = new BasicHTTPRequest(requestPath, headers);

        Assertions.assertEquals(2, testRequest.headers().size());
        Assertions.assertTrue(testRequest.headers().contains(locationHeader));
        Assertions.assertTrue(testRequest.headers().contains(contentTypeHeader));
        Assertions.assertEquals(headers, testRequest.headers());
    }

    @Test
    public void bodyTest() {
        final HTTPBasicRequestPath requestPath = new HTTPBasicRequestPath(Paths.get("target", "testLocation"));
        final Body body = new StringBody("testPayload");
        final BasicHTTPRequest testRequest = new BasicHTTPRequest(requestPath, body);

        Assertions.assertEquals(body, testRequest.body());
    }

    @Test
    public void stubTest() {
        final BasicHTTPRequest stubRequest = new BasicHTTPRequest();
        Assertions.assertEquals(0, stubRequest.headers().size());
        Assertions.assertEquals(StubBody.class, stubRequest.body().getClass());
        Assertions.assertEquals(StubPath.class, stubRequest.path().getClass());
    }

}
