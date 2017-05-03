package com.dasugames.bingoboard.controllers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.dasugames.bingoboard.datamodel.BingoSquare;
import com.dasugames.bingoboard.datamodel.MarkerType;

@Controller
public class BingoBoardMarkServlet  extends AbstractBingoController {
	
	@RequestMapping("/bingoupdatesquare")
	public ModelAndView updateSquare(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "index", required = false) String index,
			@RequestParam(value = "squarestatus", required = false) String status) {
		List<BingoSquare> bingoBoard = generateBingoBoard(request);
		
		BingoBoardWithMessage bingoBoardWithMessage = new BingoBoardWithMessage();
		bingoBoardWithMessage.setBingoSquares(bingoBoard);
		
		updateBingoBoardAndGetMessage(bingoBoardWithMessage, status, index);
	    
		saveBingoBoardAsCookie(response, bingoBoardWithMessage.getBingoSquares());
		
		ModelAndView mv = generateModelViewFromBingoBoard(bingoBoardWithMessage.getBingoSquares());
		
		String announceString = bingoBoardWithMessage.getMessage();
		if (StringUtils.isNotBlank(announceString)) {
			mv.addObject("bingoannounce", announceString);
		}


		return mv;
		
	}
	
	private static void updateBingoBoardAndGetMessage(BingoBoardWithMessage bingoBoardWithMessage, String status, String index) {
		// scrub the string literal inputs so nothing bad happens
		
		
		MarkerType changedMarker;
		int indexToChange;
		List <BingoSquare> boardBefore = bingoBoardWithMessage.getBingoSquares();
		
		if (boardBefore == null || boardBefore.size() != 25) {
			return;
		}
		
		boolean lineBefore = isBoardBingo(boardBefore);
		boolean cornersBefore = isBoardCorners(boardBefore);
		boolean blackoutBefore = isBoardBlackout(boardBefore);

		
		try {
			changedMarker = MarkerType.valueOf(status);
			indexToChange = Integer.valueOf(index);
			
			if (indexToChange >= 0 && indexToChange < 25) {
				bingoBoardWithMessage.getBingoSquares().get(indexToChange).setMarker(changedMarker);
			}
			
		} catch (IllegalArgumentException  e) {
			
		}
		
		List <BingoSquare> boardAfter = bingoBoardWithMessage.getBingoSquares();
		
		boolean lineAfter = isBoardBingo(boardAfter);
		boolean cornersAfter = isBoardCorners(boardAfter);
		boolean blackoutAfter = isBoardBlackout(boardAfter);
		
		String cornersAndOldBingo = "BINGO! You got all CORNERS. Now try going for a BLACKOUT.";
		String bingoAndOldCorners = "BINGO! You got a LINE. Now try going for a BLACKOUT.";
		String blackout = "BINGO! You got a BLACKOUT. Please play again.";
		String corners = "BINGO! You got all CORNERS. Now try going for a LINE.";
		String bingo = "BINGO! You got a LINE. Now try going for all CORNERS.";
		
		if (blackoutAfter && ! blackoutBefore) {
			bingoBoardWithMessage.setMessage(blackout);
		} else if (!lineBefore && lineAfter && cornersAfter) {
			bingoBoardWithMessage.setMessage(bingoAndOldCorners);
		} else if (!cornersBefore && cornersAfter && lineAfter) {
			bingoBoardWithMessage.setMessage(cornersAndOldBingo);
		} else if (!lineBefore && lineAfter) {
			bingoBoardWithMessage.setMessage(bingo);
		} else if (!cornersBefore && cornersAfter) {
			bingoBoardWithMessage.setMessage(corners);
		}
		
	}
	
	private class BingoBoardWithMessage {
		private String message;
		private List<BingoSquare> bingoSquares;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public List<BingoSquare> getBingoSquares() {
			return bingoSquares;
		}

		public void setBingoSquares(List<BingoSquare> bingoSquares) {
			this.bingoSquares = bingoSquares;
		}
		
		
	}
	
	

	
	
	public static QueryResultTuple queryBingoBoard(Connection conn, String index, String fill_status) throws SQLException {

		if (fill_status == null || index == null) {
			// this is sufficient to guarantee that nothing will get set
			fill_status = "EMPTY";
			index = "-1";
		}
		
		// addressing sql injection attacks
		if (!fill_status.equals("EMPTY") && !fill_status.equals("PARTIAL") && !fill_status.equals("FILLED")) {
			index = "-1";
			fill_status = "EMPTY";
		}
		
		try {
			Integer.parseInt(index);
		} catch (NumberFormatException nfe) {
			index = "-1";
		}
		
		String getOldBoardQuery = "SELECT square_index, title, description, fill_status "
				+ "FROM  square_instance, square_content "
				+ "WHERE square_instance.square_content_id = square_content.square_content_id "
				+ "AND square_instance.status = 'Active' and square_content.status = 'Active' "
				+ "AND board_instance_id = 1 order by square_index asc; ";
		
		String changeSquareMark = "UPDATE square_instance SET fill_status = '" + fill_status + "' "
				+ "WHERE square_index = " + index + " AND board_instance_id = 1;";
		
		String getUpdatedBoardQuery = "SELECT square_index, title, description, fill_status "
				+ "FROM  square_instance, square_content "
				+ "WHERE square_instance.square_content_id = square_content.square_content_id "
				+ "AND square_instance.status = 'Active' and square_content.status = 'Active' "
				+ "AND board_instance_id = 1 order by square_index asc; ";
		
		String totalQuery = getOldBoardQuery + changeSquareMark + getUpdatedBoardQuery;

		List<BingoSquare> bingoBoardOld = new ArrayList<BingoSquare>();
		List<BingoSquare> bingoBoardUpdated = new ArrayList<BingoSquare>();
		QueryResultTuple queryResult = new QueryResultTuple();
		
		try (Statement stmt = conn.createStatement()) {
			
			stmt.execute(totalQuery);
			
			// parsable result for update query
			ResultSet rs = stmt.getResultSet();
			bingoBoardOld = parseResultSetIntoBingoBoard(rs);
			stmt.getMoreResults();
			
			// no parsable result set for update query
			stmt.getMoreResults();
			
			// parsable result for update query
			rs = stmt.getResultSet();
			bingoBoardUpdated = parseResultSetIntoBingoBoard(rs);
			

			queryResult.setBingoBoardBefore(bingoBoardOld);
			queryResult.setBingoBoardAfter(bingoBoardUpdated);

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return queryResult;
	}
	

	/**
	 * This is distinct from the one in AbstractBingoController as it includes description.
	 * I may want to make all of this more generic and consolidate the methods.
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	static List<BingoSquare> parseResultSetIntoBingoBoard(ResultSet rs) throws SQLException {
		List<BingoSquare> bingoBoard = new ArrayList<BingoSquare>();
		while (rs.next()) {
			BingoSquare currentSquare = new BingoSquare();
			// TODO make this much more awesome
			Long square_index = rs.getLong("square_index");
			String title = rs.getString("title");
			String description = rs.getString("description");
			String status = rs.getString("fill_status");
			
			currentSquare.setContent(title);
			currentSquare.setMarker(MarkerType.valueOf(status));
			currentSquare.setDescription(description);

			bingoBoard.add(currentSquare);

		}
		
		return bingoBoard;
	}
	
	public static boolean isBoardBingo(List<BingoSquare> bingoBoard) {
		// sanity check
		if (bingoBoard == null || bingoBoard.size() != 25) { 
			return false;
		}
		
		// check horizontal
		for (int i = 0; i < 5; i++) {
			if (bingoBoard.get(i * 5).getMarker() == MarkerType.FILLED
					&& bingoBoard.get(i * 5 + 1).getMarker() == MarkerType.FILLED
					&& bingoBoard.get(i * 5 + 2).getMarker() == MarkerType.FILLED
					&& bingoBoard.get(i * 5 + 3).getMarker() == MarkerType.FILLED
					&& bingoBoard.get(i * 5 + 4).getMarker() == MarkerType.FILLED) {
				return true;
			}
		}
		
		// check vertical
		for (int i = 0; i < 5; i++) {
			if (bingoBoard.get(i).getMarker() == MarkerType.FILLED
					&& bingoBoard.get(i + 5).getMarker() == MarkerType.FILLED
					&& bingoBoard.get(i + 10).getMarker() == MarkerType.FILLED
					&& bingoBoard.get(i + 15).getMarker() == MarkerType.FILLED
					&& bingoBoard.get(i + 20).getMarker() == MarkerType.FILLED) {
				return true;
			}
		}
		
		// check diagonal
		if (bingoBoard.get(0).getMarker() == MarkerType.FILLED
				&& bingoBoard.get(6).getMarker() == MarkerType.FILLED
				&& bingoBoard.get(12).getMarker() == MarkerType.FILLED
				&& bingoBoard.get(20).getMarker() == MarkerType.FILLED
				&& bingoBoard.get(24).getMarker() == MarkerType.FILLED) {
			return true;
		}
		if (bingoBoard.get(4).getMarker() == MarkerType.FILLED
				&& bingoBoard.get(8).getMarker() == MarkerType.FILLED
				&& bingoBoard.get(12).getMarker() == MarkerType.FILLED
				&& bingoBoard.get(16).getMarker() == MarkerType.FILLED
				&& bingoBoard.get(20).getMarker() == MarkerType.FILLED) {
			return true;
		}
		
		
		// I got nothin
		return false;
	}
	
	public static boolean isBoardCorners(List<BingoSquare> bingoBoard) {
		if (bingoBoard == null || bingoBoard.size() != 25) { 
			return false;
		}
		
		return bingoBoard.get(0).getMarker() == MarkerType.FILLED
				&& bingoBoard.get(4).getMarker() == MarkerType.FILLED
				&& bingoBoard.get(20).getMarker() == MarkerType.FILLED
				&& bingoBoard.get(24).getMarker() == MarkerType.FILLED;
	}
	
	public static boolean isBoardBlackout(List<BingoSquare> bingoBoard) {
		if (bingoBoard == null || bingoBoard.size() != 25) { 
			return false;
		}
		
		for (int i = 0; i < 25; i++) {
			if (bingoBoard.get(i).getMarker() != MarkerType.FILLED) {
				return false;
			}
		}
		return true;
	}
	
	
	private static class QueryResultTuple {
		private List<BingoSquare> bingoBoardBefore;
		private List<BingoSquare> bingoBoardAfter;
		public List<BingoSquare> getBingoBoardBefore() {
			return bingoBoardBefore;
		}
		public void setBingoBoardBefore(List<BingoSquare> bingoBoardBefore) {
			this.bingoBoardBefore = bingoBoardBefore;
		}
		public List<BingoSquare> getBingoBoardAfter() {
			return bingoBoardAfter;
		}
		public void setBingoBoardAfter(List<BingoSquare> bingoBoardAfter) {
			this.bingoBoardAfter = bingoBoardAfter;
		}

	}
	
	
	

}
