package routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import jakarta.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class RestRouter extends RouteBuilder {
    
    @Override
    public void configure() throws Exception {

        restConfiguration()
            .component("platform-http")
            .contextPath("/");


        rest("/getAllRoutes")
            .get()
            .to("direct:getAllRoutes");

    
        from("direct:getAllRoutes")
            .to("sql: SELECT * FROM dynamic_routes_model WHERE enabled = true")
            .log("${body}")
            .marshal().json(JsonLibrary.Jackson);
    }
}
