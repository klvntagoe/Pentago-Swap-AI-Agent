package student_player;

import java.util.*;

import boardgame.Move;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;
import pentago_swap.PentagoBoardState.Piece;

public class MyTools {
    public static double getSomething() {
        return Math.random();
    }

    
        
    public static Move alphabetaCutOff(PentagoBoardState boardState, int depth) {
    	int id = boardState.getTurnPlayer();
    	Piece player = (id == 0)? Piece.WHITE : Piece.BLACK;
    	Piece opponent = (id == 1)? Piece.WHITE : Piece.BLACK;
    	ArrayList<PentagoMove> moves = boardState.getAllLegalMoves();
    	Collections.shuffle(moves); 
        ArrayList<Integer> evals = new ArrayList<>();
        for (PentagoMove m : moves) {
        	PentagoBoardState temp = (PentagoBoardState) boardState.clone();
        	temp.processMove(m);
        	evals.add(alphabetaValue(temp, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false, player, opponent));		//This function is the root of the tree, thus already accumulating the max
        }
		int max = 0;
        for (int i = 0; i < evals.size(); i++) {
        	if (evals.get(max) < evals.get(i)) max = i;
        }
        return moves.get(max);
    }
    
    
    
    public static int alphabetaValue(PentagoBoardState boardState, int depth, int alpha, int beta, boolean max, Piece player, Piece opponent) {
    	if (depth == 0 || boardState.gameOver()) return simpleEvaluation(boardState, player, opponent);
    	else {
    		ArrayList<PentagoMove> moves = boardState.getAllLegalMoves();
    		Collections.shuffle(moves);
    		if (max){
    			int value = Integer.MIN_VALUE;
                for (PentagoMove m : moves) {
                	PentagoBoardState temp = (PentagoBoardState) boardState.clone();
                	temp.processMove(m);
                	value = Math.max(value, alphabetaValue(temp, depth - 1, alpha, beta, !max, player, opponent));
                	alpha = Math.max(alpha, value);
                	if (alpha >= beta) break;
                }
                return value;
    		}else {
    			int value = Integer.MAX_VALUE;
                for (PentagoMove m : moves) {
                	PentagoBoardState temp = (PentagoBoardState) boardState.clone();
                	temp.processMove(m);
                	value = Math.min(value, alphabetaValue(temp, depth - 1, alpha, beta, !max, player, opponent));
                	beta = Math.min(alpha, value);
                	if (alpha >= beta) break;
                }
                return value;
    		}
    	}
    }
    
    
    
    public static Move minimaxCutOff(PentagoBoardState boardState, int depth) {
    	int id = boardState.getTurnPlayer();
    	Piece player = (id == 0)? Piece.WHITE : Piece.BLACK;
    	Piece opponent = (id == 1)? Piece.WHITE : Piece.BLACK;
    	ArrayList<PentagoMove> moves = boardState.getAllLegalMoves();
    	Collections.shuffle(moves);
        ArrayList<Integer> evals = new ArrayList<>();
        for (PentagoMove m : moves) {
        	PentagoBoardState temp = (PentagoBoardState) boardState.clone();
        	temp.processMove(m);
        	evals.add(minimaxValue(temp, depth - 1, false, player, opponent));		//This function is the root of the tree, thus already accumulating the max
        }
		int max = 0;
        for (int i = 0; i < evals.size(); i++) {
        	if (evals.get(max) < evals.get(i)) max = i;
        }
        return moves.get(max);
    }
    
    
    
    public static int minimaxValue(PentagoBoardState boardState, int depth, boolean max, Piece player, Piece opponent) {
        if (depth == 0 || boardState.gameOver()) return simpleEvaluation(boardState, player, opponent);
        else {
        	ArrayList<PentagoMove> moves = boardState.getAllLegalMoves();
            ArrayList<Integer> evals = new ArrayList<>();
        	Collections.shuffle(moves); 
        	if (max) {
                for (PentagoMove m : moves) {
                	PentagoBoardState temp = (PentagoBoardState) boardState.clone();
                	temp.processMove(m);
                	evals.add(minimaxValue(temp, depth - 1, !max, player, opponent));
                }
                int maxVal = Integer.MIN_VALUE;
                for (int i = 0; i < evals.size(); i++) {
                	Math.max(maxVal, evals.get(i));
                }
                return maxVal;
        	}else {
                for (PentagoMove m : moves) {
                	PentagoBoardState temp = (PentagoBoardState) boardState.clone();
                	temp.processMove(m);
                	evals.add(minimaxValue(temp, depth - 1, !max, player, opponent));
                }
                int minVal = Integer.MAX_VALUE;
                for (int i = 0; i < evals.size(); i++) {
                	Math.min(minVal, evals.get(i));
                }
                return minVal;
        	}
        }
    }
    
    
    
