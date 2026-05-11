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
                // estado 0 = aberto, 1 = inscrições encerradas, 2 = concluído, 3 = cancelado
                String status;
                switch (c.getEstado()) {
                    case 1:  status = " (INSCRIÇÕES ENCERRADAS)"; break;
                    case 2:  status = " (CONCLUÍDO)";            break;
                    case 3:  status = " (CANCELADO)";            break;
                    default: status = "";                        break;
                }
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

    public String lerCodigo() {
        System.out.print("\nDigite o código do curso: ");
        return console.nextLine().trim();
    }

    public String mostrarDetalheCursoParaInscricao(Curso curso) {
        System.out.println("\nEntrePares 1.0");
        System.out.println("--------------");
        System.out.println("> Início > Minhas inscrições > " + curso.getNome());

        System.out.println("\nCÓDIGO........: " + curso.getCodigoCompartilhavel());
        System.out.println("CURSO.........: " + curso.getNome());
        System.out.println("DESCRIÇÃO.....: " + curso.getDescricao());
        System.out.println("DATA DE INÍCIO: " + curso.getDataInicioCurso());

        System.out.println();
        System.out.println("(A) Fazer minha inscrição no curso");
        System.out.println("\n(R) Retornar ao menu anterior");
        System.out.print("\nOpção: ");

        return console.nextLine().trim().toUpperCase();
    }
}