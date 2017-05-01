package com.dasugames.bingoboard.controllers;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.dasugames.bingoboard.datamodel.BingoSquare;

@Controller
public class BingoBoardSaveServlet extends AbstractPasswordUtilsBingoSquareController {
	@RequestMapping("/bingosaveboard")
	public ModelAndView SaveBingoBoard(HttpServletRequest request, HttpServletResponse response){
		// read form fields
        String boardname = request.getParameter("boardname");
        String password = request.getParameter("password");
        System.out.println(boardname);
        System.out.println(password);
        
        
        boolean saveSuccess = false;
        
        List<BingoSquare> bingoBoard = generateBingoBoard(request);
        
        
        String announceString = null;
        if 	(isValidPassword( password) && isValidBoardname(boardname) && bingoBoard != null) {
    		// TODO implement real logic here
        	String salt = generateRandomSalt();
        	
        	String hashedPassword = generateHashFromPasswordAndSalt(password, salt);
        	
			try (Connection conn = getConnection();){

	        	saveSuccess = saveBingoBoardOnDatabase(bingoBoard, boardname, hashedPassword, salt, conn);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				announceString = "Some connection error.";
			}
        	        	
    		if (saveSuccess == false) {
    			announceString = "The given board name already exists.";
    		} else {
    			announceString = "Save was successful!";
    		}
    		
    	} else {
    		announceString = "There was a problem with your board name or password.";
    	}
    	
	    
		saveBingoBoardAsCookie(response, bingoBoard);
		
		ModelAndView mv = generateModelViewFromBingoBoard(bingoBoard);
		if (StringUtils.isNotBlank(announceString)) {
			mv.addObject("bingoannounce", announceString);
		}

		return mv;
        
	}
	
	public boolean saveBingoBoardOnDatabase(List<BingoSquare> bingoBoard, String boardname, String hashedPassword, String salt, Connection conn) {
		if (bingoBoard == null || bingoBoard.size() != 25) {
			return false;
		}
		
		// generate query
		String queryStringBoard = generateSaveQueryBoard(bingoBoard, boardname, hashedPassword, salt);
		String queryStringSquares = generateSaveQuerySquares(bingoBoard, boardname, hashedPassword, salt);
		
		// run query
		try (Statement stmt = conn.createStatement()) {

			// technically I can do these separately due to the randomly generated salt
			stmt.execute(queryStringBoard);
			stmt.execute(queryStringSquares);
			
			// I actually do not need to go through the result set in this case.
			// there is also not result set to close separately anyways.
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			// JDBCTutorialUtilities.printSQLException(e);
			return false;
		}
		
		return true;
	}
	
	public String generateSaveQueryBoard(List<BingoSquare> bingoBoard,
			String boardname, String hashedPassword, String salt) {
		String insertBoardInsertQuery = "INSERT INTO saved_board_instance (board_name, board_password, board_salt) VALUES ('"
				+ boardname + "', '" + hashedPassword + "', '" + salt + "'); ";

		return insertBoardInsertQuery;
	}
	
	public String generateSaveQuerySquares(List<BingoSquare> bingoBoard,
			String boardname, String hashedPassword, String salt) {
		
		List<String> literalQueryElements = new ArrayList<String>();
		for (int i = 0; i < 25; i++) {
			BingoSquare bingoSquare = bingoBoard.get(i);
			literalQueryElements.add(" SELECT " + i + " AS square_index, "
					+ bingoSquare.getContentId() + " AS square_content_id, '"
					+ bingoSquare.getMarker() + "' AS fill_status ");
		}

		String insertSquareInstanceQuery = "INSERT INTO square_instance (saved_board_instance_id, square_index, square_content_id, fill_status) "
				+ "SELECT saved_board_instance_id, square_index, square_content_id, fill_status FROM ( "
				+ StringUtils.join(literalQueryElements, " UNION ")
				+ " ) sub1 JOIN (SELECT saved_board_instance_id FROM saved_board_instance WHERE board_name = '"
				+ boardname
				+ "' AND board_password = '"
				+ hashedPassword
				+ "') sub2 ;";
		return insertSquareInstanceQuery;
	}
}
