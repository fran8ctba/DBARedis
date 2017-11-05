/*
 * ExceptionUtil.java
 *
 * Classe utilitária para mostrar a lista de métodos de uma exceção (StrackStrace)
 * na console, dando tratamento especial caso a exceção seja do tipo SQLException.
 *
 * © 2016 - Faculdades Opet - Todos os direitos reservados.
 *
 * Histórico
 * 14/07/2016 – Versão 1.0 - José Augusto – Criação do arquivo
 *
 */
package br.edu.opet.Util;

import java.sql.DriverManager;
import java.sql.SQLException;

public class ExceptionUtil
{

    // Método utilitário que recebe qualquer exceção e mostra o dados da
    // mesma de forma formatada.
    public static void mostrarErro(Exception pExcept, String pMsg)
    {
        // Mostrar os dados básicos como nome da exceção e mensagem
        System.out.println();
        System.out.println(pMsg);
        System.out.println("Exceção....: " + pExcept.getClass().getName());
        System.out.println("Mensagem...: " + pExcept.getMessage());

        // Caso seja uma exceção SQLException, mosra os dados da exceção do banco de dados
        if (pExcept instanceof SQLException)
        {
            SQLException tExcept = (SQLException) pExcept;
            System.out.println("SQLState...:" + tExcept.getSQLState());
            System.out.println("Error Code.:" + tExcept.getErrorCode());
            DriverManager.println("SQLState...:" + tExcept.getSQLState());
            DriverManager.println("Error Code.:" + tExcept.getErrorCode());
        }

        // Processa a lista de exceções ligadas, se houver, mostrando a lista
        Throwable tCausa = pExcept.getCause();
        while (tCausa != null)
        {
            System.out.println("Causa.....: " + tCausa.getMessage());
            tCausa = tCausa.getCause();
        }

        // Mostra na console o StackTrace da exceção
        System.out.println("Pilha de execução");
        pExcept.printStackTrace(System.out);
    }
}
