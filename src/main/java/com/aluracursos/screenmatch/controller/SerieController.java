package com.aluracursos.screenmatch.controller;

import com.aluracursos.screenmatch.dto.EpisodioDTO;
import com.aluracursos.screenmatch.dto.SerieDto;
import com.aluracursos.screenmatch.model.Episodio;
import com.aluracursos.screenmatch.service.SerieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/series")
public class SerieController {
    @Autowired
    private SerieService serieService;

    @GetMapping
    public List<SerieDto> obtenerTodasLasSeries() {
        return serieService.obtenerTodasLasSeries();
    }

    @GetMapping("/top5")
    public List<SerieDto> obtenerTop5() {
        return serieService.obtenerTop5();
    }

    @GetMapping("/recents")
    public List<SerieDto> obtenerRecents() {
        return serieService.obtenerLanzamientosMasRecientes();
    }

    @GetMapping("/{id}")
    public SerieDto obtenerPorId(@PathVariable Long id) {
        return serieService.obtenerPorId(id);
    }

    @GetMapping("/{id}/temporadas/todas")
    public List<EpisodioDTO> obtenerTemporadas(@PathVariable Long id) {
        return serieService.obtenerTemporadas(id);
    }

    @GetMapping("/{id}/temporadas/{numeroTemporada}")
    public List<EpisodioDTO> obtenerTemporadasPorNumero(@PathVariable Long id, @PathVariable Long numeroTemporada) {
        return serieService.obtenerTemporadasPorNumero(id, numeroTemporada);
    }

    @GetMapping("/categoria/{nombreGenero}")
    public List<SerieDto> obtenerSeriesPorCategoria(@PathVariable String nombreGenero){
        return serieService.obtenerSeriesPorCategoria(nombreGenero);
    }
}