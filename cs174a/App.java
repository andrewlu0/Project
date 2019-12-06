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
import java.text.ParseException;
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
						  "monthly_tasks",
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
		String create_monthly_tasks = "create table monthly_tasks("
									+"month char(20),"
									+"year char(20),"
									+"add_interest integer,"
									+"delete_closed integer,"
									+"delete_trans integer,"
									+"primary key (month,year))";
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
			statement.executeQuery(create_monthly_tasks);
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
			String set_tasks = "insert into monthly_tasks values(\'" + month + "\',\'" + year + "\',0,0,0)";
			statement.executeQuery(set_tasks);
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
		String currmonth = "";
		String curryear = "";
		try( Statement statement = _connection.createStatement() )
		{
			ResultSet currdate = statement.executeQuery("select system_date from system_date");
			if (currdate.next()){
				currmonth = currdate.getString(1).substring(5,7);
				curryear = currdate.getString(1).substring(0,4);
				if (Integer.valueOf(currmonth) < 10)
					currmonth = currmonth.substring(1,2);
			}
			if (year > Integer.valueOf(curryear) || (year == Integer.valueOf(curryear) && month > Integer.valueOf(currmonth))){
				// System.out.println("going forward");
				// System.out.println("select add_interest, delete_closed, delete_trans from monthly_tasks where month=\'"+currmonth+
				// 										"\' and year=\'" + curryear +"\'");
				ResultSet check = statement.executeQuery("select add_interest, delete_closed, delete_trans from monthly_tasks where month=\'"+currmonth+
														"\' and year=\'" + curryear +"\'");
				if (check.next()){
					// System.out.println(check.getInt(1) + " " + check.getInt(2) + " " + check.getInt(3));
					if ((check.getInt(1)+check.getInt(2)+check.getInt(3))!=3){
						System.out.println("Cannot change date. Monthly tasks not complete. Setting date to final date of month");
						ResultSet lastday = statement.executeQuery("select last_day(system_date) from system_date");
						if (lastday.next())
							day = Integer.valueOf(lastday.getString(1).substring(8,10));
							setDate(Integer.valueOf(curryear),Integer.valueOf(currmonth),day);
							return "1";
					}
				}
			}
			statement.executeQuery( "update system_date set system_date = TO_DATE(\'" + year + "-" + month + "-" + day + "\',\'YYYY-MM-DD\')");
			String add_tasks = "insert into monthly_tasks values (\'" + smonth + "\',\'" + year + "\',0,0,0)";
			ResultSet rs = statement.executeQuery("select * from monthly_tasks where  month=\'" +smonth+ "\' and year=\'" + syear + "\'");
			if (!rs.next()){
				statement.executeQuery(add_tasks);
			}
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
			topUp(id,initialTopUp);
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
		String address_parse = address.replace("'","\'");
		String create_c = "insert into customer(tid,name,addr,pin) values (\'"+tin+"\',\'"+name+"\',\'"+address_parse+"\',1717)";
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

			String[] types = {"INTEREST_CHECKING", "STUDENT_CHECKING", "SAVINGS"};
			if(!checkValidType(accountId, types))
			{
				System.out.println("You cannot perform a deposit on a account of this type");
				return "1";
			}
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
				createTransaction(accountId,"",false,amount,"DEPOSIT");
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

			acctId = acctInterface();

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
				String[] types = {"INTEREST_CHECKING", "STUDENT_CHECKING", "SAVINGS"};
				if(!checkValidType(accountId, types))
				{
					System.out.println("You cannot perform a withdrawal on a account of this type");
					return "1";
				}
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
					createTransaction("",accountId,false,amount,"WITHDRAW");
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

			acctId = acctInterface();


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

	private String purchase(String aid, double amount)
	{
		if(verifyAcct(aid) && verifyAmt(amount))
			{
				String[] types = {"POCKET"};
				if(!checkValidType(aid, types))
				{
					System.out.println("You cannot perform a purchase on a account of this type");
					return "1";
				}

				if(chargePocketFlatFee(aid))
				{
					System.out.println(aid);
					createTransaction("",aid,false,amount,"PURCHASE");
					return takeFrom(aid, amount+5.0);
				}
				else
				{
					createTransaction("",aid,false,amount,"PURCHASE");
					return takeFrom(aid, amount);
				}
			}
			else
				return "1";
	}

	private void purchaseInterface(){
		boolean cont = true;
		String acctId = "invalid";

			acctId = acctInterface();


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

		String result = purchase(acctId, withDoub);

		String[] report = result.split(" ");

		if(report[0].equals("1"))
		{
			System.out.println("ERROR: Something went wrong with the purchase, aborting...");
		}
		else{
			System.out.println("Purchase Successful! Balance of Account " + acctId + " went from $" + report[1] + " to $" + report[2] +".");
		}
	}

	public String transfer( String from, String to, double amount )
	{
		if(verifyAcct(from) && verifyAcct(to) && verifyAmt(amount))
		{
			if(amount > 2000.0)
			{
				System.out.println("You cannot transfer more than $2000.00");
				return "1";
			}
			String[] types = {"INTEREST_CHECKING", "STUDENT_CHECKING", "SAVINGS"};
			if(!checkValidType(from, types))
			{
				System.out.println("You cannot perform a transfer on a non-pocket account, please choose another account");
				return "1";
			}
			if(!checkValidType(to, types))
			{
				System.out.println("You cannot perform a transfer on a non-pocket account, please choose another account");
				return "1";
			}


			try( Statement statement = _connection.createStatement() )
			{

				String result;

				result = takeFrom(from, amount);
				

				String[] report = result.split(" ");

				if(report[0].equals("1"))
				{
					return "1";
				}

				String newFromBalance = report[2];

				result = giveTo(to, amount);

				report = result.split(" ");
				createTransaction(to,from,false,amount,"TRANSFER");
				if(report[0].equals("1"))
				{
					return "1";
				}

				else
					return "0 " + newFromBalance + " " + report[2];
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

	private void transferInterface()
	{
		System.out.println("Choose account to take from: ");
		String from = acctInterface();
		System.out.println("Choose account to give to: ");
		String to = acctInterface();
			
			
	
		double transDoub = -1.0;
		boolean cont = true;
		while(cont)
		{
			transDoub = amtInterface();
			if(transDoub != -1.0)
			{
				cont = false;
			}
		}
		
		
		String result = transfer( from, to, transDoub);
		String[] report = result.split(" ");

		if(report[0].equals("1"))
		{
			System.out.println("ERROR: Something went wrong with the transfer, aborting...");
		}
		else{
			System.out.println("Transfer Successful! Balance of From Account is $" + report[1] + " and balance of To Account is $" + report[2] +".");
		}
	}


	
	@Override
	public String topUp( String accountId, double amount )
	{
		if(verifyAcct(accountId) && verifyAmt(amount))
		{
			String[] types = {"POCKET"};
			if(!checkValidType(accountId, types))
			{
				System.out.println("You cannot perform a top-up on a non-pocket account, please choose another account");
				return "1";
			}
			String linkedAid;

			try( Statement statement = _connection.createStatement() )
			{
				try( ResultSet resultSet = statement.executeQuery( "select aid from linked_to where poc_aid = " + accountId ) )
				{
					if(resultSet.next())
						linkedAid = resultSet.getString(1);
					else
					{
						System.out.println("Cannot find Pocket Account's linked account");
						return "1";
					}
				}
				String result;
				if(chargePocketFlatFee(accountId))
				{
					System.out.println(linkedAid);
					result = takeFrom(linkedAid, amount+5.0);
				}
				else
					result = takeFrom(linkedAid, amount);
				

				String[] report = result.split(" ");

				if(report[0].equals("1"))
				{
					return "1";
				}

				String newLinkedBalance = report[2];

				result = giveTo(accountId, amount);

				report = result.split(" ");

				createTransaction(accountId, linkedAid, false, amount, "TOP-UP");
				if(report[0].equals("1"))
				{
					return "1";
				}

				else
					return "0 " + newLinkedBalance + " " + report[2];
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

	private void topUpInterface()
	{
		boolean cont = true;
		String acctId = "invalid";

			acctId = acctInterface();
			
			
	
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
		
		
		String result = topUp( acctId, pockDoub);
		String[] report = result.split(" ");

		if(report[0].equals("1"))
		{
			System.out.println("ERROR: Something went wrong with the top-up, aborting...");
		}
		else{
			System.out.println("Top-Up Successful! Balance of Pocket Account is $" + report[2] + " and balance of linked account is $" + report[1] +".");
		}
		
	}

	public String collect( String accountId, double amount )
	{
		if(verifyAcct(accountId) && verifyAmt(amount))
		{
			String[] types = {"POCKET"};
			if(!checkValidType(accountId, types))
			{
				System.out.println("You cannot perform a collect on a non-pocket account, please choose another account");
				return "1";
			}
			String linkedAid;

			try( Statement statement = _connection.createStatement() )
			{
				try( ResultSet resultSet = statement.executeQuery( "select aid from linked_to where poc_aid = " + accountId ) )
				{
					if(resultSet.next())
						linkedAid = resultSet.getString(1);
					else
					{
						System.out.println("Cannot find Pocket Account's linked account");
						return "1";
					}
				}
				String result;

				//3% fee
				double amountWithFee = amount * 1.03;

				if(chargePocketFlatFee(accountId))
				{
					result = takeFrom(accountId, amountWithFee+5.0);
				}
				else
					result = takeFrom(accountId, amountWithFee);
				

				String[] report = result.split(" ");
				if(report[0].equals("1"))
				{
					return "1";
				}

				String newPocketBalance = report[2];

				result = giveTo(linkedAid, amount);

				report = result.split(" ");

				if(report[0].equals("1"))
				{
					return "1";
				}
				else
				{
					createTransaction(linkedAid,accountId,false,amount,"COLLECT");
					return "0 " + report[2] + " " + newPocketBalance;
				}	
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

	private void collectInterface()
	{
		boolean cont = true;
		String acctId = "invalid";

			acctId = acctInterface();
			
			
	
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
		
		
		String result = collect( acctId, pockDoub);
		String[] report = result.split(" ");

		if(report[0].equals("1"))
		{
			System.out.println("ERROR: Something went wrong with the collect, aborting...");
		}
		else{
			System.out.println("Collect Successful! Balance of Pocket Account is $" + report[2] + " and balance of linked account is $" + report[1] +".");
		}
		
	}


	@Override
	public String payFriend( String from, String to, double amount )
	{
		if(verifyAcct(from) && acctExists(to) && !isClosed(to) && verifyAmt(amount))
		{
			String[] types = {"POCKET"};
			if(!checkValidType(from, types))
			{
				System.out.println("You cannot perform pay friend on a non-pocket account, please choose another account");
				return "1";
			}
			if(!checkValidType(to, types))
			{
				System.out.println("You cannot perform pay friend on a non-pocket account, please choose another account");
				return "1";
			}


			try( Statement statement = _connection.createStatement() )
			{

				String result;
				if(chargePocketFlatFee(from))
				{
					result = takeFrom(from, amount+5.0);
				}
				else
					result = takeFrom(from, amount);
				

				String[] report = result.split(" ");

				if(report[0].equals("1"))
				{
					return "1";
				}

				String newFromBalance = report[2];

				if(chargePocketFlatFee(to))
				{
					result = giveToNotYours(to, amount-5.0);
				}
				else
					result = giveToNotYours(to, amount);

				report = result.split(" ");
				createTransaction(to,from,false,amount,"PAY-FRIEND");
				if(report[0].equals("1"))
				{
					return "1";
				}

				else
					return "0 " + newFromBalance + " " + report[2];
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

	private void payFriendInterface()
	{
		System.out.println("Choose account to take from: ");
		String from = acctInterface();
		System.out.println("Choose account to give to: ");
		String to = getAcct();
			
			
	
		double pockDoub = -1.0;
		boolean cont = true;
		while(cont)
		{
			pockDoub = amtInterface();
			if(pockDoub != -1.0)
			{
				cont = false;
			}
		}
		
		
		String result = payFriend( from, to, pockDoub);
		String[] report = result.split(" ");

		if(report[0].equals("1"))
		{
			System.out.println("ERROR: Something went wrong with pay friend, aborting...");
		}
		else{
			System.out.println("Pay Friend Successful! Balance of From Account is $" + report[1] + " and balance of To Account is $" + report[2] +".");
		}
	}


	public String wire( String from, String to, double amount )
	{
		if(verifyAcct(from) && acctExists(to) && !isClosed(to) && verifyAmt(amount))
		{
			String[] types = {"INTEREST_CHECKING", "STUDENT_CHECKING", "SAVINGS"};
			if(!checkValidType(from, types))
			{
				System.out.println("You cannot perform a wire on this type, please choose another account");
				return "1";
			}
			if(!checkValidType(to, types))
			{
				System.out.println("You cannot perform a wire on this type, please choose another account");
				return "1";
			}


			try( Statement statement = _connection.createStatement() )
			{

					String result = takeFrom(from, amount * 1.02);
				

				String[] report = result.split(" ");

				if(report[0].equals("1"))
				{
					return "1";
				}

				String newFromBalance = report[2];


				result = giveToNotYours(to, amount);

				report = result.split(" ");

				if(report[0].equals("1"))
				{
					return "1";
				}
				else
				{
					createTransaction(to,from,false,amount,"WIRE");
					return "0 " + newFromBalance + " " + report[2];
				}		
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

	private void wireInterface()
	{
		System.out.println("Choose account to take from: ");
		String from = acctInterface();
		System.out.println("Choose account to give to: ");
		String to = getAcct();
			
			
	
		double acctDoub = -1.0;
		boolean cont = true;
		while(cont)
		{
			acctDoub = amtInterface();
			if(acctDoub != -1.0)
			{
				cont = false;
			}
		}
		
		
		String result = wire( from, to, acctDoub);
		String[] report = result.split(" ");

		if(report[0].equals("1"))
		{
			System.out.println("ERROR: Something went wrong with the wire, aborting...");
		}
		else{
			System.out.println("Wire Successful! Balance of From Account is $" + report[1] + " and balance of To Account is $" + report[2] +".");
		}
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
		String address_parse = address.replace("'","\'");
		String create_c = "insert into customer(tid,name,addr,pin) values (\'"+tin+"\',\'"+name+"\',\'"+address_parse+"\',1717)";
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
		if (newRate < 0 || newRate > 100){
			System.out.println("Invalid rate");
			return "1";
		}	
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

		System.out.println("Hello! Welcome to the ATM Interface!");

		signIn();
		
		transact();
	}

	public void signIn()
	{
		System.out.println("What is your Tax ID?");
		String tid = input.next();
		if(!verifyTaxId(tid))
		{
			System.out.println("INVALID Tax ID: Please try again.");
			signIn();
		}
		else{
			System.out.println("Please insert your PIN:");
			String inputPin = input.next();

			if(!verifyPin(inputPin))
			{
				System.out.println("INVALID PIN: Please try again.");
				signIn();
			}
		}

	}

	private boolean verifyPin(String pin)
	{
		String query = "select name, pin, tid from Customer";

		inputPin = Integer.toString(inputPin.hashCode());
		try( Statement statement = _connection.createStatement() )
		{
			try( ResultSet resultSet = statement.executeQuery(query) )
			{
				while( resultSet.next() )
				{
					String tempTid = resultSet.getString(3);
					String tempPin = resultSet.getString(2);
					if (tempTid.trim().equals(customerTaxID.trim()) && tempPin.trim().equals(pin.trim()))
					{
						System.out.println("PIN VERIFIED");
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

	private boolean setPin(String oldPin, String newPin)
	{
		if(verifyPin(oldPin))
		{
			try( Statement statement = _connection.createStatement() )
				{
					if(newPin.trim().length() == 4 && isInteger(newPin))
					statement.executeQuery( "update customer set pin = " + newPin.hashCode() + " where tid = \'" + customerTaxID+ "\'");
					return true;
				}
				catch( final SQLException e )
				{
					System.err.println( e.getMessage() );
					return false;
				}
		}
		else{
			System.out.println("OldPin does not match TID, abort...");
			return false;
		}
	}

	private void setPinInterface()
	{
		System.out.println("What is your old PIN?");
		String oldPin = input.next();

		System.out.println("What is your new PIN?");
		String newPin = input.next();

		if(setPin(oldPin, newPin))
		{
			System.out.println("Pin Successfully Set");
		}

	}

	public boolean isInteger(String s) {
		try { 
			Integer.parseInt(s); 
		} catch(NumberFormatException e) { 
			return false; 
		} catch(NullPointerException e) {
			return false;
		}
		// only got here if we didn't return false
		return true;
	}

	private boolean verifyTaxId(String tid)
	{
		String query = "select name, pin, tid from Customer";

		//inputPin = Integer.toString(inputPin.hashCode());
		try( Statement statement = _connection.createStatement() )
		{
			try( ResultSet resultSet = statement.executeQuery(query) )
			{
				while( resultSet.next() )
				{
					String tempTid = resultSet.getString(3);
					if (tempTid.trim().equals(tid.trim()))
					{
						customerTaxID = tid.trim();
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

	public void startSystemsInterface()
	{
		String[] actions = {"Bank Teller", "Customer ATM", "Set System Date", "Exit"};
		System.out.println("Welcome to Austin and Andrew's Banking System!");
		System.out.println("What would you like to do?:");


		for(int i = 0; i < actions.length; i++)
		{
			System.out.println(i + ":\t" + actions[i]);
		}

		while(true)
		{
			String actIn = input.next().trim();

			if(actIn.equals("0"))
			{
				startBankTellerInterface();
			} else if(actIn.equals("1"))
			{
				startATMInterface();
			} else if(actIn.equals("2"))
			{
				setDateInterface();
			} else if(actIn.equals("3"))
			{
				goodbye();
			} else {
				System.out.println("Invalid input, please try again!");
			}
		}
	}

	private void setDateInterface()
	{

		int year = 0;
		int month = 0;
		int day = 0;
		try{
			System.out.println("What year would you like to set it to?");
			year = Integer.parseInt(input.next());

			System.out.println("What month would you like to set it to?");
			month = Integer.parseInt(input.next());

			System.out.println("What day would you like to set it to?");
			day = Integer.parseInt(input.next());
		}
		catch(NumberFormatException e)
		{
			System.out.println("Invalid Input, try again");
			setDateInterface();
		}

		System.out.println("Date set to: " + setDate( year, month, day )); 

		System.out.println("Date set! Returning to main menu...");
		startSystemsInterface();
	}
	

	private void transact()
	{
		String[] actions = {"Deposit", "Top-Up", "Withdrawal", "Purchase", "Transfer", "Collect", "Wire", "Pay-Friend","Set Pin", "Exit"};
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
			purchaseInterface();
		} else if(actIn.equals("4"))
		{
			transferInterface();
		} else if(actIn.equals("5"))
		{
			collectInterface();
		} else if(actIn.equals("6"))
		{
			wireInterface();
		} else if(actIn.equals("7"))
		{
			payFriendInterface();
		} else if(actIn.equals("8"))
		{
			setPinInterface();
		} else if(actIn.equals("9"))
		{
			startSystemsInterface();
		}else {
			System.out.println("Invalid input, please try again!");
			transact();
			goodbye();
		}

		System.out.println("Your transaction has ended");
		transact();
	}

	@Override
	public String showBalance( String accountId )
	{
				
		try(Statement statement = _connection.createStatement())
		{
			try( ResultSet resultSet = statement.executeQuery( "select aid, balance from account" ) )
			{
				while( resultSet.next() ){
					if(resultSet.getString(1).trim().equals(accountId.trim()))
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

	public String takeFrom( String accountId, double amount )
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

	public String giveTo( String accountId, double amount )
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

	  public String giveToNotYours( String accountId, double amount )
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
			try( ResultSet resultSet = statement.executeQuery( "select tid from own where aid = " + aid ) )
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
		System.out.println("What amount would you like to transact?");
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
			try( ResultSet resultSet = statement.executeQuery( "select t_date from transaction where to_aid = \'" + acctId +"\'"
																+" or from_aid=\'" + acctId +"\'")) 
			{
				while( resultSet.next() )
					if (resultSet.getString(1).substring(0,7).equals(year + "-"+ month))
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
					if (closed.length()==1)
						System.out.println("No closed accounts.");
					else
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
					startSystemsInterface();
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

	private void addInterest(){
		if (!isLastDay()){
			System.out.println("Can only add interest on last day of month.\n");
			return;
		}
		int day = Integer.valueOf(getDate().substring(8,10));
		ArrayList<String> accounts = new ArrayList<String>();
		String month = "";
		String year = "";
		try( Statement statement = _connection.createStatement() )
		{	
			try ( ResultSet check = statement.executeQuery("select system_date from system_date"))
			{
				if (check.next()){
					month = check.getString(1).substring(5,7);
					year = check.getString(1).substring(0,4);
					if (Integer.valueOf(month) < 10)
						month = month.substring(1,2);
				}
				ResultSet interest = statement.executeQuery("select add_interest from monthly_tasks where month=\'" + month + "\' and year=\'" + year + "\'");
				if (interest.next()){
					if (interest.getInt(1)==1) {
						System.out.println("Interest has already been added for this month.");
						return;
					}
				}
			}
			try( ResultSet resultSet = statement.executeQuery( "select aid from account where is_closed=0 and interest_rate !=0"))
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
		for (String aid : accounts){
			double currbal=0.0,potential = 0.0,interest_rate=0.0;
			String net = "";
			try( Statement statement = _connection.createStatement() )
			{
				try( ResultSet balance = statement.executeQuery("select balance,interest_rate from account where aid=\'" + aid + "\'"))
				{
					if(balance.next()){
						currbal = balance.getDouble(1);
						interest_rate = balance.getDouble(2);
						potential = day * currbal;
					}
						
					ResultSet resultSet = statement.executeQuery("select * from transaction "
																		+"where (to_aid=\'"+aid+"\' "
																		+"or from_aid =\'"+aid+"\') ");

					while( resultSet.next() )
					{
						net = String.format("%.2f", resultSet.getDouble(6));
						if (aid.equals(resultSet.getString(4))){
							net = "-" + net;
						}
						int trans_day = Integer.valueOf(resultSet.getString(2).substring(8,10));
						potential -= (Double.valueOf(net) * (trans_day-1));
					}
				}	
				double adb = potential / day;
				double to_add = (adb*(interest_rate/100));
				double new_bal = currbal + to_add;	
				statement.executeQuery("update account set balance=" + new_bal + "where aid=\'" + aid + "\'");
				createTransaction(aid,aid,false,to_add,"ACCRUE-INTEREST");
				statement.executeQuery("update monthly_tasks set add_interest=1");
				System.out.println("Interest added.\n");
			}
			catch( final SQLException e )
			{
				System.err.println( e.getMessage() );
			}
		}
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
			statement.executeQuery("update monthly_tasks set delete_closed=1");
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
			statement.executeQuery("update monthly_tasks set delete_trans=1");
			System.out.println("Transactions deleted");
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