package io.github.alancs7.servicos;

import io.github.alancs7.daos.LocacaoDAO;
import io.github.alancs7.entidades.Filme;
import io.github.alancs7.entidades.Locacao;
import io.github.alancs7.entidades.Usuario;
import io.github.alancs7.exceptions.FilmeSemEstoqueException;
import io.github.alancs7.exceptions.LocadoraException;
import io.github.alancs7.utils.DataUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class LocacaoService {

    private LocacaoDAO dao;
    private SPCService spcService;
    private EmailService emailService;

    public Locacao alugarFilme(Usuario usuario, List<Filme> filmes) throws FilmeSemEstoqueException, LocadoraException {

        if (usuario == null) {
            throw new LocadoraException("Usuario vazio");
        }

        if (filmes == null || filmes.isEmpty()) {
            throw new LocadoraException("Filme vazio");
        }

        for (Filme filme : filmes) {
            if (filme.getEstoque() == 0) {
                throw new FilmeSemEstoqueException("Filme sem estoque");
            }
        }

        boolean negativado;
        try {
            negativado = spcService.possuiNegativacao(usuario);
        } catch (Exception e) {
            throw new LocadoraException("Problemas com SPC, tente novamente");
        }

        if (negativado) {
            throw new LocadoraException("Usuário Negativado");
        }

        Locacao locacao = new Locacao();
        locacao.setFilmes(filmes);
        locacao.setUsuario(usuario);
        locacao.setValor(calcularValorLocacao(filmes));
        locacao.setDataLocacao(new Date());

        //Entrega no dia seguinte
        Date dataEntrega = obterData();
        dataEntrega = DataUtils.adicionarDias(dataEntrega, 1);

        if (DataUtils.verificarDiaSemana(dataEntrega, Calendar.SUNDAY)) {
            dataEntrega = DataUtils.adicionarDias(dataEntrega, 1);
        }
        locacao.setDataRetorno(dataEntrega);

        //Salvando a locacao...
        dao.salvar(locacao);

        return locacao;
    }

    private static Double calcularValorLocacao(List<Filme> filmes) {
        Double valorTotal = 0.0;

        for (int i = 0; i < filmes.size(); i++) {
            Filme filme = filmes.get(i);
            Double valorFilme = filme.getPrecoLocacao();

            switch (i) {
                case 2: valorFilme *= 0.75; break;
                case 3: valorFilme *= 0.5;  break;
                case 4: valorFilme *= 0.25; break;
                case 5: valorFilme *= 0.0;  break;
            }

            valorTotal += valorFilme;
        }
        return valorTotal;
    }

    public void notificarAtrasos() {
        List<Locacao> locacoes = dao.obterLocacoesPendentes();
        for (Locacao locacao : locacoes) {
            if (locacao.getDataRetorno().before(new Date())) {
                emailService.notificarAtraso(locacao.getUsuario());
            }
        }
    }

    public void prorrogarLocacao(Locacao locacao, int dias) {
        Locacao novaLocacao = new Locacao();
        novaLocacao.setUsuario(locacao.getUsuario());
        novaLocacao.setFilmes(locacao.getFilmes());
        novaLocacao.setDataLocacao(obterData());
        novaLocacao.setDataRetorno(DataUtils.obterDataComDiferencaDias(dias));
        novaLocacao.setValor(locacao.getValor() * dias);
        dao.salvar(novaLocacao);
    }

    public Date obterData() {
        return new Date();
    }
}