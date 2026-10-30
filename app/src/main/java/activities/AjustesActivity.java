package activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import com.productos.juegosdedardos.R;
import java.util.ArrayList;
import java.util.List;
import modelos.Jugador;
import sqlite.JugadorDao;
import sqlite.PobladorDatosPrueba;

/** Preferencias originales y herramientas de prueba disponibles solo en Debug. */
public class AjustesActivity extends AppCompatActivity {
    public static final String SP_AJUSTES = "ajustes_partida";
    public static final String PREF_JUGADOR = "jugador_default";
    public static final String PREF_JUGADOR_ID = "jugador_default_id";
    public static final String PREF_MODO = "modo_default";
    public static final String PREF_DARDOS = "dardos_default";
    public static final String PREF_MANTENER_MULTIPLICADOR = "mantener_multiplicador";
    public static final String PREF_CIERRE_DOBLE = "cierre_doble_default";
    public static final String PREF_ORDEN_ALEATORIO = "orden_aleatorio_default";
    public static final String PREF_CONFIRMAR_SALIDA = "confirmar_salida";
    public static final String PREF_VIBRACION = "vibracion";
    private static final String[] MODOS = {"301", "501", "Cricket", "Cut Throat Cricket", "Double Down", "Around the Clock", "Shanghai"};
    private static final Integer[] DARDOS = {1,2,3,4};
    private Spinner spnJugadorPredeterminado, spnModoPredeterminado, spnDardosPredeterminados;
    private TextView txtAvisoJugadoresAjustes;
    private CheckBox chkMantenerMultiplicador,chkCierreDoble,chkOrdenAleatorio,chkConfirmarSalida,chkVibracion;
    private SharedPreferences preferencias;
    private JugadorDao jugadorDao;
    private final List<Jugador> jugadoresActivos = new ArrayList<>();
    private boolean primerResumeCompletado;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.blue));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this,R.color.blue));
        setContentView(R.layout.activity_ajustes);
        preferencias=getSharedPreferences(SP_AJUSTES,MODE_PRIVATE);
        jugadorDao=new JugadorDao(this);
        inicializarVistas();prepararSpinners();cargarPreferencias();configurarBotones();
    }
    @Override protected void onResume() {
        super.onResume();
        if (!primerResumeCompletado) {primerResumeCompletado=true;return;}
        Jugador anterior=jugadorSeleccionado();long id=anterior==null?0:anterior.getId();
        actualizarListaJugadores();
        if(id<=0 || !seleccionarPorId(id)) seleccionarJugadorPreferidoGuardado();
    }
    @Override protected void onDestroy(){if(jugadorDao!=null)jugadorDao.close();super.onDestroy();}
    private void inicializarVistas(){
        spnJugadorPredeterminado=findViewById(R.id.spnJugadorPredeterminado);
        spnModoPredeterminado=findViewById(R.id.spnModoPredeterminado);
        spnDardosPredeterminados=findViewById(R.id.spnDardosPredeterminados);
        txtAvisoJugadoresAjustes=findViewById(R.id.txtAvisoJugadoresAjustes);
        chkMantenerMultiplicador=findViewById(R.id.chkMantenerMultiplicador);
        chkCierreDoble=findViewById(R.id.chkCierreDoblePredeterminado);
        chkOrdenAleatorio=findViewById(R.id.chkOrdenAleatorioPredeterminado);
        chkConfirmarSalida=findViewById(R.id.chkConfirmarSalida);
        chkVibracion=findViewById(R.id.chkVibracion);
    }
    private void prepararSpinners(){configurarSpinner(spnModoPredeterminado,MODOS);configurarSpinner(spnDardosPredeterminados,DARDOS);}
    private <T> void configurarSpinner(Spinner s,T[] valores){
        ArrayAdapter<T> a=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,valores);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);s.setAdapter(a);
    }
    private void actualizarListaJugadores(){
        jugadoresActivos.clear();jugadoresActivos.addAll(jugadorDao.listarActivos());
        ArrayAdapter<Jugador> a=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,jugadoresActivos);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnJugadorPredeterminado.setAdapter(a);
        boolean hay=!jugadoresActivos.isEmpty();spnJugadorPredeterminado.setEnabled(hay);
        txtAvisoJugadoresAjustes.setVisibility(hay?View.GONE:View.VISIBLE);
    }
    private void cargarPreferencias(){
        actualizarListaJugadores();seleccionarJugadorPreferidoGuardado();
        seleccionarValor(spnModoPredeterminado,preferencias.getString(PREF_MODO,"501"));
        int d=Math.max(1,Math.min(4,preferencias.getInt(PREF_DARDOS,3)));
        spnDardosPredeterminados.setSelection(d-1);
        chkMantenerMultiplicador.setChecked(preferencias.getBoolean(PREF_MANTENER_MULTIPLICADOR,false));
        chkCierreDoble.setChecked(preferencias.getBoolean(PREF_CIERRE_DOBLE,false));
        chkOrdenAleatorio.setChecked(preferencias.getBoolean(PREF_ORDEN_ALEATORIO,false));
        chkConfirmarSalida.setChecked(preferencias.getBoolean(PREF_CONFIRMAR_SALIDA,true));
        chkVibracion.setChecked(preferencias.getBoolean(PREF_VIBRACION,true));
    }
    private void seleccionarJugadorPreferidoGuardado(){
        int pos=posicionJugadorPreferido(jugadoresActivos,preferencias.getLong(PREF_JUGADOR_ID,0L),
                preferencias.getString(PREF_JUGADOR,""));
        if(pos>=0)spnJugadorPredeterminado.setSelection(pos);
    }
    static int posicionJugadorPreferido(List<Jugador> activos,long id,String nombreAntiguo){
        if(activos==null || activos.isEmpty())return -1;
        if(id>0){for(int i=0;i<activos.size();i++)if(activos.get(i).getId()==id)return i;return 0;}
        if(nombreAntiguo!=null)for(int i=0;i<activos.size();i++)
            if(activos.get(i).getNombre().equalsIgnoreCase(nombreAntiguo.trim()))return i;
        return 0;
    }
    private boolean seleccionarPorId(long id){
        for(int i=0;i<jugadoresActivos.size();i++)if(jugadoresActivos.get(i).getId()==id){spnJugadorPredeterminado.setSelection(i);return true;}
        return false;
    }
    private Jugador jugadorSeleccionado(){Object j=spnJugadorPredeterminado.getSelectedItem();return j instanceof Jugador?(Jugador)j:null;}
    private void configurarBotones(){
        Button guardar=findViewById(R.id.btnGuardarAjustes),restablecer=findViewById(R.id.btnRestablecerAjustes),volver=findViewById(R.id.btnVolverAjustes);
        guardar.setOnClickListener(v->{guardarPreferencias();Toast.makeText(this,"Ajustes guardados",Toast.LENGTH_SHORT).show();finish();});
        restablecer.setOnClickListener(v->restablecerPreferencias());volver.setOnClickListener(v->finish());
        Button poblar=findViewById(R.id.btnPoblarDatosPrueba);
        View zona=findViewById(R.id.zonaDatosPrueba);
        if(!PobladorDatosPrueba.esDepurable(this))zona.setVisibility(View.GONE);
        else {
            zona.setVisibility(View.VISIBLE);
            poblar.setOnClickListener(v->confirmarDatosPrueba());
            actualizarEstadoPoblador(poblar);
        }
    }
    private void actualizarEstadoPoblador(Button boton){
        boolean permitido=!PobladorDatosPrueba.yaEjecutado(this) && PobladorDatosPrueba.estaVacia(this);
        boton.setEnabled(permitido);
        if(!permitido)boton.setText("Datos de prueba no disponibles");
    }
    private void confirmarDatosPrueba(){
        new AlertDialog.Builder(this)
                .setTitle("Crear datos de prueba")
                .setMessage("Se crearán 6 jugadores y 70 partidas de ejemplo, incluidos jugadores inactivos y partidas ocultas. Solo funciona una vez y con la base de datos completamente vacía. ¿Continuar?")
                .setNegativeButton("Cancelar",null)
                .setPositiveButton("Generar",(d,w)->{
                    Button b=findViewById(R.id.btnPoblarDatosPrueba);
                    b.setEnabled(false);
                    try {
                        PobladorDatosPrueba.Resumen r=PobladorDatosPrueba.poblar(this);
                        Toast.makeText(this,"Creados "+r.jugadores+" jugadores y "+r.partidas+" partidas",Toast.LENGTH_LONG).show();
                        actualizarListaJugadores();seleccionarJugadorPreferidoGuardado();
                    } catch(Exception ex){
                        new AlertDialog.Builder(this).setTitle("No se generaron los datos")
                                .setMessage(ex.getMessage()).setPositiveButton("Entendido",null).show();
                    }
                    actualizarEstadoPoblador(b);
                }).show();
    }
    private void guardarPreferencias(){
        SharedPreferences.Editor e=preferencias.edit();Jugador j=jugadorSeleccionado();
        if(j==null)e.remove(PREF_JUGADOR_ID).remove(PREF_JUGADOR);
        else{e.putLong(PREF_JUGADOR_ID,j.getId());e.putString(PREF_JUGADOR,j.getNombre());}
        e.putString(PREF_MODO,valorSeleccionado(spnModoPredeterminado));
        e.putInt(PREF_DARDOS,(Integer)spnDardosPredeterminados.getSelectedItem());
        e.putBoolean(PREF_MANTENER_MULTIPLICADOR,chkMantenerMultiplicador.isChecked());
        e.putBoolean(PREF_CIERRE_DOBLE,chkCierreDoble.isChecked());
        e.putBoolean(PREF_ORDEN_ALEATORIO,chkOrdenAleatorio.isChecked());
        e.putBoolean(PREF_CONFIRMAR_SALIDA,chkConfirmarSalida.isChecked());
        e.putBoolean(PREF_VIBRACION,chkVibracion.isChecked());e.apply();
    }
    private void restablecerPreferencias(){preferencias.edit().clear().apply();cargarPreferencias();Toast.makeText(this,"Ajustes restablecidos",Toast.LENGTH_SHORT).show();}
    private String valorSeleccionado(Spinner s){return String.valueOf(s.getSelectedItem());}
    private void seleccionarValor(Spinner s,String valor){
        for(int i=0;i<s.getCount();i++)if(String.valueOf(s.getItemAtPosition(i)).equals(valor)){s.setSelection(i);return;}
    }
}
