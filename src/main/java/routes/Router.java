package routes;

import org.apache.camel.builder.RouteBuilder;

public class Router extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("direct:serviceA")
                .routeId("serviceA")
                .log("Service A called with body: ${body}")
                .setBody(simple("Response from Service A"))
                // .to("netty-http:http://localhost:8081")
                .log("[Service A] Response Sent: ${body}");

        from("direct:serviceB")
                .routeId("serviceB")
                .log("Service D called with body: ${body}")
                .setBody(simple("Response from Service B"))
                .to("netty-http:http://localhost:8082")
                .log("[Service B] Response Sent: ${body}");
    }

}