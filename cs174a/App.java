package cs174a;                                             // THE BASE PACKAGE FOR YOUR APP MUST BE THIS ONE.  But you may add subpackages.

// You may have as many imports as you need.
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import javax.lang.model.util.ElementScanner6;

import oracle.jdbc.pool.OracleDataSource;
import oracle.jdbc.OracleConnection;
import java.lang.*;
import java.text.SimpleDateFormat;

/**
 * The most important class for your application.
 * DO NOT CHANGE ITS SIGNATURE.
 */
public class App implements Testable
{
	private OracleConnection _connection;                   // Example connection object to your DB.
	public static Scanner input = new Scanner(System.in);
	String customerTaxID;
	boolean isBankTeller = false;

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
						  "customer",
						  "linked_to",
						  "requests",
						  "system_date"
						)); 
		try( Statement statement = _connection.createStatement() )
		{
			for (int i = 0; i < table_names.size(); i++){
				System.out.println(table_names.get(i));
				statement.executeQuery("drop table " + table_names.get(i) + " cascade constraints");
			}
			statement.executeQuery("drop sequence seq_tran");
			statement.executeQuery("drop sequence seq_check");
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
		String create_tran_id = "CREATE SEQUENCE seq_tran "
							+"MINVALUE 1 "
							+"START WITH 1 "
							+"INCREMENT BY 1 "
							+"CACHE 10";
		String create_check_id = "CREATE SEQUENCE seq_check "
							+"MINVALUE 1 "
							+"START WITH 1 "
							+"INCREMENT BY 1 "
							+"CACHE 10";
		String create_customer = "CREATE TABLE CUSTOMER (" 
							+"tid CHAR(20),"
							+"Name CHAR(20)	NOT NULL,"
							+"Addr CHAR(50)	NOT NULL,"
							+"Pin CHAR(4)		DEFAULT 1717,"
							+"PRIMARY KEY(tid))";
		String create_account = "CREATE TABLE Account( aid char(20),"
							+"bb_name CHAR(20) NOT NULL,"
							+"balance DECIMAL(20,2) NOT NULL,"
							+"tid CHAR(20),"
							+"is_closed INTEGER NOT NULL,"
							+"interest_rate DECIMAL(5,2) NOT NULL,"
							+"type CHAR(20) NOT NULL,"
							+"PRIMARY KEY (aid),"
							+"FOREIGN KEY (tid) REFERENCES Customer(tid))";
		String create_own = "CREATE TABLE Own(" 
							+"aid CHAR(20),"
							+"tid CHAR(20),"
							+"PRIMARY KEY(Aid, tid),"
							+"FOREIGN KEY(Aid) REFERENCES Account(aid) ON DELETE CASCADE,"
							+"FOREIGN KEY(tid) REFERENCES Customer(tid))";				
		String create_transactions = "CREATE TABLE Transaction( trid INTEGER,"
                            +"t_date DATE NOT NULL,"
                            +"to_aid CHAR(20),"
                            +"from_aid CHAR(20),"
							+"CONSTRAINT chkIsNotNull CHECK (to_aid is not null or from_aid is not null),"
                            +"check_num INTEGER,"
                            +"amount DECIMAL(20,2),"
                            +"type CHAR(20),"
                            +"PRIMARY KEY (trid))";
		String create_requests = "CREATE TABLE Requests( tid CHAR(20),"
                       +"trid INTEGER,"
                       +"PRIMARY KEY (tid,trid),"
                       +"FOREIGN KEY (tid) REFERENCES Customer(tid),"
                       +"FOREIGN KEY (trid) REFERENCES Transaction(trid))";
		String create_linked_to = "CREATE TABLE linked_to("
						+"Poc_aid CHAR(20),"
						+"Aid CHAR(20),"
						+"PRIMARY KEY(poc_aid, aid),"
						+"FOREIGN KEY(poc_aid) REFERENCES account(aid),"
						+"FOREIGN KEY(aid) REFERENCES account(aid) ON DELETE CASCADE)";
		String create_system_date = "CREATE TABLE system_date("
						+"system_date DATE)";

		
		try( Statement statement = _connection.createStatement() )
		{
			statement.executeQuery(create_tran_id);
			statement.executeQuery(create_customer);
			statement.executeQuery(create_transactions);
			statement.executeQuery(create_account);
			statement.executeQuery(create_own);
			statement.executeQuery(create_requests);
			statement.executeQuery(create_linked_to);
			statement.executeQuery(create_system_date);
			statement.executeQuery(create_check_id);
			//Set date to current date
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			String date = formatter.format(calendar.getTime());
			String day = date.substring(0,2);
			String month = date.substring(3,5);
			String year = date.substring(6,10);
			
			statement.executeQuery("delete from system_date");
			String set_date = "insert into system_date(system_date) values (TO_DATE(\'" + year + "-" + month + "-" + day + "\',\'YYYY-MM-DD\'))";
			statement.executeQuery(set_date);
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
	
	@Override
	public String setDate( int year, int month, int day )
	{
		String syear = String.valueOf(year);
		String smonth = String.valueOf(month);
		String sday = String.valueOf(day);
		try( Statement statement = _connection.createStatement() )
		{
			statement.executeQuery( "update system_date set system_date = TO_DATE(\'" + year + "-" + month + "-" + day + "\',\'YYYY-MM-DD\')");
			return "0 " + year + "-" + month + "-" + day;
		}
		catch( final SQLException e )
		{
			System.err.println( e.getMessage() );
			return "1";
		}
	
	}
	@Override
	public String createPocketAccount( String id, String linkedId, double initialTopUp, String tin ){
		String query = "insert into account(aid,bb_name,balance,tid,is_closed,interest_rate,type) values(\'"+id+"\','SB',"+initialTopUp+",\'"+tin+"\',0,0,'POCKET')";
		try( Statement statement = _connection.createStatement() )
		{
			statement.executeQuery(query);
			createOwn(id,tin);
			createLinkedTo(id,linkedId);
			withdraw(linkedId,initialTopUp);
			return "0 " + id + " AccountType.POCKET " + initialTopUp + " " + tin;
		}
		catch( SQLException e )
		{
			System.err.println( e.getMessage() );
			return "1";
		}
	}
	private void createOwn(String id, String tin){
		String owns = "insert into own(aid,tid) values (\'"+id+"\',\'"+tin+"\') ";
		try( Statement statement = _connection.createStatement() )
		{
			statement.executeQuery(owns);
		}
		catch( SQLException e )
		{
			System.err.println( e.getMessage());
		}
	}
	private void createLinkedTo(String id, String linkedId){
		String links = "insert into linked_to values (\'"+id+"\',\'"+linkedId+"\')";
		try( Statement statement = _connection.createStatement() )
		{
			statement.executeQuery(links);
		}
		catch( SQLException e )
		{
			System.err.println( e.getMessage());
		}
	}
	private String getDate(){
		String date = "";
		try( Statement statement = _connection.createStatement() )
		{
			ResultSet rs = statement.executeQuery("select * from system_date");
			if (rs.next()){
				date = rs.getString(1);
			}
		}
		catch( SQLException e )
		{
			System.err.println( e.getMessage());
		}
		return date;
	}
	/**
	 * Create a entry in Transaction table
	 * @param to_acct Source of transaction, "" if NULL
	 * @param from_acct Destination of transaction, "" if NULL
	 * @param check Check number, -1 if NULL
	 * @param amount Amount of transaction.
	 * @param type Type of transaction.
	 */
	public void createTransaction(String to_acct,String from_acct,boolean check,double amount,String type){
		String query = "insert into transaction values(seq_tran.nextval,";
		String date = getDate();
		query += "TO_DATE(\'" + date.substring(0,10) + "\',\'YYYY-MM-DD\'),";
		query += (to_acct.length() > 0) ? "\'" + to_acct + "\',": "NULL,";
		query += (from_acct.length() >0) ? "\'" + from_acct + "\'," : "NULL,";
		query += (check) ? "seq_check.nextval," : "NULL,";
		query += amount + ",\'"+type+"\')";
		System.out.println(query);
		try( Statement statement = _connection.createStatement() )
		{
			statement.executeQuery(query);
		}
		catch( SQLException e )
		{
			System.err.println( e.getMessage());
		}
	}
	private void createModifies(int trid, String aid){
		String query = "insert into modifies values(" + trid + ",\'" + aid + "\')";
		try( Statement statement = _connection.createStatement() )
		{
			statement.executeQuery(query);
		}
		catch( SQLException e )
		{
			System.err.println( e.getMessage());
		}
	}
	@Override
	public String createCustomer( String accountId, String tin, String name, String address ){
		String create_c = "insert into customer(tid,name,addr,pin) values (\'"+tin+"\',\'"+name+"\',\'"+address+"\',1717)";
		try( Statement statement = _connection.createStatement() )
		{
			statement.executeQuery(create_c);
			createOwn(accountId,tin);
			return "0";
		}
		catch( SQLException e )
		{
			System.err.println(e.getMessage());
			return "1";
		}
	}
	@Override
	public String deposit( String accountId, double amount )
	{
		if(verifyAcct(accountId) && verifyAmt(amount))
		{
			double currBalance = 0.0;
			String result = showBalance(accountId);

			if(result.equals("1"))
			{
				System.out.println("Couldn't get balance, abort...");
				return "1";
			}
			else
			{
				String[] report = result.split(" ");

				currBalance = Double.parseDouble(report[1]);
			}

			double newBalance = currBalance + amount;

			try( Statement statement = _connection.createStatement() )
			{
				statement.executeQuery( "update account set balance = " + newBalance + " where aid = \'" + accountId+ "\'");
				return "0 " + String.format("%.2f", currBalance) + " " + String.format("%.2f", newBalance);
			}
			catch( final SQLException e )
			{
				System.err.println( e.getMessage() );
				return "1";
			}
		}
		else
		{
			return "1";
		}
	}

	private void depositInterface()
	{
		boolean cont = true;
		String acctId = "invalid";
		while(cont)
		{
			acctId = acctInterface();

			String[] types = {"INTEREST_CHECKING", "STUDENT_CHECKING", "SAVINGS"};
			if(!checkValidType(acctId, types))
			{
				System.out.println("You cannot perform a deposit on a account of this type, please choose another account");
			}
			else{
				cont = false;
			}
		}
		double depDoub = -1.0;
		cont = true;
		while(cont)
		{
			
			depDoub = amtInterface();
			if(depDoub != -1.0)
			{
				cont = false;
			}
		}

		String result = deposit( acctId, depDoub);
		String[] report = result.split(" ");

		if(report[0].equals("1"))
		{
			System.out.println("ERROR: Something went wrong with the deposit, aborting...");
		}
		else{
			System.out.println("Deposit Successful! Balance of Account " + acctId + " went from $" + report[1] + " to $" + report[2] +".");
		}
	}


	public String withdraw( String accountId, double amount )
	{	
			if(verifyAcct(accountId) && verifyAmt(amount))
			{
				double currBalance = 0.0;
				String result = showBalance(accountId);

				if(result.equals("1"))
				{
					System.out.println("Couldn't get balance, abort...");
					return "1";
				}
				else
				{
					String[] report = result.split(" ");

					currBalance = Double.parseDouble(report[1]);
				}

				double newBalance = currBalance - amount;

				if(newBalance < 0.0)
				{
					System.out.println("Insufficient funds, withdrawal aborted");
					return "1";
				}
				else if(newBalance < 0.01)
				{
					System.out.println("Balance of account after withdrawal is < 0.01, account will be closed after process finishes");
					closeAccount(accountId);
				}
				try( Statement statement = _connection.createStatement() )
				{
					statement.executeQuery( "update account set balance = " + newBalance + " where aid = \'" + accountId+ "\'");
					return "0 " + String.format("%.2f", currBalance) + " " + String.format("%.2f", newBalance);
				}
				catch( final SQLException e )
				{
					System.err.println( e.getMessage() );
					return "1";
				}
			}
			else
				return "1";
	}

	private void withdrawalInterface()
	{
		boolean cont = true;
		String acctId = "invalid";
		while(cont)
		{
			acctId = acctInterface();

			String[] types = {"INTEREST_CHECKING", "STUDENT_CHECKING", "SAVINGS"};
			if(!checkValidType(acctId, types))
			{
				System.out.println("You cannot perform a withdrawal on a account of this type, please choose another account");
			}
			else{
				cont = false;
			}
		}

		double withDoub = -1.0;
		cont = true;
		while(cont)
		{
			withDoub = amtInterface();
			if(withDoub != -1.0)
			{
				cont = false;
			}
		}

		String result = withdraw(acctId, withDoub);

		String[] report = result.split(" ");

		if(report[0].equals("1"))
		{
			System.out.println("ERROR: Something went wrong with the withdrawal, aborting...");
		}
		else{
			System.out.println("Withdrawal Successful! Balance of Account " + acctId + " went from $" + report[1] + " to $" + report[2] +".");
		}
	}


	@Override
	public String showBalance( String accountId )
	{
				
		try(Statement statement = _connection.createStatement())
		{
			try( ResultSet resultSet = statement.executeQuery( "select aid, balance from account" ) )
			{
				while( resultSet.next() ){
					if(resultSet.getString(1).trim().equals(accountId))
					{
						return "0 " + resultSet.getString(2);
					}
				}
			}
			return "1";
			
		}
		catch( final SQLException e )
		{
			System.err.println( e.getMessage() );
			return "1";
		}
	}
	@Override
	public String topUp( String accountId, double amount )
	{
		String linkedAid;
		try( Statement statement = _connection.createStatement() )
		{
			try( ResultSet resultSet = statement.executeQuery( "select aid from linked_to where poc_aid = " + accountId ) )
			{
				if(resultSet.next())
					linkedAid = resultSet.getString(1);
			}

			/*if(!take)
			{
				System.out.println("Aborting top-up...");
				return "1 " + String.format("%.2f", currBalance) + " " + String.format("%.2f", currBalance);
			}*/
		}
		catch( final SQLException e )
		{
			System.err.println( e.getMessage() );
		}
		return "stub";
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
		if (initialBalance < 1000){
			System.err.println("Initial deposit must be >= 1000");
			return "1";
		}
		String create_c = "insert into customer(tid,name,addr,pin) values (\'"+tin+"\',\'"+name+"\',\'"+address+"\',1717)";
		try( Statement statement = _connection.createStatement() )
		{
			ResultSet rs = statement.executeQuery("select * from customer where tid =\'" +tin+ "\'");
			if (!rs.next()){
				statement.executeQuery(create_c);
			}
		}
		catch( SQLException e )
		{
			System.out.println("Customer already here");
		}
		double interest_rate;
		String type;
		if (accountType == AccountType.INTEREST_CHECKING)
		{
			interest_rate = 3.0;
			type = "INTEREST_CHECKING";
		}
		else if (accountType == AccountType.STUDENT_CHECKING) interest_rate = 0.0;
		else interest_rate = 4.8;
		String query = "insert into account(aid,bb_name,balance,tid,is_closed,interest_rate,type) values(\'"+id+"\','SB',0,\'"+tin+"\',0,"+interest_rate+",\'"+accountType+"\')";
		try( Statement statement = _connection.createStatement() )
		{
			statement.executeQuery(query);
			createOwn(id,tin);
			deposit(id,initialBalance);
			return "0 " + id + " " + accountType + " " + initialBalance + " " + tin;
		}
		catch( SQLException e )
		{
			System.err.println( e.getMessage() );
			return "1";
		}
	}
	
	public String updateInterest(final AccountType accountType, double newRate)
	{
		try( Statement statement = _connection.createStatement() )
		{
			statement.executeQuery( "update account set interest_rate =" + newRate + "where type=\'"+accountType+"\'");
			System.out.println("update account set interest_rate =" + newRate + " where type=\'"+accountType+"\'");
			return "0";
		}
		catch( final SQLException e )
		{
			System.err.println( e.getMessage() );
			return "1";
		}
	}
	public static void goodbye()
	{
		System.out.println("Thank you for using our Banking Application! Goodbye!");
		System.exit(0);
	}

	//-----------------------------------ATM/App Interface------------------------------------------------

	public void startATMInterface()
	{
		checkPIN();
		
		boolean cont = true;
		while(cont)
		{
			transact();
			System.out.println("Would you like to make another transaction?\n\"1\"\tYes\n\"0\"\tNo");
			String repeat = input.next();
			
			boolean rep = repeat.equals(1);
			if(!rep)
			{
			   cont = false;
			}
		}

		goodbye();
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
				
				boolean rep = repeat.equals("1");
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
		String query = "select pin, tid from Customer";

		//inputPin = Integer.toString(inputPin.hashCode());
		try( Statement statement = _connection.createStatement() )
		{
			try( ResultSet resultSet = statement.executeQuery(query) )
			{
				while( resultSet.next() )
				{
					System.out.println(resultSet.getString(1));
					String temp = resultSet.getString(1);
					if (temp.trim().equals(inputPin.trim()))
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

	private void transact()
	{
		String[] actions = {"Deposit", "Top-Up", "Withdrawal", "Purchase", "Transfer", "Collect", "Wire", "Pay-Friend"};
		System.out.println("What would you like to do?:");

		for(int i = 0; i < actions.length; i++)
		{
			System.out.println(i + ":\t" + actions[i]);
		}

		String actIn = input.next().trim();

		if(actIn.equals("0"))
		{
			depositInterface();
		} else if(actIn.equals("1"))
		{
			topUpInterface();
		} else if(actIn.equals("2"))
		{
			withdrawalInterface();
		} else if(actIn.equals("3"))
		{
			purchaseHelper();
		} else if(actIn.equals("4"))
		{
			transferHelper();
		} else if(actIn.equals("5"))
		{
			collectHelper();
		} else if(actIn.equals("6"))
		{
			wireHelper();
		} else if(actIn.equals("7"))
		{
			payfriendHelper();
		} else {
			System.out.println("Invalid input, please try again!");
			transact();
			goodbye();
		}

		System.out.println("Your transaction is complete! Would you like to perform another transaction?\n\"1\"\tYes\n\"0\"\tNo");
		String repeat = input.next().trim();
				
		boolean rep = repeat.equals("1");

		if(rep)
		{
			transact();
		}
		else{
			goodbye();
		}
	}

	private String acctInterface()
	{
		String aid = getAcct();
		if(verifyAcct(aid))
			return aid;
		else
			return acctInterface();
	}

	private String getAcct()
	{
		boolean cont = true;

		System.out.println("What is the Account ID of the account you would like to transact in?");
		String aid = input.next();

		
		return aid.trim();
	}

	private boolean verifyAcct(String aid)
	{
		try( Statement statement = _connection.createStatement() )
		{
			try( ResultSet resultSet = statement.executeQuery( "select aid, tid, is_closed from Account" ) )
			{
				while( resultSet.next() ){
					if (acctExists(aid))
					{
						if (isBankTeller)
							return true;
						if(!ownsAcct(aid))
						{
							System.out.println("You do not have access to requested account, please choose another account");
							return false;
						}
						if(isClosed(aid))
						{
							System.out.println("Account is closed, transaction cannot be performed, please choose another account");
							return false;
						}
						else
						{
							return true;
						}
					}
					else{
						return false;
					}
				}
			}
		}
		catch( final SQLException e )
		{
			System.err.println( e.getMessage() );
			return false;
		}
		return false;
		
	}

	private boolean acctExists(String aid)
	{
		try( Statement statement = _connection.createStatement() )
		{
			try( ResultSet resultSet = statement.executeQuery( "select aid from Account where aid = " + aid ) )
			{
				if(resultSet.next())
					return true;
				else
					return false;
			}
			
		}
		catch( final SQLException e )
		{
			System.err.println( e.getMessage() );
			return false;
		}

	}

	private boolean ownsAcct(String aid)
	{
		try( Statement statement = _connection.createStatement() )
		{
			try( ResultSet resultSet = statement.executeQuery( "select tid from Account where aid = " + aid ) )
			{
				if(resultSet.next())
				{
					if(resultSet.getString(1).equals(customerTaxID))
						return true;
					else
						return false;
				}
				else{
					return false;
				}
			}
			
		}
		catch( final SQLException e )
		{
			System.err.println( e.getMessage() );
			return false;
		}
	}

	private boolean isClosed(String aid)
	{
		try( Statement statement = _connection.createStatement() )
		{
			try( ResultSet resultSet = statement.executeQuery( "select is_closed from Account where aid = " + aid ) )
			{
				if(resultSet.next())
				{
					if(resultSet.getInt(1) == 1)
						return true;
					else
						return false;
				}
				else{
					return false;
				}
			}
			
		}
		catch( final SQLException e )
			{
				System.err.println( e.getMessage() );
				return false;
			}
	}

	private double amtInterface()
	{
		System.out.println("What amount would you like to deposit?");
		String amt = input.next();
		double amtDoub = 0.0;
		try{
			amtDoub = Math.floor(Double.parseDouble(amt)*100) / 100.0;
		}
		catch(NumberFormatException e){
			System.out.println("Invalid amount!");
			return amtInterface();
		}

		if(!verifyAmt(amtDoub))
		{
			System.out.println("Invalid amount!");
			return amtInterface();
		}
		else{
			return amtDoub;
		}
	}

	private boolean verifyAmt(double amt)
	{
		if(amt < 0.0)
		{
			return false;
		}
		else{
			return true;
		}
	}
	

	private void topUpInterface()
	{
		boolean cont = true;
		String acctId = "invalid";
		while(cont)
		{
			acctId = acctInterface();

			String[] types = {"POCKET"};
			if(!checkValidType(acctId, types))
			{
				System.out.println("You cannot perform a top-up on a non-pocket account, please choose another account");
			}
			else{
				cont = false;
			}
		}
		double pockDoub = -1.0;
		cont = true;
		while(cont)
		{
			pockDoub = amtInterface();
			if(pockDoub != -1.0)
			{
				cont = false;
			}
		}
		if(chargePocketFlatFee(acctId))
		{
			pockDoub += 5.0;
		}

		String result = topUp( acctId, pockDoub);
		String[] report = result.split(" ");

		if(report[0].equals("1"))
		{
			System.out.println("ERROR: Something went wrong with the deposit, aborting...");
		}
		else{
			System.out.println("Top-Up Successful! Balance of Account " + acctId + " is $" + report[1] + " and balance of linked account is $" + report[2] +".");
		}
		
	}

	

	private void purchaseHelper()
	{
		
	}

	private void transferHelper()
	{

	}

	private void collectHelper()
	{

	}

	private void wireHelper()
	{

	}

	private void payfriendHelper()
	{

	}

	private void closeAccount(String acctId)
	{
		try(Statement statement = _connection.createStatement())
		{
			statement.executeQuery( "update account set is_closed = 1 where aid = \'" + acctId+ "\'");
			try( ResultSet resultSet = statement.executeQuery( "select type from account where aid = " + acctId ) )
			{
				if(resultSet.next())
				{
					String acctType = resultSet.getString(1).trim();
					if(!acctType.equals("POCKET"))
					{
						try( ResultSet resultSet2 = statement.executeQuery( "select poc_aid from linked_to where aid = " + acctId ) )
						{
							if(resultSet2.next())
								statement.executeQuery( "update account set is_closed = 1 where aid = \'" + resultSet2.getString(1).trim() + "\'");
						}
					}
				}
			}
		}
		catch( final SQLException e )
		{
			System.err.println( e.getMessage() );
		}
	}

	private boolean checkValidType(String acctID, String[] validType)
	{
		String acctType = "invalid";
		try( Statement statement = _connection.createStatement() )
		{
			try( ResultSet resultSet = statement.executeQuery( "select aid, type from Account where aid = " + acctID ) )
			{
				if(resultSet.next())
					acctType = resultSet.getString(2).trim();
			}
		}
		catch( SQLException e )
		{
			System.err.println( e.getMessage() );
			return false;
		}

		for(String i : validType)
		{
			if(i.equals(acctType))
			{
				return true;
			}
		}
		
		return false;
	}

	private boolean chargePocketFlatFee(String acctId)
	{
		String month = getDate().substring(5,7);
		String year = getDate().substring(0,4);

		try( Statement statement = _connection.createStatement() )
		{
			try( ResultSet resultSet = statement.executeQuery( "select t_date from transactions where aid = " + acctId ) )
			{
				while( resultSet.next() )
					if (resultSet.getString(1).substring(0,7).equals(year + "/"+ month))
					{
						System.out.println("Flat Fee charged already this month");
						return false;
					}		
			}
		}
		catch( final SQLException e )
		{
			System.err.println( e.getMessage() );
		}
		System.out.println("First transaction for this pocket account this month, $5 flat fee will be charged");
		return true;
		
	}
	//-------------------------------BANK TELLER INTERFACE-----------------------------------------
	public void startBankTellerInterface()
	{
		boolean cont = true;
		isBankTeller = true;
		while(cont)
		{
			String[] actions = {"Enter Check Transaction", 
								"Generate Monthly Statement",
								"List Closed Accounts", 
								"Generate Government Drug and Tax Evasion Report (DTER)", 
								"Customer Report", 
								"Add Interest", 
								"Create Account", 
								"Delete Closed Accounts and Customers",
								"Delete Transactions",
								"Exit"};
			System.out.println("What would you like to do?:");

			for(int i = 0; i < actions.length; i++)
			{
				System.out.println(i + ":\t" + actions[i]);
			}
			String actIn = input.next();

			switch(actIn){
				case "0":
					enterCheckTransaction();
					break;
				case "1":
					generateMonthlyStatement();
					break;
				case "2":
					System.out.println("Closed accounts:");
					String closed = listClosedAccounts();
					System.out.println(closed.substring(2,closed.length())+"\n");
					break;
				case "3":
					generateDTER();
					break;
				case "4":
					customerReport();
					break;
				case "5":
					addInterest();
					break;
				case "6":
					createAccount();
					break;
				case "7":
					deleteClosed();
					break;
				case "8":
					deleteTransactions();
					break;
				case "9":
					goodbye();
				default:
					System.out.println("Invalid input, please try again!");
					startBankTellerInterface();
			}
			System.out.println("Would you like to perform another action?\n1:\tYes\nAny Other Key:\tNo");
			if(!input.next().equals("1"))
				cont = false;
		}
		goodbye();
	}	

			
	private void enterCheckTransaction(){
		String acctId = acctInterface();
		System.out.println("How much is the check?");
		double amt = amtInterface();
		withdraw(acctId,amt);
		createTransaction("",acctId,true,amt,"WRITE-CHECK");
  	}

	private void generateMonthlyStatement(){
		if (!isLastDay()){
			System.out.println("Can only generate statement on last day of month.\n");
			return;
		}
		
		System.out.println("Enter customer tax ID:");
		String tid = input.next();
		String aid = "";
		String month = getDate().substring(5,7);
		String year = getDate().substring(0,4);
		Double total_sum = 0.0;
		ArrayList<String> accounts = new ArrayList<String>();
		ArrayList<Double> balances = new ArrayList<Double>();
		ArrayList<String> owners = new ArrayList<String>();
		try( Statement statement = _connection.createStatement() )
		{
			try( ResultSet resultSet = statement.executeQuery( "select aid,tid from own where tid=\'"+tid+"\'"))
			{
				if (!resultSet.isBeforeFirst()){
					System.out.println("Customer does not exist");
					return;
				}
				while( resultSet.next() )
				{
					System.out.println(resultSet.getString(1));
					accounts.add(resultSet.getString(1));
				}
			}		
		}
		catch( final SQLException e )
		{
			System.err.println( e.getMessage() );
		}
		for (int i = 0; i < accounts.size();i++){
			try( Statement statement = _connection.createStatement() )
			{
				try( ResultSet resultSet = statement.executeQuery( "select balance from account where aid=\'"+accounts.get(i)+"\'"))
				{
					while( resultSet.next() )
					{
						balances.add(resultSet.getDouble(1));
					}
				}		
			}
			catch( final SQLException e )
			{
				System.err.println( e.getMessage() );
			}
			aid = accounts.get(i);
			System.out.println( "Account ID: " + aid);
			System.out.println( "=====================================================");
			System.out.println("Transactions:");
			String net="";
			String temptype = "";
			Double currentBalance = balances.get(i);
			Double initialBalance = currentBalance;
			total_sum += currentBalance;
			try( Statement statement = _connection.createStatement() )
			{
				try( ResultSet resultSet = statement.executeQuery("select * from transaction "
																		+"where (to_aid=\'"+aid+"\' "
																		+"or from_aid =\'"+aid+"\') "))
				{
					if (!resultSet.isBeforeFirst())
						System.out.println("No transactions this month.");
					while( resultSet.next() )
					{
						System.out.println(resultSet.getString(1));
						if (resultSet.getString(2).substring(0,4).equals(year) && resultSet.getString(2).substring(5,7).equals(month)){
							// System.out.println(resultSet.getString(2).substring(0,4)+" "+resultSet.getString(2).substring(5,7));
							net = String.format("%.2f", resultSet.getDouble(6));
							if (aid.equals(resultSet.getString(4)) && resultSet.getString(7)!="ACCRUE-INTEREST"){
								net = "-" + net;
							}
							initialBalance -= Double.valueOf(net);
							System.out.println("Date: " + resultSet.getString(2).substring(0,10)+"\tType: " + resultSet.getString(7)+ "\tAmount: $"+net);
						}
					}
				}
			}
			catch( final SQLException e )
			{
				System.err.println( e.getMessage() );
			}
			System.out.println("Initial Balance: $"+initialBalance+"\tCurrent Balance: $"+currentBalance);
			System.out.println("Owners:");
			try( Statement statement = _connection.createStatement() )
			{
				try( ResultSet resultSet = statement.executeQuery( "select name,addr from customer where tid in (select tid from own where aid=\'"+aid+"\')"))
				{
					while( resultSet.next() )
					{
						System.out.println("Name: " + resultSet.getString(1) + "\t Address: " + resultSet.getString(2));
					}
				}		
			}
			catch( final SQLException e )
			{
				System.err.println( e.getMessage() );
			}
			System.out.println("\n");
		}
		if (total_sum > 100000){
			System.out.println("Warning: Insurance limit reached.\n");
		}
	}
	private void generateDTER(){
		if (!isLastDay()){
			System.out.println("Can only generate DTER on last day of month.\n");
			return;
		}
		System.out.println("DTER Report:");
		ArrayList<String> customers = new ArrayList<String>();
		try( Statement statement = _connection.createStatement() )
		{
			try( ResultSet resultSet = statement.executeQuery( "select tid from customer"))
			{
				while( resultSet.next() )
				{
					customers.add(resultSet.getString(1));
				}
			}		
		}
		catch( final SQLException e )
		{
			System.err.println( e.getMessage() );
		}
		for (int i = 0; i < customers.size(); i++){
			ArrayList<String> accounts = new ArrayList<String>();
			double sum = 0.0;
			try( Statement statement = _connection.createStatement() )
			{
				try( ResultSet resultSet = statement.executeQuery( "select aid from own where tid =\'" + customers.get(i) + "\'"))
				{
					while( resultSet.next() )
					{
						accounts.add(resultSet.getString(1));
					}
				}		
			}
			catch( final SQLException e )
			{
				System.err.println( e.getMessage() );
			}
			for (int j = 0; j < accounts.size();j++){
				try( Statement statement = _connection.createStatement() )
				{
					try( ResultSet resultSet = statement.executeQuery( "select amount from transaction where to_aid =\'" + accounts.get(j) + "\'"
																		+" and (type='DEPOSIT' or type='WIRE' or type='TRANSFER')"))
					{
						while( resultSet.next() )
						{
							sum += resultSet.getDouble(1);
						}
					}		
				}
				catch( final SQLException e )
				{
					System.err.println( e.getMessage() );
				}
			}
			if (sum >= 10000){
				System.out.println("Customer: " + customers.get(i) + "\tDeposits: $" + String.format("%.2f", sum));
			}
		}
	}
	private void customerReport(){
		System.out.println("Enter customer tax ID:");
		String tid = input.next();
		String aid = "";
		ArrayList<String> accounts = new ArrayList<String>();
		try( Statement statement = _connection.createStatement() )
		{
			try( ResultSet resultSet = statement.executeQuery( "select aid,tid from own where tid=\'"+tid+"\'"))
			{
				while( resultSet.next() )
				{
					accounts.add(resultSet.getString(1));
				}
			}		
		}
		catch( final SQLException e )
		{
			System.err.println( e.getMessage() );
		}
		System.out.println("Owned Accounts:");
		String status = "";
		for (int i = 0; i < accounts.size();i++){
			aid = accounts.get(i);
			try( Statement statement = _connection.createStatement() )
			{
				try( ResultSet resultSet = statement.executeQuery( "select is_closed from account where aid=\'"+aid+"\'"))
				{
					if( resultSet.next() )
					{
						status = (resultSet.getInt(1)>0) ? "Closed" : "Open";
						System.out.println("Account ID: "+ aid + "\tStatus: " + status);
					}
				}		
			}
			catch( final SQLException e )
			{
				System.err.println( e.getMessage() );
			}
		}
	}	
	//TODO
	private void addInterest(){
		if (!isLastDay()){
			System.out.println("Can only add interest on last day of month.\n");
			return;
		}
		int day = Integer.valueOf(getDate().substring(8,10));
		System.out.println(day);
	}
	private void createAccount(){
		System.out.println("Enter account type:\n"
							+"1:\tStudent Checking\n"
							+"2:\tInterest Checking\n"
							+"3:\tSavings\n"
							+"4:\tPocket\n"
							+"5:\tExit");
		String acctype = input.next();
		String id,tin;
		AccountType atype;
		System.out.println("Enter an account ID:");
		id = input.next();
		System.out.println("Enter primary owner's tax ID:");
		tin = input.next();
		System.out.println("Enter initial balance:");
		double amt = amtInterface();
		if (acctype.equals("1") || acctype.equals("2") || acctype.equals("3")){
			String name, address;
			System.out.println("Enter primary owner's name:");
			name = input.next();
			System.out.println("Enter primary owner's address:");
			address = input.next();
			if (acctype.equals("1"))
				atype = AccountType.STUDENT_CHECKING;
			else if (acctype.equals("2"))
				atype = AccountType.INTEREST_CHECKING;
			else 
				atype = AccountType.SAVINGS;
			if((createCheckingSavingsAccount(atype,id, amt, tin, name, address)).substring(0,1).equals("0"))
				System.out.println("Account created");
			else
				System.out.println("There was a problem creating the account.");
		}
		else if (acctype.equals("4")){
			String lid;
			atype = AccountType.POCKET;
			System.out.println("Enter linked checking/savings account ID:");
			lid = input.next();
			if(createPocketAccount(id, lid, amt, tin).substring(0,1).equals("0"))
				System.out.println("Account created");
			else
				System.out.println("There was a problem creating the account.");
			
		}
		else if (acctype.equals("5")){
			startBankTellerInterface();
		}
		else{
			System.out.println("Invalid input, please try again!");
			createAccount();
		}
	}
	private void deleteClosed(){
		if (!isLastDay()){
			System.out.println("Can only delete closed on last day of month.\n");
			return;
		}
		try( Statement statement = _connection.createStatement() )
		{
			statement.executeQuery("delete from account where is_closed=1");
			statement.executeQuery("delete from customer where tid not in (select tid from account)");
			System.out.println("Closed accounts and customers deleted.");
		}
		catch( final SQLException e )
		{
			System.err.println( e.getMessage() );
		}

	}
	private void deleteTransactions(){
		if (!isLastDay()){
			System.out.println("Can only delete transactions on last day of month.\n");
			return;
		}
		try( Statement statement = _connection.createStatement() )
		{
			statement.executeQuery("delete from transaction");
		}
		catch( final SQLException e )
		{
			System.err.println( e.getMessage() );
		}
	}
	private boolean isLastDay(){
		try( Statement statement = _connection.createStatement() )
		{
			try( ResultSet resultSet = statement.executeQuery( "select system_date from system_date "
															  +"where system_date = last_day(system_date)"))
			{
				if(resultSet.next() )
				{
					return true;
				}
				return false;
			}		
		}
		catch( final SQLException e )
		{
			System.err.println( e.getMessage() );
			return false;
		}
	}

}