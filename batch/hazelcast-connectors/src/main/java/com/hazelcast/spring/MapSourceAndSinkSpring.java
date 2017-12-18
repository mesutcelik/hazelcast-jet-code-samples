package com.hazelcast.spring;

/*
 * Copyright (c) 2008-2017, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.hazelcast.core.IMap;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.Pipeline;
import com.hazelcast.jet.Sinks;
import com.hazelcast.jet.Sources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import sun.jvm.hotspot.HelloWorld;

import javax.annotation.Resource;

/**
 * Demonstrates the usage of Hazelcast IMap as source and sink
 * with the Pipeline API. This will take the contents of one map
 * and write it into another map.
 */
@Component
public class MapSourceAndSinkSpring {

    private static final int ITEM_COUNT = 10;
    public static final String SOURCE_NAME = "source";
    public static final String SINK_NAME = "sink";

    @Autowired
    Pipeline p;

    @Autowired
    JetInstance jetInstance;

    @Autowired
    @Qualifier("sourceMap")
    IMap sourceMap;

    @Autowired
    @Qualifier("sinkMap")
    IMap sinkMap;

    public void runApp() {

        for (int i = 0; i < ITEM_COUNT; i++) {
            sourceMap.put(i, i);
        }

        p.drawFrom(Sources.<Integer, Integer>map(SOURCE_NAME))
                .drainTo(Sinks.map(SINK_NAME));
        jetInstance.newJob(p).join();

        System.out.println("Sink map entries: " + sinkMap.entrySet());


    }

}
