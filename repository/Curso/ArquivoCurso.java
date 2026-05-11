package repository.Curso;

import java.util.ArrayList;
import java.util.List;
import model.Curso;
import repository.Arquivo;
import repository.ArvoreBMais;
import repository.HashExtensivel;

public class ArquivoCurso extends Arquivo<Curso> {

    // Índice do relacionamento 1:N (idUsuario, idCurso)
    private ArvoreBMais<ParIntInt> indiceUsuarioCurso;

    // Índice secundário: codigoCompartilhavel → id do curso
    private HashExtensivel<ParCodigoID> indiceCodigo;

    public ArquivoCurso() throws Exception {
        super("cursos", Curso.class.getConstructor());

        indiceUsuarioCurso = new ArvoreBMais<>(
            ParIntInt.class.getConstructor(),
            5,
            ".\\dados\\cursos\\indiceUsuarioCurso.db"
        );

        indiceCodigo = new HashExtensivel<>(
            ParCodigoID.class.getConstructor(),
            4,
            ".\\dados\\cursos\\indiceCodigo.d.db",
            ".\\dados\\cursos\\indiceCodigo.c.db"
        );
    }

    @Override
    public int create(Curso c) throws Exception {
        int id = super.create(c);
        indiceUsuarioCurso.create(new ParIntInt(c.usuarioId, id));
        indiceCodigo.create(new ParCodigoID(c.getCodigoCompartilhavel(), id));
        return id;
    }

    @Override
    public boolean update(Curso novo) throws Exception {
        if (super.update(novo)) return true;
        return false;
    }

    @Override
    public boolean delete(int id) throws Exception {
        Curso c = super.read(id);
        if (c == null) return false;

        if (super.delete(id)) {
            indiceUsuarioCurso.delete(new ParIntInt(c.usuarioId, c.getId()));
            indiceCodigo.delete(ParCodigoID.hash(c.getCodigoCompartilhavel()));
            return true;
        }
        return false;
    }

    public Curso buscarPorCodigo(String codigo) throws Exception {
        ParCodigoID par = indiceCodigo.read(ParCodigoID.hash(codigo));
        if (par == null) return null;
        return read(par.getId());
    }

    // Lista cursos do usuário em ordem alfabética
    public List<Curso> listarPorUsuario(int usuarioId) throws Exception {
        List<Curso> listaFinal = new ArrayList<>();
        ArrayList<ParIntInt> lista = indiceUsuarioCurso.read(new ParIntInt(usuarioId));
        for (ParIntInt p : lista) {
            Curso c = read(p.getNum2());
            if (c != null) listaFinal.add(c);
        }
        listaFinal.sort((a, b) -> a.getNome().compareTo(b.getNome()));
        return listaFinal;
    }

    // Retorna true se o usuário tiver algum curso ativo (estado 0 ou 1)
    public boolean temCursosAtivos(int usuarioId) throws Exception {
        ArrayList<ParIntInt> lista = indiceUsuarioCurso.read(new ParIntInt(usuarioId));
        for (ParIntInt p : lista) {
            Curso c = read(p.getNum2());
            if (c != null && (c.getEstado() == 0 || c.getEstado() == 1))
                return true;
        }
        return false;
    }

    // Exclui todos os cursos inativos do usuário (estado 2 ou 3)
    public void deletarCursosInativos(int usuarioId) throws Exception {
        ArrayList<ParIntInt> lista = indiceUsuarioCurso.read(new ParIntInt(usuarioId));
        for (ParIntInt p : lista) {
            Curso c = read(p.getNum2());
            if (c != null && (c.getEstado() == 2 || c.getEstado() == 3))
                delete(c.getId());
        }
    }
}
