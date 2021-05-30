/*
 * Copyright 2018-2021 Steinar Bang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations
 * under the License.
 */
package no.priv.bang.demos.whiteboardwebapi.webapi;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockrunner.mock.web.MockHttpServletResponse;

import no.priv.bang.demos.whiteboardwebapi.webapi.Count;
import no.priv.bang.demos.whiteboardwebapi.webapi.CounterServiceServlet;
import no.priv.bang.demos.whiteboardwebapi.webapi.mocks.MockLogService;

class CounterServiceServletTest {
    static final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testDoGet() throws ServletException, IOException {
        MockLogService logservice = new MockLogService();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("http://localhost:8181/hello");
        MockHttpServletResponse response = new MockHttpServletResponse();

        CounterServiceServlet servlet = new CounterServiceServlet();
        servlet.setLogservice(logservice);

        servlet.service(request, response);

        assertEquals("application/json", response.getContentType());
        assertEquals(200, response.getStatus());
        byte[] responseBody = response.getOutputStreamBinaryContent();
        assertThat(responseBody).isNotEmpty();
        Count counter = mapper.readValue(responseBody, Count.class);
        assertEquals(0, counter.getCount());
    }

    @Test
    void testDoGetAfterCounterIncrement() throws ServletException, IOException {
        MockLogService logservice = new MockLogService();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("http://localhost:8181/hello");
        MockHttpServletResponse response = new MockHttpServletResponse();

        CounterServiceServlet servlet = new CounterServiceServlet();
        servlet.setLogservice(logservice);

        // Increment the counter twice
        HttpServletRequest postToIncrementCounter = mock(HttpServletRequest.class);
        when(postToIncrementCounter.getMethod()).thenReturn("POST");
        when(postToIncrementCounter.getRequestURI()).thenReturn("http://localhost:8181/hello");
        HttpServletResponse postResponse = new MockHttpServletResponse();
        servlet.service(postToIncrementCounter, postResponse);
        servlet.service(postToIncrementCounter, postResponse);

        servlet.service(request, response);

        assertEquals("application/json", response.getContentType());
        assertEquals(200, response.getStatus());
        byte[] responseBody = response.getOutputStreamBinaryContent();
        assertThat(responseBody).isNotEmpty();
        Count counter = mapper.readValue(responseBody, Count.class);
        assertEquals(2, counter.getCount());
    }

    @Test
    void testDoGetWithError() throws ServletException, IOException {
        MockLogService logservice = new MockLogService();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("http://localhost:8181/hello");
        MockHttpServletResponse response = spy(new MockHttpServletResponse());
        PrintWriter writer = mock(PrintWriter.class);
        doThrow(RuntimeException.class).when(writer).write(isA(char[].class), anyInt(), anyInt());
        when(response.getWriter()).thenReturn(writer);

        CounterServiceServlet servlet = new CounterServiceServlet();
        servlet.setLogservice(logservice);

        servlet.service(request, response);

        assertEquals(500, response.getStatus());
        assertEquals(0, response.getOutputStreamBinaryContent().length);
    }
}