    public static Move simpleMax(PentagoBoardState boardState) {
    	int id = boardState.getTurnPlayer();
    	Piece player = (id == 0)? Piece.WHITE : Piece.BLACK;
    	Piece opponent = (id == 1)? Piece.WHITE : Piece.BLACK;
    	ArrayList<PentagoMove> moves = boardState.getAllLegalMoves();
        ArrayList<Integer> evals = new ArrayList<>();
        for (PentagoMove m : moves) {
        	Object clone = boardState.clone();
        	PentagoBoardState temp = (PentagoBoardState) clone;
        	temp.processMove(m);
        	evals.add(simpleEvaluation(temp, player, opponent));
        }
        int max = 0;
        for (int i = 0; i < evals.size(); i++) {
        	if (evals.get(i) > evals.get(max)) max = i;
        }
        return moves.get(max);
    }
    
    
    
    /*Simple Player Evaluation Function:
     * Counts number of players sequences
     * Rewards in an exponential manner w.r.t sequence lengths
     * */
    public static int simplePlayerEvaluation(PentagoBoardState boardState, Piece player) {
    	int smallestSequence = 2;
    	int largestSequence = 5;
    	int[] count = new int[largestSequence];
    	Arrays.fill(count, 0);
    	int total = 0;
    	// Count Horizontal Points
    	//Parse Columns
    	for (int i = 0; i < PentagoBoardState.BOARD_SIZE; i++) {
    		//Set sequence size
    		for (int k = largestSequence; k >= smallestSequence; k--) {
    			//Set offset bounds
    			for (int l = 0; k + l <=PentagoBoardState.BOARD_SIZE; l++) {
    				//Parse Rows
    				int tempCount = 0;
        			for (int j = l; j < k + l; j++) {
        				if (boardState.getPieceAt(i,j) == player) tempCount++;
        				else continue;
        				if (tempCount == k) count[k - 1]++;
        			}
    			}
    		}
    	}
    	// Count Vertical Points
    	//Parse Rows
    	for (int i = 0; i < PentagoBoardState.BOARD_SIZE; i++) {
    		//Set sequence size
    		for (int k = largestSequence; k >= smallestSequence; k--) {
    			//Set offset bounds
    			for (int l = 0; k + l <=PentagoBoardState.BOARD_SIZE; l++) {
    				//Parse Columns
    				int tempCount = 0;
        			for (int j = l; j < k + l; j++) {
        				if (boardState.getPieceAt(j,i) == player) tempCount++;
        				else continue;
        				if (tempCount == k) count[k - 1]++;
        			}
    			}
    		}
    	}
    	// Count Rightward Diagonal Points
    	//Set sequence size
    	for (int k = largestSequence; k <= smallestSequence; k--) {
    		int[] initi = {0, 1, 0};	//i-coordinates for beginning
    		int[] initj = {0, 0, 1};	//j-coordinates for beginning
    		//Parse Coordinates
    		for (int x = 0; x < 3; x++) {
    			//Set offset bounds
    			for (int li = initi[x], lj = initj[x]; k + li <=PentagoBoardState.BOARD_SIZE && k + lj <= PentagoBoardState.BOARD_SIZE; li++, lj++) {
    				int tempCount = 0;
    				for (int i = li; i < k+ li; i++) {
    					for (int j = lj; j < k + lj; j++) {
            				if (boardState.getPieceAt(i,j) == player) tempCount++;
            				else continue;
            				if (tempCount == k) count[k - 1]++;
    					}
    				}
    			}
    		}
    	}
    	// Count Leftward Diagonal Points
    	//Set sequence size
    	for (int k = largestSequence; k <= smallestSequence; k--) {
    		int[] initi = {0, 1, 0};	//i-coordinates for beginning
    		int[] initj = {5, 5, 4};	//j-coordinates for beginning
    		//Parse Coordinates
    		for (int x = 0; x < 3; x++) {
    			//Set offset bounds
    			for (int li = initi[x], lj = initj[x]; k + li <=PentagoBoardState.BOARD_SIZE && lj - k >= -1; li++, lj++) {
    				int tempCount = 0;
    				for (int i = li; i < k+ li; i++) {
    					for (int j = lj; j > lj - k; j--) {
            				if (boardState.getPieceAt(i,j) == player) tempCount++;
            				else continue;
            				if (tempCount == k) count[k - 1]++;
    					}
    				}
    			}
    		}
    	}
    	//Total up scores
    	if (count[largestSequence - 1] > 0) total = Integer.MAX_VALUE;
    	else {
    		for (int i = largestSequence - 2; i >=0; i--) {
        		//Sequences of length 4 is worth 100 points
        		//Sequences of length 3 is worth 10 points
        		//Sequences of length 2 is worth 1 point
        		total += count[i]*Math.pow(10, i+1-smallestSequence);
        	}
    	}
    	return total;
    }
    
    
    
    /*Caller Evaluation Function:
     * Counts number of players sequences
     * Rewards a player in an exponential manner w.r.t sequence lengths
     * Caller's final score is determined based on the who has the upper hand (difference between user and opponents scores)
     * */
    public static int simpleEvaluation(PentagoBoardState boardState, Piece player, Piece opponent) {
    	int difference = simplePlayerEvaluation(boardState, player) - simplePlayerEvaluation(boardState, opponent);
    	//System.out.println(difference);
    	return difference;
    }
}