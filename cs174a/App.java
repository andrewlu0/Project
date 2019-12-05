package cs174a;                                             // THE BASE PACKAGE FOR YOUR APP MUST BE THIS ONE.  But you may add subpackages.

// You may have as many imports as you need.
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import oracle.jdbc.pool.OracleDataSource;
import oracle.jdbc.OracleConnection;
import java.lang.*;
import java.io.*;

/**
 * The most important class for your application.
 * DO NOT CHANGE ITS SIGNATURE.
 */
public class App implements Testable
{
	private OracleConnection _connection;                   // Example connection object to your DB.
	public static Scanner input = new Scanner(System.in);
	String customerTaxID;

	/**
	 * Default constructor.
	 * DO NOT REMOVE.
	 */
	App()
	{
		// TODO: Any actions you need.
	}

	/**
	 * This is an example access operation to the DB.
	 */
	void exampleAccessToDB()
	{
		// Statement and ResultSet are AutoCloseable and closed automatically.
		try( Statement statement = _connection.createStatement() )
		{
			try( ResultSet resultSet = statement.executeQuery( "select owner, table_name from all_tables" ) )
			{
				while( resultSet.next() )
					if (resultSet.getString(1).equals("C##ALUO"))
						System.out.println( resultSet.getString( 1 ) + " " + resultSet.getString( 2 ) + " " );
			}
		}
		catch( final SQLException e )
		{
			System.err.println( e.getMessage() );
		}
	}

	////////////////////////////// Implement all of the methods given in the interface /////////////////////////////////
	// Check the Testable.java interface for the function signatures and descriptions.

