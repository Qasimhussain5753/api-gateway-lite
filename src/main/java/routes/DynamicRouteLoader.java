package routes;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import model.dynamic_routes_model;
import processor.ErrorResponseProcessor;
import processor.NotFoundResponseProcessor;

@ApplicationScoped
public class DynamicRouteLoader {

    @Inject
    EntityManager em;

    @Inject
    CamelContext camelContext;

    public void loadRoutes(@Observes StartupEvent ev) throws Exception {
        List<dynamic_routes_model> routes = em.createQuery("SELECT r FROM dynamic_routes_model r WHERE r.enabled = true", dynamic_routes_model.class)
                                              .getResultList();

        System.out.println("Found " + routes.size() + " dynamic routes to load: " + routes.stream().map(r -> r.ttl).collect(Collectors.toList()));

        for (dynamic_routes_model route : routes) {
            String routeId = "dynamic-route-" + route.id;
            String routePath = route.path.startsWith("/") ? route.path : "/" + route.path;
            String uri = "platform-http:" + routePath;

            String targetURL = route.targetUrl.startsWith("http") ? route.targetUrl : "http://" + route.targetUrl;
            
            System.out.println(uri + " for route: " + routePath);
            if (camelContext.getRoute(routeId) != null) {
                System.out.println("Route already exists: " + routeId);
                continue;
            }

            System.out.println("Loading dynamic route: " + routeId + " for path: " + uri);

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {

                    
                    onException(Exception.class)
                                    .handled(true)
                                    .log(LoggingLevel.ERROR, "Exception caught: ${exception.stacktrace}") 
                                    .process(new ErrorResponseProcessor());

            
                    from(uri)
                        .log("Dynamic route called: " + routeId + " with body: ${body}")
                        .routeId(routeId)
                         .process(exchange -> {
                            String path = exchange.getIn().getHeader(Exchange.HTTP_PATH, String.class);
                            if (path == null) {
                                path = "";
                            }
                            exchange.setProperty("requestPath", path);
                         })
                        .choice()
                            .when(header("CamelHttpMethod").isEqualTo(route.method))
                                .toD(targetURL+"?bridgeEndpoint=true")
                                .log("Calling target URL: " + targetURL + " - method: ${header.CamelHttpMethod}")
                            .when(header("CamelHttpMethod").isNotEqualTo(route.method))
                                .setBody(constant("Method not allowed"))
                                .setHeader("Content-Type", constant("text/plain"))
                                .setHeader("CamelHttpResponseCode", constant(405))
                            .otherwise()
                                .process(new NotFoundResponseProcessor());

                                  
                    

                    from("platform-http:/api")
                        .routeId("catch-all")
                        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(404))
                        .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                        .setBody(constant("{\"error\": \"Path not found\"}"));

                }
            });
        }
    }
}
