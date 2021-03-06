/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.file;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.util.FileUtil;
import org.junit.Before;
import org.junit.Test;

public class FileProducerFileExistFailTest extends ContextTestSupport {

    @Override
    @Before
    public void setUp() throws Exception {
        deleteDirectory("target/data/file");
        super.setUp();
    }

    @Test
    public void testFail() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedBodiesReceived("Hello World");
        mock.expectedFileExists("target/data/file/hello.txt", "Hello World");

        template.sendBodyAndHeader("file://target/data/file", "Hello World", Exchange.FILE_NAME, "hello.txt");

        try {
            template.sendBodyAndHeader("file://target/data/file?fileExist=Fail", "Bye World", Exchange.FILE_NAME, "hello.txt");
            fail("Should have thrown a GenericFileOperationFailedException");
        } catch (CamelExecutionException e) {
            GenericFileOperationFailedException cause = assertIsInstanceOf(GenericFileOperationFailedException.class, e.getCause());
            assertEquals(FileUtil.normalizePath("File already exist: target/data/file/hello.txt. Cannot write new file."), cause.getMessage());
        }

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("file://target/data/file?noop=true&initialDelay=0&delay=10").convertBodyTo(String.class).to("mock:result");
            }
        };
    }
}