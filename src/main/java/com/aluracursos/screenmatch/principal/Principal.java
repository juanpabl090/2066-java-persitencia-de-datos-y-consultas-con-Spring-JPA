package com.aluracursos.screenmatch.principal;

import com.aluracursos.screenmatch.model.*;
import com.aluracursos.screenmatch.repository.SerieRepository;
import com.aluracursos.screenmatch.service.ConsumoAPI;
import com.aluracursos.screenmatch.service.ConvierteDatos;

import java.util.*;

public class Principal {
    private final String URL_BASE = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=34889018";
    private final Scanner teclado = new Scanner(System.in);
    private final ConsumoAPI consumoApi = new ConsumoAPI();
    private final ConvierteDatos conversor = new ConvierteDatos();
    private final List<DatosSerie> datosSerie = new ArrayList<>();
    private final SerieRepository serieRepository;
    private List<Serie> series;

    public Principal(SerieRepository serieRepository) {
        this.serieRepository = serieRepository;
    }

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                     1 - Buscar series\s
                     2 - Buscar episodios
                     3 - Mostrar series buscadas
                     4 - Mostrar series por titulo
                     5 - Mostrar top 5 series mejor evaluadas
                     \s
                     0 - Salir
                    \s""";
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    mostrarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarTop5Series();
                    break;
                case 6:
                    buscarSeriePorCategoria();
                    break;
                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida");
            }
        }

    }

    private DatosSerie getDatosSerie() {
        System.out.println("Escribe el nombre de la serie que deseas buscar");
        var nombreSerie = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + nombreSerie.replace(" ", "+") + API_KEY);
        System.out.println(json);
        return conversor.obtenerDatos(json, DatosSerie.class);
    }

    private void buscarEpisodioPorSerie() {
        mostrarSeriesBuscadas();
        System.out.println("Escribe el nombre de la serie que quieres el episodio");
        var nombreSerie = teclado.nextLine();
        teclado.nextLine();
        Optional<Serie> serie = series.stream()
                .filter(s -> s.getTitulo().toLowerCase().contains(nombreSerie.toLowerCase()))
                .findFirst();

        if (serie.isPresent()) {
            var serieEncontrada = serie.get();
            List<DatosTemporadas> temporadas = new ArrayList<>();
            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumoApi.obtenerDatos(URL_BASE + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DatosTemporadas datosTemporada = conversor.obtenerDatos(json, DatosTemporadas.class);
                temporadas.add(datosTemporada);
            }
            temporadas.forEach(System.out::println);
            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios()
                            .stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .toList();

            serieEncontrada.setEpisodios(episodios);
            serieRepository.save(serieEncontrada);
        }
    }

    private void buscarSerieWeb() {
        DatosSerie datos = getDatosSerie();
        Serie serie = new Serie(datos);
        serieRepository.save(serie);
        System.out.println(datos);
    }

    private void mostrarSeriesBuscadas() {
        series = serieRepository.findAll();

        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscarSeriePorTitulo() {
        System.out.println("Escribe el nombre de la serie que deseas buscar");
        var nombreSerie = teclado.nextLine();

        Optional<Serie> serieBuscada = serieRepository.findByTituloContainsIgnoreCase(nombreSerie);

        if (serieBuscada.isPresent()) {
            System.out.println("La serie buscada es:" + serieBuscada.get());
        } else {
            System.out.println("Serie no encontrada");
        }
    }

    private void buscarTop5Series() {
        List<Serie> topSeries = serieRepository.findTop5ByOrderByEvaluacionDesc();
        topSeries.forEach(serie ->
                System.out.println("Serie: " + serie.getTitulo() + "Evaluacion: " + serie.getEvaluacion()));
    }

    private void buscarSeriePorCategoria() {
        System.out.println("Escriba el genero/categoria de la serie que desea buscar");
        var genero = teclado.nextLine();
        var categoria = Categoria.fromSpanish(genero);
        List<Serie> seriesPorCategoria = serieRepository.findByGenero(categoria);
    }
}