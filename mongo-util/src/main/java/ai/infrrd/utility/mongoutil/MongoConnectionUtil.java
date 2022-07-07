package ai.infrrd.utility.mongoutil;

import ai.infrrd.utility.mongoutil.converter.BigDecimalDecimal128Converter;
import ai.infrrd.utility.mongoutil.converter.CustomConverter;
import ai.infrrd.utility.mongoutil.converter.Decimal128BigDecimalConverter;
import ai.infrrd.utility.mongoutil.entity.MongoProperties;
import ai.infrrd.utility.mongoutil.enums.Delimiter;
import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.connection.ClusterSettings;
import com.mongodb.connection.ClusterType;
import com.mongodb.connection.ConnectionPoolSettings;
import com.mongodb.connection.SocketSettings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.*;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/***
 * Class is used to deal with all mongo connection related behaviours
 */
@Slf4j
@Configuration
public class MongoConnectionUtil extends AbstractMongoClientConfiguration {

    @Autowired
    MongoProperties mongoProperties;
    @Autowired
    MongoDatabaseFactory mongoDatabaseFactory;

    @Autowired
    CustomConverter customConverter;


    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoDbFactory(), getMongoConverter(mongoDatabaseFactory));
    }
    @Bean
    public MongoDatabaseFactory mongoDbFactory() {
        return new SimpleMongoClientDatabaseFactory(mongoClient(), getDatabase());
    }
    @Bean
    public MongoClient mongoClient() {
        CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);

        MongoCredential credential = MongoCredential.createScramSha1Credential(getUsername(), getSource(), getPassword());

        ReadPreference readPreference = ReadPreference.valueOf(getReadPreference());

        ClusterSettings.Builder clusterSettingsBuilder = ClusterSettings.builder()
                .hosts(getServerAddresses())
                .requiredClusterType(ClusterType.valueOf(getClusterType()));

        if (StringUtils.isNotBlank(getReplicaSetName())) {
            clusterSettingsBuilder.requiredReplicaSetName(getReplicaSetName());
        }

        ConnectionPoolSettings connectionPoolSettings = ConnectionPoolSettings.builder()
                .minSize(getMinSize())
                .maxSize(getMaxSize())
                .maxConnectionIdleTime(getMaxConnectionIdleTimeMS(), TimeUnit.MILLISECONDS)
                .build();

        SocketSettings socketSettings = SocketSettings.builder()
                .connectTimeout(getConnectTimeoutMS(), TimeUnit.MILLISECONDS)
                .build();

        WriteConcern writeConcern = WriteConcern.valueOf(getWriteConcernName());

        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .codecRegistry(codecRegistry)
                .credential(credential)
                .readPreference(readPreference)
                .writeConcern(writeConcern)
                .applyToClusterSettings(builder -> builder.applySettings(clusterSettingsBuilder.build()))
                .applyToConnectionPoolSettings(builder -> builder.applySettings(connectionPoolSettings))
                .applyToSocketSettings(builder -> builder.applySettings(socketSettings))
                .build();

        return MongoClients.create(clientSettings);
    }
    @Bean
    public MongoConverter getMongoConverter(MongoDatabaseFactory mongoDatabaseFactory) {

        DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDatabaseFactory);
        MongoCustomConversions conversions = getCustomConversions();

        MongoMappingContext mappingContext = new MongoMappingContext();
        mappingContext.setSimpleTypeHolder(SimpleTypeHolder.DEFAULT);
        mappingContext.afterPropertiesSet();

        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, mappingContext);
        converter.setCustomConversions(conversions);
        converter.setCodecRegistryProvider(mongoDatabaseFactory);
        converter.afterPropertiesSet();

        return converter;
    }

    @Bean
    public MongoCustomConversions getCustomConversions(){
        List<Converter<?, ?>> converterList = new ArrayList<Converter<?, ?>>();
        converterList.add(new BigDecimalDecimal128Converter());
        converterList.add(new Decimal128BigDecimalConverter());
        if(customConverter !=null && !CollectionUtils.isEmpty(customConverter.getCustomConverterList()))
        {
            converterList.addAll(customConverter.getCustomConverterList());
        }
        return new MongoCustomConversions(converterList);
    }



    private List<ServerAddress> getServerAddresses() {
        List<ServerAddress> serverAddresses = new ArrayList<>();
        String mongoConnect = mongoProperties.getMongoDb().getClusterSettings().getConnect();
        log.info("Trying to create mongo server addresses from mongo connect property: {}", mongoConnect);

        /*
        Below code will create a list of server address with host and port from a string like
        mongoConnect = hostname1:port1,hostname2:port2,hostname3:port3
         */
        StringTokenizer commaSeparatedTokenizer = new StringTokenizer(mongoConnect, Delimiter.COMMA.getValue());
        while (commaSeparatedTokenizer.hasMoreTokens()) {
            String commaSeparatedToken = commaSeparatedTokenizer.nextToken();
            /*
            Below code will parse host and port from the string like hostname1:port1
             */
            StringTokenizer colonSeparatedTokenizer = new StringTokenizer(commaSeparatedToken, Delimiter.COLON.getValue());
            String host = colonSeparatedTokenizer.nextToken();
            int port = Integer.parseInt(colonSeparatedTokenizer.nextToken());
            serverAddresses.add(new ServerAddress(host, port));
        }
        return serverAddresses;
    }


    private String getClusterType() {
        return mongoProperties.getMongoDb().getClusterSettings().getClusterType();
    }

    private String getWriteConcernName() {
        return mongoProperties.getMongoDb().getWriteConcern().getName();
    }

    private String getSource() {
        return mongoProperties.getMongoDb().getCredential().getSource();
    }

    private String getUsername() {
        return mongoProperties.getMongoDb().getCredential().getUsername();
    }

    private char[] getPassword() {
        return mongoProperties.getMongoDb().getCredential().getPassword().toCharArray();
    }

    private String getDatabase() {
        return mongoProperties.getMongoDb().getDatabase();
    }

    private String getReadPreference() {
        return mongoProperties.getMongoDb().getReadPreference();
    }

    private int getConnectTimeoutMS() {
        return mongoProperties.getMongoDb().getSocketSettings().getConnectTimeoutMS();
    }

    private int getMaxConnectionIdleTimeMS() {
        return mongoProperties.getMongoDb().getConnectionPoolSettings().getMaxConnectionIdleTimeMS();
    }

    private int getMaxSize() {
        return mongoProperties.getMongoDb().getConnectionPoolSettings().getMaxSize();
    }

    private int getMinSize() {
        return mongoProperties.getMongoDb().getConnectionPoolSettings().getMinSize();
    }

    private String getReplicaSetName() {
        return mongoProperties.getMongoDb().getClusterSettings().getReplicaSetName();
    }

    @Override
    protected String getDatabaseName() {
        return mongoProperties.getMongoDb().getDatabase();
    }
}
