package modelos;

import org.junit.Test;

import static org.junit.Assert.*;

/** Pruebas unitarias de EstadisticaPartida; no necesitan SQLite ni emulador. */
public class EstadisticaPartidaTest {

    @Test
    public void modeloNuevoEmpiezaSinGuardarYConContadoresGeneralesEnCero() {
        EstadisticaPartida estadistica = new EstadisticaPartida();

        assertEquals(0L, estadistica.getId());
        assertEquals(0L, estadistica.getIdResultado());
        assertEquals(0, estadistica.getDardosLanzados());
        assertEquals(0, estadistica.getTurnosJugados());
        assertEquals(0, estadistica.getSimples());
        assertEquals(0, estadistica.getDobles());
        assertEquals(0, estadistica.getTriples());
        assertEquals(0, estadistica.getFallos());
    }

    @Test
    public void lasEstadisticasDeModosNoAplicablesEmpiezanEnNull() {
        EstadisticaPartida e = new EstadisticaPartida(9L);

        assertNull(e.getBusts());
        assertNull(e.getMarcasValidas());
        assertNull(e.getRondasPenalizadas());
        assertNull(e.getObjetivosSuperados());
        assertNull(e.getPartidaCompletada());
        assertNull(e.getPuntosValidosShanghai());
    }

    @Test
    public void identificaUnaEstadisticaConSuResultado() {
        EstadisticaPartida e = new EstadisticaPartida(9L);
        e.setId(15L);
        assertEquals(9L, e.getIdResultado());
        assertEquals(15L, e.getId());
        e.setIdResultado(20L);
        assertEquals(20L, e.getIdResultado());
    }

    @Test
    public void permiteLosContadoresComunes() {
        EstadisticaPartida e = new EstadisticaPartida();
        e.setDardosLanzados(30);
        e.setTurnosJugados(10);
        e.setSimples(12);
        e.setDobles(5);
        e.setTriples(3);
        e.setFallos(10);
        assertEquals(30, e.getDardosLanzados());
        assertEquals(10, e.getTurnosJugados());
        assertEquals(12, e.getSimples());
        assertEquals(5, e.getDobles());
        assertEquals(3, e.getTriples());
        assertEquals(10, e.getFallos());
    }

    @Test
    public void guardaLosContadoresDe301Y501() {
        EstadisticaPartida e = new EstadisticaPartida();
        e.setBusts(2);
        e.setPuntosValidos(301);
        e.setMejorTurno(180);
        e.setTurnos180(1);
        e.setDardosCierre(2);
        assertEquals(Integer.valueOf(2), e.getBusts());
        assertEquals(Integer.valueOf(301), e.getPuntosValidos());
        assertEquals(Integer.valueOf(180), e.getMejorTurno());
        assertEquals(Integer.valueOf(1), e.getTurnos180());
        assertEquals(Integer.valueOf(2), e.getDardosCierre());
    }

    @Test
    public void guardaLosContadoresDeCricket() {
        EstadisticaPartida e = new EstadisticaPartida();
        e.setMarcasValidas(12);
        e.setObjetivosCerrados(4);
        e.setPuntosGenerados(45);
        e.setPuntosRecibidos(12);
        assertEquals(Integer.valueOf(12), e.getMarcasValidas());
        assertEquals(Integer.valueOf(4), e.getObjetivosCerrados());
        assertEquals(Integer.valueOf(45), e.getPuntosGenerados());
        assertEquals(Integer.valueOf(12), e.getPuntosRecibidos());
    }

    @Test
    public void guardaLosContadoresDeDoubleDown() {
        EstadisticaPartida e = new EstadisticaPartida();
        e.setRondasPenalizadas(2);
        e.setMejorRondaDd(120);
        e.setPuntosConseguidosDd(270);
        assertEquals(Integer.valueOf(2), e.getRondasPenalizadas());
        assertEquals(Integer.valueOf(120), e.getMejorRondaDd());
        assertEquals(Integer.valueOf(270), e.getPuntosConseguidosDd());
    }

    @Test
    public void guardaLosContadoresDeAroundTheClock() {
        EstadisticaPartida e = new EstadisticaPartida();
        e.setObjetivosSuperados(20);
        e.setAciertosObjetivo(16);
        e.setPartidaCompletada(false);
        assertEquals(Integer.valueOf(20), e.getObjetivosSuperados());
        assertEquals(Integer.valueOf(16), e.getAciertosObjetivo());
        assertEquals(Boolean.FALSE, e.getPartidaCompletada());
        e.setPartidaCompletada(true);
        assertEquals(Boolean.TRUE, e.getPartidaCompletada());
    }

    @Test
    public void guardaLosContadoresDeShanghai() {
        EstadisticaPartida e = new EstadisticaPartida();
        e.setPuntosValidosShanghai(130);
        e.setMejorRondaShanghai(60);
        e.setRondasConPuntos(6);
        assertEquals(Integer.valueOf(130), e.getPuntosValidosShanghai());
        assertEquals(Integer.valueOf(60), e.getMejorRondaShanghai());
        assertEquals(Integer.valueOf(6), e.getRondasConPuntos());
    }

    @Test
    public void rechazaIdentificadoresYContadoresNegativos() {
        EstadisticaPartida e = new EstadisticaPartida();
        assertThrows(IllegalArgumentException.class, () -> e.setId(-1L));
        assertThrows(IllegalArgumentException.class, () -> e.setIdResultado(0L));
        assertThrows(IllegalArgumentException.class, () -> e.setIdResultado(-1L));
        assertThrows(IllegalArgumentException.class, () -> e.setDardosLanzados(-1));
        assertThrows(IllegalArgumentException.class, () -> e.setTurnosJugados(-2));
        assertThrows(IllegalArgumentException.class, () -> e.setSimples(-1));
        assertThrows(IllegalArgumentException.class, () -> e.setDobles(-1));
        assertThrows(IllegalArgumentException.class, () -> e.setTriples(-1));
        assertThrows(IllegalArgumentException.class, () -> e.setFallos(-1));
        assertThrows(IllegalArgumentException.class, () -> e.setBusts(-1));
        assertThrows(IllegalArgumentException.class, () -> e.setMarcasValidas(-1));
        assertThrows(IllegalArgumentException.class, () -> e.setRondasPenalizadas(-1));
        assertThrows(IllegalArgumentException.class, () -> e.setObjetivosSuperados(-1));
        assertThrows(IllegalArgumentException.class, () -> e.setPuntosValidosShanghai(-1));
    }

    @Test
    public void distingueCeroDeNullEnUnCampoEspecifico() {
        EstadisticaPartida e = new EstadisticaPartida();
        e.setBusts(0);
        e.setPartidaCompletada(false);
        assertEquals(Integer.valueOf(0), e.getBusts());
        assertEquals(Boolean.FALSE, e.getPartidaCompletada());
        e.setBusts(null);
        e.setPartidaCompletada(null);
        assertNull(e.getBusts());
        assertNull(e.getPartidaCompletada());
    }
}
