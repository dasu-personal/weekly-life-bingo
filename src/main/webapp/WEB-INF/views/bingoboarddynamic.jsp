<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>BingoLand</title>
<script>
// TODO implement this on pho
function generateSingleSquare() {
var createSquare = document.createElement("div")
createSquare.className="square"
createSquare.setAttribute("class", "square");
createSquare.setAttribute("margin","auto");
document.getElementById("boardinstance").appendChild(createSquare);
}
// TODO implement this on php
function generateBoard(){
for (var i=0; i<25; i++){
 generateSingleSquare();
}
}
</script>
<style>
.boardtitle {
    font-size: 300px;
    color: white;
padding-top:0;
height:0px;
text-align:center;
}
.board {
margin: auto;
background-color: red;
width: 1050px;
height: 2000px;
border-radius: 20px;
padding: 100px;

}
.boardgrid {

width: 1000px;
height: 1000px;
background-color: white;
border-radius: 20px;
color: black;
margin: auto;

}
.square {
width: 198px;
height: 198px;
border: 1px black solid;
float: left;
}

</style>

</head>
<body>
<div class="board">
<p  class="boardtitle">BINGO</p>
<div id="boardinstance" class="boardgrid">
<c:forEach items="${bingosquares}" var="squareinstance" varStatus="status">
  <div class="square" id="square${status.index}">${squareinstance.content}
  <c:if test="${squareinstance.marker == 'FILLED'}">
  <svg height="200" width="200" style="position:relative;top: 0px; left:0px;" >
  <circle cx="100" cy="100" r="100" stroke="black" stroke-width="3" fill="red" />
  </c:if>
</svg>
</div>
</c:forEach>
</div></div>
</body>
</html>