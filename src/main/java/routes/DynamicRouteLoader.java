package routes;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import model.dynamic_routes_model;

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
                    from(uri)
                        .log("Dynamic route called: " + routeId + " with body: ${body}")
                        .routeId(routeId)
                        .choice()
                            .when(header("CamelHttpMethod").isEqualTo(route.method))
                                .toD(targetURL+"?bridgeEndpoint=true")
                                .log("Calling target URL: " + targetURL + " with body: ${body}")
                            .otherwise()
                                .setBody(constant("Method not allowed"))
                                .setHeader("Content-Type", constant("text/plain"))
                                .setHeader("CamelHttpResponseCode", constant(405));
                }
            });
        }
    }
}
