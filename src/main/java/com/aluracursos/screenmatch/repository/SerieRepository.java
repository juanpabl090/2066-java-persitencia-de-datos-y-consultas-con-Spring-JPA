package com.aluracursos.screenmatch.repository;

import com.aluracursos.screenmatch.dto.EpisodioDTO;
import com.aluracursos.screenmatch.model.Categoria;
import com.aluracursos.screenmatch.model.Episodio;
import com.aluracursos.screenmatch.model.Serie;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SerieRepository extends JpaRepository<Serie, Long> {
    Optional<Serie> findByTituloContainsIgnoreCase(String nombreSerie);

    List<Serie> findTop5ByOrderByEvaluacionDesc();

    List<Serie> findByGenero(Categoria categoria);

    List<Serie> findBytotalTemporadasAndEvaluacion(Integer numeroDeTemporadas, Double EvaluacionSerie);

    @Query("SELECT s FROM Serie s JOIN s.episodios e GROUP BY s ORDER BY MAX(e.fechaDeLanzamiento) DESC")
    List<Serie> lanzamientosMasRecientes(Pageable pageable);

    @Query("SELECT e FROM Serie s JOIN s.episodios e where e.titulo ILIKE %:nombreEpisodio%")
    List<Episodio> episodiosPorNombre(String nombreEpisodio);

    @Query("SELECT e FROM Serie s JOIN s.episodios e where s = :serie ORDER BY e.evaluacion DESC LIMIT 5")
    List<Episodio> top5Episodios(Serie serie);

    @Query("SELECT new com.aluracursos.screenmatch.dto.EpisodioDTO(e.temporada, e.titulo, e.numeroEpisodio) FROM Serie s JOIN s.episodios e WHERE s.id = :id AND e.temporada = :numeroTemporada")
    List<EpisodioDTO> obtenerTemporadasPorNumero(@Param("id") Long id, @Param("numeroTemporada") Long numeroTemporada);
}