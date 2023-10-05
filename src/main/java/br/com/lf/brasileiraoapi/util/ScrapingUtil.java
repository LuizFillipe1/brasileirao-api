package br.com.lf.brasileiraoapi.util;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.lf.brasileiraoapi.dto.PartidaGoogleDTO;

public class ScrapingUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScrapingUtil.class);
	private static final String BASE_URL_GOOGLE= "https://www.google.com/search?q=";
	private static final String COMPLEMENTO_URL_GOOGLE = "&hl=pt-BR";
	
	public static void main(String[] args) {
		String url = BASE_URL_GOOGLE + "flamengo+vs+corinthians" + COMPLEMENTO_URL_GOOGLE;
		
		ScrapingUtil scraping = new ScrapingUtil();
		scraping.obtemInformacoesPartida(url);

	}
	public PartidaGoogleDTO obtemInformacoesPartida(String url) {
			PartidaGoogleDTO partida = new PartidaGoogleDTO();
			
			Document document = null;
			
			try {
				document = Jsoup.connect(url).get();
				
				String title = document.title();
				LOGGER.info("Titulo da pagina: {}", title);
				
				
				StatusPartida statusPartida = obtemStatusPartida(document);
				LOGGER.info("Status partida: {}",statusPartida);
				
				if (statusPartida != StatusPartida.PARTIDA_NAO_INICIADA) {
				
				
		
				String tempoPartida = obtemTempoPartida(document);
				LOGGER.info("Tempo partida: {}", tempoPartida);
				
				}
				
				String nomeTimeCasa = retornaNomeTimeCasa(document);
				LOGGER.info("Nome equipe casa: {}", nomeTimeCasa);
				
				String nomeTimeVisitante = retornaNomeTimeVisitante(document);
				LOGGER.info("Nome equipe Visitante: {}", nomeTimeVisitante);
				
				//String urlLogoEquipeCasa = retornaLogoEquipeCasa(document);
				//LOGGER.info("URL logo equipe casa: " + urlLogoEquipeCasa);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error("ERRO AO TENTAR CONECTAR NO GOOGLE COM JSOUP -> {}", e.getMessage());
			}
			
			
			return partida;
		
	}
	
	public StatusPartida obtemStatusPartida(Document document) {
		
		//Situações
		//1 - partida nao iniciada
		//2 - partida iniciada
		//3 - partida encerrada
		//4 - partida penaltis 
		StatusPartida statusPartida = StatusPartida.PARTIDA_NAO_INICIADA;
		
		
		boolean isTempoPartida = document.select("div[class=imso_mh__lv-m-stts-cont]").isEmpty();
		if (!isTempoPartida) {
			String tempoPartida = document.select("div[class=imso_mh__lv-m-stts-cont]").first().text();
			statusPartida = StatusPartida.PARTIDA_EM_ANDAMENTO;
			
			
			if (tempoPartida.contains("Pênaltis")) {
				statusPartida = StatusPartida.PARTIDA_PENALTIS;
			}
			//LOGGER.info(tempoPartida);
		}
		
		isTempoPartida = document.select("span[class=imso_mh__ft-mtch imso-medium-font imso_mh__ft-mtchc]").isEmpty();
		
		if (!isTempoPartida) {
			statusPartida = StatusPartida.PARTIDA_ENCERRADA;
		}
		//LOGGER.info(statusPartida.toString());
		return statusPartida;
		
	}
	
	public String obtemTempoPartida(Document document) {
		// jogo rolando ou intervalo ou penalidades
		String tempoPartida = null;
		
		boolean isTempoPartida = document.select("div[class=imso_mh__lv-m-stts-cont]").isEmpty();
		if (!isTempoPartida) {
			tempoPartida = document.select("div[class=imso_mh__lv-m-stts-cont]").first().text();
		}
		isTempoPartida = document.select("span[class=imso_mh__ft-mtch imso-medium-font imso_mh__ft-mtchc]").isEmpty();
		if (!isTempoPartida) {
		 tempoPartida = document.select("span[class=imso_mh__ft-mtch imso-medium-font imso_mh__ft-mtchc]").first().text();
		}
		
	
		return corrigeTempoPartida(tempoPartida);
		
		
	}
	
	public String corrigeTempoPartida(String tempo) {
		
		if (tempo.contains("'")) {
			return tempo.replace(" ", "").replace("'"," min");
		} else {
			return tempo;
		}	
	}
	
	public String retornaNomeTimeCasa(Document document) {
		
		Element elemento = document.selectFirst("div[class=imso_mh__first-tn-ed imso_mh__tnal-cont imso-tnol]");
		String nomeEquipe = elemento.select("span").text();
		
		return nomeEquipe;
	}
	
	public String retornaNomeTimeVisitante(Document document) {
		
		Element elemento = document.selectFirst("div[class=imso_mh__second-tn-ed imso_mh__tnal-cont imso-tnol]");
		String nomeEquipe = elemento.select("span").text();
		
		return nomeEquipe;
	}
	/*
	//Nao funcional
	public String retornaLogoEquipeCasa(Document document){
		Element elemento = document.selectFirst("div[class=imso_mh__first-tn-ed imso_mh__tnal-cont imso-tnol]");
		String urlLogo = "https:" + elemento.select("img[class=imso_btl__mh-logo]").attr("src");
		return urlLogo;
		
	}*/

	
}
