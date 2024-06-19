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
    private Optional<Serie> serieBuscada;
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
                     6 - buscar Serie Por Categoria
                     7 - buscar Serie Por Temporada Y Evaluacion
                     8 - buscar Episodio Por Nombre
                     9 - buscar top 5 episodios por serie
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
                case 7:
                    buscarSeriePorTemporadaYEvaluacion();
                    break;
                case 8:
                    buscarEpisodioPorNombre();
                    break;
                case 9:
                    top5EpisodioPorSerie();
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

        serieBuscada = serieRepository.findByTituloContainsIgnoreCase(nombreSerie);

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
        System.out.println("Las series de la categoria: " + genero);
        seriesPorCategoria.forEach(System.out::println);
    }

    private void buscarSeriePorTemporadaYEvaluacion() {
        System.out.println("Ingresa el numero de temporadas de la serie");
        var numTemp = teclado.nextInt();
        teclado.nextLine();
        System.out.println("Ingresa la calificacion de la serie");
        Double calSer = teclado.nextDouble();
        teclado.nextLine();

        List<Serie> seriesPorTemporadaYCalificacion = serieRepository.findBytotalTemporadasAndEvaluacion(numTemp, calSer);
        if (seriesPorTemporadaYCalificacion.isEmpty()) {
            System.out.println("No se encontraron coincidencias");
        } else {
            seriesPorTemporadaYCalificacion.forEach(series ->
                    System.out.println("Serie: " + series.getTitulo()
                            + ", Numero de temporadas: " + series.getTotalTemporadas()
                            + ", Calificacion: " + series.getEvaluacion()
                    ));
        }
    }

    private void buscarEpisodioPorNombre() {
        System.out.println("Escribe el nombre del episodio que deseas buscar");
        var nombreEpisodio = teclado.nextLine();
        List<Episodio> episodiosEncontrados = serieRepository.episodiosPorNombre(nombreEpisodio);
        episodiosEncontrados.forEach(episodio -> System.out.printf("Serie: %s\nEpisodio: %s\nTemporada: %s\nEvaluacion: %s\n",
                episodio.getSerie(), episodio.getTitulo(), episodio.getTemporada(), episodio.getEvaluacion()));
    }

    private void top5EpisodioPorSerie() {
        buscarSeriePorTitulo();
        if (serieBuscada.isPresent()) {
            Serie serie = serieBuscada.get();
            List<Episodio> episodios = serieRepository.top5Episodios(serie);
            episodios.forEach(episodio -> System.out.printf("Serie: %s - Episodio: %s - Temporada: %s - Evaluacion: %s \n",
                    episodio.getSerie().getTitulo(), episodio.getTitulo(), episodio.getTemporada(), episodio.getEvaluacion()));
        }
    }

}