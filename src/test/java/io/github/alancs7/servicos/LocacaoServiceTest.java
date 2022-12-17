package io.github.alancs7.servicos;

import io.github.alancs7.daos.LocacaoDAO;
import io.github.alancs7.entidades.Filme;
import io.github.alancs7.entidades.Locacao;
import io.github.alancs7.entidades.Usuario;
import io.github.alancs7.exceptions.FilmeSemEstoqueException;
import io.github.alancs7.exceptions.LocadoraException;
import io.github.alancs7.matchers.DiaSemanaMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.mockito.*;

import java.util.*;

import static io.github.alancs7.builders.FilmeBuilder.umFilme;
import static io.github.alancs7.builders.FilmeBuilder.umFilmeSemEstoque;
import static io.github.alancs7.builders.LocacaoBuilder.umLocacao;
import static io.github.alancs7.builders.UsuarioBuilder.umUsuario;
import static io.github.alancs7.matchers.MatchersProprios.*;
import static io.github.alancs7.utils.DataUtils.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class LocacaoServiceTest {

    @InjectMocks
    @Spy
    private LocacaoService service;

    @Mock
    private SPCService spcService;

    @Mock
    private LocacaoDAO dao;

    @Mock
    private EmailService emailService;

    @Rule
    public ErrorCollector error = new ErrorCollector();
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void deveAlugarFilme() throws Exception {

        // cenario - onde as variaveis serão inicializadas
        Usuario usuario = umUsuario().agora();
        List<Filme> filmes = Collections.singletonList(umFilme().comValor(5.0).agora());

        Mockito.doReturn(obterData(16, 12, 2022)).when(service).obterData();

        // acao - onde vamos invocar o método que devemos testar
        Locacao locacao = service.alugarFilme(usuario, filmes);

        // verificacao - coletar os resultados da ação com os cenarios especificados
        error.checkThat(locacao.getValor(), is(equalTo(5.0)));
        error.checkThat(isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
        error.checkThat(locacao.getDataLocacao(), ehHoje());
        error.checkThat(isMesmaData(locacao.getDataRetorno(), obterDataComDiferencaDias(1)), is(true));
        error.checkThat(locacao.getDataRetorno(), ehHojeComDiferencaDias(1));
    }

    @Test(expected = FilmeSemEstoqueException.class)
    public void naoDeveAlugarFilmeSemEstoque() throws Exception {
        // cenario - onde as variaveis serão inicializadas
        Usuario usuario = umUsuario().agora();
        List<Filme> filmes = Collections.singletonList(umFilmeSemEstoque().agora());

        // acao - onde vamos invocar o método que devemos testar
        service.alugarFilme(usuario, filmes);
    }

    @Test
    public void naoDeveAlugarFilmeSemUsuario() throws FilmeSemEstoqueException {
        // cenario
        List<Filme> filmes = Collections.singletonList(umFilme().agora());

        // acao
        try {
            service.alugarFilme(null, filmes);
            fail();
        } catch (LocadoraException e) {
            assertThat(e.getMessage(), is("Usuario vazio"));
        }
    }

    @Test
    public void naoDeveAlugarFilmeSemFilme() throws FilmeSemEstoqueException, LocadoraException {
        // cenario
        Usuario usuario = umUsuario().agora();

        exception.expect(LocadoraException.class);
        exception.expectMessage("Filme vazio");

        // acao
        service.alugarFilme(usuario, null);

    }

    @Test
    public void naoDeveDevolverNaSegundaAoAlugarNoSabado() throws FilmeSemEstoqueException, LocadoraException {
        //Assume.assumeTrue(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));

        // cenario
        Usuario usuario = umUsuario().agora();
        List<Filme> filmes = Collections.singletonList(umFilme().agora());

        Mockito.doReturn(obterData(17, 12, 2022)).when(service).obterData();

        // acao
        Locacao result = service.alugarFilme(usuario, filmes);

        // verificacao
        assertThat(result.getDataRetorno(), caiEm(Calendar.MONDAY));
        assertThat(result.getDataRetorno(), caiNumaSegunda());
        assertThat(result.getDataRetorno(), new DiaSemanaMatcher(Calendar.MONDAY));
    }

    @Test
    public void naoDeveAlugarFilmeParaNegativadoSPC() throws Exception {
        // cenario
        Usuario usuario = umUsuario().agora();
        List<Filme> filmes = Collections.singletonList(umFilme().agora());

        when(spcService.possuiNegativacao(any(Usuario.class))).thenReturn(true);

        // acao
        try {
            service.alugarFilme(usuario, filmes);
            fail();
        } catch (LocadoraException e) {
            assertThat(e.getMessage(), is("Usuário Negativado"));
        }

        // verificacao
        verify(spcService).possuiNegativacao(usuario);
    }

    @Test
    public void deveEnviarEmailParaLocacoesAtrasadas() {
        // cenario
        Usuario usuario1 = umUsuario().agora();
        Usuario usuario2 = umUsuario().comNome("Usuario em dia").agora();
        Usuario usuario3 = umUsuario().comNome("Outro atrasado").agora();

        List<Locacao> locacoes = Arrays.asList(
                umLocacao().comUsuario(usuario2).agora(),
                umLocacao().comUsuario(usuario1).atrasado().agora(),
                umLocacao().comUsuario(usuario3).atrasado().agora(),
                umLocacao().comUsuario(usuario3).atrasado().agora());
        when(dao.obterLocacoesPendentes()).thenReturn(locacoes);

        // acao
        service.notificarAtrasos();

        // verificacao
        verify(emailService, times(3)).notificarAtraso(any(Usuario.class));
        verify(emailService).notificarAtraso(usuario1);
        verify(emailService, atLeastOnce()).notificarAtraso(usuario3);
        verify(emailService, never()).notificarAtraso(usuario2);
        verifyNoMoreInteractions(emailService);
    }

    @Test
    public void deveTratarErroNoSPC() throws Exception {
        // cenario
        Usuario usuario = umUsuario().agora();
        List<Filme> filmes = Collections.singletonList(umFilme().agora());

        when(spcService.possuiNegativacao(usuario)).thenThrow(new Exception("Falha catastrófica"));

        // verificacao
        exception.expect(LocadoraException.class);
        exception.expectMessage("Problemas com SPC, tente novamente");

        // acao
        service.alugarFilme(usuario, filmes);
    }

    @Test
    public void deveProrrogarUmaLocacao() {
        // cenario
        Locacao locacao = umLocacao().agora();

        // acao
        service.prorrogarLocacao(locacao, 3);

        // verificacao
        ArgumentCaptor<Locacao> argumentCaptor = ArgumentCaptor.forClass(Locacao.class);
        verify(dao).salvar(argumentCaptor.capture());
        Locacao locacaoRetornado = argumentCaptor.getValue();

        error.checkThat(locacaoRetornado.getValor(), is(12.0));
        error.checkThat(locacaoRetornado.getDataLocacao(), is(ehHoje()));
        error.checkThat(locacaoRetornado.getDataRetorno(), is(ehHojeComDiferencaDias(3)));
    }
}
