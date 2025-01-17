package com.ookiisoftware.protips.modelo;

import com.google.firebase.database.Exclude;
import com.ookiisoftware.protips.auxiliar.Const;
import com.ookiisoftware.protips.auxiliar.Import;

import java.util.HashMap;

public class UserDados {

    private String info;
    private String id;
    private String nome;
    private String email;
    private String tipname;
    private String senha;
    private HashMap<String, Token> tokens;
    private String token;
    private String foto;
    private String telefone;
    private Endereco endereco;
    private Data nascimento;
    private boolean privado;
    private boolean bloqueado;
    private boolean tipster;

    public UserDados() {
        endereco = new Endereco();
        nascimento = new Data();
    }

    public void atualizarNumero(String numero) {
//        String child = isTipster ? Constantes.firebase.child.TIPSTERS : Constantes.firebase.child.PUNTERS;
        Import.getFirebase.getReference()
                .child(Const.firebase.child.USUARIO)
//                .child(Constantes.firebase.child.TIPSTERS)
                .child(getId())
                .child(Const.firebase.child.DADOS)
                .child(Const.firebase.child.TELEFONE)
                .setValue(numero);
        setTelefone(numero);
    }

    public enum Categoria {
        Apostador, Tipster
    }

    //region gets sets

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getInfo() {
        return info == null ? "" : info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getTipname() {
        return tipname;
    }

    public void setTipname(String tipname) {
        this.tipname = tipname;
    }

    @Exclude
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public boolean isPrivado() {
        return privado;
    }

    public void setPrivado(boolean privado) {
        this.privado = privado;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public HashMap<String, Token> getTokens() {
        if (tokens == null)
            tokens = new HashMap<>();
        return tokens;
    }

    public void setTokens(HashMap<String, Token> tokens) {
        this.tokens = tokens;
    }

    public Data getNascimento() {
        if(nascimento == null)
            nascimento = new Data();
        return nascimento;
    }

    public void setNascimento(Data nascimento) {
        this.nascimento = nascimento;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public boolean isBloqueado() {
        return bloqueado;
    }

    public void setBloqueado(boolean bloqueado) {
        this.bloqueado = bloqueado;
    }

    public boolean isTipster() {
        return tipster;
    }

    public void setTipster(boolean tipster) {
        this.tipster = tipster;
    }

    //endregion

}
