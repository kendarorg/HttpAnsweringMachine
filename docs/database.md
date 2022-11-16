## Database

The response/request logs are stored on the database configured through
the "db" section of the "global" main section.

### Configuration

If configured accordingly will use the internal database with Hibernate

* url: the jdbc url (default to "jdbc:h2:tcp://localhost/ham;MODE=MYSQL;")
* long: login!
* password: password!
* startInternalH2: If should start the internal db2 instead of using something else
* driver: the Jdbc driver
* hibernateDialect: the dialect for hibernate (default to "org.hibernate.dialect.MySQLDialect")

    {
        "id": "global",
        ...
        "db": {
            "url": "jdbc:h2:tcp://localhost/ham;MODE=MYSQL;",
            "login": "sa",
            "password": "sa",
            "startInternalH2" : true,
            "driver": "org.h2.Driver",
            "hibernateDialect":"org.hibernate.dialect.MySQLDialect"
        },

The directory in case of internal h2 database is the currentDir/data/ham.db

### Usage by plugins

The plugins using db should inject the "HibernateSessionFactory" bean

The Table definitions should be compatible with hibernate, implements the "DbTable"
interface and declared as @Component with at least the default constructor 
(or no constructors) e.g.

<pre>
    @Component
    @Entity
    @Table(name="REPLAYER_RECORDING")
    public class DbRecording implements DbTable {
        ...
</pre>

The session factory offers several methods

* Result, to return some data
  * transactionalResult: to modify and return data
  * queryResult: to return data
* The same but without result
  * transactional: to modify data
  * query: to query data

Here an example

    this.sessionFactory.transactional((em -> {
            var ld = new LoggingDataTable();
            ld.setContent("STARTING");
            em.persist(ld);
        }));


