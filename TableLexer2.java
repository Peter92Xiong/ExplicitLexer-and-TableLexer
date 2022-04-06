/*
 * An "table-driven" lexical analyzer for
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
 * A table-driven lexer encodes the DFA info above into a 2-dimension
 * table indexed by state and input.
 *
 * Sample usage:
 * java TableLexer2 "i in int intx"
 * Expected output: ID WS ID WS INT WS ID
 */

public class TableLexer2 {

    public static final int ERR_TOK = -1;
    public static final int EOF_TOK = 0;
    public static final int WS_TOK = 1;
    public static final int B_TOK = 2; //ID tok
    public static final int B_A_B_PLUSE_TOK = 3; //int_tok
    public static final int B_A_STAR_B_TOK = 4;


    public static void main(String[] args) {
	if (args.length != 1) {
	    System.err.println("input string argument missing");
	    System.exit(1);
	}
	
	TableLexer2 lexer = new TableLexer2(args[0]);
	int currTok;
	while ( (currTok=lexer.yylex()) != EOF_TOK) {
	    switch (currTok) {
	    case WS_TOK : 
		System.out.println("Found token: WS"); break;
	    case B_TOK : 
		System.out.println("Found token: B"); break;
	    case B_A_B_PLUSE_TOK : 
		System.out.println("Found token: (BAB)+"); break;
	    case B_A_STAR_B_TOK : 
		System.out.println("Found token: BA*B"); break;
	    case ERR_TOK : 
		System.out.println("Lexer error"); break;
	    default: 
		System.out.println("Found unknown token: "+currTok); break;
	    }
	}
    }

    private char[] input;
    private char currCh;
    private int currIdx;
    private int state;
    private int numStates;
    private int[][] stateTable = {
	// * = any letter ^int
	// sp = space
	// $ is special end of
	//         $   sp   b   a   *
	/* 0 */  { 10, 9,   1 , 10, 10},
	/* 1 */  { 10, 10,  5,  2,  10},
	/* 2 */  { 10, 10,  4,  3,  10},
	/* 3 */  { 10, 10,  5,  3,  10},
	/* 4 */  { 10, 10,  6,  10, 10},
	/* 5 */  { 10, 10,  10, 10, 10},
	/* 6 */  { 10, 10,  10, 7,  10},
	/* 7 */  { 10, 10,  8,  10, 10},
	/* 8 */  { 10, 10,  6,  10, 10},
	/* 9 */  { 10, 9,   10, 10, 10},
	/*10 */  { 10, 10,  10, 10,  10}
    };

    public TableLexer2(String input) {
	input += "$$";
	this.input = input.toCharArray();
	currIdx = 0;
	state = 0;
	numStates = stateTable.length;
    }
    private int mapCharToCol(char c) {
	if (c == '$') return 0;
	else if (c == ' ') return 1;
	else if (c == 'b') return 2;
	else if (c == 'a') return 3;
	else if (c >= 'a' && c <= 'z') return 4;
	else {
	    error(-1, c);
	}
	return 0;
    }
    public int yylex() {
	int lastToken = EOF_TOK;
	int idx;
	int newstate;

	state = 0;
	idx = mapCharToCol(input[currIdx]);
	while (true) {

	    //System.out.println("Before state change: idx="+idx+",currIdx="
	    // +currIdx+",ch="+input[currIdx]+",state="+state);
	    state = stateTable[state][idx];
	    //System.out.println("After state change: idx="+idx+",currIdx="
	    // +currIdx+",ch="+input[currIdx]+",state="+state);


	    // If new state is a "final" state
	    switch (state) {
	    case 1: lastToken = B_TOK; break;
	    case 2: break;
	    case 3: break;
	    case 4: lastToken = B_A_B_PLUSE_TOK; break;
	    case 5: lastToken = B_A_STAR_B_TOK; break;
	    case 6: 
		// "Error" state
		// We might have been in a final state prior but didn't
		// return then in order to keep going for "longest match."
		// But NOW we know that prior state was REALLY the end!
		// So, return 'lastToken' which was set in that prior state.
		// And make sure to not lose the input symbol that caused
		// the transition to this error state.
		
	    	break;
	    case 7: break;
	    case 8: lastToken = B_A_B_PLUSE_TOK; break;
	    case 9: lastToken = WS_TOK; break;
	    case 10: return lastToken;

	    default: 
		if (state >= 0 && state < numStates) {
		    // all final states should be caught above
		    // any real DFA state that is not a final state
		    // should keep the machine going, but also make sure
		    // that we note that we are not in a final state
		    lastToken = ERR_TOK;
		}
		else {
		    // This is a bad state value
		    error(state,input[currIdx]);
		}
		break;
	    }

	    idx = mapCharToCol(input[++currIdx]);
	}
    }
    private void error(int state, char ch) {
	System.err.println("Error in state " + state 
			   + " on char '" + ch + "'.");
	System.exit(1);
    }
}
