package io.github.alancs7.daos;

import io.github.alancs7.entidades.Locacao;

import java.util.List;

public class LocacaoDAOFake implements LocacaoDAO {

    @Override
    public void salvar(Locacao locacao) {

    }

    @Override
    public List<Locacao> obterLocacoesPendentes() {
        return null;
    }
}
