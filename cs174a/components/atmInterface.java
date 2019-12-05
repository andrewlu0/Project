package components;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import oracle.jdbc.pool.OracleDataSource;
import oracle.jdbc.OracleConnection;


public class atmInterface
	{
        public static Scanner input = new Scanner(System.in);

		public static void startInterface()
		{
            checkPIN();
            






        }
        
        public static void checkPIN()
        {

            boolean cont = true;
            while(cont)
            {
                System.out.println("Please insert your PIN:");
                String inputPin = input.next();
                
                if(verifyPIN(inputPin))
                {
                    cont = false;
                }
                else{
                    System.out.println("The PIN you inputed was invalid. Would you like to try again?\n\"1\"\tYes\n\"0\"\tNo");
                    String repeat = input.next();
                    
                    boolean rep = String.valueOf(repeat);
                    
                    if(!rep)
                    {
                        goodbye();
                    }
                }
            }
        }


		public static void verifyPIN(String inputPin)
		{
			
		}
		
	}

