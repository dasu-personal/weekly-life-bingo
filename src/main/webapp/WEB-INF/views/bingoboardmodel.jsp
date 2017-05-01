<%@ page language="java" contentType="text/xml; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<board>
<c:forEach items="${bingosquares}"
	var="squareinstance" varStatus="status">
	<square>
		<title>${squareinstance.content}</title>
		<status>${squareinstance.marker}</status>
		<description>${squareinstance.description}</description>
	</square>
</c:forEach>
<c:if test="${bingoannounce != null}">
	<announce>${bingoannounce}</announce>
</c:if>
</board>
