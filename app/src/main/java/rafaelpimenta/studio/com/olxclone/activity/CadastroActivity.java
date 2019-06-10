package rafaelpimenta.studio.com.olxclone.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import rafaelpimenta.studio.com.olxclone.R;
import rafaelpimenta.studio.com.olxclone.helper.ConfiguracaoFirebase;

public class CadastroActivity extends AppCompatActivity {

    private Button botaoAcessar;
    private EditText campoEmail, campoSenha;
    private Switch tipoAcesso;

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        inicializarComponentes();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        botaoAcessar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = campoEmail.getText().toString().trim();
                String senha = campoSenha.getText().toString().trim();

                if (!email.isEmpty()) {
                    if (!senha.isEmpty()) {
                        //Verifica estado do switch
                        if(tipoAcesso.isChecked()){//cadastro

                            autenticacao.createUserWithEmailAndPassword(
                                    email,senha
                            ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                     if(task.isSuccessful()){
                                         alert("Cadastro realizado com sucesso!");

                                     }else{
                                        String erroExcecao = "";

                                        try {
                                            throw  task.getException();

                                        }catch (FirebaseAuthWeakPasswordException e){
                                            erroExcecao = "Digite uma senha mais forte!";
                                        }catch (FirebaseAuthInvalidCredentialsException e){
                                            erroExcecao = "Por favor, digite um e-mail válido";
                                        }catch (FirebaseAuthUserCollisionException e){
                                            erroExcecao = "Esta conta já foi cadastrada";
                                        }catch (Exception e){
                                            erroExcecao = "ao cadastrar usuário: "+ e.getMessage();
                                            e.printStackTrace();
                                        }
                                        alert("Erro: "+erroExcecao);
                                     }
                                }
                            });
                        }else {//login

                            autenticacao.signInWithEmailAndPassword(
                                    email,senha
                            ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                    alert("Logado com sucesso");
                                    startActivity(new Intent(getApplicationContext(),AnunciosActivity.class));
                                    }else{
                                        alert("Erro ao fazer login: "+task.getException());
                                    }

                                }
                            });

                        }
                    } else {
                        alert("Preencha a Senha");
                    }
                } else {
                    alert("Preencha o E-mail");
                }
            }
        });
    }

    private void inicializarComponentes() {

        campoEmail = findViewById(R.id.editCadastroEmail);
        campoSenha = findViewById(R.id.editCadastroSenha);
        botaoAcessar = findViewById(R.id.buttonAcesso);
        tipoAcesso = findViewById(R.id.switchAcesso);

    }

    private void alert(String msg) {
        Toast.makeText(CadastroActivity.this, msg, Toast.LENGTH_SHORT).show();
    }
}
