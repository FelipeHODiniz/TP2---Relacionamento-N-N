package repository.Inscricao;

import java.util.ArrayList;
import java.util.List;
import model.CursoUsuario;
import repository.Arquivo;
import repository.ArvoreBMais;
import repository.Curso.ParIntInt;

public class ArquivoCursoUsuario extends Arquivo<CursoUsuario> {

    private ArvoreBMais<ParIntInt> indiceCursoUsuario;
    private ArvoreBMais<ParIntInt> indiceUsuarioCurso;

    public ArquivoCursoUsuario() throws Exception {
        super("inscricoes", CursoUsuario.class.getConstructor());
        
        // Inicializando as duas árvores B+ com ordem 5
        indiceCursoUsuario = new ArvoreBMais<>(
            ParIntInt.class.getConstructor(), 
            5, 
            ".\\dados\\inscricoes\\indiceCursoUsuario.btree"
        );
        
        indiceUsuarioCurso = new ArvoreBMais<>(
            ParIntInt.class.getConstructor(), 
            5, 
            ".\\dados\\inscricoes\\indiceUsuarioCurso.btree"
        );
    }

    @Override
    public int create(CursoUsuario obj) throws Exception {
        // Verifica duplicidade antes de inserir
        if (buscar(obj.getIdUsuario(), obj.getIdCurso()) != null) {
            System.out.println("Usuário já está inscrito neste curso.");
            return -1;
        }

        // Insere no arquivo de dados principal e obtém o ID gerado
        int id = super.create(obj);
        
        // Insere os pares nas árvores B+
        // Árvore 1: Chave = idCurso, Valor = id (idCursoUsuario)
        indiceCursoUsuario.create(new ParIntInt(obj.getIdCurso(), id));
        
        // Árvore 2: Chave = idUsuario, Valor = id (idCursoUsuario)
        indiceUsuarioCurso.create(new ParIntInt(obj.getIdUsuario(), id));
        
        return id;
    }

    @Override
    public boolean delete(int id) throws Exception {
        CursoUsuario obj = super.read(id);
        if (obj != null) {
            // Remove das árvores B+
            indiceCursoUsuario.delete(new ParIntInt(obj.getIdCurso(), id));
            indiceUsuarioCurso.delete(new ParIntInt(obj.getIdUsuario(), id));
            
            // Remove do arquivo de dados
            return super.delete(id);
        }
        return false;
    }

    @Override
    public boolean update(CursoUsuario obj) throws Exception {
        CursoUsuario antigo = super.read(obj.getId());
        if (antigo != null) {
            if (antigo.getIdCurso() != obj.getIdCurso() || antigo.getIdUsuario() != obj.getIdUsuario()) {
                indiceCursoUsuario.delete(new ParIntInt(antigo.getIdCurso(), antigo.getId()));
                indiceUsuarioCurso.delete(new ParIntInt(antigo.getIdUsuario(), antigo.getId()));
                
                indiceCursoUsuario.create(new ParIntInt(obj.getIdCurso(), obj.getId()));
                indiceUsuarioCurso.create(new ParIntInt(obj.getIdUsuario(), obj.getId()));
            }
            // Atualiza os dados (ex: dataInscricao) no arquivo principal
            return super.update(obj);
        }
        return false;
    }

    public CursoUsuario buscar(int idUsuario, int idCurso) throws Exception {
        // Busca na árvore de usuários (todos os cursos do usuário)
        List<ParIntInt> lista = indiceUsuarioCurso.read(new ParIntInt(idUsuario, -1));
        for (ParIntInt par : lista) {
            CursoUsuario cu = super.read(par.getNum2()); // getNum2 retorna o idCursoUsuario
            if (cu != null && cu.getIdCurso() == idCurso) {
                return cu;
            }
        }
        return null;
    }

    public List<CursoUsuario> listarPorUsuario(int idUsuario) throws Exception {
        List<CursoUsuario> resultado = new ArrayList<>();
        // Busca na árvore: Chave = idUsuario
        List<ParIntInt> lista = indiceUsuarioCurso.read(new ParIntInt(idUsuario, -1));
        
        for (ParIntInt par : lista) {
            CursoUsuario cu = super.read(par.getNum2());
            if (cu != null) {
                resultado.add(cu);
            }
        }
        return resultado;
    }

    public List<CursoUsuario> listarPorCurso(int idCurso) throws Exception {
        List<CursoUsuario> resultado = new ArrayList<>();
        // Busca na árvore: Chave = idCurso
        List<ParIntInt> lista = indiceCursoUsuario.read(new ParIntInt(idCurso, -1));
        
        for (ParIntInt par : lista) {
            CursoUsuario cu = super.read(par.getNum2());
            if (cu != null) {
                resultado.add(cu);
            }
        }
        return resultado;
    }
}
