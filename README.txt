Simple Run (all server)
    java -cp app-1.0-SNAPSHOT.jar \
        -Djsfilter.path=plugins -Dloader.path=lib/ \
        -Dloader.main=org.kendar.Main -jar app-1.0-SNAPSHOT.jar\
        org.springframework.boot.loader.PropertiesLauncher

        java "-Dloader.path=/start/services/answering/libs" -Dloader.main=org.kendar.Main -jar app-1.0-SNAPSHOT.jar \
        	 org.springframework.boot.loader.PropertiesLauncher &

        java "-Dloader.path=/start/services/answering/libs"  -Dloader.main=org.kendar.Main  \
        	 -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5005 \
        	 -Dother.dns=127.0.0.11 -Djdk.tls.acknowledgeCloseNotify=true \
        	 -jar app-1.0-SNAPSHOT.jar org.springframework.boot.loader.PropertiesLauncher &

Simple Run (dns server only)
    java -Dother.dns=dns.google,dns.local.com \
        -jar simpledns-1.0-SNAPSHOT.jar

curl -i -H "X-TEST-OVERWRITE-HOST:https:\\www.kendar.org" -XGET "http://localhost:20080"

REM  -Djdk.tls.server.protocols=TLSv1.2 -Djavax.net.debug=all



Remove the jar from the dependency
Inside the jar be sure to add the lib definitions download, here will be loaded the dependencies

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-dependency-plugin</artifactId>
                <executions>
                    <execution>
                        <id>copy-dependencies</id>
                        <phase>prepare-package</phase>
                        <goals>
                            <goal>copy-dependencies</goal>
                        </goals>
                        <configuration>
                            <outputDirectory>${project.build.directory}/classes/lib</outputDirectory>
                            <overWriteReleases>false</overWriteReleases>
                            <overWriteSnapshots>false</overWriteSnapshots>
                            <overWriteIfNewer>true</overWriteIfNewer>
                        </configuration>
                    </execution>
                </executions>
            </plugin>

Copy the target of the plugin inside the lib dir AND the dependencies not present in boot uber-jar
Run with
java -cp app-1.0-SNAPSHOT.jar -Djsfilter.path=plugins -Dloader.path=lib/ -Dloader.main=org.kendar.Main org.springframework.boot.loader.PropertiesLauncher

Those are the c:\Windows\System32\Drivers\etc\hosts entries

127.0.0.1 www.local.org
127.0.0.1 replayer.local.org
127.0.0.1 oidc.local.org
127.0.0.1 redirect.test.org
127.0.0.1 js.test.org


### SAMPLE OF DB BASED APP


@Component
public class ReplayerDb implements DerbyApplication {
    private Logger logger;
    private Environment environment;
    @Value("${replayer.db:replayer}")
    private String dbName;
    @Value("${derby.port:1527}")
    private int dbPort;
    @Value("${derby.root.user:root}")
    private String user;
    @Value("${derby.root.password:root}")
    private String password;

    public ReplayerDb(LoggerBuilder loggerBuilder, Environment environment){
        logger = loggerBuilder.build(ReplayerDb.class);
        logger.info("Replayer server LOADED");
        this.environment = environment;

    }

    @PostConstruct
    public void init(){
        MutablePropertySources propertySources = ((ConfigurableEnvironment)environment).getPropertySources();
        Map<String,Object> propMap = new HashMap<>();
        propMap.put("replayer.jdbc.url","jdbc:derby://localhost:"+ dbPort+"/"+dbName+";create=true");
        propertySources.addFirst(new MapPropertySource("replayer.calculated", propMap));
    }

    @Override
    public String dbName() {
        return dbName;
    }

    @Override
    public String canaryTable() {
        return "requests";
    }

    @Override
    public void initializeDb(Connection conn) {


        try {
            Statement createTableStatement = conn.createStatement();
            String createTable = "CREATE TABLE requests(\n" +
                    "   id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),\n" +
                    "   r_ms BIGINT NOT NULL,\n" +
                    "   r_cycleid VARCHAR(255),\n" +
                    "   r_method VARCHAR(16) ,\n" +
                    "   r_path VARCHAR(16000) ,\n" +
                    "   r_query VARCHAR(16000)  ,\n" +
                    "   r_isdone BOOLEAN DEFAULT false,\n" +
                    "   r_isstatic BOOLEAN DEFAULT false,\n" +
                    "   r_httpcode INTEGER DEFAULT 200,\n" +
                    "   r_headers CLOB  DEFAULT '' ,\n" +
                    "   r_response_text CLOB  DEFAULT NULL,\n" +
                    "   r_response_bin CLOB  DEFAULT NULL,\n" +
                    "   r_request_text CLOB  DEFAULT NULL,\n" +
                    "   r_request_bin CLOB  DEFAULT NULL,\n" +
                    "   UNIQUE (id)\n" +
                    ")";
            createTableStatement.executeUpdate(createTable);

            Statement createIndexStatemen = conn.createStatement();
            String createIndex ="CREATE INDEX basic_search ON requests\n" +
                    "(\n" +
                    "    r_cycleid,\n" +
                    "    r_ms ASC,\n" +
                    "    id ASC,\n" +
                    "    r_method,\n" +
                    "    r_path,\n" +
                    "    r_isdone\n" +
                    ")";

            createIndexStatemen.executeUpdate(createIndex);
            conn.commit();

            logger.info("Replayer database LOADED");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void resetDb() {

    }

    @Override
    public String connectionString() {
        return environment.getProperty("replayer.jdbc.url");
    }

    /*public String getSessionFactory(){
        DataSource dataSource = BlogDataSourceFactory.getBlogDataSource();
        TransactionFactory transactionFactory =
                new JdbcTransactionFactory();
        Environment environment =
                new Environment("development", transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.addMapper(BlogMapper.class);
        SqlSessionFactory sqlSessionFactory =
                new SqlSessionFactoryBuilder().build(configuration);
    }*/
}



curl -F 'img_avatar=@favicon.ico' http://replayer.local.org/api/recording
curl \
  -F "userid=1" \
  -F "filecomment=This is an image file" \
  -F "image=@favicon.ico" \
  http://replayer.local.org/api/recording