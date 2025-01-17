package com.ookiisoftware.protips.modelo;

import android.app.Activity;
import android.net.Uri;
import android.view.View;
import android.widget.ProgressBar;

import com.google.firebase.database.DatabaseReference;
import com.ookiisoftware.protips.R;
import com.ookiisoftware.protips.auxiliar.Const;
import com.ookiisoftware.protips.auxiliar.Criptografia;
import com.ookiisoftware.protips.auxiliar.Import;

import java.util.Comparator;

public class PostPerfil {

    //region Variáveis
    private static final String TAG = "PostPerfil";

    private String id;
    private String foto;
    private String titulo;
    private String texto;
    private String data;
    private String id_tipster;
    //endregion

    //region Métodos

    public void salvar(final Activity activity, final ProgressBar progressBar, boolean isFotoLocal) {
        if (isFotoLocal) {
            Import.getFirebase.getStorage()
                    .child(Const.firebase.child.POSTES_PERFIL)
                    .child(getId())
                    .putFile(Uri.parse(getFoto()))
                    .addOnSuccessListener(taskSnapshot -> {
                        if (taskSnapshot.getMetadata() != null && taskSnapshot.getMetadata().getReference() != null)
                            taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(task -> {
                                if (task.getResult() != null) {
                                    setFoto(task.getResult().toString());
                                    salvar();
                                }
                            });
                    }).addOnFailureListener(e -> {
                        Import.Alert.snakeBar(activity, activity.getResources().getString(R.string.post_erro));
                        if (progressBar != null)
                            progressBar.setVisibility(View.GONE);
                    });
        } else {
            salvar();
        }
    }

    private void salvar() {
        String id = getId_tipster() == null ? Import.getFirebase.getId() : getId_tipster();
        DatabaseReference reference = Import.getFirebase.getReference();
        reference
                .child(Const.firebase.child.USUARIO)
                .child(id)
                .child(Const.firebase.child.POSTES_PERFIL)
                .child(Criptografia.criptografar(getData()))
                .setValue(this);

        Import.getFirebase.getTipster().getPost_perfil().put(getId(), this);
        Import.activites.getMainActivity().perfilFragment.adapterUpdate();
    }

    public void excluir(final Activity activity) {
        String id = getId_tipster();
        Import.getFirebase.getReference()
                .child(Const.firebase.child.USUARIO)
                .child(id)
                .child(Const.firebase.child.POSTES_PERFIL)
                .child(getId())
                .removeValue()
                .addOnCompleteListener(aVoid -> {
                    Import.getFirebase.getStorage()
                            .child(Const.firebase.child.POSTES_PERFIL)
                            .child(getId()).delete();

                    Import.getFirebase.getTipster().getPost_perfil().remove(getId());
                    Import.Alert.snakeBar(activity, activity.getResources().getString(R.string.msg_post_excluido));
                    Import.activites.getMainActivity().perfilFragment.adapterUpdate();
                })
                .addOnFailureListener(e -> {
                    Import.Alert.e(TAG, "excluir", e);
                    Import.Alert.snakeBar(activity, activity.getResources().getString(R.string.erro_excluir_post));
                });
    }

    //endregion

    //region gets sets

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getId_tipster() {
        return id_tipster;
    }

    public void setId_tipster(String id_tipster) {
        this.id_tipster = id_tipster;
    }

    //endregion

    public static class orderByDate implements Comparator<PostPerfil> {
        public int compare(PostPerfil left, PostPerfil right) {
            return right.getData().compareTo(left.getData());
        }
    }

}
