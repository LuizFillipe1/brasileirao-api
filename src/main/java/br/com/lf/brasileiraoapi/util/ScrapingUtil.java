package br.com.lf.brasileiraoapi.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.lf.brasileiraoapi.dto.PartidaGoogleDTO;

public class ScrapingUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScrapingUtil.class);
	private static final String BASE_URL_GOOGLE= "https://www.google.com/search?q=";
	private static final String COMPLEMENTO_URL_GOOGLE = "&hl=pt-BR";
	private static final String CASA = "casa";
	private static final String VISITANTE = "visitante";
	private static final String ITEM_GOL = "div[class=imso_gs__gs-r]";
	private static final String DIV_GOLS_CASA = "div[class=imso_gs__tgs imso_gs__left-team]";
	private static final String DIV_GOLS_VISITANTE = "div[class=imso_gs__tgs imso_gs__right-team]";
	private static final String DIV_PENALIDADES = "div[class=imso_mh_s__psn-sc]";
	private static final String DIV_PLACAR_CASA = "div[class=imso_mh__l-tm-sc imso_mh__scr-it imso-light-font]";
	private static final String DIV_PLACAR_VISITANTE = "div[class=imso_mh__r-tm-sc imso_mh__scr-it imso-light-font]";
	private static final String DIV_NOME_CASA = "div[class=imso_mh__first-tn-ed imso_mh__tnal-cont imso-tnol]";
	private static final String DIV_NOME_VISITANTE = "div[class=imso_mh__second-tn-ed imso_mh__tnal-cont imso-tnol]";
	private static final String DIV_PARTIDA_ANDAMENTO = "div[class=imso_mh__lv-m-stts-cont]";
	private static final String DIV_PARTIDA_ENCERRADA = "span[class=imso_mh__ft-mtch imso-medium-font imso_mh__ft-mtchc]";
	private static final String SPAN = "span";
	private static final String PENALTIS = "Pênaltis";
		
	public PartidaGoogleDTO obtemInformacoesPartida(String url) {
			PartidaGoogleDTO partida = new PartidaGoogleDTO();
			Document document = null;
			
			try {
				document = Jsoup.connect(url).get();
				
				String title = document.title();
				LOGGER.info("Titulo da pagina: {}", title);
				
				StatusPartida statusPartida = obtemStatusPartida(document);
				partida.setStatusPartida(statusPartida.toString());
				LOGGER.info("Status partida: {}",statusPartida);
				
				if (statusPartida != StatusPartida.PARTIDA_NAO_INICIADA) {
					
				String tempoPartida = obtemTempoPartida(document);
				partida.setTempoPartida(tempoPartida);
				LOGGER.info("Tempo partida: {}", tempoPartida);
				
				Integer placarEquipeCasa = recuperaPlacarEquipe(document, DIV_PLACAR_CASA);
				partida.setPlacarEquipeCasa(placarEquipeCasa);
				LOGGER.info("Placar equipe casa: {}",placarEquipeCasa);
				
				Integer placarEquipeVisitante = recuperaPlacarEquipe(document, DIV_PLACAR_VISITANTE);
				partida.setPlacarEquipeVisitante(placarEquipeVisitante);
				LOGGER.info("Placar equipe visitante: {}",placarEquipeVisitante);
				
				String golsEquipeCasa = recuperaGolsEquipe(document, DIV_GOLS_CASA);
				partida.setGolsEquipeCasa(golsEquipeCasa);
				LOGGER.info("Gols equipe casa: {} ",golsEquipeCasa);
				
				String golsEquipeVisitante = recuperaGolsEquipe(document, DIV_GOLS_VISITANTE);
				partida.setGolsEquipeVisitante(golsEquipeVisitante);
				LOGGER.info("Gols equipe casa: {} ",golsEquipeVisitante);
				
				
				Integer placarEstendidoEquepeCasa = buscaPenalidades(document,CASA);
				partida.setPlacarEstendidoEquipeCasa(placarEstendidoEquepeCasa);
				LOGGER.info("Placar estendido equipe casa: {}", placarEstendidoEquepeCasa);
				
				Integer placarEstendidoEquepeVisitante = buscaPenalidades(document, VISITANTE);
				partida.setPlacarEstendidoEquipeVisitante(placarEstendidoEquepeVisitante);
				LOGGER.info("Placar estendido equipe visitante: {}", placarEstendidoEquepeVisitante);
				
				}
				
				String nomeTimeCasa = retornaNomeTime(document, DIV_NOME_CASA);
				partida.setNomeEquipeCasa(nomeTimeCasa);
				LOGGER.info("Nome equipe casa: {}", nomeTimeCasa);
				
				String nomeTimeVisitante = retornaNomeTime(document, DIV_NOME_VISITANTE);
				partida.setNomeEquipeVisitante(nomeTimeVisitante);
				LOGGER.info("Nome equipe Visitante: {}", nomeTimeVisitante);
				
				//String urlLogoEquipeCasa = retornaLogoEquipeCasa(document);
				//LOGGER.info("URL logo equipe casa: " + urlLogoEquipeCasa);
				return partida;
				
			} catch (IOException e) {
				LOGGER.error("ERRO AO TENTAR CONECTAR NO GOOGLE COM JSOUP -> {}", e.getMessage());
			}
				
			return null;
	}
	
	public StatusPartida obtemStatusPartida(Document document) {
		
		//Situações
		//1 - partida nao iniciada
		//2 - partida iniciada
		//3 - partida encerrada
		//4 - partida penaltis 
		StatusPartida statusPartida = StatusPartida.PARTIDA_NAO_INICIADA;
		
		boolean isTempoPartida = document.select(DIV_PARTIDA_ANDAMENTO).isEmpty();
		if (!isTempoPartida) {
			String tempoPartida = document.select(DIV_PARTIDA_ANDAMENTO).first().text();
			statusPartida = StatusPartida.PARTIDA_EM_ANDAMENTO;
			
			if (tempoPartida.contains(PENALTIS)) {
				statusPartida = StatusPartida.PARTIDA_PENALTIS;
			}
		}
		isTempoPartida = document.select(DIV_PARTIDA_ENCERRADA).isEmpty();
		
		if (!isTempoPartida) {
			statusPartida = StatusPartida.PARTIDA_ENCERRADA;
		}
		return statusPartida;
		
	}
	
	public String obtemTempoPartida(Document document) {
		// jogo rolando ou intervalo ou penalidades
		String tempoPartida = null;
		
		boolean isTempoPartida = document.select(DIV_PARTIDA_ANDAMENTO).isEmpty();
		if (!isTempoPartida) {
			tempoPartida = document.select(DIV_PARTIDA_ANDAMENTO).first().text();
		}
		isTempoPartida = document.select(DIV_PARTIDA_ENCERRADA).isEmpty();
		if (!isTempoPartida) {
			tempoPartida = document.select(DIV_PARTIDA_ENCERRADA).first().text();
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
	
	public String retornaNomeTime(Document document, String itemHtml) {
		
		Element elemento = document.selectFirst(itemHtml);
		String nomeEquipe = elemento.select(SPAN).text();
		
		return nomeEquipe;
	}
	
	/*
	//Nao funcional
	public String retornaLogoEquipeCasa(Document document){
		Element elemento = document.selectFirst("div[class=imso_mh__first-tn-ed imso_mh__tnal-cont imso-tnol]");
		String urlLogo = "https:" + elemento.select("img[class=imso_btl__mh-logo]").attr("src");
		return urlLogo;
		
	}*/
	
	public Integer recuperaPlacarEquipe(Document document, String itemHtml){
			String placarEquipe = document.selectFirst(itemHtml).text();
			return formataPlacarStringInteger(placarEquipe);
		}

	public String recuperaGolsEquipe(Document document, String itemHtml) {
		
		List<String> golsEquipe = new ArrayList<>();
		Elements elementos = document.select(itemHtml).select(ITEM_GOL);
		for (Element e : elementos) {
			String infoGol = e.select(ITEM_GOL).text();
			golsEquipe.add(infoGol);
		}
		return String.join(", ", golsEquipe);
	}
	
	public Integer buscaPenalidades(Document document,String tipoEquipe) {
		
		boolean isPenalidades = document.select(DIV_PENALIDADES).isEmpty();
		if (!isPenalidades) {
			String penalidades = document.select(DIV_PENALIDADES).text();
			String penalidadesCompleta = penalidades.substring(0,5).replace(" ", "");
			String[] divisao = penalidadesCompleta.split("-");
			
			return tipoEquipe.equals(CASA) ? formataPlacarStringInteger(divisao[0]) : formataPlacarStringInteger(divisao[1]);
		}		
		return null;
	}
	
	public Integer formataPlacarStringInteger(String placar) {
		Integer valor;
		try {
			valor = Integer.parseInt(placar);
		} catch (Exception e) {
			valor = 0;	
		}
		return valor;
	}
	
	public String montaUrlGoogle(String nomeEquipeCasa, String nomeEquipeVisitante) {
		
		try {
			String equipeCasa = nomeEquipeCasa.replace(" ", "+").replace("-", "+");
			String equipeVisitante = nomeEquipeVisitante.replace(" ", "+").replace("-", "+");
			
			return BASE_URL_GOOGLE +equipeCasa + "+x+" + equipeVisitante + COMPLEMENTO_URL_GOOGLE;
		} catch (Exception e) {
			
			LOGGER.error("ERRO: {}", e.getMessage());
		}
		
		
		return null;
	}
}
