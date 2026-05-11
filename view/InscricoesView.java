package view;

import java.util.List;
import java.util.Scanner;
import model.Curso;

public class InscricoesView {

    private final Scanner console;

    public InscricoesView(Scanner console) {
        this.console = console;
    }

    public String lerOpcaoMenuInscricoes(List<Curso> inscricoes) {
        System.out.println("\nEntrePares 1.0");
        System.out.println("--------------");
        System.out.println("> Início > Minhas inscrições\n");
        System.out.println("INSCRIÇÕES");

        if (inscricoes == null || inscricoes.isEmpty()) {
            System.out.println("Você ainda não possui inscrições.");
        } else {
            for (int i = 0; i < inscricoes.size(); i++) {
                Curso c = inscricoes.get(i);
                // No InscricaoController, o estado '1' é considerado aberto.
                // Se for diferente de 1, consideramos as inscrições encerradas.
                String status = (c.getEstado() != 1) ? " (INSCRIÇÕES ENCERRADAS)" : "";
                System.out.printf("(%d) %s - %s%s\n", (i + 1), c.getNome(), c.getDataInicioCurso(), status);
            }
        }

        System.out.println("\n(A) Buscar curso por código");
        System.out.println("(B) Buscar curso por palavras-chave");
        System.out.println("(C) Listar todos os cursos\n");
        System.out.println("(R) Retornar ao menu anterior\n");
        System.out.print("Opção: ");

        return console.nextLine().trim().toUpperCase();
    }

    public void mostrarMensagem(String mensagem) {
        System.out.println(mensagem);
    }
}