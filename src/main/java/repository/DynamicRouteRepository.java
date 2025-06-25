package repository;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import model.dynamic_routes_model;

@ApplicationScoped
public class DynamicRouteRepository {

    @PersistenceContext
    EntityManager em;

    public List<dynamic_routes_model> findEnabledRoutes() {
        List<dynamic_routes_model> routes = em.createQuery("SELECT r FROM dynamic_routes_model r WHERE r.enabled = true", dynamic_routes_model.class)
                .getResultList();
        System.out.println("Found " + routes.size() + " enabled dynamic routes.");
        return  routes;
    }
}