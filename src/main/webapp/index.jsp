<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Proyecto ASR new...</title>
</head>
<body>
<h1>Ejemplo de Proyecto de ASR con Cloudant ahora con DevOps (con DevOps):</h1>
<hr />
<p>Opciones sobre la base de datos Cloudant de Pablo Bonet </p>
<ul>
<li><a href="listar">Listar</a></li>
<form action="insertar" method="post">
<li>Palabra en espa�ol:
<input type="text" id="palabra" name="palabra">
<button type="submit">Guardar en Cloudant</button></li>
</form>
<form action="text2speech" method="post">
<li>Texto: 
<input type="text" id="texto" name="texto">
<button type="submit">Convertir</button></li>
</form>
</ul>
</body>
</html>