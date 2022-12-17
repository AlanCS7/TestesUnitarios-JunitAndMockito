package io.github.alancs7.servicos;

import io.github.alancs7.exceptions.NaoPodeDividirPorZeroException;

public class Calculadora {

    public int somar(int a, int b) {
        return a + b;
    }

    public int subtrair(int a, int b) {
        return a - b;
    }

    public double dividir(int a, int b) throws NaoPodeDividirPorZeroException {
        if (a == 0 || b == 0) {
            throw new NaoPodeDividirPorZeroException();
        }
        return a / b;
    }
}
