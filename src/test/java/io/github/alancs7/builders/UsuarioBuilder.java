package io.github.alancs7.builders;

import io.github.alancs7.entidades.Usuario;

public class UsuarioBuilder {

    private Usuario usuario;

    private UsuarioBuilder() {
    }

    public static UsuarioBuilder umUsuario() {
        UsuarioBuilder builder = new UsuarioBuilder();
        builder.usuario = new Usuario();
        builder.usuario.setNome("Usuario 1");
        return builder;
    }

    public UsuarioBuilder comNome(String nome) {
        usuario.setNome(nome);
        return this;
    }

    public Usuario agora() {
        return usuario;
    }
}