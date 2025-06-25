import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import routes.DynamicRouteLoader;

@ApplicationScoped
public class AppStartup {
    @Inject
    DynamicRouteLoader loader;
}
