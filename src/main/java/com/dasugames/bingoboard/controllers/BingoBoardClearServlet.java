package com.dasugames.bingoboard.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.dasugames.bingoboard.datamodel.BingoSquare;
import com.dasugames.bingoboard.datamodel.MarkerType;

@Controller
public class BingoBoardClearServlet extends AbstractBingoController {
	 
		@RequestMapping("/bingoclearmarkers")
		public ModelAndView generateJspWebsite(HttpServletRequest request, HttpServletResponse response) {
			
			List<BingoSquare> bingoBoard = generateBingoBoard(request);
			
			clearBingoBoard(bingoBoard);
		    
			saveBingoBoardAsCookie(response, bingoBoard);
			
			ModelAndView mv = generateModelViewFromBingoBoard(bingoBoard);

			return mv;
		}

		protected static void clearBingoBoard(List<BingoSquare> bingoBoard) {
			for (BingoSquare bingoSquare : bingoBoard) {
				bingoSquare.setMarker(MarkerType.EMPTY);
			}
		}
	

}
