import java.util.Arrays;
import java.util.Scanner;

public class InputProgram {
	public static void main(String[] args) {
		//Command Line Argument = CLA
		//Stores index where the CLA has been called
		int op1 = Arrays.asList(args).indexOf("-o");
		int op2 = Arrays.asList(args).indexOf("-t");
		boolean op3 = Arrays.asList(args).contains("-h"); //Stores if option 3 was called
		
		Scanner input = new Scanner(System.in); //Handles standard input
		
		//Loop to continuously get standard input until exit
		System.out.println("Standard Input: ");
		while(input.hasNext()) {
			System.out.println(input.nextLine());
		}
		
		input.close(); //Closes Scanner
		
		//Outputs which command arguments have been called
		System.out.println("Command line arguments: ");
		if(op1 >= 0)
			System.out.println("option 1: " + args[op1 + 1]);
		
		if(op2 >= 0)
			System.out.println("option 2: " + args[op2 + 1]);
		
		if(op3)
			System.out.println("option 3");

	} //main()
}//class
