package com.dasugames.bingoboard.controllers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.dasugames.bingoboard.datamodel.BingoSquare;
 
@Controller
public class BingoBoardSquareServlet extends AbstractBingoController {
	
	@RequestMapping("/bingogetsquare")
	public ModelAndView generateJspWebsite(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "index", required = false) String squareIndex) {
		
		List<BingoSquare> bingoBoard = generateBingoBoard(request);
		
		List<BingoSquare> bingoSquare = getBingoSquareAtIndex(bingoBoard, squareIndex);
		
		ModelAndView mv = generateModelViewFromBingoBoard(bingoSquare);

		return mv;
	}
	
	public static List<BingoSquare> getBingoSquareAtIndex(List<BingoSquare> bingoBoard, String squareIndex) {
		if (bingoBoard == null || bingoBoard.size() != 25) {
			return null;
		}
		List<BingoSquare> singleBingoSquare = new ArrayList<BingoSquare>(1);
		try {
			singleBingoSquare.add(bingoBoard.get(Integer.valueOf(squareIndex)));
		} catch (NumberFormatException e) {
			
			BingoSquare errorSquare = new BingoSquare();
			errorSquare.setDescription("ERROR");
			singleBingoSquare.add(errorSquare);
		}
		
		return singleBingoSquare;
		
	}
	
	protected List<BingoSquare> generateBingoBoard(HttpServletRequest request) {
		String cookie = getBoardCookie(request);
		List<BingoSquare> bingoBoard = null;
		try (Connection conn = getConnection();){

			if (cookie != null) {
				bingoBoard = generateCookieBingoBoard(cookie, conn);	
			} 
			
			// we do not want to generate a new bingo board if something is wrong here
			if (bingoBoard == null || bingoBoard.size() != 25) {
				return null;
			}

			//bingoBoard = queryBingoBoard(conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bingoBoard;
	}
	


}