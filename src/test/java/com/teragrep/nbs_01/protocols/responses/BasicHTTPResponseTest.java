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
package com.teragrep.nbs_01.protocols.responses;

import com.teragrep.nbs_01.protocols.http.body.Body;
import com.teragrep.nbs_01.protocols.http.body.StringBody;
import com.teragrep.nbs_01.protocols.http.body.StubBody;
import com.teragrep.nbs_01.protocols.http.BasicHTTPResponse;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class BasicHTTPResponseTest {

    @Test
    void statusTest() {
        BasicHTTPResponse testResponse = new BasicHTTPResponse(HttpStatus.OK_200);
        Assertions.assertEquals(200, testResponse.status());
    }

    @Test
    void bodyTest() {
        Body body = new StringBody("testPayload");
        BasicHTTPResponse testResponse = new BasicHTTPResponse(HttpStatus.OK_200, body);

        Assertions.assertEquals(body, testResponse.body());
    }

    @Test
    void headersTest() {
        Path responsePath = Paths.get("target", "testLocation");
        Header locationHeader = new BasicHeader("Location", responsePath.toString());
        Header contentTypeHeader = new BasicHeader("Content-Type", "application/json");
        List<Header> headers = new ArrayList<>();
        headers.add(locationHeader);
        headers.add(contentTypeHeader);
        BasicHTTPResponse testResponse = new BasicHTTPResponse(HttpStatus.OK_200, headers);

        Assertions.assertEquals(2, testResponse.headers().size());
        Assertions.assertTrue(testResponse.headers().contains(locationHeader));
        Assertions.assertTrue(testResponse.headers().contains(contentTypeHeader));
        Assertions.assertEquals(headers, testResponse.headers());
    }

    @Test
    void stubTest() {
        BasicHTTPResponse testResponse = new BasicHTTPResponse(HttpStatus.NOT_FOUND_404);
        Assertions.assertEquals(StubBody.class, testResponse.body().getClass());
        Assertions.assertEquals(0, testResponse.headers().size());
    }
}
