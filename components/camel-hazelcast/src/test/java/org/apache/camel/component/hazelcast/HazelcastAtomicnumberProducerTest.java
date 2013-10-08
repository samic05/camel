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

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;
import org.apache.camel.builder.RouteBuilder;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.*;

public class HazelcastAtomicnumberProducerTest extends HazelcastCamelTestSupport {

    @Mock
    private IAtomicLong atomicNumber;

    @Override
    protected void trainHazelcastInstance(HazelcastInstance hazelcastInstance) {
        when(hazelcastInstance.getAtomicLong("foo")).thenReturn(atomicNumber);
    }

    @Override
    protected void verifyHazelcastInstance(HazelcastInstance hazelcastInstance) {
        verify(hazelcastInstance, atLeastOnce()).getAtomicLong("foo");
    }

    @After
    public void verifyAtomicNumberMock() {
        verifyNoMoreInteractions(atomicNumber);
    }

    @Test
    public void testSet() {
        template.sendBody("direct:set", 4711);
        verify(atomicNumber).set(4711);
    }

    @Test
    public void testGet() {
        when(atomicNumber.get()).thenReturn(1234L);
        long body = template.requestBody("direct:get", null, Long.class);
        verify(atomicNumber).get();
        assertEquals(1234, body);
    }

    @Test
    public void testIncrement() {
        when(atomicNumber.incrementAndGet()).thenReturn(11L);
        long body = template.requestBody("direct:increment", null, Long.class);
        verify(atomicNumber).incrementAndGet();
        assertEquals(11, body);
    }

    @Test
    public void testDecrement() {
        when(atomicNumber.decrementAndGet()).thenReturn(9L);
        long body = template.requestBody("direct:decrement", null, Long.class);
        verify(atomicNumber).decrementAndGet();
        assertEquals(9, body);
    }

    @Test
    public void testDestroy() throws InterruptedException {
        template.sendBody("direct:destroy", null);
        verify(atomicNumber).destroy();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {

                from("direct:set").setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.SETVALUE_OPERATION))
                        .to(String.format("hazelcast:%sfoo", HazelcastConstants.ATOMICNUMBER_PREFIX));

                from("direct:get").setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.GET_OPERATION)).to(String.format("hazelcast:%sfoo", HazelcastConstants.ATOMICNUMBER_PREFIX));

                from("direct:increment").setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.INCREMENT_OPERATION)).to(
                        String.format("hazelcast:%sfoo", HazelcastConstants.ATOMICNUMBER_PREFIX));

                from("direct:decrement").setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.DECREMENT_OPERATION)).to(
                        String.format("hazelcast:%sfoo", HazelcastConstants.ATOMICNUMBER_PREFIX));

                from("direct:destroy").setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.DESTROY_OPERATION)).to(
                        String.format("hazelcast:%sfoo", HazelcastConstants.ATOMICNUMBER_PREFIX));

            }
        };
    }

}
