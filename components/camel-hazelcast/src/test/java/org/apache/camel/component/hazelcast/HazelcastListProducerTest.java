/**
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
package org.apache.camel.component.hazelcast;

import java.util.ArrayList;
import java.util.List;

import com.hazelcast.core.HazelcastInstance;

import com.hazelcast.core.IList;
import org.apache.camel.builder.RouteBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

public class HazelcastListProducerTest extends HazelcastCamelTestSupport {

    @Mock
    private IList<String> list;

    @Override
    protected void trainHazelcastInstance(HazelcastInstance hazelcastInstance) {
        when(hazelcastInstance.<String>getList("bar")).thenReturn(list);
    }

    @Override
    protected void verifyHazelcastInstance(HazelcastInstance hazelcastInstance) {
        verify(hazelcastInstance, atLeastOnce()).getList("bar");
    }

    @After
    public final void verifyListMock() {
        verifyNoMoreInteractions(list);
    }

    @Test
    public void addValue() throws InterruptedException {
        template.sendBody("direct:add", "bar");
        verify(list).add("bar");
    }

    @Test
    public void removeValue() throws InterruptedException {
        template.sendBody("direct:removevalue", "foo2");
        verify(list).remove("foo2");
    }

    @Test
    public void getValueWithIdx() {
        when(list.get(1)).thenReturn("foo2");
        template.sendBodyAndHeader("direct:get", "test", HazelcastConstants.OBJECT_POS, 1);
        verify(list).get(1);
        assertEquals("foo2", consumer.receiveBody("seda:out", 5000, String.class));
    }

    @Test
    public void setValueWithIdx() {
        template.sendBodyAndHeader("direct:set", "test", HazelcastConstants.OBJECT_POS, 1);
        verify(list).set(1, "test");
    }

    @Test
    public void removeValueWithIdx() {
        template.sendBodyAndHeader("direct:removevalue", null, HazelcastConstants.OBJECT_POS, 1);
        verify(list).remove(1);
    }

    @Test
    public void removeValueWithoutIdx() {
        template.sendBody("direct:removevalue", "foo1");
        verify(list).remove("foo1");
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:add").setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.ADD_OPERATION)).to(String.format("hazelcast:%sbar", HazelcastConstants.LIST_PREFIX));

                from("direct:set").setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.SETVALUE_OPERATION)).to(String.format("hazelcast:%sbar", HazelcastConstants.LIST_PREFIX));

                from("direct:get").setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.GET_OPERATION)).to(String.format("hazelcast:%sbar", HazelcastConstants.LIST_PREFIX))
                        .to("seda:out");

                from("direct:removevalue").setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.REMOVEVALUE_OPERATION)).to(
                        String.format("hazelcast:%sbar", HazelcastConstants.LIST_PREFIX));
            }
        };
    }

}

