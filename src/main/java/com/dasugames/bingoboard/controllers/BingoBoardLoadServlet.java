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
import org.springframework.web.servlet.ModelAndView;

import com.dasugames.bingoboard.datamodel.BingoSquare;
import com.dasugames.bingoboard.datamodel.MarkerType;

@Controller
public class BingoBoardLoadServlet extends AbstractPasswordUtilsBingoSquareController {
	@RequestMapping("/bingoloadboard")
	public ModelAndView LoadBingoBoard(HttpServletRequest request, HttpServletResponse response) {
		
		// read form fields
        String boardname = request.getParameter("boardname");
        String password = request.getParameter("password");
        System.out.println(boardname);
        System.out.println(password);
        
        
        
        List<BingoSquare> bingoBoard = generateBingoBoard(request);
        
        
		String announceString = null;
		if (isValidPassword(password) && isValidBoardname(boardname)) {

			try (Connection conn = getConnection();){
				
				// TODO implement real logic here
				// generate query
				String queryTableSavesForSalt = generateSaltQuery(boardname);

				String salt = null;

				// query database for salt
				try (Statement stmt = conn.createStatement();
						ResultSet rs = stmt.executeQuery(queryTableSavesForSalt);) {



					while (rs.next()) {

						// Long index = rs.getLong("square_index");
						salt = rs.getString("board_salt");
					}
				} catch (SQLException e) {
					System.out.println(e.getMessage());
					// JDBCTutorialUtilities.printSQLException(e);

				}

				// parse statement to get salt

				// generate hash from password and salt
				String hashedPassword = null;
				if (salt != null) {
					hashedPassword = generateHashFromPasswordAndSalt(password,
							salt);
				}
				
		        System.out.println(salt);
		        System.out.println(hashedPassword);
				List<BingoSquare> loadedBingoBoard = null;

				loadedBingoBoard = loadBingoBoardFromDatabase(boardname,
						hashedPassword, conn);
				if (loadedBingoBoard != null && loadedBingoBoard.size() == 25) {
					bingoBoard = loadedBingoBoard;
					announceString = "Load was successful!";
				} else {
					announceString = "Boardname and password are incorrect.";
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				announceString = "Some connection error.";
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
	
	private List<BingoSquare> loadBingoBoardFromDatabase(String boardname, String hashedPassword, Connection conn) {
		if (StringUtils.isEmpty(boardname) || StringUtils.isEmpty(hashedPassword)) {
			return null;
		}
		
		String loadQuery = generateLoadQuery(boardname, hashedPassword);
		System.out.println(loadQuery);
		List<BingoSquare> bingoBoardFromQuery = new ArrayList<BingoSquare>();
		try (Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(loadQuery);) {

			while (rs.next() ) {
				
				BingoSquare bingoSquare = new BingoSquare();
				String title = rs.getString("title");
				String description = rs.getString("description");
				String marker = rs.getString("fill_status");
				bingoSquare.setMarker(MarkerType.valueOf(marker));
				bingoSquare.setDescription(description);
				bingoSquare.setContent(title);
				
				bingoBoardFromQuery.add(bingoSquare);

			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return null;
		}
		
		return bingoBoardFromQuery;
		
	}
	
	private String generateSaltQuery(String boardname) {
		return "SELECT board_salt FROM saved_board_instance WHERE board_name = '" + boardname +"';";
	}
	
	private String generateLoadQuery(String boardname, String hashedpass) {
		return "SELECT square_index, title, description, fill_status FROM saved_board_instance, square_instance, square_content "
				+ "WHERE saved_board_instance.saved_board_instance_id = square_instance.saved_board_instance_id AND square_instance.square_content_id = square_content.square_content_id "
				+ "AND board_name = '"
				+ boardname
				+ "' AND board_password = '"
				+ hashedpass
				+ "' AND square_instance.status = 'Active' AND square_content.status = 'Active' ORDER BY square_index ASC;";
	}
	

}
