package br.com.lf.brasileiraoapi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.lf.brasileiraoapi.dto.PartidaGoogleDTO;
import br.com.lf.brasileiraoapi.entity.Partida;
import br.com.lf.brasileiraoapi.util.ScrapingUtil;
import br.com.lf.brasileiraoapi.util.StatusPartida;

@Service
public class ScrapingService {

	@Autowired
	private ScrapingUtil scrapingUtil;
	
	@Autowired
	private PartidaService partidaService;
	
	public void verificaPartidaPeriodo() {
		Integer quantidadePartida = partidaService.buscarQuantidadePartidasPeriodo();
		
		if (quantidadePartida > 0) {
			List<Partida> partidas = partidaService.listarPartidasPeriodo();
			
			partidas.forEach(partida -> {
				String urlPartida = scrapingUtil.montaUrlGoogle(
						partida.getEquipeCasa().getNomeEquipe(),
						partida.getEquipeVisitante().getNomeEquipe());
				
				PartidaGoogleDTO partidaGoogle = scrapingUtil.obtemInformacoesPartida(urlPartida);
				
				partidaService.atualizaPartida(partida, partidaGoogle);

			});
		}
	}
}