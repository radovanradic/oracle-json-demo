package org.example;

import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.Micronaut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.utility.DockerImageName;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        boolean useContainer = true;
        OracleContainer oracleContainer = null;
        if (useContainer) {
            oracleContainer = new OracleContainer(DockerImageName.parse("gvenzl/oracle-free:latest-faststart")
                    .asCompatibleSubstituteFor("gvenzl/oracle-xe"))
                    .withDatabaseName("test");
        }
        try {
            if (useContainer) {
                oracleContainer.start();
            }

            ApplicationContext applicationContext = initApp(oracleContainer);
            DemoRunner demoRunner = applicationContext.getBean(DemoRunner.class);

            demoRunner.runDemo();

            applicationContext.stop();
        } catch (Exception e) {
            LOG.error("An error occurred", e);
            System.exit(1);
        } finally {
            if (oracleContainer != null) {
                oracleContainer.stop();
                oracleContainer.close();
            }
        }
    }

    /**
     * Initializes application context using datasource parameters from the underlying test container.
     * If app is using OracleContainer test container, then it will use datasource parameters from the container.
     * Other way could be running this docker command
     * >> docker run -p 1521:1521 -e ORACLE_PWD=test -d --name oracle container-registry.oracle.com/database/free
     * and then these hard-coded values would be used
     *
     * @param oracleContainer the Oracle test container started before app init
     * @return the application context
     */
    private static ApplicationContext initApp(OracleContainer oracleContainer) {
        String username = "system";
        String password = "test";
        String url = "jdbc:oracle:thin:@127.0.0.1:1521/FREEPDB1";
        if (oracleContainer != null) {
            username = oracleContainer.getUsername();
            password = oracleContainer.getPassword();
            url = oracleContainer.getJdbcUrl();
        }
        ApplicationContext applicationContext = Micronaut.run(Main.class, "-jdbc-url=" + url,
                "-jdbc-username=" + username, "-jdbc-password=" + password);
        return applicationContext;
    }
}