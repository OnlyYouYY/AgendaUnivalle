package com.vitanova.agendaunivalle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vitanova.agendaunivalle.AgregarNota.Agregar_Nota;
import com.vitanova.agendaunivalle.ListarNotas.Listar_Notas;
import com.vitanova.agendaunivalle.NotasArchivadas.Notas_Archivadas;
import com.vitanova.agendaunivalle.Perfil.Perfil_Usuario;

public class MenuPrincipal extends AppCompatActivity {

    Button CerrarSesion,AgregarNotas,ListarNotas,Archivados,Perfil,AcercaDe;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;

    TextView UidPrincipal,NombresPrincipal, CorreoPrincipal;
    Button EstadoCuentaPrincipal;
    ProgressBar progressBarDatos;
    ProgressDialog progressDialog;

    LinearLayoutCompat Linear_Nombres, Linear_Correo, Linear_Verificacion;

    DatabaseReference Usuarios;

    Dialog dialog_cuenta_verificada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Agenda Univalle");

        UidPrincipal = findViewById(R.id.UidPrincipal);
        NombresPrincipal = findViewById(R.id.NombresPrincipal);
        CorreoPrincipal = findViewById(R.id.CorreoPrincipal);
        EstadoCuentaPrincipal = findViewById(R.id.EstadoCuentaPrincipal);
        progressBarDatos = findViewById(R.id.progressBarDatos);

        dialog_cuenta_verificada = new Dialog(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Espere por favor...");
        progressDialog.setCanceledOnTouchOutside(false);

        Linear_Nombres = findViewById(R.id.Linear_Nombres);
        Linear_Correo = findViewById(R.id.Linear_Correo);
        Linear_Verificacion = findViewById(R.id.Linear_Verificacion);

        Usuarios = FirebaseDatabase.getInstance().getReference("Usuarios");

        AgregarNotas = findViewById(R.id.AgregarNotas);
        ListarNotas = findViewById(R.id.ListarNotas);
        Archivados = findViewById(R.id.Archivados);
        Perfil = findViewById(R.id.Perfil);
        AcercaDe = findViewById(R.id.AcercaDe);
        CerrarSesion = findViewById(R.id.CerrarSesion);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        EstadoCuentaPrincipal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user.isEmailVerified()){
                    //Toast.makeText(MenuPrincipal.this, "Cuenta ya Verificada", Toast.LENGTH_SHORT).show();
                    AnimacionCuentaVerificada();
                }else{
                    VerificarCuentaCorreo();
                }
            }
        });

        AgregarNotas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String uid_usuario = UidPrincipal.getText().toString();
                String correo_usuario = CorreoPrincipal.getText().toString();

                Intent intent = new Intent(MenuPrincipal.this, Agregar_Nota.class);
                intent.putExtra("Uid",uid_usuario);
                intent.putExtra("Correo",correo_usuario);
                startActivity(intent);
            }
        });
        ListarNotas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuPrincipal.this, Listar_Notas.class));
                Toast.makeText(MenuPrincipal.this, "Listar Notas", Toast.LENGTH_SHORT).show();
            }
        });
        Archivados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuPrincipal.this, Notas_Archivadas.class));
                Toast.makeText(MenuPrincipal.this, "Notas Archivadas", Toast.LENGTH_SHORT).show();
            }
        });
        Perfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuPrincipal.this, Perfil_Usuario.class));
                Toast.makeText(MenuPrincipal.this, "Perfil de Usuario", Toast.LENGTH_SHORT).show();
            }
        });
        AcercaDe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MenuPrincipal.this, "Acerca de", Toast.LENGTH_SHORT).show();
            }
        });





        CerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SalirAplicacion();
            }
        });
    }

    private void VerificarCuentaCorreo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Verificar Cuenta")
                .setMessage("¿Estas seguro(a) de enviar instrucciones de verificacion a su correo electronico?"
                +user.getEmail())
                .setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EnviarCorreoAVerificacion();
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MenuPrincipal.this, "Cancelado por el usuario", Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }

    private void EnviarCorreoAVerificacion() {
        progressDialog.setMessage("Enviando instrucciones de verificacion a su correo electronico"+user.getEmail());
        progressDialog.show();

        user.sendEmailVerification()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //Envio exitoso
                        progressDialog.dismiss();
                        Toast.makeText(MenuPrincipal.this, "Instrucciones enviadas, revise su bandeja "+ user.getEmail(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Fallo envio
                        Toast.makeText(MenuPrincipal.this, "Fallo debido a: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void VerificarEstadoDeCuenta(){
        String Verificado = "Verificado";
        String No_Verificado = "No Verificado";
        if(user.isEmailVerified()){
            EstadoCuentaPrincipal.setText(Verificado);
            EstadoCuentaPrincipal.setBackgroundColor(Color.rgb(34, 153, 84));
        }else{
            EstadoCuentaPrincipal.setText(No_Verificado);
            EstadoCuentaPrincipal.setBackgroundColor(Color.rgb(231, 76, 60));
        }
    }

    private void AnimacionCuentaVerificada(){
        Button EntendidoVerificado;

        dialog_cuenta_verificada.setContentView(R.layout.dialogo_cuenta_verificada);

        EntendidoVerificado = dialog_cuenta_verificada.findViewById(R.id.EntendidoVerificado);

        EntendidoVerificado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_cuenta_verificada.dismiss();
            }
        });
        dialog_cuenta_verificada.show();
        dialog_cuenta_verificada.setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onStart() {
        ComprobarInicioSesion();
        super.onStart();
    }

    private void ComprobarInicioSesion(){
        if(user != null){
            //El usuario ha iniciado sesion
            CargaDatos();
        }else{
            //Lo dirigira al MainActivity
            startActivity(new Intent(MenuPrincipal.this,MainActivity.class));
            finish();
        }
    }

    private void CargaDatos(){

        VerificarEstadoDeCuenta();

        Usuarios.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Si el usuario existe
                if(snapshot.exists()){
                    //El progress bar se oculta
                    progressBarDatos.setVisibility(View.GONE);
                    //Los datos se muestran
                    //UidPrincipal.setVisibility(View.VISIBLE);
                    //NombresPrincipal.setVisibility(View.VISIBLE);
                    //CorreoPrincipal.setVisibility(View.VISIBLE);
                    Linear_Nombres.setVisibility(View.VISIBLE);
                    Linear_Correo.setVisibility(View.VISIBLE);
                    Linear_Verificacion.setVisibility(View.VISIBLE);

                    //Obtener Datos
                    String uid = ""+snapshot.child("uid").getValue();
                    String nombres = ""+snapshot.child("nombres").getValue();
                    String correo = ""+snapshot.child("correo").getValue();

                    //Seteo en textview
                    UidPrincipal.setText(uid);
                    NombresPrincipal.setText(nombres);
                    CorreoPrincipal.setText(correo);

                    //Habilitar los botones de menu
                    AgregarNotas.setEnabled(true);
                    ListarNotas.setEnabled(true);
                    Archivados.setEnabled(true);
                    Perfil.setEnabled(true);
                    AcercaDe.setEnabled(true);
                    CerrarSesion.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void SalirAplicacion() {
        firebaseAuth.signOut();
        startActivity(new Intent(MenuPrincipal.this,MainActivity.class));
        Toast.makeText(this,"Cerraste sesion exitosamente",Toast.LENGTH_SHORT).show();
    }
}