<%@page contentType="text/html" pageEncoding="UTF-8" %>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
    <title><fmt:message key="title.manager"/></title>
</head>
<body>

<h1><fmt:message key="title.reportlist"/></h1>

<ul>
    <li><a target="_blank" href="<c:url value='/managertools/report/1'/>"><fmt:message key="title.report01"/></a></li>
    <li><a target="_blank" href="<c:url value='/managertools/report/2'/>"><fmt:message key="title.report02"/></a></li>
    <li><a target="_blank" href="<c:url value='/managertools/report/3'/>"><fmt:message key="title.report03"/></a></li>
    <li><a target="_blank" href="<c:url value='/managertools/report/4'/>"><fmt:message key="title.report04"/></a></li>
    <li><a target="_blank" href="<c:url value='/managertools/report/5'/>"><fmt:message key="title.report05"/></a></li>
    <li><a target="_blank" href="<c:url value='/managertools/report/6'/>"><fmt:message key="title.report06"/></a></li>
</ul>
<br/>
<br/>
<c:if test="${pentahoUrl != null}">
	<a target="_blank" href=${pentahoUrl}>Отчеты в Pentaho</a>
</c:if>
</body>
</html>