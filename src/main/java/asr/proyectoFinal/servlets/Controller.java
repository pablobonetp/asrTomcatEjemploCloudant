package asr.proyectoFinal.servlets;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

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
import com.ibm.watson.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.text_to_speech.v1.model.SynthesizeOptions;

import asr.proyectoFinal.dao.CloudantPalabraStore;
import asr.proyectoFinal.dominio.Palabra;

import com.ibm.cloud.sdk.core.http.HttpMediaType;
import com.ibm.cloud.sdk.core.security.Authenticator;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.text_to_speech.v1.model.DeleteUserDataOptions;
import com.ibm.watson.text_to_speech.v1.model.GetPronunciationOptions;
import com.ibm.watson.text_to_speech.v1.model.GetVoiceOptions;
import com.ibm.watson.text_to_speech.v1.model.MarkTiming;
import com.ibm.watson.text_to_speech.v1.model.Marks;
import com.ibm.watson.text_to_speech.v1.model.Pronunciation;
import com.ibm.watson.text_to_speech.v1.model.SynthesizeOptions;
import com.ibm.watson.text_to_speech.v1.model.Timings;
import com.ibm.watson.text_to_speech.v1.model.Voice;
import com.ibm.watson.text_to_speech.v1.model.Voices;
import com.ibm.watson.text_to_speech.v1.model.WordTiming;
import com.ibm.watson.text_to_speech.v1.util.WaveUtils;
import com.ibm.watson.text_to_speech.v1.websocket.BaseSynthesizeCallback;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;




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
					}
				}
				break;
				case "/text2speech":
				try {
					text2speech();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
					out.println(String.format("Almacenada la palabra"));
					//out.print(s);
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
	
	public static void text2speech() throws InterruptedException, IOException
	{
		Authenticator authenticator = new IamAuthenticator("UBByl754umLk3bOp81b-A0k5_Qqla5mXI7xr8BikwLKB");
		TextToSpeech service = new TextToSpeech(authenticator);

		String text = "It's beginning to look a lot like Christmas";
		SynthesizeOptions synthesizeOptions = new SynthesizeOptions.Builder()
		  .text(text)
		  //.accept(SynthesizeOptions.Accept.AUDIO_OGG_CODECS_OPUS)
		  .accept(HttpMediaType.AUDIO_WAV)
		  .build();

		// a callback is defined to handle certain events, like an audio transmission or a timing marker
		// in this case, we'll build up a byte array of all the received bytes to build the resulting file
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		service.synthesizeUsingWebSocket(synthesizeOptions, new BaseSynthesizeCallback() {
		  @Override
		  public void onAudioStream(byte[] bytes) {
		    // append to our byte array
		    try {
		      byteArrayOutputStream.write(bytes);
		    } catch (IOException e) {
		      e.printStackTrace();
		    }
		  }
		});

		// quick way to wait for synthesis to complete, since synthesizeUsingWebSocket() runs asynchronously
		Thread.sleep(5000);

		// create file with audio data
		String filename = "synthesize_websocket_test.ogg";
		OutputStream fileOutputStream = new FileOutputStream(filename);
		byteArrayOutputStream.writeTo(fileOutputStream);

		// clean up
		byteArrayOutputStream.close();
		fileOutputStream.close();
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
