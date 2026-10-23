package com.example.gimnasio_sara;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class GimnasioSaraApplicationTests {
    @LocalServerPort int port;
    final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    final ObjectMapper mapper = new ObjectMapper();
    final String ejercicio = """
        {"nombre":"Sentadilla","descripcion":"Flexiona las rodillas con espalda neutra","grupoMuscular":"Piernas","equipo":"Peso corporal"}
        """;

    HttpResponse<String> request(String method, String path, String body) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
            .timeout(Duration.ofSeconds(15)).header("Content-Type", "application/json")
            .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body))
            .build(), HttpResponse.BodyHandlers.ofString());
    }
    JsonNode json(HttpResponse<String> response) { return mapper.readTree(response.body()); }
    long crearEjercicio() throws Exception {
        var response = request("POST", "/api/ejercicios", ejercicio);
        assertEquals(201, response.statusCode(), response.body());
        assertTrue(response.headers().firstValue("location").orElseThrow().startsWith("/api/ejercicios/"));
        return json(response).get("id").asLong();
    }
    String rutina(String ids) {
        return "{\"nombre\":\"Fuerza\",\"descripcion\":\"Sesión completa\",\"nivel\":\"PRINCIPIANTE\",\"duracionMinutos\":45,\"ejercicioIds\":[" + ids + "]}";
    }
    long crearRutina(String ids) throws Exception {
        var response = request("POST", "/api/rutinas", rutina(ids));
        assertEquals(201, response.statusCode(), response.body());
        return json(response).get("id").asLong();
    }

    @Test void crudCompletoYRelacionMuchosAMuchos() throws Exception {
        long e1 = crearEjercicio(), e2 = crearEjercicio();
        long r1 = crearRutina(e1 + "," + e2), r2 = crearRutina("" + e1);
        assertEquals(2, json(request("GET", "/api/rutinas/" + r1, null)).get("ejercicios").size());
        assertEquals(2, json(request("GET", "/api/ejercicios/" + e1, null)).get("rutinaIds").size());
        assertEquals(200, request("GET", "/api/ejercicios", null).statusCode());
        assertEquals(200, request("GET", "/api/rutinas", null).statusCode());
        var update = request("PUT", "/api/ejercicios/" + e1, ejercicio.replace("Sentadilla", "Sentadilla actualizada"));
        assertEquals(200, update.statusCode());
        assertEquals("Sentadilla actualizada", json(update).get("nombre").asText());
        assertEquals(200, request("PUT", "/api/rutinas/" + r2, rutina("" + e2)).statusCode());
        assertEquals(1, json(request("GET", "/api/ejercicios/" + e1, null)).get("rutinaIds").size());
        assertEquals(204, request("DELETE", "/api/rutinas/" + r1, null).statusCode());
        assertEquals(200, request("GET", "/api/ejercicios/" + e1, null).statusCode());
        assertEquals(204, request("DELETE", "/api/ejercicios/" + e2, null).statusCode());
        assertEquals(0, json(request("GET", "/api/rutinas/" + r2, null)).get("ejercicios").size());
        assertEquals(404, request("GET", "/api/ejercicios/" + e2, null).statusCode());
        assertEquals(204, request("DELETE", "/api/rutinas/" + r2, null).statusCode());
        assertEquals(204, request("DELETE", "/api/ejercicios/" + e1, null).statusCode());
    }

    @Test void asociacionIdempotenteYDesasociacion() throws Exception {
        long e = crearEjercicio(), r = crearRutina("");
        for (int i = 0; i < 2; i++) {
            var response = request("PUT", "/api/rutinas/" + r + "/ejercicios/" + e, null);
            assertEquals(200, response.statusCode());
            assertEquals(1, json(response).get("ejercicios").size());
        }
        assertEquals(204, request("DELETE", "/api/rutinas/" + r + "/ejercicios/" + e, null).statusCode());
        assertEquals(0, json(request("GET", "/api/rutinas/" + r, null)).get("ejercicios").size());
        assertEquals(204, request("DELETE", "/api/rutinas/" + r, null).statusCode());
        assertEquals(204, request("DELETE", "/api/ejercicios/" + e, null).statusCode());
    }

    @Test void validacionesYRecursosInexistentes() throws Exception {
        assertEquals(400, request("POST", "/api/ejercicios", "{}").statusCode());
        assertEquals(400, request("POST", "/api/ejercicios", ejercicio.replace("Sentadilla", "   ")).statusCode());
        assertEquals(400, request("POST", "/api/rutinas", rutina("").replace(":45", ":0")).statusCode());
        assertEquals(400, request("POST", "/api/rutinas", rutina("").replace("PRINCIPIANTE", "OTRO")).statusCode());
        assertEquals(400, request("POST", "/api/rutinas", rutina("-1")).statusCode());
        assertEquals(400, request("POST", "/api/ejercicios", "{mal").statusCode());
        assertEquals(400, request("GET", "/api/ejercicios/abc", null).statusCode());
        assertEquals(404, request("GET", "/api/rutinas/9223372036854775807", null).statusCode());
        assertEquals(404, request("POST", "/api/rutinas", rutina("9223372036854775807")).statusCode());
    }

    @Test void actualizacionConEjercicioInexistenteEsAtomica() throws Exception {
        long e = crearEjercicio(), r = crearRutina("" + e);
        assertEquals(404, request("PUT", "/api/rutinas/" + r, rutina("9223372036854775807")).statusCode());
        assertEquals(e, json(request("GET", "/api/rutinas/" + r, null)).get("ejercicios").get(0).get("id").asLong());
        request("DELETE", "/api/rutinas/" + r, null); request("DELETE", "/api/ejercicios/" + e, null);
    }

    @Test void frontendYSaludDisponibles() throws Exception {
        var page = request("GET", "/", null);
        assertEquals(200, page.statusCode()); assertTrue(page.body().contains("SARA"));
        assertEquals(200, request("GET", "/styles.css", null).statusCode());
        assertEquals(200, request("GET", "/app.js", null).statusCode());
        var health = request("GET", "/actuator/health", null);
        assertEquals(200, health.statusCode()); assertEquals("UP", json(health).get("status").asText());
    }
}
