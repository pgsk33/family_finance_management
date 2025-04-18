{
    Connection connection;

    {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://root@localhost:3306/schuldenapp");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        FamilyFinanceManagement ffm = new FamilyFinanceManagement();
        ffm.mainLoop();
    }

    private void mainLoop() {
        try {
            while (true) {
                System.out.println("Choose a function");
                System.out.println("(0) exit program (1) output balance (2) add user (3) output user (4) reset database (5) add entry");
                switch (sc.nextInt()) {
                    case 0:
                        System.exit(0);
                    case 1:
                        outputBalance();
                        break;
                    case 2:
                        addUser();
                        break;
                    case 3:
                        outputAllUser();
                        break;
                    case 4:
                        resetDatabase();
                        break;
                    case 5:
                        addEntry();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void outputBalance() throws SQLException {
        double balance = 0;
        System.out.println("Zwischen welchen 2 Nutzern soll der Schuldenstand ermittelt werden?");
        int userID1 = askForAndReturnUserID();
        int userID2 = askForAndReturnUserID();

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT e.IDeintrag, e.datum, e.betrag, e.stand, e.kommentar, n1.nutzername AS vorstrecker_name, n2.nutzername AS bezahler_name, e.vorstreckerID, e.bezahlerID " +
                "FROM einträge e " +
                "LEFT JOIN nutzer n1 ON e.vorstreckerID = n1.IDnutzer " +
                "LEFT JOIN nutzer n2 ON e.bezahlerID = n2.IDnutzer " +
                "WHERE (e.vorstreckerID = " + userID1 + " AND e.bezahlerID = " + userID2 + ") OR (e.vorstreckerID = " + userID2 + " AND e.bezahlerID = " + userID1 + ") OR e.vorstreckerID = " + userID1 + " OR e.bezahlerID = " + userID1 + " OR e.vorstreckerID = " + userID2 + " OR e.bezahlerID = " + userID2 + " " +
                "ORDER BY e.datum;");
        while (resultSet.next()) {
            System.out.print(resultSet.getDate("datum"));
            System.out.print("  ");
            System.out.print(resultSet.getInt("betrag"));
            System.out.print("  ");

            if (userID1 == resultSet.getInt("vorstreckerID")) {
                balance += resultSet.getInt("betrag");
            } else {
                balance -= resultSet.getInt("betrag");
            }

            System.out.print(balance + "   ");
            System.out.print(resultSet.getString("kommentar"));
            System.out.print("  ");
            System.out.print(getUsernameWithID(resultSet.getInt("vorstreckerID"), connection));
            System.out.print("  ");
            System.out.print(getUsernameWithID(resultSet.getInt("bezahlerID"),connection));
            System.out.println("  ");
        }
        statement.close();
        resultSet.close();

        System.out.println(getUsernameWithID(userID1, connection) +" bekommt von " + getUsernameWithID(userID2, connection) +" " + balance + " Euro.");
    }

    private static String getUsernameWithID(int searchID, Connection connection) throws SQLException {
        Statement myps = connection.createStatement();
        ResultSet myrs = myps.executeQuery("Select nutzername from nutzer where IDnutzer = " + searchID + ";");
        while (myrs.next()) {
            return myrs.getString("nutzername");
        }
        return "error";
    }

    private int askForAndReturnUserID() throws SQLException {
        outputAllUser();
        System.out.println("Geben sie userID an: ");
        return sc.nextInt();
    }

    public void outputAllUser() throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("Select IDnutzer, nutzername from nutzer");
        while (resultSet.next()) {
            System.out.print(resultSet.getInt("IDnutzer"));
            System.out.print("  ");
            System.out.print(resultSet.getString("nutzername"));
            System.out.println("");
        }
        resultSet.close();
        statement.close();
    }

    public void addUser() throws SQLException {
        System.out.print("Geben sie einen Nutzernamen an: ");
        String username = sc.next();
        System.out.print("Geben sie ein Passwort an: ");
        String password = sc.next();
        PreparedStatement addUserStatement = connection.prepareStatement("insert into nutzer values\n" +
                "(null, '" + username + "', '" + password + "');");
        addUserStatement.execute();
        addUserStatement.close();
    }

    public void resetDatabase() throws SQLException {
        Statement resetDatabaseStatement = connection.createStatement();
        resetDatabaseStatement.executeUpdate("drop database if exists schuldenapp;");
        resetDatabaseStatement.executeUpdate("create database schuldenapp;");
        resetDatabaseStatement.executeUpdate("use schuldenapp;");
        resetDatabaseStatement.executeUpdate("create table nutzer(\n" +
                "IDnutzer int Not Null auto_increment,\n" +
                "nutzername varchar(10) not null default '',\n" +
                "passwort varchar(20) not null default '',\n" +
                "primary key (IDnutzer));");
        resetDatabaseStatement.executeUpdate("create table einträge(\n" +
                "IDeintrag int(2) NOT NULL auto_increment,\n" +
                "datum datetime,\n" +
                "betrag decimal NOT NULL,\n" +
                "stand decimal,\n" +
                "kommentar varchar(255) NOT NULL default '',\n" +
                "vorstreckerID int(2),\n" +
                "bezahlerID int(2),\n" +
                "primary key (IDeintrag),\n" +
                "foreign key (vorstreckerID) REFERENCES nutzer(IDnutzer),\n" +
                "foreign key (bezahlerID) REFERENCES nutzer(IDnutzer)\n" +
                ");");
        resetDatabaseStatement.close();
    }

    public void addEntry() throws SQLException {
        String date = "1111-11-11";
        double amount;
        double balance = 0;
        String comment;
        int lenderID;
        int debtorID;

        System.out.println("Welche Datumsoption? (1) Heute  (2) Eigenes Datum?");
        switch (sc.nextInt()) {
            case 1:
                date = String.valueOf(LocalDate.now());
                break;
            case 2:
                System.out.println("Datum eingeben (YYYY-MM-DD)");
                date = sc.next();
                break;
        }
        System.out.println("Betrag:");
        amount = sc.nextDouble();
        System.out.println("Kommentar (MAX 255 CHARACTERS)");
        comment = sc.next();
        outputAllUser();
        System.out.println("Vorstreckender: (ID)");
        lenderID = sc.nextInt();
        System.out.println("Bezahlender: (ID)");
        debtorID = sc.nextInt();
        PreparedStatement ps = connection.prepareStatement("insert into einträge values (null, '" + date + "' , " + amount + ", " + balance + ", '" + comment + "', " + lenderID + ", " + debtorID + ");");
        ps.execute();
    }
}

