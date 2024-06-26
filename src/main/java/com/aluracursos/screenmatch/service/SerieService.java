package com.aluracursos.screenmatch.service;

import com.aluracursos.screenmatch.dto.EpisodioDTO;
import com.aluracursos.screenmatch.dto.SerieDto;
import com.aluracursos.screenmatch.model.Categoria;
import com.aluracursos.screenmatch.model.Episodio;
import com.aluracursos.screenmatch.model.Serie;
import com.aluracursos.screenmatch.repository.SerieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SerieService {

    @Autowired
    private SerieRepository serieRepository;

    public List<SerieDto> convierteDatos(List<Serie> serieAConvertir) {
        return serieAConvertir.stream()
                .map(serie -> new SerieDto(serie.getId(), serie.getTitulo(), serie.getTotalTemporadas(),
                        serie.getEvaluacion(), serie.getPoster(), serie.getGenero(), serie.getActores(), serie.getSinopsis()))
                .toList();
    }

    public List<SerieDto> obtenerTodasLasSeries() {
        return convierteDatos(serieRepository.findAll());
    }

    public List<SerieDto> obtenerTop5() {
        return convierteDatos(serieRepository.findTop5ByOrderByEvaluacionDesc());
    }

    public List<SerieDto> obtenerLanzamientosMasRecientes() {
        Pageable pageable = PageRequest.of(0, 5);
        return convierteDatos(serieRepository.lanzamientosMasRecientes(pageable));
    }

    public SerieDto obtenerPorId(Long id) {
        Optional<Serie> serie = serieRepository.findById(id);
        if (serie.isPresent()) {
            Serie serie1 = serie.get();
            return new SerieDto(serie1.getId(), serie1.getTitulo(), serie1.getTotalTemporadas(),
                    serie1.getEvaluacion(), serie1.getPoster(), serie1.getGenero(), serie1.getActores(), serie1.getSinopsis());
        } else {
            return null;
        }
    }

    public List<EpisodioDTO> obtenerTemporadas(Long id) {
        Optional<Serie> serie = serieRepository.findById(id);
        if (serie.isPresent()) {
            Serie serieS = serie.get();
            System.out.println("Serie encontrada: {}" + serieS);
            List<Episodio> episodios = serieS.getEpisodios();
            System.out.println("Episodios encontrados: {}" + episodios);
            return episodios.stream()
                    .map(episodio -> new EpisodioDTO(
                            episodio.getTemporada(), episodio.getTitulo(), episodio.getNumeroEpisodio()))
                    .toList();
        } else {
            System.out.println("Serie no encontrada con ID {}" + id);
        }
        return null;
    }

    public List<EpisodioDTO> obtenerTemporadasPorNumero(Long id, Long numeroTemporada) {
        return serieRepository.obtenerTemporadasPorNumero(id, numeroTemporada).stream()
                .map(episodio -> new EpisodioDTO(episodio.temporada(),episodio.titulo(),episodio.numeroEpisodio()))
                .toList();
    }

    public List<SerieDto> obtenerSeriesPorCategoria(String nombreGenero) {
        Categoria categoria = Categoria.fromSpanish(nombreGenero);
        return convierteDatos(serieRepository.findByGenero(categoria));
    }
}