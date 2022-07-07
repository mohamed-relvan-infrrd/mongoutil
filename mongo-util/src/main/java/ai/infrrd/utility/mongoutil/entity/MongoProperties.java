package ai.infrrd.utility.mongoutil.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(ignoreInvalidFields = true)
@Getter
@Setter
public class MongoProperties {

    private MongoDb mongoDb;

    @Getter
    @Setter
    public static class MongoDb {
        private String database;
        private ClusterSettings clusterSettings;
        private Credential credential;
        private String readPreference;
        private SocketSettings socketSettings;
        private ConnectionPoolSettings connectionPoolSettings;
        private WriteConcern writeConcern;
    }

    @Getter
    @Setter
    public static class ClusterSettings {
        private String connect;
        private String clusterType;
        private String replicaSetName;
    }

    @Getter
    @Setter
    public static class Credential {
        private String username;
        private String password;
        private String source;
    }

    @Getter
    @Setter
    public static class SocketSettings {
        private int connectTimeoutMS;
    }

    @Getter
    @Setter
    public static class ConnectionPoolSettings {
        private int maxSize;
        private int minSize;
        private int maxConnectionIdleTimeMS;

    }

    @Getter
    @Setter
    public static class WriteConcern {
        private String name;

    }


}
