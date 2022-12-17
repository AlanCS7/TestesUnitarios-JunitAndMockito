package io.github.alancs7.servicos;

import io.github.alancs7.exceptions.NaoPodeDividirPorZeroException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CalculadoraTest {

    private Calculadora calc;

    @Before
    public void setup() {
        calc = new Calculadora();
    }

    @Test
    public void deveSomarDoisNumeros() {
        // cenario
        int a = 5;
        int b = 3;

        // acao
        int result = calc.somar(a, b);

        // verificacao
        assertEquals(8, result);
    }

    @Test
    public void deveSubtrairDoisNumeros() {
        // cenario
        int a = 5;
        int b = 3;

        // acao
        int result = calc.subtrair(a, b);

        // verificacao
        assertEquals(2, result);

    }

    @Test
    public void deveDividirDoisNumeros() throws NaoPodeDividirPorZeroException {
        // cenario
        int a = 10;
        int b = 2;

        // acao
        double result = calc.dividir(a, b);

        // verificacao
        assertEquals(5.0, result, 0.0);
    }

    @Test(expected = NaoPodeDividirPorZeroException.class)
    public void deveLancarExcecaoAoDividirPorZero() throws NaoPodeDividirPorZeroException {
        // cenario
        int a = 10;
        int b = 0;

        // acao
        calc.dividir(a, b);

    }
}
