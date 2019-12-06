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
						  "modifies",
						  "system_date"
						)); 
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
		String create_tran_id = "CREATE SEQUENCE seq_tran"
							+"MINVALUE 1"
							+"START WITH 1"
							+"INCREMENT BY 1"
							+"CACHE 10";
		String create_check_id = "CREATE SEQUENCE seq_check"
							+"MINVALUE 1"
							+"START WITH 1"
							+"INCREMENT BY 1"
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
							+"FOREIGN KEY(Aid) REFERENCES Account(aid),"
							+"FOREIGN KEY(tid) REFERENCES Customer(tid))";				
		String create_transactions = "CREATE TABLE Transaction( trid INTEGER,"
                            +"t_date DATE NOT NULL,"
                            +"to_aid CHAR(20),"
                            +"from_aid CHAR(20),"
                            +"check_num INTEGER,"
                            +"amount DECIMAL(20,2),"
                            +"type CHAR(20),"
                            +"PRIMARY KEY (trid))";
		String create_requests = "CREATE TABLE Requests( tid CHAR(20),"
                       +"trid INTEGER,"
                       +"PRIMARY KEY (tid,trid),"
                       +"FOREIGN KEY (tid) REFERENCES Customer(tid),"
                       +"FOREIGN KEY (trid) REFERENCES Transaction(trid))";
		String create_modifies = "CREATE TABLE Modifies( trid INTEGER,"
                       +"aid char(20) NOT NULL,"
                       +"PRIMARY KEY (trid, aid),"
                       +"FOREIGN KEY (trid) REFERENCES Transaction(trid),"
                       +"FOREIGN KEY (aid) REFERENCES Account(aid))";
		String create_linked_to = "CREATE TABLE linked_to("
						+"Poc_aid CHAR(20),"
						+"Aid CHAR(20),"
						+"PRIMARY KEY(poc_aid, aid),"
						+"FOREIGN KEY(poc_aid) REFERENCES account(aid),"
						+"FOREIGN KEY(aid) REFERENCES account(aid))";
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
			statement.executeQuery(create_modifies);
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
		String linked = "insert into linked_to values (\'"+id+"\',\'"+linkedId+"\')'";
		try( Statement statement = _connection.createStatement() )
		{
			statement.executeQuery(query);
			statement.executeQuery(linked);
			createOwn(id,tin);
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
	/**
	 * Create a entry in Transaction table
	 * @param to_acct Source of transaction, "" if NULL
	 * @param from_acct Destination of transaction, "" if NULL
	 * @param check Check number, -1 if NULL
	 * @param ammount Ammount of transaction.
	 * @param type Type of transaction.
	 */
	public void createTransaction(String to_acct,String from_acct,boolean check,double ammount,String type){
		String query = "insert into transaction values(seq_tran.nextval,";
		String date = "";
		try( Statement statement = _connection.createStatement() )
		{
			ResultSet rs = statement.executeQuery("select * from system_date");
			if (rs.next()){
				date = rs.getString(1);
				System.out.println(date);
			}
		}
		catch( SQLException e )
		{
			System.err.println( e.getMessage());
		}
		query += "TO_DATE(\'" + date.substring(0,10) + "\',\'YYYY-MM-DD\'),";
		query += (to_acct.length() > 0) ? "\'" + to_acct + "\',": "NULL,";
		query += (from_acct.length() >0) ? "\'" + from_acct + "\'," : "NULL,";
		query += (check) ? "seq_check.nextval," : "NULL,";
		query += ammount + ",\'"+type+"\')";
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
		double currBalance = 0.0;
		try( Statement statement = _connection.createStatement() )
		{
			try( ResultSet resultSet = statement.executeQuery( "select balance from account where aid = \'" + accountId + "\'" ) )
			{
				if(resultSet.next())
					currBalance = resultSet.getDouble(1);
			}

			double newBalance = currBalance + amount;
			statement.executeQuery( "update account set balance = " + newBalance + " where aid = \'" + accountId+ "\'");
			return "0 " + String.format("%.2f", currBalance) + " " + String.format("%.2f", newBalance);
		}
		catch( final SQLException e )
		{
			System.err.println( e.getMessage() );
			return "1 " + String.format("%.2f", currBalance) + " " + String.format("%.2f", currBalance);
		}
	}
	public String withdraw( String accountId, double amount )
	{
		double currBalance = 0.0;
		try( Statement statement = _connection.createStatement() )
		{
			try( ResultSet resultSet = statement.executeQuery( "select balance from account where aid = \'" + accountId + "\'" ) )
			{
				if(resultSet.next())
					currBalance = resultSet.getDouble(1);
			}

			double newBalance = currBalance - amount;

			if(newBalance < 0.0)
			{
				System.out.println("Insufficient funds, withdrawal aborted");
				return "1 " + String.format("%.2f", currBalance) + " " + String.format("%.2f", currBalance);
			}
			else if(newBalance < 0.01)
			{
				System.out.println("Balance of account after withdrawal is < 0.01, account will be closed after process finishes");
				closeAccount(accountId);
			}

			statement.executeQuery( "update account set balance = " + newBalance + " where aid = \'" + accountId+ "\'");
			return "0 " + String.format("%.2f", currBalance) + " " + String.format("%.2f", newBalance);
		}
		catch( final SQLException e )
		{
			System.err.println( e.getMessage() );
			return "1 " + String.format("%.2f", currBalance) + " " + String.format("%.2f", currBalance);
		}
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

	//-----------------------------------------------------------------------------------

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
		String query = "select pin, tid from Customer";

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

	private void transact()
	{
		String[] actions = {"Deposit", "Top-Up", "Withdrawal", "Purchase", "Transfer", "Collect", "Wire", "Pay-Friend"};
		System.out.println("What would you like to do?:");

		for(int i = 0; i < actions.length; i++)
		{
			System.out.println(i + ":\t" + actions[i]);
		}

		String actIn = input.next();

		if(actIn.equals(actions[0]))
		{
			depositHelper();
		} else if(actIn.equals(actions[1]))
		{
			topUpHelper();
		} else if(actIn.equals(actions[2]))
		{
			withdrawalHelper();
		} else if(actIn.equals(actions[3]))
		{
			purchaseHelper();
		} else if(actIn.equals(actions[4]))
		{
			transferHelper();
		} else if(actIn.equals(actions[5]))
		{
			collectHelper();
		} else if(actIn.equals(actions[6]))
		{
			wireHelper();
		} else if(actIn.equals(actions[7]))
		{
			payfriendHelper();
		} else {
			System.out.println("Invalid input, please try again!");
			transact();
			goodbye();
		}

		System.out.println("Your transaction is complete, would you like to perform another transaction?\n\"1\"\tYes\n\"0\"\tNo");
		String repeat = input.next();
				
		boolean rep = repeat.equals(1);

		if(rep)
		{
			transact();
		}
		else{
			goodbye();
		}
	}

	private String getAcct()
	{
		boolean cont = true;

		System.out.println("What is the Account ID of the account you would like to deposit in?");
		String acctId = input.next();

		boolean valid = false;
		try( Statement statement = _connection.createStatement() )
		{
			try( ResultSet resultSet = statement.executeQuery( "select aid, tid, is_closed from Account" ) )
			{
				while( resultSet.next() )
					if (resultSet.getString(1).equals(acctId))
					{
						System.out.println( "Account ID is Valid");
						if(resultSet.getString(2).equals(customerTaxID))
						{
							System.out.println("Account Verified to belong to you");
							valid = true;
						}
						else{
							System.out.println("You do not have access to requested account, please choose another account");
							return getAcct();
						}
						
						if(resultSet.getInt(3) == 1)
						{
							System.out.println("Account is close, transaction cannot be performed, please choose another account");
							return getAcct();
						}
						else
						{
							return acctId;
						}
						
					}
			}
			System.out.println("Account ID is Invalid, please choose another account");
		}
		catch( final SQLException e )
		{
			System.err.println( e.getMessage() );
		}
		return getAcct();
	}

	private double verifyAmount()
	{
		String amt = input.next();
		try{
			double amtDoub = Math.floor(Double.parseDouble(amt)) / 100.0;
			if(amtDoub < 0.0)
			{
				System.out.println("Invalid amount!");
				return -1.0;
			}
			return amtDoub;
		}
		catch(NumberFormatException e){
			System.out.println("Invalid amount!");
			return -1.0;
		}
	}

	private void depositHelper()
	{
		boolean cont = true;
		String acctId = "invalid";
		while(cont)
		{
			acctId = getAcct();

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
			System.out.println("What amount would you like to deposit?");
			depDoub = verifyAmount();
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

	private void topUpHelper()
	{
		
	}

	private void withdrawalHelper()
	{
		boolean cont = true;
		String acctId = "invalid";
		while(cont)
		{
			acctId = getAcct();

			String[] types = {"INTEREST_CHECKING", "STUDENT_CHECKING", "SAVINGS"};
			if(!checkValidType(acctId, types))
			{
				System.out.println("You cannot perform a deposit on a account of this type, please choose another account");
			}
			else{
				cont = false;
			}
		}

		double withDoub = -1.0;
		cont = true;
		while(cont)
		{
			System.out.println("What amount would you like to withdraw?");
			withDoub = verifyAmount();
			if(withDoub != -1.0)
			{
				cont = false;
			}
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
	
	//-------------------------------BANK TELLER INTERFACE-----------------------------------------
	public void startBankTellerInterface()
	{
		boolean cont = true;
		while(cont)
		{
			String[] actions = {"Enter Check Transaction", 
								"Generate Monthly Statement",
								"List Closed Accounts", 
								"Generate Government Drug and Tax Evasion Report (DTER)", 
								"Customer Report", 
								"Add Interest:", 
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
					enterCheckTransaction();
					break;
				case "2":
					enterCheckTransaction();
					break;
				case "3":
					enterCheckTransaction();
					break;
				case "4":
					enterCheckTransaction();
					break;
				case "5":
					enterCheckTransaction();
					break;
				case "6":
					enterCheckTransaction();
					break;
				case "7":
					enterCheckTransaction();
					break;
				case "8":
					enterCheckTransaction();
					break;
				case "9":
					goodbye();
				default:
					System.out.println("Invalid input, please try again!");
					startBankTellerInterface();
		}
		System.out.println("Would you like to perform another action?\n1:\tYes\nAny Other Key:\tNo");
      	if(!input.next().equals("1"))
		{
			cont = false;
		}
		}
		goodbye();
	}


	private boolean checkValidType(String acctID, String[] validType)
	{
		String acctType = "invalid";
		try( Statement statement = _connection.createStatement() )
		{
			try( ResultSet resultSet = statement.executeQuery( "select aid, type from Account where aid = " + acctID ) )
			{
				if(resultSet.next())
					acctType = resultSet.getString(2);
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
			else{
				return false;
			
		}
	}
	
	private void enterCheckTransaction(){
		String acctId = getAcct();
		System.out.println("How much is the check?");
		double amt = verifyAmount();
		withdraw(acctId,amt);
		//createTransaction("",acctId,);
		//createModifies();
  	}
		

private void closeAccount(String acctId)
	{
		try(Statement statement = _connection.createStatement())
		{
			statement.executeQuery( "update account set is_closed = 1 where aid = \'" + acctId+ "\'");
		}
		catch( final SQLException e )
		{
			System.err.println( e.getMessage() );
		}
	}

}