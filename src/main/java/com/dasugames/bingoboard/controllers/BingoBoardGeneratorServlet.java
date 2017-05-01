package com.dasugames.bingoboard.controllers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.dasugames.bingoboard.datamodel.BingoSquare;

@Controller
public class BingoBoardGeneratorServlet extends AbstractBingoController {
	@RequestMapping("/bingogeneratenewboard")
	public ModelAndView generateNewBingoBoard(HttpServletRequest request, HttpServletResponse response) {
		
		List<BingoSquare> bingoBoard = generateBingoBoard(request);
	    
		saveBingoBoardAsCookie(response, bingoBoard);
		
		ModelAndView mv = generateModelViewFromBingoBoard(bingoBoard);

		return mv;
	}
	
	protected List<BingoSquare> generateBingoBoard(HttpServletRequest request) {
		List<BingoSquare> bingoBoard = null;
		try (Connection conn  = getConnection();){

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
	

}
