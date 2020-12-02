package asr.proyectoFinal.servlets;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.Buffer;
import java.nio.file.Files;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.cloud.sdk.core.security.Authenticator;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.language_translator.v3.LanguageTranslator;
import com.ibm.watson.language_translator.v3.model.TranslateOptions;
import com.ibm.watson.language_translator.v3.model.TranslationResult;

import asr.proyectoFinal.dao.CloudantPalabraStore;
import asr.proyectoFinal.dominio.Palabra;

import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.SynthesizeOptions;
import com.ibm.watson.developer_cloud.text_to_speech.v1.util.WaveUtils;


/**
 * Servlet implementation class Controller
 */
@WebServlet(urlPatterns = {"/listar", "/insertar", "/hablar"})
public class Controller extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		
		PrintWriter out = response.getWriter();
		out.println("<html><head><meta charset=\"UTF-8\"></head><body>");
		
		CloudantPalabraStore store = new CloudantPalabraStore();
		System.out.println(request.getServletPath());
		switch(request.getServletPath())
		{
			case "/listar":
				if(store.getDB() == null)
					  out.println("No hay DB");
				else
					out.println("Palabras en la BD Cloudant:<br />" + store.getAll());
				break;
				
			case "/insertar":
				Palabra palabra = new Palabra();
				String parametro = request.getParameter("palabra");

				if(parametro==null)
				{
					out.println("usage: /insertar?palabra=palabra_a_traducir");
				}
				else
				{
					if(store.getDB() == null) 
					{
						out.println(String.format("Palabra: %s", palabra));
					}
					else
					{	
						//palabra.setName(parametro);
						palabra.setName(translate(parametro,"es","en",false));
						store.persist(palabra);
					    out.println(String.format("Almacenada la palabra: %s", palabra.getName()));
					    text2speech("1");
					}
				}
				break;
		}
		out.println("</html>");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	public static void text2speech(String args) {
		//String[] args
	     TextToSpeech textToSpeech = new TextToSpeech();
	     textToSpeech.setApiKey("YBK6xNhS-bPWPp_7A7wy72Sf1mVZ9s8orvua5GL3ZxHL");
	     textToSpeech.setEndPoint("https://api.eu-gb.text-to-speech.watson.cloud.ibm.com/instances/ce925c92-73a4-4f92-82a1-f89f9016e6da");
	     //textToSpeech.setUsernameAndPassword(username, password);

	     try {
	       SynthesizeOptions synthesizeOptions =
	       new SynthesizeOptions.Builder()
	         .text("Hello World!")
	         .accept("audio/wav")
	         .voice("en-US_AllisonVoice")
	         .build();

	       InputStream inputStream =
	       textToSpeech.synthesize(synthesizeOptions).execute();
	       InputStream in = WaveUtils.reWriteWaveHeader(inputStream);

	       OutputStream out = new FileOutputStream("test.wav");
	       byte[] buffer = new byte[1024];
	       int length;
	       while ((length = in.read(buffer)) > 0) {
	       out.write(buffer, 0, length);
	       }

	       out.close();
	       in.close();
	       inputStream.close();
	      } catch (IOException e) {
	        e.printStackTrace();
	      }

	   }
	

	
	public static String translate(String palabra, String sourceModel, String destModel,
			boolean conversational)
			{
			String model;
			if(sourceModel.equals("en") || sourceModel.equals("es") ||
			destModel.equals("en") || destModel.equals("es"))
			{
			model=sourceModel+"-"+destModel;
			if(conversational)
			model+="-conversational";
			}
			else
			model="en-es";
			//Authenticator authenticator = new IamAuthenticator("sGBqIGkLecdsa4RdsA3imHp_lvb7MMlZNzdasq-PgkmCXdsf59P0");
			Authenticator authenticator = new IamAuthenticator("i-fU9PnyEySAPFpRbyU6U5Rg29LfAubOHzXeQ281bAn5");
			LanguageTranslator languageTranslator = new LanguageTranslator("2018-05-01",
			authenticator);
			//languageTranslator.setServiceUrl("https://gatewaylon.watsonplatform.net/language-translator/api");
			languageTranslator.setServiceUrl("https://api.eu-gb.language-translator.watson.cloud.ibm.com/instances/b741ba34-2c06-4fff-947e-ab347657e65e");
			TranslateOptions translateOptions = new TranslateOptions.Builder()
			 .addText(palabra)
			 .modelId(model)
			 .build();
			TranslationResult translationResult =
			languageTranslator.translate(translateOptions).execute().getResult();
			System.out.println(translationResult);
			String traduccionJSON = translationResult.toString();
			JsonObject rootObj = JsonParser.parseString(traduccionJSON).getAsJsonObject();
			JsonArray traducciones = rootObj.getAsJsonArray("translations");
			String traduccionPrimera = palabra;
			if(traducciones.size()>0)
			traduccionPrimera =
			traducciones.get(0).getAsJsonObject().get("translation").getAsString();
			return traduccionPrimera;
			}

}
