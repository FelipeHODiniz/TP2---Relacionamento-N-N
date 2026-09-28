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

    // Classe auxiliar para retornar dados de inscritos com data
    public static class InscritoDados {
        public Usuario usuario;
        public String dataInscricao;
        public int idInscricao;

        public InscritoDados(Usuario usuario, String dataInscricao, int idInscricao) {
            this.usuario = usuario;
            this.dataInscricao = dataInscricao;
            this.idInscricao = idInscricao;
        }
    }

    // Estados do curso
    private static final int ESTADO_ABERTO = 0;        // Inscrições abertas
    private static final int ESTADO_EM_ANDAMENTO = 1; // Curso em andamento
    private static final int ESTADO_FINALIZADO = 2;    // Curso finalizado
    private static final int ESTADO_CANCELADO = 3;     // Curso cancelado
    
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

    /**
     * Realiza inscrição com validações de integridade referencial.
     * 
     * REGRAS APLICADAS:
     * 1. Usuário deve existir
     * 2. Curso deve existir
     * 3. Curso deve estar com estado ABERTO (0)
     * 4. Usuário NÃO pode se inscrever em curso de sua própria autoria
     * 5. Usuário NÃO pode estar já inscrito no curso
     * 
     * @param idUsuario ID do usuário tentando se inscrever
     * @param idCurso ID do curso
     * @return true se inscrito com sucesso, false caso contrário
     */
    public boolean inscrever(int idUsuario, int idCurso) {
        try {
            // VALIDAÇÃO 1: Usuário deve existir
            Usuario usuario = arqUsuario.read(idUsuario);
            if (usuario == null) {
                System.err.println("✗ Erro: Usuário ID " + idUsuario + " não existe no sistema.");
                return false;
            }

            // VALIDAÇÃO 2: Curso deve existir
            Curso curso = arqCurso.read(idCurso);
            if (curso == null) {
                System.err.println("✗ Erro: Curso ID " + idCurso + " não existe no sistema.");
                return false;
            }

            // VALIDAÇÃO 3: Curso deve estar em estado ABERTO para inscrições
            if (curso.getEstado() != ESTADO_ABERTO) {
                String estadoDescricao = descreverEstado(curso.getEstado());
                System.err.println("✗ Erro: Inscrições para este curso estão encerradas (estado: " + estadoDescricao + ").");
                return false;
            }

            // VALIDAÇÃO 4: Usuário NÃO pode se inscrever em curso de sua própria autoria
            // DECISÃO: BLOQUEADO (proprietário não pode ser inscrito em seus próprios cursos)
            if (curso.usuarioId == idUsuario) {
                System.err.println("✗ Erro: Você não pode se inscrever em seu próprio curso.");
                return false;
            }

            // VALIDAÇÃO 5: Rejeitar duplicatas - usar método específico de busca
            CursoUsuario inscricaoExistente = arqCursoUsuario.buscar(idUsuario, idCurso);
            if (inscricaoExistente != null) {
                System.err.println("✗ Erro: Você já está inscrito neste curso.");
                return false;
            }

            // ✓ TODAS AS VALIDAÇÕES PASSARAM - Efetuar inscrição
            String dataAtual = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            CursoUsuario novaInscricao = new CursoUsuario(idCurso, idUsuario, dataAtual);
            int idInscricao = arqCursoUsuario.create(novaInscricao);

            if (idInscricao != -1) {
                System.out.println("✓ Sucesso: Inscrição confirmada! (ID: " + idInscricao + ")");
                return true;
            } else {
                System.err.println("✗ Erro: Falha ao registrar inscrição no banco de dados.");
                return false;
            }

        } catch (Exception e) {
            System.err.println("✗ Erro ao tentar realizar a inscrição: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Cancelar (por usuário) - O próprio usuário cancela sua inscrição
    public boolean cancelarPorUsuario(int idUsuario, int idCurso) {
        try {
            CursoUsuario inscricao = arqCursoUsuario.buscar(idUsuario, idCurso);
            if (inscricao != null) {
                boolean sucesso = arqCursoUsuario.delete(inscricao.getId());
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

            CursoUsuario inscricao = arqCursoUsuario.buscar(idUsuarioInscrito, idCurso);
            if (inscricao != null) {
                boolean sucesso = arqCursoUsuario.delete(inscricao.getId());
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
            List<CursoUsuario> inscricoes = arqCursoUsuario.listarPorUsuario(idUsuario);
            for (CursoUsuario inscricao : inscricoes) {
                Curso curso = arqCurso.read(inscricao.getIdCurso());
                if (curso != null) {
                    cursosDoUsuario.add(curso);
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
            List<CursoUsuario> inscricoes = arqCursoUsuario.listarPorCurso(idCurso);
            for (CursoUsuario inscricao : inscricoes) {
                Usuario usuario = arqUsuario.read(inscricao.getIdUsuario());
                if (usuario != null) {
                    usuariosInscritos.add(usuario);
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao listar os inscritos no curso: " + e.getMessage());
        }
        return usuariosInscritos;
    }

    public List<InscritoDados> listarInscritosComDados(int idCurso) {
        List<InscritoDados> inscritos = new ArrayList<>();
        try {
            List<CursoUsuario> inscricoes = arqCursoUsuario.listarPorCurso(idCurso);
            for (CursoUsuario inscricao : inscricoes) {
                Usuario usuario = arqUsuario.read(inscricao.getIdUsuario());
                if (usuario != null) {
                    inscritos.add(new InscritoDados(usuario, inscricao.getDataInscricao(), inscricao.getId()));
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao listar os inscritos no curso: " + e.getMessage());
        }
        return inscritos;
    }

    /**
     * Método auxiliar para descrever o estado de um curso de forma legível.
     * @param estado Código numérico do estado
     * @return Descrição legível do estado
     */
    private String descreverEstado(int estado) {
        switch (estado) {
            case ESTADO_ABERTO: return "ABERTO";
            case ESTADO_EM_ANDAMENTO: return "EM ANDAMENTO";
            case ESTADO_FINALIZADO: return "FINALIZADO";
            case ESTADO_CANCELADO: return "CANCELADO";
            default: return "DESCONHECIDO";
        }
    }
}
