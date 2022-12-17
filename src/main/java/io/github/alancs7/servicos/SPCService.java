package io.github.alancs7.servicos;

import io.github.alancs7.entidades.Usuario;

public interface SPCService {

    boolean possuiNegativacao(Usuario usuario) throws Exception;
}
