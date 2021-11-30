package dev.educery.server;

import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import static dev.educery.server.MainController.ConfigurationFile;
import static dev.educery.server.MainController.FacadePackage;
import static dev.educery.server.MainController.StoragePackage;
import dev.educery.utils.Logging;
import static dev.educery.utils.Utils.hasSome;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Launches embedded Tomcat hosting CXF servlet.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
@Configuration
@EnableAutoConfiguration
@ImportResource({ ConfigurationFile })
@ComponentScan(basePackages = { FacadePackage, StoragePackage })
public class MainController extends WebMvcConfigurerAdapter implements Logging {

    public static final String ApiPath = "/api/*";
    public static final String FacadePackage = "dev.educery.services";
    public static final String StoragePackage = "dev.educery.storage";
    public static final String ConfigurationFile = "classpath:hosted-service.xml";

    @Autowired ApplicationContext context;

    @Value("${server.port:9001}")
    int serverPort;

    @Value("${server.address:}")
    String serverAddress;

    @Value("${spring.profiles.active:}")
    String springProfiles;

    static ConfigurableApplicationContext ServiceContext;
    public static void shutDown() { ServiceContext.close(); }
    public static void main(String... args) { ServiceContext = startApplication(args); }

    public static final String DefaultProfile = "direct";
    static final String SpringProfile = "spring.profiles.active";
    public static void setProfile(String p) { System.setProperty(SpringProfile, p); }
    public static ConfigurableApplicationContext startApplication(String... args) {
        Logging.StaticLogger.report("starting service");
        if (hasSome(args)) setProfile(args[0]); else setProfile(DefaultProfile);
        ServiceContext = SpringApplication.run(MainController.class, args);
        return ServiceContext;
    }


    static final String StartMessage = "hosting service at %s:%d with profiles '%s'";
    @Bean public TomcatServletWebServerFactory containerFactory() {
        report(String.format(StartMessage, serverAddress, serverPort, springProfiles));
        return new TomcatServletWebServerFactory(Empty, serverPort);
    }

    @Bean public ServletRegistrationBean servletRegistration() {
        ServletRegistrationBean result = new ServletRegistrationBean(new CXFServlet(), ApiPath);
        result.setLoadOnStartup(1);
        return result;
    }

    @Override public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/docs/").setViewName("forward:/docs/index.html");
        registry.addViewController("/docs/ui/").setViewName("forward:/docs/ui/index.html");
    }

    @Override public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/docs/**")
                .addResourceLocations("classpath:/resources/docs/");
    }

} // MainController
