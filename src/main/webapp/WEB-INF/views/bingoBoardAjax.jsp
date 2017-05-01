<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>BingoLand</title>
<script src="resources/js/bingoboard2.js" type="text/javascript"></script>
<link href="resources/css/bingoboard.css" rel="stylesheet" type="text/css"></link>
</head>
<body>

	<div id="editSquare" class="overlay">
		<div class="overlay-content">
			<div id="squareDescription">Wacky Amazing Description of what
				this square means</div>
			<br />
			<button type="button" id="fillButton"
				onclick="document.getElementById('circlemark0').className = 'markerFILLED';">Mark</button>
			<button type="button" id="partialButton"
				onclick="document.getElementById('circlemark0').className = 'markerPARTIAL';">In
				Progress</button>
			<button type="button" id="emptyButton"
				onclick="document.getElementById('circlemark0').className = 'markerEMPTY';">Clear</button>
			<button type="button" id="closeButton" onclick="closeEditSquare()">Back</button>
		</div>
	</div>

	<div id="fileBoard" class="menutab">
		<div class="overlay-content">
			<button type="button" id="clearBoard" onclick="clearAllMarkers()">Clear
				markers</button>
				<br/>
			<button type="button" id="generateNewBoard" onclick="generateNewBoard()">Generate
				new board</button>
				<br/>
			<button type="button" id="saveButton" onclick="openSaveBoard()">Save</button>
			<br/>
			<button type="button" id="loadButton" onclick="openLoadBoard()">Load</button>
		</div>
	</div>

	<div id="saveOperation" class="overlay">
		<div class="overlay-content">
			<div id="saveDescription">Save the current table.</div>
			<br /> Board name:<br> <input type="text" id="boardnameSave"
				value=""> <br> Password:<br> <input type="password"
				id="passwordSave" value=""> <br>
			<br> <input type="submit" onclick="submitSave()" type="button"
				value="Submit">
				<button type="button" id="closeButton" onclick="closeSaveBoard()">Back</button>
		</div>
	</div>

	<div id="loadOperation" class="overlay">
		<div class="overlay-content">
			<div id="saveDescription">Load a saved table.</div>
			<br /> Board name:<br> <input type="text" id="boardnameLoad"
				value=""> <br> Password:<br> <input type="password"
				id="passwordLoad" value=""> <br>
			<br> <input type="submit" onclick="submitLoad()" type="button"
				value="Submit">
				<button type="button" id="closeButton" onclick="closeLoadBoard()">Back</button>
		</div>
	</div>

	<div id="gameNotification" class="overlay">
		<div class="overlay-content">
			<div id="overlay-message">You did a BINGO!</div>
			<br />
			<button type="button" id="closeButton"
				onclick="closeGameNotification()">Back</button>
		</div>
	</div>

	<div class="board">
		<p class="boardtitle">BINGO</p>
		<div id="boardinstance" class="boardgrid">
			<c:forEach items="${bingosquares}" var="squareinstance"
				varStatus="status">
				<div class="square" id="square${status.index}"
					onclick="openEditSquare(${status.index})">
					<div style='position: absolute' id="circlemark${status.index}"
						class="marker${squareinstance.marker}">
						<div id="squaretitle${status.index}" class="fittext">${squareinstance.content}</div>
					</div>
				</div>
			</c:forEach>
		</div>
	</div>

</body>

</html>