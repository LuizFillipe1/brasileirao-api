package br.com.lf.brasileiraoapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.lf.brasileiraoapi.entity.Partida;

@Repository
public interface PartidaRepository extends JpaRepository<Partida, Long>{

}
