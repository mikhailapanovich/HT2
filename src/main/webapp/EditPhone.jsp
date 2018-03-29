<?xml version="1.0" encoding="UTF-8" ?>
<%@ page import="app.Person"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.HashMap"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Редактирование номера телефона</title>
</head>
<body>

<%
	HashMap<String,String> jsp_parameters = new HashMap<String,String>();
	Person person = new Person();
	String phone_id = request.getParameter("phone_id");
	String phone_number = "";
	String error_message = "";
	String user_message = "";

	if (request.getAttribute("jsp_parameters") != null)	{
		jsp_parameters = (HashMap<String,String>)request.getAttribute("jsp_parameters");
	}

	if (request.getAttribute("person") != null) {
		person = (Person)request.getAttribute("person");
	}
	
	if (request.getAttribute("phone_number") != null) {
		phone_number = (String)request.getAttribute("phone_number");
	}
	
	error_message = jsp_parameters.get("error_message");
	user_message = jsp_parameters.get("current_action_result_label");
%>

<form action="<%= request.getContextPath() %>/" method="post">
<input type="hidden" name="phone_id" value="<%= phone_id %>"/>
<input type="hidden" name="action" value="<%= jsp_parameters.get("next_action") %>"/>
<table align="center" border="1" width="70%">

<%  if ((error_message != null)&&(!error_message.equals(""))) { %>
  <tr>
    <td colspan="2" align="center"><span style="color:red"><%= error_message %></span></td>
  </tr>
<%  } %>

<%	if ((user_message != null)&&(!user_message.equals(""))) { %>
  <tr>
    <td colspan="2" align="center"><span style="color:red"><%=user_message%></span></td>
  </tr>
<%  } %>

  <tr>
    <td colspan="2" align="center">
      Информация о телефоне владельца:
      <%= person.getSurname() %>
      <%= person.getName( )%>
      <%= person.getMiddlename() %>
    </td>
  </tr>
  <tr>
    <td>Номер:</td>
    <td><input type="text" name="phone_number" value="<%= phone_number %>"/></td>
  </tr>
  <tr>
    <td colspan="2" align="center">
      <input type="submit" value="<%= jsp_parameters.get("next_action_label") %>" /><br />
      <a href="<%= request.getContextPath() %>/?action=edit">Вернуться к данным о человеке</a>
    </td>
  </tr> 
</table>
</form>
</body>
</html>