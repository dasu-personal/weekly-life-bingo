package com.dasugames.bingoboard.controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;

import com.dasugames.bingoboard.datamodel.BingoSquare;
import com.dasugames.bingoboard.datamodel.MarkerType;

public class AbstractBingoControllerTest {
	
	@Test(expected = IllegalArgumentException.class)
	public void generateDecoratorQueryTestNull() {
		AbstractBingoController.generateDecoratorQuery(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void generateDecoratorQueryTestTooSmall() {
		List<BingoSquare> bingoBoard = new ArrayList<BingoSquare>();
		AbstractBingoController.generateDecoratorQuery(bingoBoard);
	}
	
	@Test
	public void generateDecoratorQueryTest() {
		List<BingoSquare> bingoBoard = new ArrayList<BingoSquare>();
		for (int i = 0; i < 25; i++) {
			BingoSquare bingoSquare = new BingoSquare();
			bingoSquare.setContentId(i);
			bingoBoard.add(bingoSquare);
		}
		String resultantSql = AbstractBingoController.generateDecoratorQuery(bingoBoard);
		
		Assert.assertEquals(
				"SELECT square_index, title, description FROM square_content JOIN ( " +
		"SELECT 0 as square_index, 0 as square_content_id  UNION "+
						"SELECT 1 as square_index, 1 as square_content_id  UNION "+ 
		"SELECT 2 as square_index, 2 as square_content_id  UNION "+
						"SELECT 3 as square_index, 3 as square_content_id  UNION "+
		"SELECT 4 as square_index, 4 as square_content_id  UNION "+
						"SELECT 5 as square_index, 5 as square_content_id  UNION "+
		"SELECT 6 as square_index, 6 as square_content_id  UNION "+
						"SELECT 7 as square_index, 7 as square_content_id  UNION "+
		"SELECT 8 as square_index, 8 as square_content_id  UNION "+
						"SELECT 9 as square_index, 9 as square_content_id  UNION "+
		"SELECT 10 as square_index, 10 as square_content_id  UNION "+
						"SELECT 11 as square_index, 11 as square_content_id  UNION "+
		"SELECT 12 as square_index, 12 as square_content_id  UNION "+
						"SELECT 13 as square_index, 13 as square_content_id  UNION "+
		"SELECT 14 as square_index, 14 as square_content_id  UNION "+
						"SELECT 15 as square_index, 15 as square_content_id  UNION "+
		"SELECT 16 as square_index, 16 as square_content_id  UNION "+
						"SELECT 17 as square_index, 17 as square_content_id  UNION "+
		"SELECT 18 as square_index, 18 as square_content_id  UNION "+
						"SELECT 19 as square_index, 19 as square_content_id  UNION "+
		"SELECT 20 as square_index, 20 as square_content_id  UNION "+
						"SELECT 21 as square_index, 21 as square_content_id  UNION "+
		"SELECT 22 as square_index, 22 as square_content_id  UNION "+
						"SELECT 23 as square_index, 23 as square_content_id  UNION "+
		"SELECT 24 as square_index, 24 as square_content_id ) subq "+
						"ON square_content.square_content_id = subq.square_content_id ORDER BY square_index ASC;",
				resultantSql);
	}
	
	@Test
	public void parseResultSetIntoBingoBoardTest() throws SQLException {
		// mock everything
		ResultSet rs = Mockito.mock(ResultSet.class);
		Mockito.when(rs.next()).thenReturn(true,true,true,true, true,false);
		Mockito.when(rs.getString("title")).thenReturn("a", "b", "c", "d", "e");
		Mockito.when(rs.getString("fill_status")).thenReturn("EMPTY", "PARTIAL", "FILLED", "PARTIAL", "FILLED");
		Mockito.when(rs.getLong("square_content_id")).thenReturn(1l,2l,3l,4l,5l);
		
		List<BingoSquare> outputBingoBoard = AbstractBingoController.parseResultSetIntoBingoBoard(rs);
		
		Assert.assertEquals(5, outputBingoBoard.size());
		Assert.assertEquals(MarkerType.EMPTY, outputBingoBoard.get(0).getMarker());
		Assert.assertEquals("a", outputBingoBoard.get(0).getContent());
		Assert.assertEquals("a", outputBingoBoard.get(0).getContent());
		Assert.assertEquals(1l, outputBingoBoard.get(0).getContentId());
		
	}
	
	@Test
	public void convertBingoBoardToCookieStringTest() {
		List<BingoSquare> bingoBoard = new ArrayList<BingoSquare>();
		
		for (int i = 0; i < 25; i++) {
			BingoSquare currBingoSquare = new BingoSquare();
			currBingoSquare.setContent("Content");
			currBingoSquare.setContentId(i * 2);
			currBingoSquare.setDescription("Description");
			currBingoSquare.setMarker(MarkerType.EMPTY);
			bingoBoard.add(currBingoSquare);
		}
		
		String cookieString = AbstractBingoController.convertBingoBoardToCookieString(bingoBoard);
		
		Assert.assertEquals("0,EMPTY;2,EMPTY;4,EMPTY;6,EMPTY;8,EMPTY;10,EMPTY;12,EMPTY;14,EMPTY;16,EMPTY;18,EMPTY;20,EMPTY;22,EMPTY;24,EMPTY;26,EMPTY;28,EMPTY;30,EMPTY;32,EMPTY;34,EMPTY;36,EMPTY;38,EMPTY;40,EMPTY;42,EMPTY;44,EMPTY;46,EMPTY;48,EMPTY", cookieString);
	}
	
	@Test
	public void parseBingoBoardFromCookieStringTest() {
		String cookieString = "0,EMPTY;2,EMPTY;4,EMPTY;6,EMPTY;8,EMPTY;10,EMPTY;12,EMPTY;14,EMPTY;16,EMPTY;18,EMPTY;20,EMPTY;22,EMPTY;24,EMPTY;26,EMPTY;28,EMPTY;30,EMPTY;32,EMPTY;34,EMPTY;36,EMPTY;38,EMPTY;40,EMPTY;42,EMPTY;44,EMPTY;46,EMPTY;48,EMPTY";
		
		List<BingoSquare> bingoBoard = AbstractBingoController.parseBingoBoardFromCookieString(cookieString);
		
		Assert.assertEquals(25, bingoBoard.size());
		Assert.assertEquals(8, bingoBoard.get(4).getContentId());
		Assert.assertEquals(MarkerType.EMPTY, bingoBoard.get(4).getMarker());
	}
	

}
