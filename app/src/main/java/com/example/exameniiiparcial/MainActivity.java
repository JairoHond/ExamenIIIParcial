package com.example.exameniiiparcial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.exameniiiparcial.modelo.usuarios;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private List<usuarios> listusu = new ArrayList<usuarios>();
    ArrayAdapter<usuarios> arrayAdapterusu;
    EditText txtDesc,txtPeriodista,txtFecha;
    private Date fechaSeleccionada;

    Button btnAdd,btnEdit,btnBorrar,btnFoto;
    ListView listView;

    ImageView objImagen;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    usuarios usuarioSelected;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PETICION_ACCESO_CAM = 100;
    byte[] byteArray;

    private DatePickerDialog.OnDateSetListener fechalist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        objImagen = (ImageView) findViewById(R.id.imageView);

        txtDesc=findViewById(R.id.txtDesc);
        txtPeriodista=findViewById(R.id.txtPeriodista);
        txtFecha=findViewById(R.id.txtFecha);

        txtFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDatePickerDialog();
            }
        });

        listView=findViewById(R.id.ListView);

        btnAdd=findViewById(R.id.btnCrear);
        btnBorrar=findViewById(R.id.btnEliminar);
        btnEdit=findViewById(R.id.btnUpdate);
        btnFoto = (Button) findViewById(R.id.btnFoto);

        iniciarFirebase();
        listaDatos();

        btnFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                permisos();
            }
        });



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                usuarioSelected=(usuarios) parent.getItemAtPosition(position);
                txtDesc.setText(usuarioSelected.getDescripcion());
                txtPeriodista.setText(usuarioSelected.getPeriodista());
                txtFecha.setText(usuarioSelected.getFecha());
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregar();
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editar();
            }
        });

        btnBorrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eliminar();
            }
        });
    }

    public void iniciarFirebase()
    {
        FirebaseApp.initializeApp(this);
        firebaseDatabase= FirebaseDatabase.getInstance();
        firebaseDatabase.setPersistenceEnabled(true);
        databaseReference= firebaseDatabase.getReference();
    }

    public void listaDatos()
    {
        databaseReference.child("usuarios").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listusu.clear();
                for (DataSnapshot objSnaptchot : snapshot.getChildren()){
                    usuarios u= objSnaptchot.getValue(usuarios.class);
                    listusu.add(u);
                    arrayAdapterusu=new ArrayAdapter<usuarios>(MainActivity.this, android.R.layout.simple_list_item_1,listusu);
                    listView.setAdapter(arrayAdapterusu);
                    //objImagen.setImageResource(R.mipmap.img);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void agregar()
    {
        String periodista=txtPeriodista.getText().toString();
        String desc=txtDesc.getText().toString();
        String fecha=txtFecha.getText().toString();

        if(periodista.equals("")||desc.equals("")||fecha.equals(""))
        {
            validacion();
        }
        else{
            usuarios u= new usuarios();
            u.setID(UUID.randomUUID().toString());
            u.setDescripcion(desc);
            u.setPeriodista(periodista);
            u.setFecha(fecha);
            databaseReference.child("usuarios").child(u.getID()).setValue(u);
            Toast.makeText(this,"Dato agregado correctamente", Toast.LENGTH_LONG).show();
            Limpiar();
        }
    }

    public void eliminar()
    {
        usuarios u=new usuarios();
        u.setID(usuarioSelected.getID());
        databaseReference.child("usuarios").child(u.getID()).removeValue();
        Toast.makeText(this,"Registro Eliminados correctamente", Toast.LENGTH_LONG).show();
        Limpiar();
    }

    public void editar()
    {
        usuarios u= new usuarios();
        u.setID(usuarioSelected.getID());
        u.setDescripcion(txtDesc.getText().toString().trim());
        u.setPeriodista(txtPeriodista.getText().toString().trim());
        u.setFecha(txtFecha.getText().toString().trim());
        databaseReference.child("usuarios").child(u.getID()).setValue(u);
        Toast.makeText(this,"Registro Actualizados correctamente", Toast.LENGTH_LONG).show();
        Limpiar();

    }

    private void validacion() {
        String desc=txtDesc.getText().toString();
        String periodista=txtPeriodista.getText().toString();
        String fecha=txtFecha.getText().toString();
        if(desc.equals(""))
        {
            txtDesc.setError("Campo Vacio");
        }
        else if(periodista.equals(""))
        {
            txtPeriodista.setError("Campo Vacio");
        }
        else if(fecha.equals(""))
        {
            txtFecha.setError("Campo Vacio");
        }
    }

    private void Limpiar() {
        txtDesc.setText("");
        txtFecha.setText("");
        txtPeriodista.setText("");
    }

    private void permisos() {
        if(ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{ android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, PETICION_ACCESO_CAM);
        }else{
            tomarFoto();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PETICION_ACCESO_CAM){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                tomarFoto();
            }
        }else{
            Toast.makeText(getApplicationContext(), "SE NECESITAN PERMISOS DE LA CAMARA", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            getBytes(data);
        }
    }

    private void getBytes(Intent data){
        Bitmap photo = (Bitmap) data.getExtras().get("data");
        objImagen.setImageBitmap(photo);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byteArray = stream.toByteArray();
    }

    private void tomarFoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void mostrarDatePickerDialog() {
        final Calendar calendario = Calendar.getInstance();
        int anio = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int dia = calendario.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendario.set(year, monthOfYear, dayOfMonth);
                        fechaSeleccionada = calendario.getTime();

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        String fechaString = sdf.format(fechaSeleccionada);
                        txtFecha.setText(fechaString);
                    }
                },
                anio, mes, dia);

        datePickerDialog.show();
    }
}