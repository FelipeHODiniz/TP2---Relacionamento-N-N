package controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import model.Curso;
import model.CursoUsuario;
import model.Usuario;
import repository.Curso.ArquivoCurso;
import repository.Inscricao.ArquivoCursoUsuario;
import repository.Usuario.ArquivoUsuario;

public class InscricaoController {
    
    private final ArquivoCursoUsuario arqCursoUsuario;
    private final ArquivoCurso arqCurso;
    private final ArquivoUsuario arqUsuario;

    public InscricaoController() throws Exception {
        this.arqCursoUsuario = new ArquivoCursoUsuario();
        this.arqCurso = new ArquivoCurso();
        this.arqUsuario = new ArquivoUsuario();
    }

    public void close() throws Exception {
        arqCursoUsuario.close();
        arqCurso.close();
        arqUsuario.close();
    }

    public boolean inscrever(int idUsuario, int idCurso) {
        try {
            // 0. Validar se o usuário existe
            Usuario usuario = arqUsuario.read(idUsuario);
            if (usuario == null) {
                System.out.println("Aviso: O usuário selecionado não existe.");
                return false;
            }

            // 1. Validar se o curso existe e está com inscrições abertas
            Curso curso = arqCurso.read(idCurso);
            
            // Assumindo que estado '0' significa 'Aberto/Ativo'. Ajuste conforme sua lógica.
            if (curso == null || curso.getEstado() != 0) { 
                System.out.println("Aviso: O curso selecionado não existe ou as inscrições estão encerradas.");
                return false;
            }

            // 2. Rejeitar duplicatas
            List<CursoUsuario> todasInscricoes = arqCursoUsuario.readAll(); // Assumindo um método que lista todos
            for (CursoUsuario inscricao : todasInscricoes) {
                if (inscricao.getIdUsuario() == idUsuario && inscricao.getIdCurso() == idCurso) {
                    System.out.println("Aviso: Você já está inscrito neste curso!");
                    return false;
                }
            }

            // 3. Efetivar a inscrição
            String dataAtual = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            CursoUsuario novaInscricao = new CursoUsuario(idCurso, idUsuario, dataAtual);
            arqCursoUsuario.create(novaInscricao);

            System.out.println("Sucesso: Inscrição confirmada!");
            return true;

        } catch (Exception e) {
            System.err.println("Erro ao tentar realizar a inscrição: " + e.getMessage());
            return false;
        }
    }

    // Método privado auxiliar para encontrar o ID do registro CursoUsuario
    private int buscarIdInscricao(int idUsuario, int idCurso) throws Exception {
        List<CursoUsuario> todasInscricoes = arqCursoUsuario.readAll();
        for (CursoUsuario inscricao : todasInscricoes) {
            if (inscricao.getIdUsuario() == idUsuario && inscricao.getIdCurso() == idCurso) {
                return inscricao.getId();
            }
        }
        return -1; // Retorna -1 se a inscrição não existir
    }

    // Cancelar (por usuário) - O próprio usuário cancela sua inscrição
    public boolean cancelarPorUsuario(int idUsuario, int idCurso) {
        try {
            int idInscricao = buscarIdInscricao(idUsuario, idCurso);
            if (idInscricao != -1) {
                boolean sucesso = arqCursoUsuario.delete(idInscricao);
                if (sucesso) {
                    System.out.println("Inscrição cancelada com sucesso.");
                }
                return sucesso;
            } else {
                System.out.println("Aviso: Inscrição não encontrada.");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Erro ao tentar cancelar a inscrição: " + e.getMessage());
            return false;
        }
    }

    // Cancelar (pelo proponente) - O dono do curso remove um inscrito
    public boolean cancelarPorProponente(int idProponente, int idUsuarioInscrito, int idCurso) {
        try {
            // Valida se quem está tentando remover é realmente o dono do curso
            Curso curso = arqCurso.read(idCurso);
            if (curso == null || curso.usuarioId != idProponente) {
                System.out.println("Erro: Apenas o dono do curso pode remover inscritos.");
                return false;
            }

            int idInscricao = buscarIdInscricao(idUsuarioInscrito, idCurso);
            if (idInscricao != -1) {
                boolean sucesso = arqCursoUsuario.delete(idInscricao);
                if (sucesso) {
                    System.out.println("Inscrito removido do curso com sucesso.");
                }
                return sucesso;
            } else {
                System.out.println("Aviso: O usuário selecionado não está inscrito neste curso.");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Erro ao tentar remover o inscrito: " + e.getMessage());
            return false;
        }
    }

    public List<Curso> listarCursosDoUsuario(int idUsuario) {
        List<Curso> cursosDoUsuario = new ArrayList<>();
        try {
            List<CursoUsuario> todasInscricoes = arqCursoUsuario.readAll();
            for (CursoUsuario inscricao : todasInscricoes) {
                if (inscricao.getIdUsuario() == idUsuario) {
                    Curso curso = arqCurso.read(inscricao.getIdCurso());
                    if (curso != null) {
                        cursosDoUsuario.add(curso);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao listar os cursos do usuário: " + e.getMessage());
        }
        return cursosDoUsuario;
    }

    public List<Usuario> listarInscritosNoCurso(int idCurso) {
        List<Usuario> usuariosInscritos = new ArrayList<>();
        try {
            List<CursoUsuario> todasInscricoes = arqCursoUsuario.readAll();
            for (CursoUsuario inscricao : todasInscricoes) {
                if (inscricao.getIdCurso() == idCurso) {
                    Usuario usuario = arqUsuario.read(inscricao.getIdUsuario());
                    if (usuario != null) {
                        usuariosInscritos.add(usuario);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao listar os inscritos no curso: " + e.getMessage());
        }
        return usuariosInscritos;
    }
}