// package rest;

// import org.apache.camel.ProducerTemplate;
// import org.apache.camel.builder.RouteBuilder;

// import jakarta.enterprise.context.ApplicationScoped;

// public class rest extends RouteBuilder {

//     @Override
//     public void configure() throws Exception {

//         // restConfiguration().component("netty-http");

//         //     rest("/call")
//         //                 .get()
//         //                 .to("direct:callService");


//         //     from("direct:callService")
//         //             .log("Calling internal or external service...").log("Received POST request with body: ${body}")
//         //             .setHeader("Content-Type", constant("application/json")) // optional, auto-set usually
//         //             .transform().simple("{\"echoed\": \"${body}\"}");
//         // }
// }
