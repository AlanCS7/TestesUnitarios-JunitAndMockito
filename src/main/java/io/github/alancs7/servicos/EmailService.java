package io.github.alancs7.servicos;

import io.github.alancs7.entidades.Usuario;

public interface EmailService {

    void notificarAtraso(Usuario usuario);
}
