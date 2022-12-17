package io.github.alancs7.suites;

import io.github.alancs7.servicos.CalculadoraTest;
import io.github.alancs7.servicos.CalculoValorLocacaoTest;
import io.github.alancs7.servicos.LocacaoServiceTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CalculadoraTest.class,
        CalculoValorLocacaoTest.class,
        LocacaoServiceTest.class
})
public class SuiteExecucao {
}
