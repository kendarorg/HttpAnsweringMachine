https://medium.com/@niktrix/getting-rid-of-systemd-resolved-consuming-port-53-605f0234f32f


sudo lsof -i -P -n | grep LISTEN

stop systemd-resolved “ sudo systemctl stop systemd-resolved”
edit /etc/systemd/resolved.conf with these
[Resolve]
DNS=8.8.8.8
DNSStubListener=no

sudo ln -sf /run/systemd/resolve/resolv.conf /etc/resolv.conf


Simple Run (all server)
java -cp app-2.1.3.jar \
-Djsfilter.path=plugins -Dloader.path=lib/ \
-Dloader.main=org.kendar.Main -jar app-2.1.3.jar\
org.springframework.boot.loader.PropertiesLauncher

        java "-Dloader.path=/start/services/answering/libs" -Dloader.main=org.kendar.Main -jar app-2.1.3.jar \
        	 org.springframework.boot.loader.PropertiesLauncher &

        java "-Dloader.path=/start/services/answering/libs"  -Dloader.main=org.kendar.Main  \
        	 -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5005 \
        	 -Dother.dns=127.0.0.11 -Djdk.tls.acknowledgeCloseNotify=true \
        	 -jar app-2.1.3.jar org.springframework.boot.loader.PropertiesLauncher &

Simple Run (dns server only)
java -Dother.dns=dns.google,dns.local.com \
-jar simpledns-2.1.3.jar

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
java -cp app-2.1.3.jar -Djsfilter.path=plugins -Dloader.path=lib/ -Dloader.main=org.kendar.Main org.springframework.boot.loader.PropertiesLauncher

Those are the c:\Windows\System32\Drivers\etc\hosts entries

127.0.0.1 www.local.org
127.0.0.1 replayer.local.org
127.0.0.1 oidc.local.org
127.0.0.1 redirect.test.org
127.0.0.1 js.test.org


### SAMPLE OF DB BASED APP

Notice the name of variables are the path inside the json

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



curl -F 'img_avatar=@favicon.ico' http://replayer.local.org/api/plugins/replayer/recording
curl \
-F "userid=1" \
-F "filecomment=This is an image file" \
-F "image=@favicon.ico" \
http://replayer.local.org/api/plugins/replayer/recording



https://github.com/springfox/springfox/blob/master/springfox-spring-web/src/main/java/springfox/documentation/spring/web/scanners/ApiListingScanner.java

      List<RequestMappingContext> requestMappings = nullToEmptyList(requestMappingsByResourceGroup.get(resourceGroup));
      for (RequestMappingContext each : sortedByMethods(requestMappings)) {

https://github.com/springfox/springfox/blob/ab5868471cdbaf54dac01af12933fe0437cf2b01/springfox-data-rest/src/main/java/springfox/documentation/spring/data/rest/SpecificationBuilder.java#L316


=======================================================


C:\Data\PortableApps\PortableApps\GoogleChromePortable\GoogleChromePortable.exe --proxy-server="socks5://192.168.56.2:1080"

C:\Data\PortableApps\PortableApps\GoogleChromePortable\GoogleChromePortable.exe --proxy-server="socks5://localhost:1080"
mkdir -p /Users/edaros/tmp/chrome
/Applications/Google\ Chrome.app/Contents/MacOS/Google\ Chrome "--proxy-server=socks5://localhost:1080" --user-data-dir=/Users/edaros/tmp/chrome


docker image prune -f

chrome://net-internals/#dns


https://www.microsoft.com/en-gb/download/details.aspx?id=17148
portqry -n 127.0.0.1 -e 53
portqry -n 172.25.3.2 -e 53

"%JAVA_HOME%\bin\keytool" -import -file ca.der -alias HamCert ^
-keystore "%JAVA_HOME%\lib\security\cacerts" -storepass changeit -noprompt



curl -isb -I --socks5-hostname localhost:1080 http://www.local.test/index.html
curl -isb -I --socks5-hostname localhost:1080 https://www.repubblica.it
curl -isb -I --socks5-hostname localhost:1080 https://www.libero.it
curl -isb -I --socks5-hostname localhost:1080 http://www.local.test/api/dns/list


openjdk11-demos
openjdk11-doc

cleanup
demo
jmods
include
man


SCENARIOS TO TEST

            options.addOption("b", false, "NOT buildDeploymentArtifacts");
            options.addOption("j", false, "NOT testAndGenerateJacoco");
            options.addOption("lh", false, "NOT testLocalHam");
            options.addOption("cs", false, "NOT testCalendarSample");
            options.addOption("csf", false, "NOT testCalendarSampleFull");
            options.addOption("lc", false, "Use composer local"); (not published)
            options.addOption("d", false, "NOT buildDockerImages");
            options.addOption("td", false, "NOT testDockerCalendarAndQuotesSamples");

RUN WITHout ALL globaltest.bat -b -j -lh -cs -csf -d -td (-lc)








