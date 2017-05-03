package com.dasugames.bingoboard.controllers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;

import com.dasugames.bingoboard.datamodel.BingoSquare;
import com.dasugames.bingoboard.datamodel.MarkerType;

public abstract class AbstractBingoController {

	
	@Autowired
	private DataSource sqlSource;

	protected static Cookie generateCookie(List<BingoSquare> bingoBoard) {
		if (bingoBoard == null) {
			return null;
		}
		
		String bingoBoardString = convertBingoBoardToCookieString(bingoBoard);
		Cookie boardCookie = null;
		try {
			boardCookie = new Cookie("bingoboard", URLEncoder.encode(bingoBoardString, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// this should never happen
			e.printStackTrace();
		}
		return boardCookie;
	}

	protected static String convertBingoBoardToCookieString(
			List<BingoSquare> bingoBoard) {
		List<String> bingoSquareStrings = new ArrayList<String>(25);
		for (BingoSquare bingoSquare : bingoBoard) {
			bingoSquareStrings.add(bingoSquare.getContentId()+","+bingoSquare.getMarker());
		}
		String bingoBoardString = StringUtils.join(bingoSquareStrings,";");
		return bingoBoardString;
	}
	
	protected static List<BingoSquare> parseBingoBoardFromCookieString(String cookie){
		String[] splitCookie = cookie.split(";");
		if (splitCookie.length != 25) {
			return null;
		}
		List<BingoSquare> bingoBoard = new ArrayList<BingoSquare>(25);

		
		for (int i = 0; i < 25; i ++) {
			BingoSquare square = new BingoSquare();
			String[] squareString = splitCookie[i].split(",");
			if (squareString.length != 2) {
				return null;
			}
			try {
				// valueOf should be sufficient to scrub
				square.setContentId(Integer.valueOf(squareString[0]));
				square.setMarker(MarkerType.valueOf(squareString[1]));
			} catch (NumberFormatException e) {
				return null;
			}
			
			bingoBoard.add(square);
		}
		
		return bingoBoard;
	}
	
	
	protected static String getBoardCookie(HttpServletRequest request) {
		
		Cookie[] requestCookies = request.getCookies();
		
		if (requestCookies == null) {
			return null;
		}
		
		for (int i = 0; i < requestCookies.length; i++) {
			Cookie currentCookie = requestCookies[i];
			if ("bingoboard".equals(currentCookie.getName())){
				try {
					return URLDecoder.decode(currentCookie.getValue(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					return null;
				}
			}
		}
		return null;
	}
	
	protected Connection getConnection() throws SQLException {
		return sqlSource.getConnection();
	}
	
	public static void decorateBingoBoardWithQuery(List<BingoSquare> bingoBoard, Connection conn) {
		if (bingoBoard == null || bingoBoard.size() != 25) {
			return;
		}
		
		// generate query
		String queryString = generateDecoratorQuery(bingoBoard);
		
		// run query
		try (Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(queryString);) {

			
			Iterator<BingoSquare> bingoIter = bingoBoard.iterator();
			while (rs.next() && bingoIter.hasNext()) {
				BingoSquare bingoSquare = bingoIter.next();
				
				String title = rs.getString("title");
				String description = rs.getString("description");

				bingoSquare.setDescription(description);
				bingoSquare.setContent(title);

			}
		} catch (SQLException e) {
			// TODO do the hokey pokey
			// JDBCTutorialUtilities.printSQLException(e);
			return;
		}
		
	}
	
	protected static List<BingoSquare> generateFreshBingoBoard(Connection conn) throws SQLException {

		String query = "SELECT rowId, square_content_id, title, fill_status FROM "
				+ "(SELECT rowId, square_content_id, title, 'EMPTY' AS fill_status FROM "
				+ "(SELECT if(@i=12,24,@i) AS rowId, @i:=@i+1 AS incrementer, square_content_id, title FROM "
				+ "(SELECT sc.square_content_id, sc.title FROM square_content sc, (SELECT @i:=0) r "
				+ "WHERE is_free_space = FALSE AND status = 'Active' ORDER BY RAND() LIMIT 24) subq) squaresubq "
				+ "UNION SELECT rowId, square_content_id, title, 'FILLED' AS fill_status FROM "
				+ "(SELECT 12 AS rowId, square_content_id, title FROM square_content "
				+ "WHERE is_free_space = TRUE AND status = 'Active' ORDER BY RAND() LIMIT 1) freesubq) insertSubQuery "
				+ "ORDER BY length(rowId), rowId ASC;";

		System.out.println(query);
		
		try (Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(query);) {

			List<BingoSquare> bingoBoard = parseResultSetIntoBingoBoard(rs);
			
			return bingoBoard;
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return null;
		}
		
	}

	static List<BingoSquare> parseResultSetIntoBingoBoard(ResultSet rs)
			throws SQLException {
		List<BingoSquare> bingoBoard = new ArrayList<BingoSquare>(25);
		while (rs.next()) {
			BingoSquare currentSquare = new BingoSquare();
			
			String title = rs.getString("title");
			String status = rs.getString("fill_status");
			long content_id = rs.getLong("square_content_id");
			
			currentSquare.setContent(title);
			currentSquare.setMarker(MarkerType.valueOf(status));
			currentSquare.setContentId((int) content_id);
			bingoBoard.add(currentSquare);

		}
		return bingoBoard;
	}
	
	protected static String generateDecoratorQuery(List<BingoSquare> bingoBoard) {
		
		if (bingoBoard == null || bingoBoard.size() != 25) {
			throw new IllegalArgumentException("generate decorator query needs a bingo board of 25 squares");
		}
		
		List<String> subQuery = new ArrayList<String>(25);
		for (int i = 0; i < 25; i++) {
			subQuery.add("SELECT " + i + " as square_index, " + bingoBoard.get(i).getContentId() + " as square_content_id ");
		}
		String queryString = "SELECT square_index, title, description FROM square_content JOIN ( "
				+ StringUtils.join(subQuery, " UNION ")
				+ ") subq ON square_content.square_content_id = subq.square_content_id "
				+ "ORDER BY square_index ASC;";
		return queryString; 
	}
	
	protected static List<BingoSquare> generateCookieBingoBoard(String cookie,
			Connection conn) {
		List<BingoSquare> bingoBoard;
		// parse out cookie into bingoboard
		bingoBoard = parseBingoBoardFromCookieString(cookie);
		
		// decorate cookie with query data
		decorateBingoBoardWithQuery(bingoBoard, conn);
		
		// no need to generate a new cookie to set to the response
		// as nothing has been updated
		return bingoBoard;
	}
	protected static void saveBingoBoardAsCookie(HttpServletResponse response,
			List<BingoSquare> bingoBoard) {
		// generate cookie string from new bingo board
		Cookie boardCookie = generateCookie(bingoBoard);
		
		// attach the cookie to the response
		if (boardCookie != null) {
			response.addCookie(boardCookie);
		}
	}
	
	protected List<BingoSquare> generateBingoBoard(HttpServletRequest request) {
		String cookie = getBoardCookie(request);

		List<BingoSquare> bingoBoard = null;
		try (Connection conn = getConnection();){ // dataSource.getConnection();){// getConnection();) {

			if (cookie != null) {
				bingoBoard = generateCookieBingoBoard(cookie, conn);
						
			} 
			if (bingoBoard == null || bingoBoard.size() != 25) {
				bingoBoard = generateFreshBingoBoard(conn);
				
			}

			//bingoBoard = queryBingoBoard(conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bingoBoard;
	}
	
	static protected ModelAndView generateModelViewFromBingoBoard(
			List<BingoSquare> bingoBoard) {
		ModelAndView mv = new ModelAndView("bingoboardmodel");
		mv.addObject("bingosquares", bingoBoard);
		return mv;
	}


}
