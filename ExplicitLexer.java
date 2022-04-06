/*
 * An "explicit, manually-coded lexical analyzer for
 * the classroom example:
 * INT_KW, ID, WS
 *
 * INT_KW RE = int     = letter i then letter n then letter t
 *   ID   RE = locase+ = 1 or more lower case letters
 *   WS   RE = " "+    = 1 or more space characters 
 *
 * We build NFAs for each RE, then connected them together,
 * then did subset construction to turn the big NFA into a DFA.
 *
 * These state numbers may differ slightly from our class notes but
 * you should be able to follow.
 *
 * 0 --> 1 on ^i^sp (any character that is not i or not space)
 * 0 --> 5 on sp
 * 0 --> 2 on i
 * 1 --> 1 on ^sp         1 is a "final" state reporting ID
 * 2 --> 1 on ^n^sp       2 is a "final" state reporting ID
 * 2 --> 3 on n           
 * 3 --> 1 on ^t^sp       3 is a "final" state reporting ID
 * 3 --> 4 on t
 * 4 --> 1 on ^sp         4 is a "final" state reporting INT
 * 5 --> 5 on sp          5 is a "final" state reporting WS
 * implied error state 6 has all unspecified transitions; specifically:
 * 1 --> 6 on sp
 * 2 --> 6 on sp
 * 3 --> 6 on sp
 * 4 --> 6 on sp
 * 5 --> 6 on any letter
 *
 * An explicit lexer uses a loop with each iteration being one transition 
 * in the machine. Inside the loop is a giant switch on the current state.
 * The switch cases will consider the input symbol (character) and change
 * the state; this uses an if-else chain.
 *
 * Sample usage:
 * java ExplicitLexer "i in int intx"
 * Expected output: ID WS ID WS INT WS ID
 */

public class ExplicitLexer {

    public static final int EOF_TOK = 0;
    public static final int WS_TOK = 1;
    public static final int B_TOK = 2; //ID
    public static final int B_A_B_PLUSE_TOK = 3; //INT
    public static final int B_A_STAR_B_TOK = 4; 


    public static void main(String[] args) {
	if (args.length != 1) {
	    System.err.println("input string argument missing");
	    System.exit(1);
	}
	
	ExplicitLexer lexer = new ExplicitLexer(args[0]);
	int currTok;
	while ( (currTok=lexer.yylex()) != EOF_TOK) { //scan
	    switch (currTok) {
	    case WS_TOK : 
		System.out.println("Found token: WS"); break;
	    case B_TOK : 
		System.out.println("Found token: B"); break;
	    case B_A_B_PLUSE_TOK : 
		System.out.println("Found token: (BAB)+"); break;
        case B_A_STAR_B_TOK :
        System.out.println("Found token: BA*B"); break;
	    default: 
		System.out.println("Found unknown token: "+currTok); break;
	    }
	}
    }

    // ------------------------------------------------------------------
	//bellow this is: yylex----------------------------

    private char[] input;
    private char currCh; //this is why it is ....wth
    private int currIdx;
    private int state;

    public ExplicitLexer(String input) {

	/* We will need to know when we are at the end of the input.
	   We could check currIdx against length of the input array.
	   But we opt instead to append a "special symbol" not in Sigma
	   that will serve as a marker.
	*/
	input += "$$";

	// Using String class charAt() is way more inefficient than
	//  char array access with an index.
	this.input = input.toCharArray();
	currIdx = 0;

	state = 0;
    }

    public int yylex() {  //scan
	// initialize this request from "parser" for a 
	int lastToken = EOF_TOK;
	state = 0;

	// get the first input character
	currCh = input[currIdx];
	while (true) {
	    //	    System.err.println("state:" + state 
	    //		       + " idx:" + currIdx
	    //		       + " ch:" + currCh);

	    // switch the code block for the current state
	    switch (state) {
	    case 0: 
		// STATE 0
		// * 0 --> 1 on ^i^sp 
		// * 0 --> 5 on sp
		// * 0 --> 2 on i
		if (currCh == '$') return EOF_TOK;
		else if (currCh == ' ') { state = 10; }
		else if (currCh == 'b') { state = 1; }
		else if (currCh >= 'a' && currCh <= 'z') { state = 0; }
		else error(state,currCh);
		break;

	    case 1: 
		// STATE 1 (final state for ID)
		// * 1 --> 1 on ^sp 
		// * 1 --> 6 on sp
		// * 1 --> 6 on $ (end of input)

		// remember we are a final state but keep trying for longer
		lastToken = B_TOK; 

		if (currCh == '$') { state = 11; }
		else if (currCh == ' ') { state = 10; }
		else if (currCh >= 'a' && currCh <= 'z') { state = 0; }
		else error(state,currCh);
		break;

	    case 2: 
		// STATE 2 (final state for ID)
		// * 2 --> 1 on ^n^sp       
		// * 2 --> 3 on n           
		// * 2 --> 6 on sp           
		// * 2 --> 6 on $

		// remember we are a final state but keep trying for longer
		lastToken = B_TOK; 

		if (currCh == '$') { state = 6; }
		else if (currCh == ' ') { state = 6; }
		else if (currCh == 'n') { state = 3; }
		else if (currCh >= 'a' && currCh <= 'z') { state = 1; }
		else error(state,currCh);
		break;

	    case 3: 
		// STATE 3 (final state for ID)
		// * 3 --> 1 on ^t^sp       
		// * 3 --> 4 on t
		// * 3 --> 6 on sp
		// * 3 --> 6 on $

		// remember we are a final state but keep trying for longer
		lastToken = B_TOK; 

		if (currCh == '$') { state = 6; }
		else if (currCh == ' ') { state = 6; }
		else if (currCh == 't') { state = 4; }
		else if (currCh >= 'a' && currCh <= 'z') { state = 1; }
		else error(state,currCh);
		break;

	    case 4:
		// STATE 4 (final state for INT)
		// * 4 --> 1 on ^sp         4 is a "final" state reporting INT
		// * 4 --> 6 on sp
		// * 4 --> 6 on $

		// remember we are a final state but keep trying for longer
		lastToken = B_A_B_PLUSE_TOK;//INTKW_TOK; 

		if (currCh == '$') { state = 6; }
		else if (currCh == ' ') { state = 6; }
		else if (currCh >= 'a' && currCh <= 'z') { state = 1; }
		else error(state,currCh);
		break;

	    case 5: 
		// STATE 5 (final state for WS)
		// * 5 --> 5 on sp          
		// * 5 --> 6 on ^sp
		// * 5 --> 6 on $

		// remember we are a final state but keep trying for longer
		lastToken = WS_TOK; 

		if (currCh == '$') { state = 6; }
		else if (currCh == ' ') { state = 5; }
		else if (currCh >= 'a' && currCh <= 'z') { state = 6; }
		else error(state,currCh);
		break;

	    case 6 :
		// ERROR STATE
		// push back input symbol, report "last token"
		currIdx--;
		return lastToken;

	    default: 
		error(state, currCh);
		return EOF_TOK;
	    }

		
		//done with loop get next char
	    // get the next input character
	    currCh = input[++currIdx];
	}
    }
    private void error(int state, char ch) {
	System.err.println("Error in state " + state 
			   + " on char '" + ch + "'.");
	System.exit(1);
    }
}
