package com.dasugames.bingoboard.controllers;


import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.dasugames.bingoboard.datamodel.BingoSquare;

@Controller
public class BingoBoardDynamicController3 extends AbstractBingoController {
 
	@RequestMapping("/bingodyn3")
	public ModelAndView generateJspWebsite(HttpServletRequest request, HttpServletResponse response) {
		
		List<BingoSquare> bingoBoard = generateBingoBoard(request);
	    
		saveBingoBoardAsCookie(response, bingoBoard);
		
		ModelAndView mv = generateModelViewFromBingoBoard(bingoBoard);

		return mv;
	}

	static protected ModelAndView generateModelViewFromBingoBoard(
			List<BingoSquare> bingoBoard) {
		
		ModelAndView mv = new ModelAndView("bingoBoardAjax");
		
		mv.addObject("bingosquares", bingoBoard);
		
		return mv;
	}

	
}