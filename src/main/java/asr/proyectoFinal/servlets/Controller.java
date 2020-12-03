package asr.proyectoFinal.servlets;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

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
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.EntitiesOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.Features;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.KeywordsOptions;



/**
 * Servlet implementation class Controller
 */
@WebServlet(urlPatterns = {"/listar", "/insertar", "/hablar", "/text2speech"})
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
					    //text2speech("1");
					    //text2speech();
					    
					}
				}
				case "/text2speech":
					//text2speech();
					//Palabra palabra1 = new Palabra();
					//String parametro1 = request.getParameter("palabra");
					//palabra1.setName(text2speech());
					String s = naturalunderstanding();
					//naturalunderstanding();
					out.println(String.format("Almacenada la palabra"));
					out.print(s);
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
	
	public static String naturalunderstanding() 
	{
		NaturalLanguageUnderstanding service = new NaturalLanguageUnderstanding("2018-03-16");
		service.setApiKey("feevSExoX_L5S3GLqmQybGWNnjUgcb7X6bYf1voK0oP9");
		
		String text = "IBM is an American multinational technology " +
			       "company headquartered in Armonk, New York, " +
			       "United States, with operations in over 170 countries.";
		
		EntitiesOptions entitiesOptions = new EntitiesOptions.Builder()
			       .emotion(true)
			       .sentiment(true)
			       .limit(2)
			       .build();
		return text;
	}
	
	
	public static void naturalunderstanding2() {
	
	/*NaturalLanguageUnderstanding service = new NaturalLanguageUnderstanding(
	           "2018-03-16",
	           "username",
	           "password"
	         );*/
	
	NaturalLanguageUnderstanding service = new NaturalLanguageUnderstanding("2018-03-16");
	service.setApiKey("feevSExoX_L5S3GLqmQybGWNnjUgcb7X6bYf1voK0oP9");
	

	     //The text we want to analyze. You can insert any other text you like.
	     String text = "IBM is an American multinational technology " +
	       "company headquartered in Armonk, New York, " +
	       "United States, with operations in over 170 countries.";

	     //Entities and keywords are parameters you get back from the service about your text.
	     EntitiesOptions entitiesOptions = new EntitiesOptions.Builder()
	       .emotion(true)
	       .sentiment(true)
	       .limit(2)
	       .build();

	     KeywordsOptions keywordsOptions = new KeywordsOptions.Builder()
	       .emotion(true)
	       .sentiment(true)
	       .limit(2)
	       .build();

	     Features features = new Features.Builder()
	       .entities(entitiesOptions)
	       .keywords(keywordsOptions)
	       .build();

	     AnalyzeOptions parameters = new AnalyzeOptions.Builder()
	       .text(text)
	       .features(features)
	       .build();

	     //Take the parameters and send them to your service for resutls.
	     AnalysisResults response = service
	       .analyze(parameters)
	       .execute();

	     //print the result
	     System.out.println(response);
	     //return response.toString();
	     
		}
	
	
	
	public static String text2speech() throws IOException
	{
		String p = "Puerta";
		TextToSpeech textToSpeech = new TextToSpeech();
		textToSpeech.setApiKey("_BY39GCbpkCbcW09LfMVbvluQtVYU2vDAQ5s1pP1ZKFL");
	    SynthesizeOptions synthesizeOptions =
	       new SynthesizeOptions.Builder()
	         .text("Hello World!")
	         .accept("audio/wav")
	         .voice("en-US_AllisonVoice")
	         .build();
	       InputStream inputStream =
	       textToSpeech.synthesize(synthesizeOptions).execute();
	       InputStream in = WaveUtils.reWriteWaveHeader(inputStream);

		return p;
	}
	public static String text2speech2() throws FileNotFoundException
	{	
		String p = "Puerta";
		TextToSpeech textToSpeech = new TextToSpeech();
		textToSpeech.setApiKey("_BY39GCbpkCbcW09LfMVbvluQtVYU2vDAQ5s1pP1ZKFL");
	     //textToSpeech.setUsernameAndPassword(username, password);
		OutputStream out = null;

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

	       out = new FileOutputStream("/Users/pablobonet/ASR/testasr.wav");
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
	     //return out;
	     return p;
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
