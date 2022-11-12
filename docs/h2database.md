  {
    "id": "global",
    "system": true,
    "db": {
      "url": "jdbc:h2:tcp://localhost/ham;MODE=MYSQL",
      "login": "sa",
      "password": "sa",
      "startInternalH2" : true
    },

Everything should work if the external db is MYSQL compatible
The directory in case of h2 database is the currentDir/data/ham.db


HibernateSessionFactory sessionFactory

 this.sessionFactory.transactional((em -> {
            var ld = new LoggingDataTable();
            ld.setContent("STARTING");
            em.persist(ld);
        }));

        this.sessionFactory.query((em -> {
            List<LoggingDataTable> listEmployee = em.createQuery("SELECT e FROM LoggingDataTable e").getResultList();
            System.out.println("BUF");
        }));