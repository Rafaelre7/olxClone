package rafaelpimenta.studio.com.olxclone.activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.santalu.maskedittext.MaskEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;
import rafaelpimenta.studio.com.olxclone.R;
import rafaelpimenta.studio.com.olxclone.helper.ConfiguracaoFirebase;
import rafaelpimenta.studio.com.olxclone.helper.Permissoes;
import rafaelpimenta.studio.com.olxclone.model.Anuncio;

public class CadastrarAnuncioActivity extends AppCompatActivity
        implements View.OnClickListener {

    private EditText campoTitulo, campoDescricao;
    private CurrencyEditText campoValor;
    private MaskEditText campoTelefone;
    private ImageView imagem1, imagem2, imagem3;
    private Spinner campoEstado, campoCategoria;
    private Anuncio anuncio;
    private StorageReference storage;
    private android.app.AlertDialog dialog;


    private String[] permissoes = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA

    };

    private List<String> listaFotosRecuperadas = new ArrayList<>();

    private List<String> listaUrlFotos = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_anuncio);

        //Configuracoes iniciais
        storage = ConfiguracaoFirebase.getFirebaseStorage();

        //Validar permissoes
        Permissoes.validarPermissoes(permissoes, this, 1);

        inicializarComponentes();

        carregarDadosSpinner();
    }

    private void carregarDadosSpinner() {
        /*String [] estados = new String[]{
                "SP","MT"
        };*/
        //Configurar estados
        String[] estados = getResources().getStringArray(R.array.estados);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                estados
        );
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        campoEstado.setAdapter(arrayAdapter);

        String[] categorias = getResources().getStringArray(R.array.categoria);
        ArrayAdapter<String> arrayCategorias = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                categorias
        );
        arrayCategorias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        campoCategoria.setAdapter(arrayCategorias);


    }

    private void inicializarComponentes() {
        campoTitulo = findViewById(R.id.editTitulo);
        campoDescricao = findViewById(R.id.editDescricao);
        campoValor = findViewById(R.id.editValor);
        campoTelefone = findViewById(R.id.editTelefone);
        imagem1 = findViewById(R.id.imageCadastro1);
        imagem2 = findViewById(R.id.imageCadastro2);
        imagem3 = findViewById(R.id.imageCadastro3);
        campoEstado = findViewById(R.id.spinnerEstado);
        campoCategoria = findViewById(R.id.spinnerCategoria);

        //Configurar eventos que a classe ira controlar
        imagem1.setOnClickListener(this);
        imagem2.setOnClickListener(this);
        imagem3.setOnClickListener(this);


        //Configura localidade para pt - > Portugues BR -> Brasil || vem como padrão dinamico
        Locale locale = new Locale("pt", "BR");
        campoValor.setLocale(locale);

    }

    public void salvarAnuncio() {
        //Exibi o dado carregando
        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Salvando Anúncio")
                .setCancelable(false)
                .build();
        dialog.show();

        /*Salvar imagem no Storage*/

        for (int i = 0; i < listaFotosRecuperadas.size(); i++) {
            String urlmagem = listaFotosRecuperadas.get(i);
            int tamanhoLista = listaFotosRecuperadas.size();
            salvarFotoStorage(urlmagem, tamanhoLista, i);
        }

    }

    private void salvarFotoStorage(final String urlmagem, final int totalFotos, int contador) {

        //Cria o nó dentro do storage
        final StorageReference imagemAnuncio = storage.child("imagens")
                .child("anuncio")
                .child(anuncio.getIdAnuncio())
                .child("imagem" + contador);

        //Fazer upload do arquivo
        UploadTask uploadTask = imagemAnuncio.putFile(Uri.parse(urlmagem));
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri> >() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if(!task.isSuccessful()){
                    throw task.getException();
                }
                return imagemAnuncio.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    Uri downloadUrl = task.getResult();
                    listaUrlFotos.add(downloadUrl.toString());
                    if(totalFotos == listaUrlFotos.size()){
                        anuncio.setFotos(listaUrlFotos);
                        anuncio.salvar();

                        dialog.dismiss();
                        finish();
                        alerta("Anuncio salvo com sucesso !");
                    }
                }
            }
        });
    }


    private Anuncio configurarAnuncio() {

        String estado = campoEstado.getSelectedItem().toString().trim();
        String categoria = campoCategoria.getSelectedItem().toString().trim();
        String titulo = campoTitulo.getText().toString().trim();
        String valor = campoValor.getText().toString();
        String telefone = campoTelefone.getText().toString().trim();
        String descricao = campoDescricao.getText().toString().trim();

        Anuncio anuncio = new Anuncio();
        anuncio.setEstado(estado);
        anuncio.setCategoria(categoria);
        anuncio.setTitulo(titulo);
        anuncio.setValor(valor);
        anuncio.setTelefone(telefone);
        anuncio.setDescricao(descricao);

        return anuncio;
    }

    public void validarDadosAnuncio(View view) {

        anuncio = configurarAnuncio();
        String valor = String.valueOf(campoValor.getRawValue());

        if (listaFotosRecuperadas.size() != 0) {
            if (!anuncio.getEstado().isEmpty()) {
                if (!anuncio.getCategoria().isEmpty()) {
                    if (!anuncio.getTitulo().isEmpty()) {
                        if (!valor.isEmpty() && !valor.equals("0")) {
                            if (!anuncio.getTelefone().isEmpty()) {
                                if (!anuncio.getDescricao().isEmpty()) {
                                    salvarAnuncio();
                                } else {
                                    alerta("Preencha o campo descrição !");
                                }
                            } else {
                                alerta("Preencha o campo telefone corretamente !");
                            }
                        } else {
                            alerta("Preencha o campo valor !");
                        }
                    } else {
                        alerta("Preencha o campo título !");
                    }

                } else {
                    alerta("Selecione a categoria !");
                }

            } else {
                alerta("Selecione o estado !");
            }
        } else {
            alerta("Selecione ao menos uma foto!");
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResultado : grantResults) {
            if (permissaoResultado == PackageManager.PERMISSION_DENIED) {
                alertaValidacaoPermissao();
            }
        }
    }

    private void alertaValidacaoPermissao() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o app é necessario aceitar as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.imageCadastro1:
                escolherImagem(1);
                break;
            case R.id.imageCadastro2:
                escolherImagem(2);
                break;
            case R.id.imageCadastro3:
                escolherImagem(3);
                break;
            default:
                break;
        }
    }

    private void escolherImagem(int requestCode) {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, requestCode);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            //Recupera a imagem

            Uri imagemSelecionada = data.getData();
            String caminhoImagem = imagemSelecionada.toString();

            //Configura no ImageView
            if (requestCode == 1) {
                imagem1.setImageURI(imagemSelecionada);
//                listaFotosRecuperadas.add(caminhoImagem);
            } else if (requestCode == 2) {
                imagem2.setImageURI(imagemSelecionada);
            } else if (requestCode == 3) {
                imagem3.setImageURI(imagemSelecionada);
            }


            listaFotosRecuperadas.add(caminhoImagem);

        }
    }

    private void alerta(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
