/*
 * Copyright 2010-2016 ksyun.com, Inc. or its affiliates. All Rights
 * Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://ksyun.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is
 * distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either
 * express or implied. See the License for the specific language
 * governing
 * permissions and limitations under the License.
 */
package com.ksc.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpClientConnection;
import org.apache.http.conn.ConnectionRequest;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.protocol.HttpContext;
import org.junit.Before;
import org.junit.Test;

import com.ksc.http.IdleConnectionReaper;

public class IdleConnectionReaperTest {
    @Before
    public void init() {
        IdleConnectionReaper.shutdown();
    }

    @Test
    public void forceShutdown() throws Exception {
        assertEquals(0, IdleConnectionReaper.size());
        for (int i = 0; i < 3; i++) {
            assertTrue(IdleConnectionReaper
                    .registerConnectionManager(new TestClientConnectionManager()));
            assertEquals(1, IdleConnectionReaper.size());
            assertTrue(IdleConnectionReaper.shutdown());
            assertEquals(0, IdleConnectionReaper.size());
            assertFalse(IdleConnectionReaper.shutdown());
        }

    }

    @Test
    public void autoShutdown() throws Exception {
        assertEquals(0, IdleConnectionReaper.size());
        for (int i = 0; i < 3; i++) {
            HttpClientConnectionManager m = new TestClientConnectionManager();
            HttpClientConnectionManager m2 = new TestClientConnectionManager();
            assertTrue(IdleConnectionReaper
                    .registerConnectionManager(m));
            assertEquals(1, IdleConnectionReaper.size());
            assertTrue(IdleConnectionReaper
                    .registerConnectionManager(m2));
            assertEquals(2, IdleConnectionReaper.size());
            assertTrue(IdleConnectionReaper.removeConnectionManager(m));
            assertEquals(1, IdleConnectionReaper.size());
            assertTrue(IdleConnectionReaper.removeConnectionManager(m2));
            assertEquals(0, IdleConnectionReaper.size());
            assertFalse(IdleConnectionReaper.shutdown());
        }
    }

    private static class TestClientConnectionManager implements HttpClientConnectionManager {
        @Override
        public void releaseConnection(HttpClientConnection conn, Object newState, long validDuration, TimeUnit timeUnit) {}
        @Override
        public void connect(HttpClientConnection conn, HttpRoute route, int connectTimeout, HttpContext context) throws IOException {}
        @Override
        public void upgrade(HttpClientConnection conn, HttpRoute route, HttpContext context) throws IOException {}
        @Override
        public void routeComplete(HttpClientConnection conn, HttpRoute route, HttpContext context) throws IOException {}
        @Override public void shutdown() {}
        @Override public void closeIdleConnections(long idletime, TimeUnit tunit) {}
        @Override public void closeExpiredConnections() { }

        @Override
        public ConnectionRequest requestConnection(HttpRoute route, Object state) {
            return null;
        }
    }
}