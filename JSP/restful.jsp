<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<html>
<head>
<title>Sample Application JSP Page</title>
</head>
<body bgcolor=white>

<table border="0">
<tr>
<td>
<h1>Sample Restful IA Application</h1>
</td>
</tr>
</table>
<FORM ACTION="restful.jsp" METHOD="POST">
            Enter Patient ID:
            <INPUT TYPE="TEXT" NAME="criterion">
            <INPUT TYPE="SUBMIT" value="Submit">
        </FORM>
<br>

<%@ page import="RestfulSampleNoGUI" %>

<%
RestfulSampleNoGUI test = new RestfulSampleNoGUI ( );
String absoluteDiskPath = getServletContext().getRealPath("/");
String results = test.executeStaticQuery ( ( request.getParameter ( "criterion" ) != null ? request.getParameter ( "criterion" ) : null ), absoluteDiskPath );
results = results.replaceAll ( "@FB@", "<td>" );
results = results.replaceAll ( "@FE@", "</td>" );
results = results.replaceAll ( "@LB@", "<tr>" );
results = results.replaceAll ( "@LE@", "</tr>" );




out.print ( "<table border='1' cellpadding='10'>" );
out.print ( "<tr><td><b>ID Paciente</b></td><td><b>Nombre</b></td><td><b>Apellidos</b></td><td><b>ID Fichero</b></td></tr>" );
out.print ( results  );
out.print ( "</table>" );

%>
</body>
</html>