	@Override
	public String initializeSystem()
	{
		// Some constants to connect to your DB.
		final String DB_URL = "jdbc:oracle:thin:@cs174a.cs.ucsb.edu:1521/orcl";
		final String DB_USER = "c##aluo";
		final String DB_PASSWORD = "5740170";

		// Initialize your system.  Probably setting up the DB connection.
		final Properties info = new Properties();
		info.put( OracleConnection.CONNECTION_PROPERTY_USER_NAME, DB_USER );
		info.put( OracleConnection.CONNECTION_PROPERTY_PASSWORD, DB_PASSWORD );
		info.put( OracleConnection.CONNECTION_PROPERTY_DEFAULT_ROW_PREFETCH, "20" );

		try
		{
			final OracleDataSource ods = new OracleDataSource();
			ods.setURL( DB_URL );
			ods.setConnectionProperties( info );
			_connection = (OracleConnection) ods.getConnection();

			// Get the JDBC driver name and version.
			final DatabaseMetaData dbmd = _connection.getMetaData();
			System.out.println( "Driver Name: " + dbmd.getDriverName() );
			System.out.println( "Driver Version: " + dbmd.getDriverVersion() );

			// Print some connection properties.
			System.out.println( "Default Row Prefetch Value is: " + _connection.getDefaultRowPrefetch() );
			System.out.println( "Database Username is: " + _connection.getUserName() );
			System.out.println();

			return "0";
		}
		catch( final SQLException e )
		{
			System.err.println( e.getMessage() );
			return "1";
		}
	}
	/**
	 * Drops all tables
	 */
	@Override
	public String dropTables()
	{
		ArrayList<String> table_names = new ArrayList<String>(
			Arrays.asList("transaction", 
                          "account", 
                          "own",
						  "ch_acct",
						  "int_ch_acct",
						  "st_ch_acct",
						  "sav_acct",
						  "poc_acct",
						  "customer",
						  "linked_to",
						  "requests",
						  "modifies",
						  "system_date")); 
		try( Statement statement = _connection.createStatement() )
		{
			for (int i = 0; i < table_names.size(); i++){
				System.out.println(table_names.get(i));
				statement.executeQuery("drop table " + table_names.get(i) + " cascade constraints");
			}
			return "0";
		}
		catch( SQLException e )
		{
			System.err.println( e.getMessage() );
			return "1";
		}
	}
	/**
	 * Creates all tables
	 */
	@Override
	public String createTables()
	{
		String create_customer = "CREATE TABLE CUSTOMER (" 
							+"tid CHAR(20),"
							+"Name CHAR(20)	NOT NULL,"
							+"Addr CHAR(50)	NOT NULL,"
							+"Pin CHAR(4)		DEFAULT 1717,"
							+"PRIMARY KEY(tid))";
		String create_account = "CREATE TABLE Account( aid INTEGER, bb_name CHAR(10) NOT NULL,"
							+ "balance DECIMAL NOT NULL,"
							+ "primary_owner CHAR(20),"
							+"is_closed INTEGER NOT NULL,"
							+"interest_rate DECIMAL NOT NULL,"
							+"flat_fee INTEGER,"
							+"PRIMARY KEY (aid),"
							+"FOREIGN KEY (primary_owner) REFERENCES Customer(tid))";
		String create_own = "CREATE TABLE Own(" 
							+"aid INTEGER,"
							+"tid CHAR(20),"
							+"PRIMARY KEY(Aid, tid),"
							+"FOREIGN KEY(Aid) REFERENCES Account(aid),"
							+"FOREIGN KEY(tid) REFERENCES Customer(tid))";				
		String create_transactions = "CREATE TABLE Transaction( trid INTEGER,"
                            +"t_date DATE NOT NULL,"
                            +"to_aid INTEGER,"
                            +"from_aid INTEGER,"
                            +"check_num INTEGER,"
                            +"amount DECIMAL,"
                            +"type CHAR(20),"
                            +"PRIMARY KEY (trid))";
		String create_requests = "CREATE TABLE Requests( tid CHAR(20),"
                       +"trid INTEGER,"
                       +"PRIMARY KEY (tid,trid),"
                       +"FOREIGN KEY (tid) REFERENCES Customer(tid),"
                       +"FOREIGN KEY (trid) REFERENCES Transaction(trid))";
		String create_modifies = "CREATE TABLE Modifies( trid INTEGER,"
                       +"aid INTEGER NOT NULL,"
                       +"PRIMARY KEY (trid, aid),"
                       +"FOREIGN KEY (trid) REFERENCES Transaction(trid),"
                       +"FOREIGN KEY (aid) REFERENCES Account(aid))";
		String create_linked_to = "CREATE TABLE linked_to("
						+"Poc_aid INTEGER,"
						+"Aid INTEGER,"
						+"PRIMARY KEY(poc_aid, aid),"
						+"FOREIGN KEY(poc_aid) REFERENCES account(aid),"
						+"FOREIGN KEY(aid) REFERENCES account(aid))";
		String create_system_date = "CREATE TABLE system_date("
						+"year INTEGER NOT NULL,"
						+"month INTEGER NOT NULL,"
						+"day INTEGER NOT NULL,"
						+"PRIMARY KEY (year,month,day))";
		
		try( Statement statement = _connection.createStatement() )
		{
			statement.executeQuery(create_customer);
			statement.executeQuery(create_transactions);
			statement.executeQuery(create_account);
			statement.executeQuery(create_own);
			statement.executeQuery(create_requests);
			statement.executeQuery(create_modifies);
			statement.executeQuery(create_linked_to);
			statement.executeQuery(create_system_date);
			return "0";
		}
		catch( SQLException e )
		{
			System.err.println( e.getMessage() );
			return "1";
		}
	}
	/**
	 * Example of one of the testable functions.
	 */
	@Override
	public String listClosedAccounts()
	{
		String result = "0";
		try( Statement statement = _connection.createStatement() )
		{
			try( ResultSet resultSet = statement.executeQuery( "select aid, is_closed from account where is_closed=1" ) )
			{
				while( resultSet.next() )
				{
					result = result + " " + String.valueOf(resultSet.getInt(1));
				}		
			}
			return result;
		}
		catch( final SQLException e )
		{
			System.err.println( e.getMessage() );
			return "1";
		}
	}
	/**
	 * Sets date
	 */
	@Override
	public String setDate( int year, int month, int day )
	{
		try( Statement statement = _connection.createStatement() )
		{
			try( ResultSet resultSet = statement.executeQuery( "select aid, is_closed from account where is_closed=1" ) )
			{
				while( resultSet.next() )
				{
					result = result + " " + String.valueOf(resultSet.getInt(1));
				}		
			}
			return result;
		}
		catch( final SQLException e )
		{
			System.err.println( e.getMessage() );
			return "1";
		}
	}
	@Override
	public String createPocketAccount( String id, String linkedId, double initialTopUp, String tin ){
		return "0 stub";
	}
	@Override
	public String createCustomer( String accountId, String tin, String name, String address ){
		return "0 stub";
	}
	@Override
	public String deposit( String accountId, double amount )
	{
		return "0stub";
	}
	@Override
	public String showBalance( String accountId )
	{
		return "0stub";
	}
	@Override
	public String topUp( String accountId, double amount )
	{
		return "0 stub";
	}
	@Override
	public String payFriend( String from, String to, double amount )
	{
		return "0stub";
	}
	/**
	 * Another example.
	 */
	@Override
	public String createCheckingSavingsAccount( final AccountType accountType, final String id, final double initialBalance, final String tin, final String name, final String address )
	{
		return "0 " + id + " " + accountType + " " + initialBalance + " " + tin;
	}

	public static void goodbye()
	{
		System.out.println("Thank you for using our Banking Application! Goodbye!");
		System.exit(0);
	}

	//-----------------------------------------------------------------------------------

		public void startATMInterface()
		{
            checkPIN();
            






        }
        
        public void checkPIN()
        {
            boolean cont = true;
                System.out.println("Please insert your PIN:");
                String inputPin = input.next();
                
                if(verifyPIN(inputPin))
                {
                    cont = false;
                }
                else{
                    System.out.println("The PIN you inputed was invalid. Would you like to try again?\n\"1\"\tYes\n\"0\"\tNo");
                    String repeat = input.next();
					
					boolean rep = repeat.equals(1);
                    if(rep)
                    {
                       checkPIN();
					}
					else
					{
						goodbye();
					}
                }
        }


		private boolean verifyPIN(String inputPin)
		{
			String query = "select pin, tid, from Customer";

			inputPin = Integer.toString(inputPin.hashCode());
			try( Statement statement = _connection.createStatement() )
			{
				try( ResultSet resultSet = statement.executeQuery(query) )
				{
					while( resultSet.next() )
					{
						if (resultSet.getString(1).equals(inputPin))
						{
							System.out.println("PIN VERIFIED");
							customerTaxID = resultSet.getString(2);
							return true;
						}
					}
					
				}
			}
			catch( final SQLException e )
			{
				System.err.println( e.getMessage() );
			}
			return false;
		}

}
