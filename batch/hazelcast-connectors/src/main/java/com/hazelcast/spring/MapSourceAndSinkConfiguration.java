package com.hazelcast.spring;

import com.hazelcast.core.IMap;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.Pipeline;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.hazelcast.spring")
public class MapSourceAndSinkConfiguration {

    @Bean
    public JetInstance newJetInstance() {
        return Jet.newJetInstance();
    }

    @Bean
    @Qualifier("sourceMap")
    public IMap source(JetInstance jetInstance) {
        return jetInstance.getMap(MapSourceAndSinkSpring.SOURCE_NAME);
    }

    @Bean
    @Qualifier("sinkMap")
    public IMap sink(JetInstance jetInstance) {
        return jetInstance.getMap(MapSourceAndSinkSpring.SINK_NAME);
    }

    @Bean
    public Pipeline pipeline() {
        return Pipeline.create();
    }

}
