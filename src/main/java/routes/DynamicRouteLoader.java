package routes;

import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import model.dynamic_routes_model;

@ApplicationScoped
public class DynamicRouteLoader {
    @Inject
    EntityManager em;

    @Inject
    CamelContext camelContext;


    @PostConstruct
    void loadRoutes() throws Exception {
        // Load dynamic routes from the database
       List<dynamic_routes_model> routes = em.createQuery("SELECT r FROM dynamic_routes_model r WHERE r.enabled = true", model.dynamic_routes_model.class)
                       .getResultList();
        System.out.println("Found " + routes.size() + " dynamic routes to load.");

        for (dynamic_routes_model route : routes) {
           String routeId = "dynamic-" + route.id;
           System.out.println("Loading dynamic route: " + routeId + " for path: " + route.path + " with method: " + route.method + " to target URL: " + route.targetUrl);
           if(camelContext.getRouteController().getRouteStatus(routeId) == null) {
               // Create a new route for each dynamic route
               camelContext.addRoutes(new RouteBuilder() {
                   @Override
                   public void configure() throws Exception {
                       from("platform-http:" + route.path)
                           .choice()
                               .when(header("CamelHttpMethod").isEqualTo(route.method))
                                   .to(route.targetUrl)
                               .otherwise()
                                   .setBody(constant("Method not allowed"))
                                   .setHeader("Content-Type", constant("text/plain"))
                                   .setHeader("CamelHttpResponseCode", constant(405));
                   }
               });
           }
        }   

    }
    
}
