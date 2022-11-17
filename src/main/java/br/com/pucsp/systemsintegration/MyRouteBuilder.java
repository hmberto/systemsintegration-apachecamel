package br.com.pucsp.systemsintegration;

import org.apache.camel.builder.RouteBuilder;

public class MyRouteBuilder extends RouteBuilder {
    public void configure() {

        from("file:src/input?noop=true")
            .choice()
                .when(xpath("/person/city = 'London'"))
                    .log("UK message")
                    .to("file:src/output/uk")
                .otherwise()
                    .log("Other message")
                    .to("file:src/output/others");
    }
}