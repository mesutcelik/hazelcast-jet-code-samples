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

package refman;

import com.hazelcast.core.IMap;
import com.hazelcast.jet.core.DAG;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.core.Partitioner;
import com.hazelcast.jet.core.Vertex;
import com.hazelcast.jet.core.processor.Processors;
import com.hazelcast.jet.core.processor.SinkProcessors;
import com.hazelcast.jet.core.processor.SourceProcessors;

import java.util.Map.Entry;
import java.util.regex.Pattern;

import static com.hazelcast.jet.core.Edge.between;
import static com.hazelcast.jet.Traversers.traverseArray;
import static com.hazelcast.jet.aggregate.AggregateOperations.counting;
import static com.hazelcast.jet.function.DistributedFunctions.entryKey;
import static com.hazelcast.jet.function.DistributedFunctions.wholeItem;

/**
 * Code appearing in the Reference Manual, Under the Hood -> How
 * Distributed Computing Works in Jet.
 */
//CHECKSTYLE:OFF
public class WordCountCoreApiRefMan {
    public static void main(String[] args) throws Exception {

        DAG dag = new DAG();
        Vertex source = dag.newVertex("source", SourceProcessors.readMapP("lines"));

        // (lineNum, line) -> words
        Pattern delimiter = Pattern.compile("\\W+");
        Vertex tokenize = dag.newVertex("tokenize",
                Processors.flatMapP((Entry<Integer, String> e) ->
                        traverseArray(delimiter.split(e.getValue().toLowerCase()))
                                .filter(word -> !word.isEmpty()))
        );

        // word -> (word, count)
        Vertex accumulate = dag.newVertex("accumulate",
                Processors.accumulateByKeyP(wholeItem(), counting())
        );

        // (word, count) -> (word, count)
        Vertex combine = dag.newVertex("combine",
                Processors.combineByKeyP(counting())
        );

        Vertex sink = dag.newVertex("sink", SinkProcessors.writeMapP("counts"));

        dag.edge(between(source, tokenize))
           .edge(between(tokenize, accumulate)
                   .partitioned(wholeItem(), Partitioner.HASH_CODE))
           .edge(between(accumulate, combine)
                   .distributed()
                   .partitioned(entryKey()))
           .edge(between(combine, sink));


        JetInstance jet = Jet.newJetInstance();
        Jet.newJetInstance();

        try {

            IMap<Integer, String> map = jet.getMap("lines");
            map.put(0, "It was the best of times,");
            map.put(1, "it was the worst of times,");
            map.put(2, "it was the age of wisdom,");
            map.put(3, "it was the age of foolishness,");
            map.put(4, "it was the epoch of belief,");
            map.put(5, "it was the epoch of incredulity,");
            map.put(6, "it was the season of Light,");
            map.put(7, "it was the season of Darkness");
            map.put(8, "it was the spring of hope,");
            map.put(9, "it was the winter of despair,");
            map.put(10, "we had everything before us,");
            map.put(11, "we had nothing before us,");
            map.put(12, "we were all going direct to Heaven,");
            map.put(13, "we were all going direct the other way --");
            map.put(14, "in short, the period was so far like the present period, that some of "
                    + "its noisiest authorities insisted on its being received, for good or for "
                    + "evil, in the superlative degree of comparison only.");

            jet.newJob(dag).join();
            System.out.println(jet.getMap("counts").entrySet());
            //      Expected output:
            // [heaven=1, times=2, of=12, its=2, far=1, light=1, noisiest=1,
            // the=14, other=1, incredulity=1, worst=1, hope=1, on=1, good=1, going=2,
            // like=1, we=4, was=11, best=1, nothing=1, degree=1, epoch=2, all=2,
            // that=1, us=2, winter=1, it=10, present=1, to=1, short=1, period=2,
            // had=2, wisdom=1, received=1, superlative=1, age=2, darkness=1, direct=2,
            // only=1, in=2, before=2, were=2, so=1, season=2, evil=1, being=1,
            // insisted=1, despair=1, belief=1, comparison=1, some=1, foolishness=1,
            // or=1, everything=1, spring=1, authorities=1, way=1, for=2]
        }
        finally {
            Jet.shutdownAll();
        }
    }
}
